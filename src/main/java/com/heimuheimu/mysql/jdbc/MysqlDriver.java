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

package com.heimuheimu.mysql.jdbc;

import com.heimuheimu.mysql.jdbc.constant.DriverVersion;
import com.heimuheimu.mysql.jdbc.facility.SQLFeatureNotSupportedExceptionBuilder;
import com.heimuheimu.mysql.jdbc.util.LogBuildUtil;
import com.heimuheimu.mysql.jdbc.util.MysqlConnectionBuildUtil;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Mysql 数据库驱动，用于创建 Mysql 连接。
 *
 * <p><strong>说明：</strong>{@code MysqlDriver} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class MysqlDriver implements Driver {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(MysqlDriver.class);

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        if (acceptsURL(url)) {
            try {
                return MysqlConnectionBuildUtil.build(url, info);
            } catch (Exception e) {
                LinkedHashMap<String, Object> parametersMap = new LinkedHashMap<>();
                parametersMap.put("url", url);
                parametersMap.put("info", info);
                String errorMessage = LogBuildUtil.buildMethodExecuteFailedLog("MysqlDriver#connect(String url, Properties info)",
                        "get mysql connection failed", parametersMap);
                LOG.error(errorMessage, e);
                throw new SQLException(errorMessage, e);
            }
        } else {
            return null;
        }
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return MysqlConnectionBuildUtil.acceptsURL(url);
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return new DriverPropertyInfo[0];
    }

    @Override
    public int getMajorVersion() {
        return DriverVersion.DRIVER_MAJOR_VERSION;
    }

    @Override
    public int getMinorVersion() {
        return DriverVersion.DRIVER_MINOR_VERSION;
    }

    @Override
    public boolean jdbcCompliant() {
        return false;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlDriver#getParentLogger()");
    }
}
