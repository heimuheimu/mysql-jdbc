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

import com.heimuheimu.mysql.jdbc.monitor.SQLFeatureNotSupportedMonitor;
import com.heimuheimu.naivemonitor.prometheus.PrometheusCollector;
import com.heimuheimu.naivemonitor.prometheus.PrometheusData;
import com.heimuheimu.naivemonitor.prometheus.PrometheusSample;
import com.heimuheimu.naivemonitor.util.DeltaCalculator;

import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link SQLFeatureNotSupportedException} 异常次数信息采集器，采集时会返回以下数据：
 * <ul>
 *     <li>mysql_jdbc_sql_feature_not_supported 相邻两次采集周期内出现的 SQLFeatureNotSupportedException 异常次数</li>
 * </ul>
 *
 * @author heimuheimu
 * @since 1.1
 */
public class SQLFeatureNotSupportedPrometheusCollector implements PrometheusCollector {

    /**
     * 差值计算器
     */
    private final DeltaCalculator deltaCalculator = new DeltaCalculator();

    @Override
    public List<PrometheusData> getList() {
        SQLFeatureNotSupportedMonitor monitor = SQLFeatureNotSupportedMonitor.getInstance();
        List<PrometheusData> dataList = new ArrayList<>();
        dataList.add(PrometheusData.buildGauge("mysql_jdbc_sql_feature_not_supported", "")
                .addSample(PrometheusSample.build(deltaCalculator.delta("sqlFeatureNotSupportedCount", monitor.getCount()))));
        return dataList;
    }
}
