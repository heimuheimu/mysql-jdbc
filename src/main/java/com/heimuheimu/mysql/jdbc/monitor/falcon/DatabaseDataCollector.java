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
import com.heimuheimu.mysql.jdbc.monitor.DataSourceMonitor;
import com.heimuheimu.mysql.jdbc.monitor.DataSourceMonitorFactory;
import com.heimuheimu.mysql.jdbc.monitor.DatabaseMonitor;
import com.heimuheimu.mysql.jdbc.monitor.DatabaseMonitorFactory;
import com.heimuheimu.mysql.jdbc.util.MysqlConnectionBuildUtil;
import com.heimuheimu.naivemonitor.falcon.FalconData;
import com.heimuheimu.naivemonitor.falcon.support.AbstractFalconDataCollector;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    private final DataSourceMonitor dataSourceMonitor;

    private volatile long lastConnectionLeakedCount = 0;

    private volatile long lastGetConnectionFailedCount = 0;

    /**
     * 构造一个 Mysql 数据库监控信息采集器。
     *
     * @param jdbcURL Mysql JDBC URL，例如：jdbc:mysql://localhost:3306/demo
     * @param collectorName 采集器名称，生成 Falcon 的 Metric 名称时使用，每个库唯一
     * @throws IllegalArgumentException 如果 JDBC URL 不符合规则，将抛出此异常
     */
    public DatabaseDataCollector(String jdbcURL, String collectorName) throws IllegalArgumentException {
        try {
            Map<String, Object> properties = MysqlConnectionBuildUtil.parseURL(jdbcURL);
            String host = (String) properties.get(MysqlConnectionBuildUtil.PROPERTY_HOST);
            String databaseName = (String) properties.get(MysqlConnectionBuildUtil.PROPERTY_DATABASE_NAME);
            this.collectorName = collectorName;
            databaseSocketDataCollector = new DatabaseSocketDataCollector(host, databaseName, this.collectorName);
            databaseExecutionDataCollector = new DatabaseExecutionDataCollector(host, databaseName, this.collectorName);
            this.databaseMonitor = DatabaseMonitorFactory.get(host, databaseName);
            this.dataSourceMonitor = DataSourceMonitorFactory.get(host, databaseName);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Create `DatabaseDataCollector` failed: `invalid jdbc url`. `jdbcURL`:`"
                    + jdbcURL + "`.", e);
        }
    }

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
        this.dataSourceMonitor = DataSourceMonitorFactory.get(host, databaseName);
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

        long acquiredConnectionCount = dataSourceMonitor.getAcquiredConnectionCount();
        falconDataList.add(create("_datasource_acquired_connection_count", acquiredConnectionCount));

        long maxAcquiredConnectionCount = dataSourceMonitor.getMaxAcquiredConnectionCount();
        dataSourceMonitor.resetMaxAcquiredConnectionCount();
        falconDataList.add(create("_datasource_max_acquired_connection_count", maxAcquiredConnectionCount));

        long connectionLeakedCount = dataSourceMonitor.getConnectionLeakedCount();
        falconDataList.add(create("_datasource_connection_leaked_count", connectionLeakedCount - lastConnectionLeakedCount));
        lastConnectionLeakedCount = connectionLeakedCount;

        long getConnectionFailedCount = dataSourceMonitor.getGetConnectionFailedCount();
        falconDataList.add(create("_datasource_get_connection_failed_count", getConnectionFailedCount - lastGetConnectionFailedCount));
        lastGetConnectionFailedCount = getConnectionFailedCount;

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
