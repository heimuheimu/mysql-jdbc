/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 heimuheimu
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.heimuheimu.mysql.jdbc.datasource;

import com.heimuheimu.mysql.jdbc.ConnectionConfiguration;
import com.heimuheimu.mysql.jdbc.MysqlConnection;
import com.heimuheimu.mysql.jdbc.constant.BeanStatusEnum;
import com.heimuheimu.mysql.jdbc.datasource.listener.MysqlDataSourceListener;
import com.heimuheimu.mysql.jdbc.datasource.listener.SilentMysqlDataSourceListener;
import com.heimuheimu.mysql.jdbc.facility.SQLFeatureNotSupportedExceptionBuilder;
import com.heimuheimu.mysql.jdbc.facility.parameter.ConstructorParameterChecker;
import com.heimuheimu.mysql.jdbc.facility.parameter.Parameters;
import com.heimuheimu.mysql.jdbc.monitor.DataSourceMonitor;
import com.heimuheimu.mysql.jdbc.monitor.DataSourceMonitorFactory;
import com.heimuheimu.mysql.jdbc.net.SocketConfiguration;
import com.heimuheimu.mysql.jdbc.util.LogBuildUtil;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Mysql 数据库连接池。
 *
 * <p><strong>说明：</strong>{@code Mysql} 类是程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class MysqlDataSource implements DataSource, Closeable {

    private static final org.slf4j.Logger MYSQL_CONNECTION_LOG = LoggerFactory.getLogger("MYSQL_CONNECTION_LOG");

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(MysqlDataSource.class);

    /**
     * 建立 Mysql 数据库连接使用的配置信息
     */
    private final ConnectionConfiguration connectionConfiguration;

    /**
     * 连接池使用的配置信息
     */
    private final DataSourceConfiguration dataSourceConfiguration;

    /**
     * Mysql 数据库连接池事件监听器
     */
    private final MysqlDataSourceListener listener;

    /**
     * Mysql 数据库连接池信息监控器
     */
    private final DataSourceMonitor dataSourceMonitor;

    /**
     * 当前连接池维护的可复用数据库连接列表
     */
    private final CopyOnWriteArrayList<MysqlPooledConnection> connectionList = new CopyOnWriteArrayList<>();

    /**
     * {@link #connectionList} 元素发生变更操作时，使用的私有锁
     */
    private final Object connectionListUpdateLock = new Object();

    /**
     * 当前可用的数据库连接队列
     */
    private final LinkedBlockingQueue<Integer> availableConnectionIndexQueue = new LinkedBlockingQueue<>();

    /**
     * 数据库连接恢复任务是否运行
     */
    private boolean isRescueTaskRunning = false;

    /**
     * 数据库连接恢复任务使用的私有锁
     */
    private final Object rescueTaskLock = new Object();

    /**
     * Redis 直连客户端列表所处状态
     */
    private volatile BeanStatusEnum state = BeanStatusEnum.NORMAL;

    /**
     * 构造一个 Mysql 数据库连接池。
     *
     * @param connectionConfiguration 建立 Mysql 数据库连接使用的配置信息，不允许为 {@code null}
     * @param dataSourceConfiguration 连接池使用的配置信息，不允许为 {@code null}
     * @param listener Mysql 数据库连接池事件监听器，允许为 {@code null}
     * @throws IllegalArgumentException 如果 {@code connectionConfiguration} 或 {@code dataSourceConfiguration} 为 {@code null}，将会抛出此异常
     */
    public MysqlDataSource(ConnectionConfiguration connectionConfiguration, DataSourceConfiguration dataSourceConfiguration,
                           MysqlDataSourceListener listener)
            throws IllegalArgumentException{
        ConstructorParameterChecker checker = new ConstructorParameterChecker("MysqlDataSource", LOG);
        checker.addParameter("connectionConfiguration", connectionConfiguration);
        checker.addParameter("dataSourceConfiguration", dataSourceConfiguration);

        checker.check("connectionConfiguration", "isNull", Parameters::isNull);
        checker.check("dataSourceConfiguration", "isNull", Parameters::isNull);

        this.connectionConfiguration = connectionConfiguration;
        this.dataSourceConfiguration = dataSourceConfiguration;
        this.listener = new SilentMysqlDataSourceListener(listener);
        this.dataSourceMonitor = DataSourceMonitorFactory.get(connectionConfiguration.getHost(), connectionConfiguration.getDatabaseName());
        boolean hasAvailableClient = false;
        for (int i = 0; i < dataSourceConfiguration.getPoolSize(); i++) {
            boolean isSuccess = createConnection(-1);
            if (isSuccess) {
                hasAvailableClient = true;
                MYSQL_CONNECTION_LOG.info("Add `MysqlConnection` success. `clientIndex`:`{}`. `host`:`{}`. `databaseName`:`{}`.",
                        i, connectionConfiguration.getHost(), connectionConfiguration.getDatabaseName());
                this.listener.onCreated(connectionConfiguration.getHost(), connectionConfiguration.getDatabaseName());
            } else {
                MYSQL_CONNECTION_LOG.error("Add `MysqlConnection` failed. `clientIndex`:`{}`. `host`:`{}`. `databaseName`:`{}`.",
                        i, connectionConfiguration.getHost(), connectionConfiguration.getDatabaseName());
                this.listener.onClosed(connectionConfiguration.getHost(), connectionConfiguration.getDatabaseName());
            }
        }
        if (!hasAvailableClient) {
            String errorMessage = "There is no available `MysqlConnection`. `host`:`" + connectionConfiguration.getHost()
                    + "`. `databaseName`:`" + connectionConfiguration.getDatabaseName() + "`.";
            LOG.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
        LeakedConnectionDetector.register(this);
    }

    @Override
    public Connection getConnection() throws SQLException {
        MysqlPooledConnection connection = null;
        try {
            long checkoutTimeout = dataSourceConfiguration.getCheckoutTimeout();
            long leftCheckoutTimeout = checkoutTimeout;
            int retryTimes = 0; // 获取数据库连接重试次数
            Integer connectionIndex;
            while (connection == null && retryTimes < dataSourceConfiguration.getPoolSize()) {
                retryTimes++;
                if (checkoutTimeout == 0) {
                    connectionIndex = availableConnectionIndexQueue.take();
                } else {
                    long startTime = System.currentTimeMillis();
                    if (leftCheckoutTimeout > 0) {
                        connectionIndex = availableConnectionIndexQueue.poll(leftCheckoutTimeout, TimeUnit.MILLISECONDS);
                        leftCheckoutTimeout = leftCheckoutTimeout - (System.currentTimeMillis() - startTime);
                    } else {
                        connectionIndex = availableConnectionIndexQueue.poll();
                    }
                }

                if (connectionIndex != null) {
                    connection = connectionList.get(connectionIndex);
                    if (connection != null) {
                        if (!connection.acquire(dataSourceConfiguration.getMaxOccupyTime())) {
                            connection = null;
                        }
                    } else {
                        startRescueTask(); // make sure rescue task is running
                    }
                }
            }
        } catch (Exception e) {
            String errorMessage = "Get available `MysqlConnection` failed: `unexpected error`." + buildLogForParameters();
            LOG.error(errorMessage, e);
            throw new SQLException(errorMessage, e);
        }

        if (connection != null) {
            dataSourceMonitor.onConnectionAcquired();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Get `MysqlConnection` success. `clientIndex`:`{}`. `host`:`{}`. `databaseName`:`{}`.",
                        connection.getConnectionIndex(), connectionConfiguration.getHost(), connectionConfiguration.getDatabaseName());
            }
            return connection;
        } else {
            dataSourceMonitor.onGetConnectionFailed();
            String errorMessage = "Get available `MysqlConnection` failed: `no available connection`." + buildLogForParameters();
            LOG.error(errorMessage);
            throw new SQLException(errorMessage);
        }
    }

    /**
     * 从连接池中获取一个可用的数据库连接，传入的数据库用户名和密码在当前实现中不起任何作用。
     *
     * @param username 数据库用户名，该值无作用
     * @param password 数据库密码，该值无作用
     * @return 可用的数据库连接
     * @throws SQLException 如果没有可用数据库连接，将会抛出此异常
     */
    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return getConnection();
    }

    @Override
    public int getLoginTimeout() {
        SocketConfiguration socketConfiguration = connectionConfiguration.getSocketConfiguration();
        if (socketConfiguration == null) {
            return SocketConfiguration.DEFAULT.getConnectionTimeout() / 1000;
        } else {
            return socketConfiguration.getConnectionTimeout() / 1000;
        }
    }

    @Override
    public synchronized void close() {
        if (state != BeanStatusEnum.CLOSED) {
            state = BeanStatusEnum.CLOSED;
            for (MysqlPooledConnection connection : connectionList) {
                if (connection != null) {
                    connection.closePhysicalConnection();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T unwrap(Class<T> iface) {
        return (T) this;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return MysqlDataSource.class == iface;
    }

    /**
     * 获得当前连接池维护的可复用数据库连接列表，该方法不会返回 {@code null}。
     *
     * @return 当前连接池维护的可复用数据库连接列表，不会为 {@code null}
     */
    List<MysqlPooledConnection> getConnectionList() {
        return connectionList;
    }

    /**
     * 获得 Mysql 数据库连接池信息监控器。
     *
     * @return Mysql 数据库连接池信息监控器
     */
    DataSourceMonitor getDataSourceMonitor() {
        return dataSourceMonitor;
    }

    /**
     * 在数据库连接创建完成后，将会调用此方法，子类可继承此方法，对数据库连接执行自定义初始化操作，例如设置数据库时区。
     *
     * @param pooledConnection Mysql 数据库连接
     */
    @SuppressWarnings("EmptyMethod")
    protected void prepareConnection(MysqlPooledConnection pooledConnection) {
        // this is a no-op
    }

    /**
     * 创建一个数据库连接，并将其放入列表指定索引位置，如果索引位置小于 0，则在列表中新增该数据库连接。
     *
     * @param connectionIndex 索引位置，如果为 -1，则在列表中添加
     * @return 是否创建成功
     */
    private boolean createConnection(int connectionIndex) {
        MysqlPooledConnection connection = null;
        if (connectionIndex < 0) {
            connectionIndex = connectionList.size();
        }
        try {
            connection = new MysqlPooledConnection(connectionIndex, connectionConfiguration, dataSourceConfiguration.getTimeout(),
                    dataSourceConfiguration.getSlowExecutionThreshold(), this::removeUnavailableClient, this::onPooledConnectionClosed);
            prepareConnection(connection);
        } catch (Exception ignored) {}

        synchronized (connectionListUpdateLock) {
            if (connection != null && !connection.isClosed()) {
                if (connectionIndex < connectionList.size()) {
                    connectionList.set(connectionIndex, connection);
                } else {
                    connectionList.add(connection);
                }
                availableConnectionIndexQueue.add(connectionIndex);
                return true;
            } else {
                if (connectionIndex < connectionList.size()) {
                    connectionList.set(connectionIndex, null);
                } else {
                    connectionList.add(null);
                }
                return false;
            }
        }
    }

    /**
     * 当可复用数据库连接关闭时，将其索引放入可用索引队列中。
     *
     * @param pooledConnection 可复用数据库连接
     */
    private void onPooledConnectionClosed(MysqlPooledConnection pooledConnection) {
        dataSourceMonitor.onConnectionReleased();
        availableConnectionIndexQueue.add(pooledConnection.getConnectionIndex());
        if (LOG.isDebugEnabled()) {
            LOG.debug("Return `MysqlConnection` success. `clientIndex`:`{}`. `host`:`{}`. `databaseName`:`{}`.",
                    pooledConnection.getConnectionIndex(), connectionConfiguration.getHost(), connectionConfiguration.getDatabaseName());
        }
    }

    /**
     * 从数据库连接列表中移除不可用的数据库连接。
     *
     * @param unavailableConnection 不可用的数据库连接
     */
    private void removeUnavailableClient(MysqlConnection unavailableConnection) {
        if (unavailableConnection == null) { // should not happen, just for bug detection
            String errorMessage = "Remove unavailable mysql connection failed: `null client`." + buildLogForParameters();
            LOG.error(errorMessage);
            throw new NullPointerException(errorMessage);
        }
        boolean isRemoveSuccess = false;
        int clientIndex;
        synchronized (connectionListUpdateLock) {
            //noinspection SuspiciousMethodCalls
            clientIndex = connectionList.indexOf(unavailableConnection);
            if (clientIndex >= 0) {
                connectionList.set(clientIndex, null);
                isRemoveSuccess = true;
                MysqlPooledConnection unavailablePooledConnection = (MysqlPooledConnection) unavailableConnection;
                unavailablePooledConnection.close();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Remove `MysqlConnection` from connection list success. `clientIndex`:`{}`.{}", clientIndex,
                            buildLogForParameters());
                }
            }
        }
        if (isRemoveSuccess && (state != BeanStatusEnum.CLOSED)) {
            startRescueTask();
            listener.onClosed(connectionConfiguration.getHost(), connectionConfiguration.getDatabaseName());
        }
    }

    /**
     * 启动 Mysql 连接重连恢复任务。
     */
    private void startRescueTask() {
        if (state == BeanStatusEnum.NORMAL) {
            synchronized (rescueTaskLock) {
                if (!isRescueTaskRunning) {
                    Thread rescueThread = new Thread() {

                        @Override
                        public void run() {
                            long startTime = System.currentTimeMillis();
                            MYSQL_CONNECTION_LOG.info("MysqlConnection rescue task has been started. `host`:`{}`. `databaseName`:`{}`.",
                                    connectionConfiguration.getHost(), connectionConfiguration.getDatabaseName());
                            try {
                                while (state == BeanStatusEnum.NORMAL) {
                                    boolean hasRecovered = true;
                                    for (int i = 0; i < connectionList.size(); i++) {
                                        if (connectionList.get(i) == null) {
                                            boolean isSuccess = createConnection(i);
                                            if (isSuccess) {
                                                MYSQL_CONNECTION_LOG.info("Rescue MysqlConnection success. `clientIndex`:`{}`. `host`:`{}`. `databaseName`:`{}`.",
                                                        i, connectionConfiguration.getHost(), connectionConfiguration.getDatabaseName());
                                                listener.onRecovered(connectionConfiguration.getHost(), connectionConfiguration.getDatabaseName());
                                            } else {
                                                hasRecovered = false;
                                                MYSQL_CONNECTION_LOG.warn("Rescue MysqlConnection failed. `clientIndex`:`{}`. `host`:`{}`. `databaseName`:`{}`.",
                                                        i, connectionConfiguration.getHost(), connectionConfiguration.getDatabaseName());
                                            }
                                        }
                                    }
                                    if (hasRecovered) {
                                        break;
                                    } else {
                                        Thread.sleep(500); // 还有未恢复的客户端，等待 500ms 后继续尝试
                                    }
                                }
                                MYSQL_CONNECTION_LOG.info("MysqlConnection rescue task has been finished. `cost`: {}ms. `host`:`{}`. `databaseName`:`{}`.",
                                        System.currentTimeMillis() - startTime, connectionConfiguration.getHost(), connectionConfiguration.getDatabaseName());
                            } catch (Exception e) {
                                MYSQL_CONNECTION_LOG.error("MysqlConnection rescue task execute failed: `{}`. `cost`:`{}ms`. `host`:`{}`. `databaseName`:`{}`.",
                                        e.getMessage(), System.currentTimeMillis() - startTime, connectionConfiguration.getHost(), connectionConfiguration.getDatabaseName());
                                LOG.error("MysqlConnection rescue task execute failed. `cost`:`" + (System.currentTimeMillis() - startTime)
                                        + "ms`. `host`:`" + connectionConfiguration.getHost() + "`. `databaseName`:`"
                                        + connectionConfiguration.getDatabaseName() + "`.", e);
                            } finally {
                                rescueOver();
                            }
                        }

                        private void rescueOver() {
                            synchronized (rescueTaskLock) {
                                isRescueTaskRunning = false;
                            }
                        }
                    };
                    rescueThread.setName("mysql-connection-rescue-task");
                    rescueThread.setDaemon(true);
                    rescueThread.start();
                    isRescueTaskRunning = true;
                }
            }
        }
    }

    /**
     * 返回当前 {@code MysqlDataSource} 相关参数信息，用于日志打印。
     */
    private String buildLogForParameters() {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("connectionConfiguration", connectionConfiguration);
        parameterMap.put("dataSourceConfiguration", dataSourceConfiguration);
        return LogBuildUtil.build(parameterMap);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlDataSource#getLogWriter()");
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlDataSource#setLogWriter(PrintWriter out)");
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlDataSource#getParentLogger()");
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlDataSource#setLoginTimeout(int seconds)",
                "You should use SocketConfiguration#setConnectionTimeout(int connectionTimeout)");
    }
}
