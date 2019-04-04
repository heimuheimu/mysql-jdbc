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

import com.heimuheimu.mysql.jdbc.util.LogBuildUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.LinkedHashMap;

/**
 * 基于 Mysql 实现的 {@link Savepoint} 对象。
 *
 * @author heimuheimu
 */
public class MysqlSavepoint implements Savepoint {

    /**
     * {@code MysqlConnection} 错误日志
     */
    private static final Logger LOG = LoggerFactory.getLogger(MysqlSavepoint.class);

    /**
     * Savepoint 名称
     */
    private final String savepointName;

    /**
     * 构造一个 {@code MysqlSavepoint} 实例。
     *
     * @param savepointName Savepoint 名称
     */
    public MysqlSavepoint(String savepointName) {
        this.savepointName = savepointName;
    }

    @Override
    public int getSavepointId() throws SQLException {
        LinkedHashMap<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("savepointName", savepointName);
        String errorMessage = LogBuildUtil.buildMethodExecuteFailedLog("MysqlSavepoint#getSavepointId()",
                "this is a named savepoint", parameterMap);
        LOG.error(errorMessage);
        throw new SQLException(errorMessage);
    }

    @Override
    public String getSavepointName() {
        return savepointName;
    }

    @Override
    public String toString() {
        return "MysqlSavepoint{" +
                "savepointName='" + savepointName + '\'' +
                '}';
    }
}
