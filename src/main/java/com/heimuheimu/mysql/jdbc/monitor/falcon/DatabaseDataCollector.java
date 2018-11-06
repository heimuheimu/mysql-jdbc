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
import com.heimuheimu.mysql.jdbc.monitor.DatabaseMonitor;
import com.heimuheimu.mysql.jdbc.monitor.DatabaseMonitorFactory;
import com.heimuheimu.naivemonitor.falcon.FalconData;
import com.heimuheimu.naivemonitor.falcon.support.AbstractFalconDataCollector;

import java.util.ArrayList;
import java.util.List;

/**
 * Mysql 数据库监控信息采集器。
 *
 * @author heimuheimu
 */
public class DatabaseDataCollector extends AbstractFalconDataCollector {

    private final DatabaseSocketDataCollector databaseSocketDataCollector;

    private final DatabaseExecutionDataCollector databaseExecutionDataCollector;

    private final DatabaseMonitor databaseMonitor;

    private final String collectorName;

    private volatile long lastSelectRowsCount = 0;

    private volatile long lastInsertRowsCount = 0;

    private volatile long lastUpdateRowsCount = 0;

    private volatile long lastDeleteRowsCount = 0;

    /**
     * 构造一个 Mysql 数据库监控信息采集器。
     *
     * @param host Mysql 连接目标地址
     * @param databaseName 数据库名称
     * @param collectorName 采集器名称，用于区分同一数据库的主库、从库
     */
    public DatabaseDataCollector(String host, String databaseName, String collectorName) {
        this.collectorName = collectorName;
        databaseSocketDataCollector = new DatabaseSocketDataCollector(host, databaseName, this.collectorName);
        databaseExecutionDataCollector = new DatabaseExecutionDataCollector(host, databaseName, this.collectorName);
        this.databaseMonitor = DatabaseMonitorFactory.get(host, databaseName);
    }

    @Override
    public List<FalconData> getList() {
        List<FalconData> falconDataList = new ArrayList<>();

        falconDataList.addAll(databaseSocketDataCollector.getList());
        falconDataList.addAll(databaseExecutionDataCollector.getList());

        long selectRowsCount = databaseMonitor.getSelectRowsCount();
        falconDataList.add(create("_select_rows_count", selectRowsCount - lastSelectRowsCount));
        lastSelectRowsCount = selectRowsCount;

        long insertRowsCount = databaseMonitor.getInsertRowsCount();
        falconDataList.add(create("_insert_rows_count", insertRowsCount - lastInsertRowsCount));
        lastInsertRowsCount = insertRowsCount;

        long updateRowsCount = databaseMonitor.getUpdateRowsCount();
        falconDataList.add(create("_update_rows_count", updateRowsCount - lastUpdateRowsCount));
        lastUpdateRowsCount = updateRowsCount;

        long deleteRowsCount = databaseMonitor.getDeleteRowsCount();
        falconDataList.add(create("_delete_rows_count", deleteRowsCount - lastDeleteRowsCount));
        lastDeleteRowsCount = deleteRowsCount;

        return falconDataList;
    }

    @Override
    protected String getModuleName() {
        return FalconDataCollectorConstant.MODULE_NAME;
    }

    @Override
    protected String getCollectorName() {
        return this.collectorName;
    }

    @Override
    public int getPeriod() {
        return FalconDataCollectorConstant.REPORT_PERIOD;
    }
}
