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

import com.heimuheimu.mysql.jdbc.monitor.DataSourceMonitor;
import com.heimuheimu.mysql.jdbc.monitor.DataSourceMonitorFactory;
import com.heimuheimu.naivemonitor.prometheus.PrometheusCollector;
import com.heimuheimu.naivemonitor.prometheus.PrometheusData;
import com.heimuheimu.naivemonitor.prometheus.PrometheusSample;
import com.heimuheimu.naivemonitor.util.DeltaCalculator;

import java.util.ArrayList;
import java.util.List;

/**
 * Mysql 数据库连接池信息采集器，采集时会返回以下数据：
 * <ul>
 *     <li>mysql_jdbc_datasource_acquired_connection_count{database="$databaseAlias"} 当前连接池正在使用的连接数量</li>
 *     <li>mysql_jdbc_datasource_max_acquired_connection_count{database="$databaseAlias"} 相邻两次采集周期内连接池使用的最大连接数量</li>
 *     <li>mysql_jdbc_datasource_connection_leaked_count{database="$databaseAlias"} 相邻两次采集周期内连接池发生连接泄漏的次数</li>
 *     <li>mysql_jdbc_datasource_get_connection_failed_count{database="$databaseAlias"} 相邻两次采集周期内连接池获取不到连接的次数</li>
 * </ul>
 *
 * @author heimuheimu
 * @since 1.1
 */
public class DataSourcePrometheusCollector implements PrometheusCollector {

    /**
     * 需要采集的数据库名称别名列表，与 {@link #monitorList} 一一对应
     */
    private final List<String> databaseAliasList;

    /**
     * 需要采集的 Mysql 数据库连接池信息监控器列表，与 {@link #databaseAliasList} 一一对应
     */
    private final List<DataSourceMonitor> monitorList;

    /**
     * 差值计算器
     */
    private final DeltaCalculator deltaCalculator = new DeltaCalculator();

    /**
     * 构造一个 DataSourcePrometheusCollector 实例。
     *
     * @param configurationList 配置信息列表，不允许为 {@code null} 或空
     * @throws IllegalArgumentException 如果 configurationList 为 {@code null} 或空，将会抛出此异常
     */
    public DataSourcePrometheusCollector(List<DatabasePrometheusCollectorConfiguration> configurationList) throws IllegalArgumentException {
        if (configurationList == null || configurationList.isEmpty()) {
            throw new IllegalArgumentException("Create `DataSourcePrometheusCollector` failed: `configurationList could not be empty`.");
        }
        this.databaseAliasList = new ArrayList<>();
        this.monitorList = new ArrayList<>();
        for (DatabasePrometheusCollectorConfiguration configuration : configurationList) {
            this.databaseAliasList.add(configuration.getDatabaseAlias());
            this.monitorList.add(DataSourceMonitorFactory.get(configuration.getHost(), configuration.getDatabaseName()));
        }
    }

    @Override
    public List<PrometheusData> getList() {
        PrometheusData acquiredConnectionCountData = PrometheusData.buildGauge("mysql_jdbc_datasource_acquired_connection_count", "");
        PrometheusData maxAcquiredConnectionCountData = PrometheusData.buildGauge("mysql_jdbc_datasource_max_acquired_connection_count", "");
        PrometheusData connectionLeakedCountData = PrometheusData.buildGauge("mysql_jdbc_datasource_connection_leaked_count", "");
        PrometheusData getConnectionFailedCountData = PrometheusData.buildGauge("mysql_jdbc_datasource_get_connection_failed_count", "");
        for (int i = 0; i < databaseAliasList.size(); i++) {
            String databaseAlias = databaseAliasList.get(i);
            DataSourceMonitor monitor = monitorList.get(i);
            // add mysql_jdbc_datasource_acquired_connection_count sample
            acquiredConnectionCountData.addSample(PrometheusSample.build(monitor.getAcquiredConnectionCount())
                    .addSampleLabel("database", databaseAlias));

            // add mysql_jdbc_datasource_max_acquired_connection_count sample
            maxAcquiredConnectionCountData.addSample(PrometheusSample.build(monitor.getMaxAcquiredConnectionCount())
                    .addSampleLabel("database", databaseAlias));
            monitor.resetMaxAcquiredConnectionCount();

            // add mysql_jdbc_datasource_connection_leaked_count sample
            connectionLeakedCountData.addSample(PrometheusSample.build(deltaCalculator.delta("connectionLeakedCount",
                    monitor.getConnectionLeakedCount())).addSampleLabel("database", databaseAlias));

            // add mysql_jdbc_datasource_get_connection_failed_count sample
            getConnectionFailedCountData.addSample(PrometheusSample.build(deltaCalculator.delta("getConnectionFailedCount",
                    monitor.getGetConnectionFailedCount())).addSampleLabel("database", databaseAlias));
        }
        List<PrometheusData> dataList = new ArrayList<>();
        dataList.add(acquiredConnectionCountData);
        dataList.add(maxAcquiredConnectionCountData);
        dataList.add(connectionLeakedCountData);
        dataList.add(getConnectionFailedCountData);
        return dataList;
    }
}
