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

package com.heimuheimu.mysql.jdbc.monitor.falcon;

import com.heimuheimu.mysql.jdbc.constant.FalconDataCollectorConstant;
import com.heimuheimu.mysql.jdbc.monitor.SocketMonitorFactory;
import com.heimuheimu.naivemonitor.falcon.support.AbstractSocketDataCollector;
import com.heimuheimu.naivemonitor.monitor.SocketMonitor;

import java.util.Collections;
import java.util.List;

/**
 * Mysql 数据库 Socket 读、写信息采集器。
 *
 * @author heimuheimu
 */
class DatabaseSocketDataCollector extends AbstractSocketDataCollector {

    private final List<SocketMonitor> socketMonitors;

    /**
     * 当前采集器名称
     */
    private final String collectorName;

    /**
     * 构造一个数据库 Socket 读、写信息采集器。
     *
     * @param host Mysql 连接目标地址
     * @param databaseName 数据库名称
     * @param collectorName 采集器名称
     */
    DatabaseSocketDataCollector(String host, String databaseName, String collectorName) {
        this.socketMonitors = Collections.singletonList(SocketMonitorFactory.get(host, databaseName));
        this.collectorName = collectorName;
    }

    @Override
    protected List<SocketMonitor> getSocketMonitorList() {
        return socketMonitors;
    }

    @Override
    protected String getModuleName() {
        return FalconDataCollectorConstant.MODULE_NAME;
    }

    @Override
    protected String getCollectorName() {
        return collectorName;
    }

    @Override
    public int getPeriod() {
        return FalconDataCollectorConstant.REPORT_PERIOD;
    }
}
