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

import com.heimuheimu.mysql.jdbc.monitor.SocketMonitorFactory;
import com.heimuheimu.naivemonitor.monitor.SocketMonitor;
import com.heimuheimu.naivemonitor.prometheus.PrometheusData;
import com.heimuheimu.naivemonitor.prometheus.PrometheusSample;
import com.heimuheimu.naivemonitor.prometheus.support.AbstractSocketPrometheusCollector;

import java.util.ArrayList;
import java.util.List;

/**
 * Mysql 数据库 Socket 读、写信息采集器，采集时会返回以下数据：
 * <ul>
 *     <li>mysql_jdbc_socket_read_count{database="$databaseAlias",remoteAddress="$remoteAddress"} 相邻两次采集周期内 Socket 读取的次数</li>
 *     <li>mysql_jdbc_socket_read_bytes{database="$databaseAlias",remoteAddress="$remoteAddress"} 相邻两次采集周期内 Socket 读取的字节总数</li>
 *     <li>mysql_jdbc_socket_max_read_bytes{database="$databaseAlias",remoteAddress="$remoteAddress"} 相邻两次采集周期内单次 Socket 读取的最大字节数</li>
 *     <li>mysql_jdbc_socket_write_count{database="$databaseAlias",remoteAddress="$remoteAddress"} 相邻两次采集周期内 Socket 写入的次数</li>
 *     <li>mysql_jdbc_socket_write_bytes{database="$databaseAlias",remoteAddress="$remoteAddress"} 相邻两次采集周期内 Socket 写入的字节总数</li>
 *     <li>mysql_jdbc_socket_max_write_bytes{database="$databaseAlias",remoteAddress="$remoteAddress"} 相邻两次采集周期内单次 Socket 写入的最大字节数</li>
 * </ul>
 *
 * @author heimuheimu
 * @since 1.1
 */
public class DatabaseSocketPrometheusCollector extends AbstractSocketPrometheusCollector {

    /**
     * 需要采集的数据库名称别名列表，与 {@link #monitorList} 一一对应
     */
    private final List<String> databaseAliasList;

    /**
     * 需要采集的 Mysql 数据库 Socket 读、写信息监控器列表，与 {@link #databaseAliasList} 一一对应
     */
    private final List<SocketMonitor> monitorList;

    /**
     * 构造一个 DatabaseSocketPrometheusCollector 实例。
     *
     * @param configurationList 配置信息列表，不允许为 {@code null} 或空
     * @throws IllegalArgumentException 如果 configurationList 为 {@code null} 或空，将会抛出此异常
     */
    public DatabaseSocketPrometheusCollector(List<DatabasePrometheusCollectorConfiguration> configurationList) throws IllegalArgumentException {
        if (configurationList == null || configurationList.isEmpty()) {
            throw new IllegalArgumentException("Create `DatabaseSocketPrometheusCollector` failed: `configurationList could not be empty`.");
        }
        this.databaseAliasList = new ArrayList<>();
        this.monitorList = new ArrayList<>();
        for (DatabasePrometheusCollectorConfiguration configuration : configurationList) {
            this.databaseAliasList.add(configuration.getDatabaseAlias());
            this.monitorList.add(SocketMonitorFactory.get(configuration.getHost(), configuration.getDatabaseName()));
        }
    }

    @Override
    protected String getMetricPrefix() {
        return "mysql_jdbc";
    }

    @Override
    protected List<SocketMonitor> getMonitorList() {
        return monitorList;
    }

    @Override
    protected void afterAddSample(int monitorIndex, PrometheusData data, PrometheusSample sample) {
        sample.addSampleLabel("database", databaseAliasList.get(monitorIndex));
    }
}
