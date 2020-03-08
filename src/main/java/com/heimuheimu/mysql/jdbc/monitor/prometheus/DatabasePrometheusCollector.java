/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 heimuheimu
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

package com.heimuheimu.mysql.jdbc.monitor.prometheus;

import com.heimuheimu.mysql.jdbc.monitor.DatabaseMonitor;
import com.heimuheimu.mysql.jdbc.monitor.DatabaseMonitorFactory;
import com.heimuheimu.naivemonitor.prometheus.PrometheusCollector;
import com.heimuheimu.naivemonitor.prometheus.PrometheusData;
import com.heimuheimu.naivemonitor.prometheus.PrometheusSample;
import com.heimuheimu.naivemonitor.util.DeltaCalculator;

import java.util.ArrayList;
import java.util.List;

/**
 * Mysql 数据库 SQL 语句统计信息采集器，采集时会返回以下数据：
 * <ul>
 *     <li>mysql_jdbc_select_count{database="$databaseAlias"} 相邻两次采集周期内 SELECT 语句执行总次数</li>
 *     <li>mysql_jdbc_select_rows_count{database="$databaseAlias"} 相邻两次采集周期内所有 SELECT 语句返回的记录总数</li>
 *     <li>mysql_jdbc_max_select_rows_count{database="$databaseAlias"} 相邻两次采集周期内单条 SELECT 语句返回的最大记录数</li>
 *     <li>mysql_jdbc_insert_count{database="$databaseAlias"} 相邻两次采集周期内所有 INSERT 语句执行总次数</li>
 *     <li>mysql_jdbc_insert_rows_count{database="$databaseAlias"} 相邻两次采集周期内所有 INSERT 语句插入的记录总数</li>
 *     <li>mysql_jdbc_max_insert_rows_count{database="$databaseAlias"} 相邻两次采集周期内单条 INSERT 语句插入的最大记录数</li>
 *     <li>mysql_jdbc_update_count{database="$databaseAlias"} 相邻两次采集周期内所有 UPDATE 语句执行总次数</li>
 *     <li>mysql_jdbc_update_rows_count{database="$databaseAlias"} 相邻两次采集周期内所有 UPDATE 语句更新的记录总数</li>
 *     <li>mysql_jdbc_max_update_rows_count{database="$databaseAlias"} 相邻两次采集周期内单条 UPDATE 语句更新的最大记录数</li>
 *     <li>mysql_jdbc_delete_count{database="$databaseAlias"} 相邻两次采集周期内所有 DELETE 语句执行总次数</li>
 *     <li>mysql_jdbc_delete_rows_count{database="$databaseAlias"} 相邻两次采集周期内所有 DELETE 语句删除的记录总数</li>
 *     <li>mysql_jdbc_max_delete_rows_count{database="$databaseAlias"} 相邻两次采集周期内单条 DELETE 语句删除的最大记录数</li>
 * </ul>
 *
 * @author heimuheimu
 * @since 1.1
 */
public class DatabasePrometheusCollector implements PrometheusCollector {

    /**
     * 需要采集的数据库名称别名列表，与 {@link #monitorList} 一一对应
     */
    private final List<String> databaseAliasList;

    /**
     * 需要采集的 Mysql 数据库 SQL 语句统计信息监控器列表，与 {@link #databaseAliasList} 一一对应
     */
    private final List<DatabaseMonitor> monitorList;

    /**
     * 差值计算器
     */
    private final DeltaCalculator deltaCalculator = new DeltaCalculator();

    /**
     * 构造一个 DatabasePrometheusCollector 实例。
     *
     * @param configurationList 配置信息列表，不允许为 {@code null} 或空
     * @throws IllegalArgumentException 如果 configurationList 为 {@code null} 或空，将会抛出此异常
     */
    public DatabasePrometheusCollector(List<DatabasePrometheusCollectorConfiguration> configurationList) throws IllegalArgumentException {
        if (configurationList == null || configurationList.isEmpty()) {
            throw new IllegalArgumentException("Create `DatabasePrometheusCollector` failed: `configurationList could not be empty`.");
        }
        this.databaseAliasList = new ArrayList<>();
        this.monitorList = new ArrayList<>();
        for (DatabasePrometheusCollectorConfiguration configuration : configurationList) {
            this.databaseAliasList.add(configuration.getDatabaseAlias());
            this.monitorList.add(DatabaseMonitorFactory.get(configuration.getHost(), configuration.getDatabaseName()));
        }
    }

