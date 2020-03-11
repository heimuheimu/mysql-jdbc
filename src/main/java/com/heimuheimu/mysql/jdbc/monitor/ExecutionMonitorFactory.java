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

import com.heimuheimu.naivemonitor.monitor.ExecutionMonitor;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Mysql 命令执行信息监控工厂类。
 *
 * @author heimuheimu
 */
public class ExecutionMonitorFactory {

    /**
     * Mysql 命令执行错误码：MYSQL 服务端执行异常
     */
    public static final int ERROR_CODE_MYSQL_ERROR = -1;

    /**
     * Mysql 命令执行错误码：管道或命令已关闭
     */
    public static final int ERROR_CODE_ILLEGAL_STATE = -2;

    /**
     * Mysql 命令执行错误码：执行超时
     */
    public static final int ERROR_CODE_TIMEOUT = -3;

    /**
     * Mysql 命令执行错误码：参数值设置不正确
     */
    public static final int ERROR_CODE_INVALID_PARAMETER = -4;

    /**
     * Mysql 命令执行错误码：查询结果集 {@code ResultSet} 操作错误
     */
    public static final int ERROR_CODE_RESULTSET_ERROR = -5;

    /**
     * Mysql 命令执行错误码：预期外异常
     */
    public static final int ERROR_CODE_UNEXPECTED_ERROR = -6;

    /**
     * Mysql 命令执行错误码：慢查
     */
    public static final int ERROR_CODE_SLOW_EXECUTION = -7;

    /**
     * Mysql 命令执行错误码：主键或唯一索引冲突
     */
    public static final int ERROR_CODE_DUPLICATE_ENTRY_FOR_KEY = -8;

    private static final ConcurrentHashMap<String, ExecutionMonitor> MYSQL_EXECUTION_MONITOR_MAP = new ConcurrentHashMap<>();

    private static final Object lock = new Object();

    private ExecutionMonitorFactory() {
        // private constructor
    }

    /**
     * 根据 Mysql 连接目标地址和数据库名称获得对应的操作执行信息监控器，该方法不会返回 {@code null}。
     *
     * @param host Mysql 连接目标地址
     * @param databaseName 数据库名称
     * @return 数据库操作执行信息监控器，该方法不会返回 {@code null}
     */
    public static ExecutionMonitor get(String host, String databaseName) {
        String key = host;
        if (databaseName != null && !databaseName.isEmpty()) {
            key += "/" + databaseName;
        }
        ExecutionMonitor monitor = MYSQL_EXECUTION_MONITOR_MAP.get(key);
        if (monitor == null) {
            synchronized (lock) {
                monitor = MYSQL_EXECUTION_MONITOR_MAP.get(key);
                if (monitor == null) {
                    monitor = new ExecutionMonitor();
                    MYSQL_EXECUTION_MONITOR_MAP.put(key, monitor);
                }
            }
        }
        return monitor;
    }
}
