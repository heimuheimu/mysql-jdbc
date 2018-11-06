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

package com.heimuheimu.mysql.jdbc.monitor;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Mysql 数据库信息监控器工厂类。
 *
 * @author heimuheimu
 */
public class DataSourceMonitorFactory {

    private static final ConcurrentHashMap<String, DataSourceMonitor> DATASOURCE_MONITOR_MAP = new ConcurrentHashMap<>();

    private static final Object lock = new Object();

    private DataSourceMonitorFactory() {
        // private constructor
    }

    /**
     * 根据 Mysql 连接目标地址和数据库名称获得对应的数据库连接池信息监控器，该方法不会返回 {@code null}。
     *
     * @param host Mysql 连接目标地址
     * @param databaseName 数据库名称
     * @return 数据库连接池信息监控器，该方法不会返回 {@code null}
     */
    public static DataSourceMonitor get(String host, String databaseName) {
        String key = host;
        if (databaseName != null && !databaseName.isEmpty()) {
            key += "/" + databaseName;
        }
        DataSourceMonitor monitor = DATASOURCE_MONITOR_MAP.get(key);
        if (monitor == null) {
            synchronized (lock) {
                monitor = DATASOURCE_MONITOR_MAP.get(key);
                if (monitor == null) {
                    monitor = new DataSourceMonitor();
                    DATASOURCE_MONITOR_MAP.put(key, monitor);
                }
            }
        }
        return monitor;
    }
}
