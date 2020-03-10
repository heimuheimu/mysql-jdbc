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

import com.heimuheimu.mysql.jdbc.monitor.ExecutionMonitorFactory;
import com.heimuheimu.naivemonitor.monitor.ExecutionMonitor;
import com.heimuheimu.naivemonitor.prometheus.PrometheusData;
import com.heimuheimu.naivemonitor.prometheus.PrometheusSample;
import com.heimuheimu.naivemonitor.prometheus.support.AbstractExecutionPrometheusCollector;

import java.util.ArrayList;
import java.util.List;

/**
 * Mysql 命令执行信息采集器，采集时会返回以下数据：
 * <ul>
 *     <li>mysql_jdbc_exec_count{database="$databaseAlias"} 相邻两次采集周期内 SQL 语句执行次数</li>
 *     <li>mysql_jdbc_exec_peak_tps_count{database="$databaseAlias"} 相邻两次采集周期内每秒最大 SQL 语句执行次数</li>
 *     <li>mysql_jdbc_avg_exec_time_millisecond{database="$databaseAlias"} 相邻两次采集周期内每条 SQL 语句平均执行时间，单位：毫秒</li>
 *     <li>mysql_jdbc_max_exec_time_millisecond{database="$databaseAlias"} 相邻两次采集周期内单条 SQL 语句最大执行时间，单位：毫秒</li>
 * </ul>
 *
 * @author heimuheimu
 * @since 1.1
 */
public class DatabaseExecutionPrometheusCollector extends AbstractExecutionPrometheusCollector {

    /**
     * 需要采集的数据库名称别名列表，与 {@link #monitorList} 一一对应
     */
    private final List<String> databaseAliasList;

    /**
     * 需要采集的 Mysql 命令执行信息监控器列表，与 {@link #databaseAliasList} 一一对应
     */
    private final List<ExecutionMonitor> monitorList;

    /**
     * 构造一个 DatabaseExecutionPrometheusCollector 实例。
     *
     * @param configurationList 配置信息列表，不允许为 {@code null} 或空
     * @throws IllegalArgumentException 如果 configurationList 为 {@code null} 或空，将会抛出此异常
     */
    public DatabaseExecutionPrometheusCollector(List<DatabasePrometheusCollectorConfiguration> configurationList) throws IllegalArgumentException {
        if (configurationList == null || configurationList.isEmpty()) {
            throw new IllegalArgumentException("Create `DatabaseExecutionPrometheusCollector` failed: `configurationList could not be empty`.");
        }
        this.databaseAliasList = new ArrayList<>();
        this.monitorList = new ArrayList<>();
        for (DatabasePrometheusCollectorConfiguration configuration : configurationList) {
            this.databaseAliasList.add(configuration.getDatabaseAlias());
            this.monitorList.add(ExecutionMonitorFactory.get(configuration.getHost(), configuration.getDatabaseName()));
        }
    }

    @Override
    protected String getMetricPrefix() {
        return "mysql_jdbc";
    }

    @Override
    protected List<ExecutionMonitor> getMonitorList() {
        return monitorList;
    }

    @Override
    protected String getMonitorId(ExecutionMonitor monitor, int index) {
        return String.valueOf(index);
    }

    @Override
    protected void afterAddSample(int monitorIndex, PrometheusData data, PrometheusSample sample) {
        sample.addSampleLabel("database", databaseAliasList.get(monitorIndex));
    }
}
