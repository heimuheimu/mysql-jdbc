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
import com.heimuheimu.mysql.jdbc.monitor.ExecutionMonitorFactory;
import com.heimuheimu.naivemonitor.falcon.support.AbstractExecutionDataCollector;
import com.heimuheimu.naivemonitor.monitor.ExecutionMonitor;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mysql 数据库执行信息采集器。
 */
class DatabaseExecutionDataCollector extends AbstractExecutionDataCollector {

    private static final Map<Integer, String> ERROR_METRIC_SUFFIX_MAP;

    static {
        ERROR_METRIC_SUFFIX_MAP = new HashMap<>();
        ERROR_METRIC_SUFFIX_MAP.put(ExecutionMonitorFactory.ERROR_CODE_MYSQL_ERROR, "_mysql_error");
        ERROR_METRIC_SUFFIX_MAP.put(ExecutionMonitorFactory.ERROR_CODE_ILLEGAL_STATE, "_illegal_state");
        ERROR_METRIC_SUFFIX_MAP.put(ExecutionMonitorFactory.ERROR_CODE_TIMEOUT, "_timeout");
        ERROR_METRIC_SUFFIX_MAP.put(ExecutionMonitorFactory.ERROR_CODE_INVALID_PARAMETER, "_invalid_parameter");
        ERROR_METRIC_SUFFIX_MAP.put(ExecutionMonitorFactory.ERROR_CODE_RESULTSET_ERROR, "_result_error");
        ERROR_METRIC_SUFFIX_MAP.put(ExecutionMonitorFactory.ERROR_CODE_UNEXPECTED_ERROR, "_unexpected_error");
        ERROR_METRIC_SUFFIX_MAP.put(ExecutionMonitorFactory.ERROR_CODE_SLOW_EXECUTION, "_sql_slow_execution");
    }

    private final List<ExecutionMonitor> executionMonitors;

    /**
     * 当前采集器名称
     */
    private final String collectorName;

    /**
     * 构造一个 Mysql 数据库执行信息采集器。
     *
     * @param host Mysql 连接目标地址
     * @param databaseName 数据库名称
     * @param collectorName 采集器名称
     */
    DatabaseExecutionDataCollector(String host, String databaseName, String collectorName) {
        this.executionMonitors = Collections.singletonList(ExecutionMonitorFactory.get(host, databaseName));
        this.collectorName = collectorName;
    }

    @Override
    protected List<ExecutionMonitor> getExecutionMonitorList() {
        return executionMonitors;
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
    protected Map<Integer, String> getErrorMetricSuffixMap() {
        return ERROR_METRIC_SUFFIX_MAP;
    }

    @Override
    public int getPeriod() {
        return FalconDataCollectorConstant.REPORT_PERIOD;
    }
}