    @Override
    public List<PrometheusData> getList() {
        //build metrics for select
        PrometheusData selectCountData = PrometheusData.buildGauge("mysql_jdbc_select_count", "");
        PrometheusData selectRowsCountData = PrometheusData.buildGauge("mysql_jdbc_select_rows_count", "");
        PrometheusData maxSelectRowsCountData = PrometheusData.buildGauge("mysql_jdbc_max_select_rows_count", "");
        //build metrics for insert
        PrometheusData insertCountData = PrometheusData.buildGauge("mysql_jdbc_insert_count", "");
        PrometheusData insertRowsCountData = PrometheusData.buildGauge("mysql_jdbc_insert_rows_count", "");
        PrometheusData maxInsertRowsCountData = PrometheusData.buildGauge("mysql_jdbc_max_insert_rows_count", "");
        //build metrics for update
        PrometheusData updateCountData = PrometheusData.buildGauge("mysql_jdbc_update_count", "");
        PrometheusData updateRowsCountData = PrometheusData.buildGauge("mysql_jdbc_update_rows_count", "");
        PrometheusData maxUpdateRowsCountData = PrometheusData.buildGauge("mysql_jdbc_max_update_rows_count", "");
        //build metrics for delete
        PrometheusData deleteCountData = PrometheusData.buildGauge("mysql_jdbc_delete_count", "");
        PrometheusData deleteRowsCountData = PrometheusData.buildGauge("mysql_jdbc_delete_rows_count", "");
        PrometheusData maxDeleteRowsCountData = PrometheusData.buildGauge("mysql_jdbc_max_delete_rows_count", "");
        for (int i = 0; i < databaseAliasList.size(); i++) {
            String databaseAlias = databaseAliasList.get(i);
            DatabaseMonitor monitor = monitorList.get(i);
            // add samples for select
            selectCountData.addSample(PrometheusSample.build(deltaCalculator.delta("selectCount", monitor.getSelectCount()))
                    .addSampleLabel("database", databaseAlias));
            selectRowsCountData.addSample(PrometheusSample.build(deltaCalculator.delta("selectRowsCount", monitor.getSelectRowsCount()))
                    .addSampleLabel("database", databaseAlias));
            maxSelectRowsCountData.addSample(PrometheusSample.build(monitor.getMaxSelectRowsCount())
                    .addSampleLabel("database", databaseAlias));
            monitor.resetMaxSelectRowsCount();
            // add samples for insert
            insertCountData.addSample(PrometheusSample.build(deltaCalculator.delta("insertCount", monitor.getInsertCount()))
                    .addSampleLabel("database", databaseAlias));
            insertRowsCountData.addSample(PrometheusSample.build(deltaCalculator.delta("insertRowsCount", monitor.getInsertRowsCount()))
                    .addSampleLabel("database", databaseAlias));
            maxInsertRowsCountData.addSample(PrometheusSample.build(monitor.getMaxInsertRowsCount())
                    .addSampleLabel("database", databaseAlias));
            monitor.resetMaxInsertRowsCount();
            // add samples for update
            updateCountData.addSample(PrometheusSample.build(deltaCalculator.delta("updateCount", monitor.getUpdateCount()))
                    .addSampleLabel("database", databaseAlias));
            updateRowsCountData.addSample(PrometheusSample.build(deltaCalculator.delta("updateRowsCount", monitor.getUpdateRowsCount()))
                    .addSampleLabel("database", databaseAlias));
            maxUpdateRowsCountData.addSample(PrometheusSample.build(monitor.getMaxUpdateRowsCount())
                    .addSampleLabel("database", databaseAlias));
            monitor.resetMaxUpdateRowsCount();
            // add samples for delete
            deleteCountData.addSample(PrometheusSample.build(deltaCalculator.delta("deleteCount", monitor.getDeleteCount()))
                    .addSampleLabel("database", databaseAlias));
            deleteRowsCountData.addSample(PrometheusSample.build(deltaCalculator.delta("deleteRowsCount", monitor.getDeleteRowsCount()))
                    .addSampleLabel("database", databaseAlias));
            maxDeleteRowsCountData.addSample(PrometheusSample.build(monitor.getMaxDeleteRowsCount())
                    .addSampleLabel("database", databaseAlias));
            monitor.resetMaxDeleteRowsCount();
        }
        List<PrometheusData> dataList = new ArrayList<>();
        dataList.add(selectCountData);
        dataList.add(selectRowsCountData);
        dataList.add(maxSelectRowsCountData);
        dataList.add(insertCountData);
        dataList.add(insertRowsCountData);
        dataList.add(maxInsertRowsCountData);
        dataList.add(updateCountData);
        dataList.add(updateRowsCountData);
        dataList.add(maxUpdateRowsCountData);
        dataList.add(deleteCountData);
        dataList.add(deleteRowsCountData);
        dataList.add(maxDeleteRowsCountData);
        return dataList;
    }
}