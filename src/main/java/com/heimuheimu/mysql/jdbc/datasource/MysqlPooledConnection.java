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
import com.heimuheimu.mysql.jdbc.facility.UnusableServiceNotifier;
import com.heimuheimu.mysql.jdbc.net.BuildSocketException;

import java.sql.SQLException;

/**
 * 可复用 Mysql 数据库连接，通过 {@link MysqlDataSource} 进行管理。
 *
 * @author heimuheimu
 */
public class MysqlPooledConnection extends MysqlConnection {

    /**
     * 当前数据库连接在列表中的索引位置
     */
    private final int connectionIndex;

    /**
     * {@code MysqlPooledConnection} 连接关闭通知器
     */
    private final PooledConnectionClosedNotifier closedNotifier;

    /**
     * 当前数据库连接是否已被获取
     */
    private boolean isAcquired = false;

    /**
     * 当前数据库连接最后一次从连接池被获取的时间戳
     */
    private long lastAcquiredTimestamp = 0;

    /**
     * 当前数据库允许被占用的最大时间，单位：毫秒，如果为 0，则没有最大时间限制
     */
    private long maxOccupyTime = 0;

    /**
     * 获取当前数据库连接私有锁
     */
    private Object acquireLock = new Object();

    /**
     * 构造一个可重复使用的 Mysql 数据库连接。
     *
     * @param connectionIndex 当前数据库连接在连接池中的索引位置
     * @param configuration 建立 Mysql 数据库连接使用的配置信息，不允许为 {@code null}
     * @param timeout SQL 执行超时时间，单位：毫秒，如果等于 0，则没有超时时间限制，不允许设置小于 0 的值
     * @param slowExecutionThreshold 执行 Mysql 命令过慢最小时间，单位：毫秒，不能小于等于 0
     * @param unusableServiceNotifier {@code MysqlConnection} 不可用通知器，允许为 {@code null}
     * @param closedNotifier {@code MysqlPooledConnection} 连接关闭通知器
     * @throws IllegalArgumentException 如果 {@code configuration} 为 {@code null}，将会抛出此异常
     * @throws IllegalArgumentException 如果 {@code timeout} 小于 0，将会抛出此异常
     * @throws IllegalArgumentException 如果 {@code slowExecutionThreshold} 小于等于 0，将会抛出此异常
     * @throws BuildSocketException 如果创建与 Mysql 服务器的 Socket 连接失败，将会抛出此异常
     */
    public MysqlPooledConnection(int connectionIndex, ConnectionConfiguration configuration, int timeout,
                                 int slowExecutionThreshold, UnusableServiceNotifier<MysqlConnection> unusableServiceNotifier,
                                 PooledConnectionClosedNotifier closedNotifier) throws IllegalArgumentException, BuildSocketException {
        super(configuration, timeout, slowExecutionThreshold, unusableServiceNotifier);
        this.connectionIndex = connectionIndex;
        this.closedNotifier = closedNotifier;
    }

    /**
     * 占用当前数据库连接，如果占用成功，返回 {@code true}，否则返回 {@code false}。
     *
     * @param maxOccupyTime 最大占用时间，单位：毫秒，如果为 0，则没有最大时间限制
     * @return 是否占用成功
     */
    public boolean acquire(long maxOccupyTime) {
        if (isClosed())
            return false;
        synchronized (acquireLock) {
            if (!isAcquired) {
                isAcquired = true;
                lastAcquiredTimestamp = System.currentTimeMillis();
                this.maxOccupyTime = maxOccupyTime;
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * 判断当前数据库连接是否已泄漏（被占用时间超过获取连接时指定的 {@code maxOccupyTime}），如果当前连接已关闭，则返回 {@code false}。
     *
     * @return 是否已泄漏
     */
    public boolean isLeaked() {
        synchronized (acquireLock) {
            if (isAcquired && maxOccupyTime > 0) {
                return (System.currentTimeMillis() - lastAcquiredTimestamp) > maxOccupyTime;
            } else {
                return false;
            }
        }
    }

    /**
     * 获得当前数据库连接在连接池中的索引位置。
     *
     * @return 当前数据库连接在连接池中的索引位置
     */
    public int getConnectionIndex() {
        return connectionIndex;
    }

    @Override
    public void close() {
        synchronized (acquireLock) {
            if (isAcquired) {
                isAcquired = false;
                if (closedNotifier != null) {
                    closedNotifier.onClosed(this);
                }
            }
        }
    }

    /**
     * 关闭数据库物理连接，释放资源。
     *
     * @throws SQLException 如果关闭过程中发生错误，将会抛出此异常
     */
    public void closePhysicalConnection() {
        super.close();
    }

    @Override
    public String toString() {
        return "MysqlPooledConnection{" +
                "connectionIndex=" + connectionIndex +
                ", isAcquired=" + isAcquired +
                ", lastAcquiredTimestamp=" + lastAcquiredTimestamp +
                ", maxOccupyTime=" + maxOccupyTime +
                ", mysqlChannel=" + mysqlChannel +
                ", lastServerStatusInfo=" + lastServerStatusInfo +
                ", executionMonitor=" + executionMonitor +
                ", databaseMonitor=" + databaseMonitor +
                ", timeout=" + timeout +
                ", slowExecutionThreshold=" + slowExecutionThreshold +
                ", currentDatabaseName='" + currentDatabaseName + '\'' +
                ", transactionIsolation=" + transactionIsolation +
                ", readOnlyFlag=" + readOnlyFlag +
                '}';
    }
}
