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
import com.heimuheimu.mysql.jdbc.facility.SQLFeatureNotSupportedExceptionBuilder;
import com.heimuheimu.mysql.jdbc.monitor.DatabaseMonitor;
import com.heimuheimu.mysql.jdbc.monitor.ExecutionMonitorFactory;
import com.heimuheimu.mysql.jdbc.util.BytesUtil;
import com.heimuheimu.mysql.jdbc.util.LogBuildUtil;
import com.heimuheimu.mysql.jdbc.util.StringUtil;
import com.heimuheimu.naivemonitor.monitor.ExecutionMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Date;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Consumer;

/**
 * {@link PreparedStatement} 实现类，通过 {@link SQLCommand} 执行 SQL 语句并返回结果。
 *
 * <p><strong>说明：</strong>{@code TextPreparedStatement} 类是非线程安全的，不允许多个线程使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class TextPreparedStatement extends TextStatement implements PreparedStatement {

    /**
     * {@link java.sql.Date} 类型数据格式化器
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd");

    /**
     * {@link java.sql.Time} 类型数据格式化
     */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    /**
     * {@link java.sql.Timestamp} 类型数据格式化
     */
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS");

    /**
     * MYSQL 支持的最小 GMT 时间：'1000-01-07 00:00:00'
     */
    private static final long MYSQL_MIN_TIME = -30609705600000L;

    /**
     * MYSQL 支持的最大 GMT 时间：'9999-12-30 23:59:59'
     */
    private static final long MYSQL_MAX_TIME = 253402214399000L;

    /**
     * {@code TextPreparedStatement} 错误信息日志
     */
    private static final Logger LOG = LoggerFactory.getLogger(TextPreparedStatement.class);

    /**
     * SQL NULL 值
     */
    private static final String NULL_VALUE = "NULL";

    /**
     * SQL 语句模版，参数值在模版中使用 "?" 作为占位符
     */
    private final String sqlTemplate;

    /**
     * SQL 语句拆分后列表，每两个内容中间需填充一个参数值
     */
    private final List<String> sqlParts;

    /**
     * 参数值数组
     */
    private final AtomicReferenceArray<String> parameterValues;

    /**
     * 构造一个 {@code TextStatement} 实例。
     *
     * @param mysqlConnection 创建当前 {@code TextStatement} 实例的 Mysql 数据库连接
     * @param executionMonitor Mysql 命令执行信息监控器，不允许为 {@code null}
     * @param slowExecutionThreshold 执行 Mysql 命令过慢最小时间，单位：纳秒，不能小于等于 0
     */
    public TextPreparedStatement(String sqlTemplate, MysqlConnection mysqlConnection, ExecutionMonitor executionMonitor,
                                 DatabaseMonitor databaseMonitor, long slowExecutionThreshold) {
        super(mysqlConnection, executionMonitor, databaseMonitor, slowExecutionThreshold);
        this.sqlTemplate = sqlTemplate;
        this.sqlParts = parseSqlTemplate(sqlTemplate);
        this.parameterValues = new AtomicReferenceArray<>(this.sqlParts.size() - 1);
    }

    /**
     * 解析 SQL 语句模版，将其拆分后生成对应的 SQL 语句列表，每两个内容中间需填充一个参数值。
     *
     * @param sqlTemplate SQL 语句模版
     * @return SQL 语句拆分后列表，每两个内容中间需填充一个参数值
     */
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

    /**
     * 根据当前 SQL 语句模版和已设置的参数值组装成完整的 SQL 语句。
     *
     * @return SQL 语句
     * @throws SQLException 如果有参数值尚未进行设置，将会抛出此异常
     */
    private String buildSql() throws SQLException {
        StringBuilder buffer = new StringBuilder(256);
        for (int i = 0; i < (sqlParts.size() - 1); i++) {
            buffer.append(sqlParts.get(i));
            String parameterValue = parameterValues.get(i);
            if (parameterValue != null) {
                buffer.append(parameterValue);
            } else {
                executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_INVALID_PARAMETER);
                String errorMessage = buildSetParameterErrorMessage("buildSql()", (i + 1), null,
                        "build sql failed, parameter value has not been set", null);
                LOG.error(errorMessage);
                throw new SQLException(errorMessage);
            }
        }
        buffer.append(sqlParts.get(sqlParts.size() - 1));
        return buffer.toString();
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        checkParameterIndex( "setNull(int parameterIndex, int sqlType)", parameterIndex, null);
        parameterValues.set(parameterIndex - 1, NULL_VALUE);
    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        checkParameterIndex( "setNull(int parameterIndex, int sqlType, String typeName)", parameterIndex, null);
        parameterValues.set(parameterIndex - 1, NULL_VALUE);
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        checkParameterIndex( "setBoolean(int parameterIndex, boolean x)", parameterIndex, x);
        parameterValues.set(parameterIndex - 1, x ? "1" : "0");
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        checkParameterIndex( "setByte(int parameterIndex, byte x)", parameterIndex, x);
        String hex = BytesUtil.toHex(new byte[]{x});
        parameterValues.set(parameterIndex - 1, "X'" + hex + "'");
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        checkParameterIndex( "setBytes(int parameterIndex, byte[] x)", parameterIndex, x);
        if (x != null) {
            String hex = BytesUtil.toHex(x);
            parameterValues.set(parameterIndex - 1, "X'" + hex + "'");
        } else {
            parameterValues.set(parameterIndex - 1, NULL_VALUE);
        }
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        checkParameterIndex( "setShort(int parameterIndex, short x)", parameterIndex, x);
        parameterValues.set(parameterIndex - 1, String.valueOf(x));
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        checkParameterIndex( "setInt(int parameterIndex, int x)", parameterIndex, x);
        parameterValues.set(parameterIndex - 1, String.valueOf(x));
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        checkParameterIndex( "setLong(int parameterIndex, long x)", parameterIndex, x);
        parameterValues.set(parameterIndex - 1, String.valueOf(x));
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        checkParameterIndex( "setFloat(int parameterIndex, float x)", parameterIndex, x);
        parameterValues.set(parameterIndex - 1, String.valueOf(x));
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        checkParameterIndex( "setDouble(int parameterIndex, double x)", parameterIndex, x);
        parameterValues.set(parameterIndex - 1, String.valueOf(x));
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        checkParameterIndex( "setBigDecimal(int parameterIndex, BigDecimal x)", parameterIndex, x);
        if (x != null) {
            parameterValues.set(parameterIndex - 1, x.toPlainString());
        } else {
            parameterValues.set(parameterIndex - 1, NULL_VALUE);
        }
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        checkParameterIndex( "setString(int parameterIndex, String x)", parameterIndex, x);
        if (x != null) {
            parameterValues.set(parameterIndex - 1, "'" + StringUtil.escape(x) + "'");
        } else {
            parameterValues.set(parameterIndex - 1, NULL_VALUE);
        }
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        setString(parameterIndex, value);
    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
        checkParameterIndex( "setURL(int parameterIndex, URL x)", parameterIndex, x);
        if (x != null) {
            parameterValues.set(parameterIndex - 1, "'" + StringUtil.escape(x.toString()) + "'");
        } else {
            parameterValues.set(parameterIndex - 1, NULL_VALUE);
        }
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
        setDate(parameterIndex, x, Calendar.getInstance());
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        String methodName = "setDate(int parameterIndex, Date x, Calendar cal)";
        checkParameterIndex(methodName, parameterIndex, x);
        if (x != null) {
            try {
                if (cal == null) {
                    cal = Calendar.getInstance();
                }
                cal.setTimeInMillis(getSafeTime(x));
                LocalDateTime ldt = LocalDateTime.ofInstant(cal.toInstant(), cal.getTimeZone().toZoneId());
                parameterValues.set(parameterIndex - 1, "'" + ldt.format(DATE_FORMATTER) + "'");
            } catch (Exception e) {
                executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_INVALID_PARAMETER);
                Calendar finalCal = cal;
                String errorMessage = buildSetParameterErrorMessage(methodName, parameterIndex, x,
                        "set date parameter failed", parameterMap -> parameterMap.put("calendar", finalCal));
                LOG.error(errorMessage, e);
                throw new SQLException(errorMessage, e);
            }
        } else {
            parameterValues.set(parameterIndex - 1, NULL_VALUE);
        }
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        setTime(parameterIndex, x, Calendar.getInstance());
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        String methodName = "setTime(int parameterIndex, Time x, Calendar cal)";
        checkParameterIndex(methodName, parameterIndex, x);
        if (x != null) {
            try {
                if (cal == null) {
                    cal = Calendar.getInstance();
                }
                cal.setTimeInMillis(getSafeTime(x));
                LocalDateTime ldt = LocalDateTime.ofInstant(cal.toInstant(), cal.getTimeZone().toZoneId());
                parameterValues.set(parameterIndex - 1, "'" + ldt.format(TIME_FORMATTER) + "'");
            } catch (Exception e) {
                executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_INVALID_PARAMETER);
                Calendar finalCal = cal;
                String errorMessage = buildSetParameterErrorMessage(methodName, parameterIndex, x,
                        "set time parameter failed", parameterMap -> parameterMap.put("calendar", finalCal));
                LOG.error(errorMessage, e);
                throw new SQLException(errorMessage, e);
            }
        } else {
            parameterValues.set(parameterIndex - 1, NULL_VALUE);
        }
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        setTimestamp(parameterIndex, x, Calendar.getInstance());
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        String methodName = "setTimestamp(int parameterIndex, Timestamp x, Calendar cal)";
        checkParameterIndex(methodName, parameterIndex, x);
        if (x != null) {
            try {
                if (cal == null) {
                    cal = Calendar.getInstance();
                }
                cal.setTimeInMillis(getSafeTime(x));
                LocalDateTime ldt = LocalDateTime.ofInstant(cal.toInstant(), cal.getTimeZone().toZoneId());
                parameterValues.set(parameterIndex - 1, "'" + ldt.format(TIMESTAMP_FORMATTER) + "'");
            } catch (Exception e) {
                executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_INVALID_PARAMETER);
                Calendar finalCal = cal;
                String errorMessage = buildSetParameterErrorMessage(methodName, parameterIndex, x,
                        "set timestamp parameter failed", parameterMap -> parameterMap.put("calendar", finalCal));
                LOG.error(errorMessage, e);
                throw new SQLException(errorMessage, e);
            }
        } else {
            parameterValues.set(parameterIndex - 1, NULL_VALUE);
        }
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        setAsciiStream(parameterIndex, x, -1);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        setAsciiStream(parameterIndex, x,  (int) length);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        setInputStreamByName("AsciiStream", parameterIndex, x, length);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        setBinaryStream(parameterIndex, x, -1);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        setBinaryStream(parameterIndex, x, (int) length);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        setInputStreamByName("BinaryStream", parameterIndex, x, length);
    }

    @Override
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        setInputStreamByName("UnicodeStream", parameterIndex, x, length);
    }

    private void setInputStreamByName(String streamName, int parameterIndex, InputStream x, int length) throws SQLException {
        String methodName = "set" + streamName + "(int parameterIndex, InputStream x, int length)";
        checkParameterIndex(methodName, parameterIndex, x);
        if (x != null) {
            try {
                byte[] bytes = readBytesFromInputStream(x, length);
                String hex = BytesUtil.toHex(bytes);
                parameterValues.set(parameterIndex - 1, "X'" + hex + "'");
            } catch (Exception e) {
                executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_INVALID_PARAMETER);
                String errorMessage = buildSetParameterErrorMessage(methodName, parameterIndex, x,
                        "set " + streamName + " parameter failed", parameterMap -> parameterMap.put("length", length));
                LOG.error(errorMessage, e);
                throw new SQLException(errorMessage, e);
            }
        } else {
            parameterValues.set(parameterIndex - 1, NULL_VALUE);
        }
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        setCharacterStream(parameterIndex, reader, -1);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        setCharacterStream(parameterIndex, reader, (int) length);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        setReaderByName("CharacterStream", parameterIndex, reader, length);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        setNCharacterStream(parameterIndex, value, -1L);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        setReaderByName("NCharacterStream", parameterIndex, value, (int) length);
    }

    private void setReaderByName(String streamName, int parameterIndex, Reader reader, int length) throws SQLException {
        String methodName = "set" + streamName + "(int parameterIndex, Reader reader, int length)";
        checkParameterIndex(methodName, parameterIndex, reader);
        if (reader != null) {
            try {
                String value = readStringFromReader(reader, length);
                parameterValues.set(parameterIndex - 1, "'" + StringUtil.escape(value) + "'");
            } catch (Exception e) {
                executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_INVALID_PARAMETER);
                String errorMessage = buildSetParameterErrorMessage(methodName, parameterIndex, reader,
                        "set " + streamName + " parameter failed", parameterMap -> parameterMap.put("length", length));
                LOG.error(errorMessage, e);
                throw new SQLException(errorMessage, e);
            }
        } else {
            parameterValues.set(parameterIndex - 1, NULL_VALUE);
        }
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        String methodName = "setObject(int parameterIndex, Object x)";
        checkParameterIndex(methodName, parameterIndex, x);
        if (x != null) {
            if (x instanceof Boolean) {
                setBoolean(parameterIndex, (Boolean) x);
            } else if (x instanceof Byte) {
                setByte(parameterIndex, (Byte) x);
            } else if (x instanceof byte[]) {
                setBytes(parameterIndex, (byte[]) x);
            } else if (x instanceof Short) {
                setShort(parameterIndex, (Short) x);
            } else if (x instanceof Integer) {
                setInt(parameterIndex, (Integer) x);
            } else if (x instanceof Long) {
                setLong(parameterIndex, (Long) x);
            } else if (x instanceof Float) {
                setFloat(parameterIndex, (Float) x);
            } else if (x instanceof Double) {
                setDouble(parameterIndex, (Double) x);
            } else if (x instanceof BigDecimal) {
                setBigDecimal(parameterIndex, (BigDecimal) x);
            } else if (x instanceof String) {
                setString(parameterIndex, (String) x);
            } else if (x instanceof URL) {
                setURL(parameterIndex, (URL) x);
            } else if (x instanceof Date) {
                setDate(parameterIndex, (Date) x);
            } else if (x instanceof Time) {
                setTime(parameterIndex, (Time) x);
            } else if (x instanceof Timestamp) {
                setTimestamp(parameterIndex, (Timestamp) x);
            } else if (x instanceof java.util.Date) {
                Timestamp t = new Timestamp(((java.util.Date) x).getTime());
                setTimestamp(parameterIndex, t);
            } else if (x instanceof InputStream) {
                setBinaryStream(parameterIndex, (InputStream) x);
            } else if (x instanceof Reader) {
                setCharacterStream(parameterIndex, (Reader) x);
            } else {
                executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_INVALID_PARAMETER);
                String errorMessage = buildSetParameterErrorMessage(methodName, parameterIndex, x,
                        "set object parameter failed, unsupported java type", null);
                LOG.error(errorMessage);
                throw new SQLException(errorMessage);
            }
        } else {
            setNull(parameterIndex, Types.NULL);
        }
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        setObject(parameterIndex, x);
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        setObject(parameterIndex, x);
    }

    @Override
    public void setObject(int parameterIndex, Object x, SQLType targetSqlType) throws SQLException {
        setObject(parameterIndex, x);
    }

    @Override
    public void setObject(int parameterIndex, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        setObject(parameterIndex, x);
    }

    @Override
    public void clearParameters() {
        for (int i = 0; i < parameterValues.length(); i++) {
            parameterValues.set(i, null);
        }
    }

    @Override
    public ResultSetMetaData getMetaData() {
        if (resultSet != null) {
            return resultSet.getMetaData();
        } else {
            return null;
        }
    }

    @Override
    public boolean execute() throws SQLException {
        String sql = buildSql();
        return execute(sql);
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        String sql = buildSql();
        return executeQuery(sql);
    }

    @Override
    public int executeUpdate() throws SQLException {
        String sql = buildSql();
        return executeUpdate(sql);
    }

    @Override
    public long executeLargeUpdate() throws SQLException {
        String sql = buildSql();
        return executeLargeUpdate(sql);
    }

    /**
     * 检查设置的参数位置索引是否越界，如果越界，则抛出 {@code SQLException} 异常。
     *
     * @param methodName 调用该检查方法的方法名
     * @param parameterIndex 参数位置索引
     * @param parameterValue 参数值
     * @throws SQLException 如果参数位置索索引越界，则抛出 {@code SQLException} 异常
     */
    private void checkParameterIndex(String methodName, int parameterIndex, Object parameterValue) throws SQLException {
        if (parameterIndex <= 0 || parameterIndex > parameterValues.length()) {
            executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_INVALID_PARAMETER);
            String errorMessage = buildSetParameterErrorMessage(methodName, parameterIndex, parameterValue,
                    "parameter index out of range", null);
            LOG.error(errorMessage);
            throw new SQLException(errorMessage);
        }
    }

    /**
     * 生成设置参数值相关方法执行失败时的通用错误信息。
     *
     * @param methodName 方法名
     * @param parameterIndex 参数位置索引
     * @param parameterValue 参数值
     * @param desc 错误描述信息
     * @param parameterMapConsumer 参数 Map 消费器，允许为 {@code null}
     * @return 置参数值相关方法执行失败时的通用错误信息
     */
    private String buildSetParameterErrorMessage(String methodName, int parameterIndex, Object parameterValue,
                                                 String desc, Consumer<Map<String, Object>> parameterMapConsumer) {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("parameterIndex", parameterIndex);
        parameterMap.put("parameterValue", parameterValue);
        if (parameterMapConsumer != null) {
            parameterMapConsumer.accept(parameterMap);
        }
        parameterMap.put("sqlTemplate", sqlTemplate);
        parameterMap.put("parameterValues", parameterValues);
        parameterMap.put("mysqlChannel", mysqlChannel);
        return LogBuildUtil.buildMethodExecuteFailedLog("TextPreparedStatement#" + methodName,
                desc, parameterMap);
    }

    /**
     * 获取日期对应的毫秒时间戳，该方法不会返回大于 {@link #MYSQL_MAX_TIME} 的值或小于 {@link #MYSQL_MIN_TIME} 的值。
     *
     * @param date 日期
     * @return 允许范围内的时间戳
     */
    private long getSafeTime(java.util.Date date) {
        long time = date.getTime();
        if (time > MYSQL_MAX_TIME) {
            time = MYSQL_MAX_TIME;
        } else if (time < MYSQL_MIN_TIME) {
            time = MYSQL_MIN_TIME;
        }
        return time;
    }

    /**
     * 将 {@code inputStream} 内的字节内容全部读取后，以字节数组形式返回。
     *
     * @param inputStream 输入流
     * @param length 读取的最大字节长度，如果小于 0，则不进行限制
     * @return 字节数组
     * @throws IOException 如果读取过程中出现 IO 错误，将会抛出此异常
     */
    private byte[] readBytesFromInputStream(InputStream inputStream, int length) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
        byte[] buffer = new byte[1024]; // 最大读取 1KB 数据
        int remainCount = length < 0 ? Integer.MAX_VALUE : length;
        int count;
        while ((count = inputStream.read(buffer)) != -1) {
            os.write(buffer, 0, Math.min(count, remainCount));
            remainCount = remainCount - count;
            if (remainCount <= 0) {
                break;
            }
        }
        return os.toByteArray();
    }

    /**
     * 将 {@code reader} 内的字符内容全部读取后，以字符串形式返回。
     *
     * @param reader 输入流
     * @param length 读取的最大字符串长度，如果小于 0，则不进行限制
     * @return 字符串
     * @throws IOException 如果读取过程中出现 IO 错误，将会抛出此异常
     */
    private String readStringFromReader(Reader reader, int length) throws IOException {
        StringBuilder builder = new StringBuilder(1024);
        char[] buffer = new char[1024];
        int remainCount = length < 0 ? Integer.MAX_VALUE : length;
        int count;
        while ((count = reader.read(buffer)) != -1) {
            builder.append(buffer, 0, Math.min(count, remainCount));
            remainCount = remainCount - count;
            if (remainCount <= 0) {
                break;
            }
        }
        return builder.toString();
    }

    @Override
    public void addBatch() throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("TextPreparedStatement#addBatch()",
                "mysql-jdbc does not support batch statements.");
    }

    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("TextPreparedStatement#setRef(int parameterIndex, Ref x)");
    }


    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("TextPreparedStatement#setBlob(int parameterIndex, Blob x)");
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("TextPreparedStatement#setBlob(int parameterIndex, InputStream inputStream)");
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("TextPreparedStatement#setBlob(int parameterIndex, InputStream inputStream, long length)");
    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("TextPreparedStatement#setClob(int parameterIndex, Clob x)");
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("TextPreparedStatement#setClob(int parameterIndex, Reader reader)");
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("TextPreparedStatement#setClob(int parameterIndex, Reader reader, long length)");
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("TextPreparedStatement#setNClob(int parameterIndex, NClob value)");
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("TextPreparedStatement#setNClob(int parameterIndex, Reader reader)");
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("TextPreparedStatement#setNClob(int parameterIndex, Reader reader, long length)");
    }

    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("TextPreparedStatement#setArray(int parameterIndex, Array x)");
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("TextPreparedStatement#setRowId(int parameterIndex, RowId x)");
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("TextPreparedStatement#setSQLXML(int parameterIndex, SQLXML xmlObject)");
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("TextPreparedStatement#getParameterMetaData()");
    }
}
