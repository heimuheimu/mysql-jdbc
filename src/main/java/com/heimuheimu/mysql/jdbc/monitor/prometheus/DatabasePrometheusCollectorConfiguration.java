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

import com.heimuheimu.mysql.jdbc.util.MysqlConnectionBuildUtil;

import java.net.MalformedURLException;
import java.util.Map;

/**
 * Mysql 数据库信息 Prometheus 采集器配置。
 *
 * @author heimuheimu
 * @since 1.1
 */
public class DatabasePrometheusCollectorConfiguration {

    /**
     * Mysql 连接目标地址，例如：localhost:3306
     */
    private final String host;

    /**
     * 数据库名称
     */
    private final String databaseName;

    /**
     * 数据库名称别名，通常情况下与数据库名称一致，如存在主、从，则需要通过别名进行区分
     */
    private final String databaseAlias;

    /**
     * 构造一个 PrometheusCollectorConfiguration 实例。
     *
     * @param jdbcURL Mysql JDBC URL，例如：jdbc:mysql://localhost:3306/demo
     * @param databaseAlias 数据库名称别名，通常情况下与数据库名称一致，如存在主、从，则需要通过别名进行区分，不允许为 {@code null} 或空
     * @throws IllegalArgumentException 如果 jdbcURL 不符合规则，将会抛出此异常
     * @throws IllegalArgumentException 如果 databaseAlias 为 {@code null} 或空，将会抛出此异常
     */
    public DatabasePrometheusCollectorConfiguration(String jdbcURL, String databaseAlias) throws IllegalArgumentException {
        if (databaseAlias == null || databaseAlias.isEmpty()) {
            throw new IllegalArgumentException("Create `DatabasePrometheusCollectorConfiguration` failed: `databaseAlias could not be null or empty`.");
        }
        try {
            Map<String, Object> properties = MysqlConnectionBuildUtil.parseURL(jdbcURL);
            this.host = (String) properties.get(MysqlConnectionBuildUtil.PROPERTY_HOST);
            this.databaseName = (String) properties.get(MysqlConnectionBuildUtil.PROPERTY_DATABASE_NAME);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Create `DatabasePrometheusCollectorConfiguration` failed: `invalid jdbc url`. `jdbcURL`:`"
                    + jdbcURL + "`.", e);
        }
        this.databaseAlias = databaseAlias;
    }

    /**
     * 构造一个 PrometheusCollectorConfiguration 实例。
     *
     * @param host Mysql 连接目标地址，例如：localhost:3306
     * @param databaseName 数据库名称
     * @param databaseAlias 数据库名称别名，通常情况下与数据库名称一致，如存在主、从，则需要通过别名进行区分，不允许为 {@code null} 或空
     * @throws IllegalArgumentException 如果 databaseAlias 为 {@code null} 或空，将会抛出此异常
     */
    public DatabasePrometheusCollectorConfiguration(String host, String databaseName, String databaseAlias) throws IllegalArgumentException {
        if (databaseAlias == null || databaseAlias.isEmpty()) {
            throw new IllegalArgumentException("Create `DatabasePrometheusCollectorConfiguration` failed: `databaseAlias could not be null or empty`.");
        }
        this.host = host;
        this.databaseName = databaseName;
        this.databaseAlias = databaseAlias;
    }

    /**
     * 获得 Mysql 连接目标地址，例如：localhost:3306。
     *
     * @return Mysql 连接目标地址，例如：localhost:3306
     */
    public String getHost() {
        return host;
    }

    /**
     * 获得数据库名称。
     *
     * @return 数据库名称
     */
    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * 获得数据库名称别名，通常情况下与数据库名称一致，如存在主、从，则需要通过别名进行区分。
     *
     * @return 数据库名称别名
     */
    public String getDatabaseAlias() {
        return databaseAlias;
    }

    @Override
    public String toString() {
        return "PrometheusCollectorConfiguration{" +
                "host='" + host + '\'' +
                ", databaseName='" + databaseName + '\'' +
                ", databaseAlias='" + databaseAlias + '\'' +
                '}';
    }
}
