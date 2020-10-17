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
import com.heimuheimu.mysql.jdbc.ConnectionInfo;
import com.heimuheimu.mysql.jdbc.monitor.DataSourceMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 数据库连接池连接泄漏检查器。
 *
 * <p><strong>说明：</strong>{@code LeakedConnectionDetector} 类是程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class LeakedConnectionDetector {

    private static final Logger LOG = LoggerFactory.getLogger(LeakedConnectionDetector.class);

    private static final Logger MYSQL_CONNECTION_LOG = LoggerFactory.getLogger("MYSQL_CONNECTION_LOG");

    private static final CopyOnWriteArrayList<MysqlDataSource> DATA_SOURCE_LIST = new CopyOnWriteArrayList<>();

    private static final Object LOCK = new Object();

    private static LeakedConnectionDetectTask DETECT_TASK = null;

    private static volatile boolean IS_SHUTDOWN = false;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            IS_SHUTDOWN = true;
            synchronized (LOCK) {
                if (DETECT_TASK != null) {
                    DETECT_TASK.interrupt();
                }
            }
        }));
    }

    public static void register(MysqlDataSource dataSource) {
        DATA_SOURCE_LIST.add(dataSource);
        startDetectTask();
    }

    /**
     * 启动数据库连接池泄漏检查线程。
     */
    private static void startDetectTask() {
        synchronized (LOCK) {
            if (DETECT_TASK == null) {
                DETECT_TASK = new LeakedConnectionDetectTask();
                DETECT_TASK.setName("leaked-mysql-connection-detector");
                DETECT_TASK.setDaemon(true);
                DETECT_TASK.start();
            }
        }
    }

    /**
     * 连接泄漏检查任务。
     */
    private static class LeakedConnectionDetectTask extends Thread {

        @Override
        public void run() {
            MYSQL_CONNECTION_LOG.info("Leaked connection detect task has been started.");
            try {
                while (!IS_SHUTDOWN) {
                    for (MysqlDataSource dataSource : DATA_SOURCE_LIST) {
                        List<MysqlPooledConnection> connectionList = dataSource.getConnectionList();
                        DataSourceMonitor dataSourceMonitor = dataSource.getDataSourceMonitor();
                        for (int i = 0; i < connectionList.size(); i++) {
                            try {
                                MysqlPooledConnection connection = connectionList.get(i);
                                if (connection != null) {
                                    if (connection.isLeaked()) {
                                        ConnectionInfo connectionInfo = connection.getMysqlChannel().getConnectionInfo();
                                        ConnectionConfiguration connectionConfiguration = connection.getMysqlChannel().getConnectionConfiguration();
                                        MYSQL_CONNECTION_LOG.warn("Found leaked connection. `connectionId`:`{}`. `host`:`{}`. `databaseName`:`{}`.",
                                                connectionInfo != null ? connectionInfo.getConnectionId() : "", connectionConfiguration.getHost(),
                                                connectionConfiguration.getDatabaseName());
                                        dataSourceMonitor.onConnectionLeaked();
                                        connection.closePhysicalConnection();
                                    }
                                }
                            } catch (Exception e) { // should not happen, just for bug detect
                                LOG.error("Leaked connection detect failed: `unexpected error`.", e);
                            }
                        }
                    }
                    //noinspection BusyWait
                    Thread.sleep(5000);
                }
                MYSQL_CONNECTION_LOG.info("Leaked connection detect task has been stopped.");
            } catch(InterruptedException e) {
                MYSQL_CONNECTION_LOG.info("Leaked connection detect task has been stopped.");
            } catch (Exception e) { // should not happen, just for bug detect
                LOG.error("Leaked connection detect task execute failed: `unexpected error`.", e);
                MYSQL_CONNECTION_LOG.error("Leaked connection detect task stopped: `unexpected error`.");
            }
        }
    }
}
