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

import com.heimuheimu.mysql.jdbc.command.SQLCommand;
import com.heimuheimu.mysql.jdbc.monitor.ExecutionMonitorFactory;
import com.heimuheimu.mysql.jdbc.util.LogBuildUtil;
import com.heimuheimu.naivemonitor.monitor.ExecutionMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * {@link PreparedStatement} 实现类，通过 {@link SQLCommand} 执行 SQL 语句并返回结果。
 *
 * <p><strong>说明：</strong>{@code TextPreparedStatement} 类是非线程安全的，不允许多个线程使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class TextPreparedStatement extends TextStatement implements PreparedStatement {

    /**
     * {@code TextPreparedStatement} 错误信息日志
     */
    private static final Logger LOG = LoggerFactory.getLogger(TextPreparedStatement.class);

    private final String sqlTemplate;

    private final List<String> sqlParts;

    private final AtomicReferenceArray<String> parameterValues;

    /**
     * 构造一个 {@code TextStatement} 实例。
     *
     * @param mysqlConnection 创建当前 {@code TextStatement} 实例的 Mysql 数据库连接
     * @param executionMonitor Mysql 命令执行信息监控器，不允许为 {@code null}
     * @param slowExecutionThreshold 执行 Mysql 命令过慢最小时间，单位：纳秒，不能小于等于 0
     */
    public TextPreparedStatement(String sqlTemplate, MysqlConnection mysqlConnection, ExecutionMonitor executionMonitor,
                                 long slowExecutionThreshold) {
        super(mysqlConnection, executionMonitor, slowExecutionThreshold);
        this.sqlTemplate = sqlTemplate;
        this.sqlParts = parseSqlTemplate(sqlTemplate);
        this.parameterValues = new AtomicReferenceArray<>(this.sqlParts.size() - 1);
    }

    private List<String> parseSqlTemplate(String sqlTemplate) {
        List<String> sqlParts = new ArrayList<>();
        int fromIndex = 0;
        int questionMarkIndex;
        while ((questionMarkIndex = sqlTemplate.indexOf("?", fromIndex)) != -1) {
            sqlParts.add(sqlTemplate.substring(fromIndex, questionMarkIndex));
            fromIndex = questionMarkIndex + 1;
        }
        if (fromIndex < sqlTemplate.length()) {
            sqlParts.add(sqlTemplate.substring(fromIndex));
        } else {
            sqlParts.add("");
        }
        return sqlParts;
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        return null;
    }

    @Override
    public int executeUpdate() throws SQLException {
        return 0;
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        checkParameterIndex( "setNull(int parameterIndex, int sqlType)", parameterIndex);
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        checkParameterIndex( "setBoolean(int parameterIndex, boolean x)", parameterIndex);
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {

    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {

    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {

    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {

    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {

    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {

    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {

    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {

    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {

    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {

    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {

    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {

    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {

    }

    @Override
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {

    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {

    }

    @Override
    public void clearParameters() throws SQLException {

    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {

    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {

    }

    @Override
    public boolean execute() throws SQLException {
        return false;
    }

    @Override
    public void addBatch() throws SQLException {

    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {

    }

    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {

    }

    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {

    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {

    }

    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {

    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return null;
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {

    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {

    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {

    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {

    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {

    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return null;
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {

    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {

    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {

    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {

    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {

    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {

    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {

    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {

    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {

    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {

    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {

    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {

    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {

    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {

    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {

    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {

    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {

    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {

    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {

    }

    @Override
    public void setObject(int parameterIndex, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {

    }

    @Override
    public void setObject(int parameterIndex, Object x, SQLType targetSqlType) throws SQLException {

    }

    @Override
    public long executeLargeUpdate() throws SQLException {
        return 0;
    }

    /**
     * 检查设置的参数位置索引是否越界，如果越界，则抛出 {@code SQLException} 异常。
     *
     * @param methodName 调用该检查方法的方法名
     * @param parameterIndex 参数位置索引
     * @throws SQLException 如果参数位置索索引越界，则抛出 {@code SQLException} 异常
     */
    private void checkParameterIndex(String methodName, int parameterIndex) throws SQLException {
        if (parameterIndex < 0 || parameterIndex >= parameterValues.length()) {
            executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_INVALID_PARAMETER);
            Map<String, Object> parameterMap = new LinkedHashMap<>();
            parameterMap.put("parameterIndex", parameterIndex);
            parameterMap.put("parameterValues", parameterValues);
            parameterMap.put("sqlTemplate", sqlTemplate);
            parameterMap.put("mysqlChannel", mysqlChannel);
            String errorMessage = LogBuildUtil.buildMethodExecuteFailedLog("TextPreparedStatement#" + methodName,
                    "parameter index out of range", parameterMap);
            LOG.error(errorMessage);
            throw new SQLException(errorMessage);
        }
    }
}
