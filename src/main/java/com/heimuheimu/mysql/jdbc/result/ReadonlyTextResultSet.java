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

package com.heimuheimu.mysql.jdbc.result;

import com.heimuheimu.mysql.jdbc.ConnectionInfo;
import com.heimuheimu.mysql.jdbc.facility.SQLFeatureNotSupportedExceptionBuilder;
import com.heimuheimu.mysql.jdbc.monitor.ExecutionMonitorFactory;
import com.heimuheimu.mysql.jdbc.packet.ColumnTypeMappingUtil;
import com.heimuheimu.mysql.jdbc.packet.MysqlPacket;
import com.heimuheimu.mysql.jdbc.packet.command.text.ColumnDefinition41ResponsePacket;
import com.heimuheimu.mysql.jdbc.packet.command.text.TextResultsetResponsePacket;
import com.heimuheimu.mysql.jdbc.packet.command.text.TextResultsetRowResponsePacket;
import com.heimuheimu.mysql.jdbc.packet.generic.EOFPacket;
import com.heimuheimu.naivemonitor.monitor.ExecutionMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.function.Function;

/**
 * 执行 {@link com.heimuheimu.mysql.jdbc.command.SQLCommand} 查询命令返回的只读 {@link ResultSet} 实现类，{@code ReadonlyTextResultSet}
 * 不依赖任何 IO 资源，无需调用 {@link #close()} 进行资源释放，{@link #isClosed()} 方法将永远返回 {@code false}。
 *
 * <p><strong>说明：</strong>{@code ReadonlyTextResultSet} 类是非线程安全的，不允许多个线程使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class ReadonlyTextResultSet extends ReadonlyScrollResultSet {
    
    private static final Logger LOG = LoggerFactory.getLogger(ReadonlyTextResultSet.class);

    /**
     * "TextResultset" 数据包信息
     */
    private final TextResultsetResponsePacket textResultsetResponsePacket;

    /**
     * "ColumnDefinition41" 数据包列表
     */
    private final List<ColumnDefinition41ResponsePacket> columnDefinition41ResponsePacketList;

    /**
     * 列索引 Map，Key 为列名称，Value 为该列索引位置
     */
    private final Map<String, Integer> columnIndexMap;

    /**
     * "TextResultsetRow" 数据包列表
     */
    private final List<TextResultsetRowResponsePacket> textResultsetRowResponsePacketList;

    /**
     * 接收响应数据的 Mysql 数据库连接信息
     */
    private final ConnectionInfo connectionInfo;

    /**
     * SQL 语句执行器
     */
    private final Statement statement;

    /**
     * Mysql 命令执行信息监控器
     */
    protected final ExecutionMonitor executionMonitor;

    /**
     * 查询结果总行数
     */
    private final int rowsSize;

    /**
     * 最后被读取列的值是否为 SQL NULL
     */
    private boolean wasNull = false;

    /**
     * 构造一个 Mysql 数据库查询结果 {@link ResultSet} 实现类。
     *
     * @param mysqlPackets SQL 查询语句返回的响应数据包列表
     * @param connectionInfo 接收响应数据的 Mysql 数据库连接信息
     * @param statement SQL 语句执行器
     * @throws IllegalArgumentException 如果解析响应数据包时出现错误，将会抛出此异常
     */
    public ReadonlyTextResultSet(List<MysqlPacket> mysqlPackets, ConnectionInfo connectionInfo,
                                 Statement statement, ExecutionMonitor executionMonitor) throws IllegalArgumentException {
        this.connectionInfo = connectionInfo;
        this.statement = statement;
        this.executionMonitor = executionMonitor;
        int i = 0;
        this.textResultsetResponsePacket = TextResultsetResponsePacket.parse(mysqlPackets.get(i++),
                connectionInfo.getCapabilitiesFlags());
        // 列定义解析
        columnIndexMap = new HashMap<>();
        columnDefinition41ResponsePacketList = new ArrayList<>();
        if (textResultsetResponsePacket.isMetadataFollows()) {
            for (int j = 0; j < textResultsetResponsePacket.getColumnCount(); j++) {
                ColumnDefinition41ResponsePacket columnDefinition41 = ColumnDefinition41ResponsePacket
                        .parse(mysqlPackets.get(i++), connectionInfo.getJavaCharset());
                columnIndexMap.put(columnDefinition41.getColumnName(), j + 1);
                columnDefinition41ResponsePacketList.add(columnDefinition41);
            }
        }
        // 行数据解析
        i++; // 跳过第一个 EOF 包
        textResultsetRowResponsePacketList = new ArrayList<>();
        MysqlPacket rowPacket = mysqlPackets.get(i++);
        while (!EOFPacket.isEOFPacket(rowPacket)) {
            textResultsetRowResponsePacketList.add(TextResultsetRowResponsePacket.parse(rowPacket));
            rowPacket = mysqlPackets.get(i++);
        }
        rowsSize = textResultsetRowResponsePacketList.size();
    }

    @Override
    public int getRowsSize() {
        return rowsSize;
    }

    @Override
    public void close() throws SQLException {
        // no resource need to be released
    }

    @Override
    public boolean isClosed() throws SQLException {
        return false;
    }

    @Override
    public boolean wasNull() throws SQLException {
        return wasNull;
    }

    @Override
    public int findColumn(String columnLabel) throws SQLException {
        Integer columnIndex = columnIndexMap.get(columnLabel);
        if (columnIndex != null) {
            return columnIndex;
        } else {
            executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_RESULTSET_ERROR);
            String errorMessage = "Get column value failed: `column is not exist in query result`. Column name: `" +
                    columnLabel + "`. Current row: " + getRow() + "`. Connection info: `" + connectionInfo + "`.";
            LOG.error(errorMessage);
            throw new SQLException(errorMessage);
        }
    }

    private <R> R getColumnValue(int columnIndex, Function<byte[], R> converter) throws SQLException {
        int currentRow = getRow();
        if (currentRow >= 1 && currentRow <= rowsSize) {
            TextResultsetRowResponsePacket rowResponsePacket = textResultsetRowResponsePacketList.get(currentRow - 1);
            List<byte[]> columnValues = rowResponsePacket.getColumnsValues();
            if (columnIndex >= 0 && columnIndex < columnValues.size()) {
                byte[] columnValue = columnValues.get(columnIndex);
                wasNull = (columnValue == null);
                try {
                    return converter.apply(columnValue);
                } catch (Exception e) {
                    ColumnDefinition41ResponsePacket columnDefinition41ResponsePacket = null;
                    if (columnIndex < columnDefinition41ResponsePacketList.size()) {
                        columnDefinition41ResponsePacket = columnDefinition41ResponsePacketList.get(columnIndex);
                    }
                    String errorMessage = "Get column value failed: `convert value error`. Column index: `" +
                            columnIndex + "`. Current row: `" + currentRow + "`. Column value: `" + Arrays.toString(columnValue)
                            + "`. Column definition: `" + columnDefinition41ResponsePacket + "`. Connection info: `" + connectionInfo + "`.";
                    LOG.error(errorMessage, e);
                    throw new SQLException(errorMessage, e);
                }
            } else {
                String errorMessage = "Get column value failed: `columnIndex out of range`. Invalid column index: `" +
                        columnIndex + "`. Columns size: `" + columnValues.size() + "`. Current row: `" + currentRow
                        + "`. Rows size: `" + rowsSize + "`. Connection info: `" + connectionInfo + "`.";
                LOG.error(errorMessage);
                throw new SQLException(errorMessage);
            }
        } else {
            String errorMessage = "Get column value failed: `invalid row`. Current row: `" + currentRow
                    + "`. Rows size: `" + rowsSize + "`. Connection info: `" + connectionInfo + "`.";
            LOG.error(errorMessage);
            throw new SQLException(errorMessage);
        }
    }

    @Override
    public String getString(int columnIndex) throws SQLException {
        return getColumnValue(columnIndex, bytesValue -> {
            if (bytesValue != null) {
                return new String(bytesValue, connectionInfo.getJavaCharset());
            } else {
                return null;
            }
        });
    }

    @Override
    public String getString(String columnLabel) throws SQLException {
        return getString(findColumn(columnLabel));
    }

    @Override
    public String getNString(int columnIndex) throws SQLException {
        return getString(columnIndex);
    }

    @Override
    public String getNString(String columnLabel) throws SQLException {
        return getString(columnLabel);
    }

    @Override
    public boolean getBoolean(int columnIndex) throws SQLException {
        String value = getString(columnIndex);
        if (value != null) {
            if (value.contains("0")) {
                return false;
            } else if (value.contains("1")) {
                return true;
            } else {
                String errorMessage = "Get column boolean value failed: `invalid value, value must contain a '0'(false) or contain a '1'(true)`. Column index: `"
                        + columnIndex + "`. Current row: `" + getRow() + "`. Invalid column value: `" + value + "`. Connection info: `"
                        + connectionInfo + "`.";
                LOG.error(errorMessage);
                throw new SQLException(errorMessage);
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean getBoolean(String columnLabel) throws SQLException {
        return getBoolean(findColumn(columnLabel));
    }

    @Override
    public byte getByte(int columnIndex) throws SQLException {
        return getColumnValue(columnIndex, bytesValue -> {
            if (bytesValue != null && bytesValue.length > 0) {
                return bytesValue[0];
            } else {
                return (byte) 0;
            }
        });
    }

    @Override
    public byte getByte(String columnLabel) throws SQLException {
        return getByte(findColumn(columnLabel));
    }

    private <T extends Number> T parseNumber(int columnIndex, Function<String, T> numberParser, T nullValue) throws SQLException {
        String value = getString(columnIndex);
        if (value != null) {
            try {
                return numberParser.apply(value);
            } catch (Exception e) {
                String errorMessage = "Get column number value failed: `parse number error`. Column index: `" + columnIndex
                        + "`. Current row: `" + getRow() + "`. Invalid column value: `" + value + "`. Connection info: `"
                        + connectionInfo + "`.";
                LOG.error(errorMessage, e);
                throw new SQLException(errorMessage, e);
            }
        } else {
            return nullValue;
        }
    }

    @Override
    public short getShort(int columnIndex) throws SQLException {
        return parseNumber(columnIndex, Short::parseShort, (short) 0);
    }

    @Override
    public short getShort(String columnLabel) throws SQLException {
        return getShort(findColumn(columnLabel));
    }

    @Override
    public int getInt(int columnIndex) throws SQLException {
        return parseNumber(columnIndex, Integer::parseInt, 0);
    }

    @Override
    public int getInt(String columnLabel) throws SQLException {
        return getInt(findColumn(columnLabel));
    }

    @Override
    public long getLong(int columnIndex) throws SQLException {
        return parseNumber(columnIndex, Long::parseLong, 0L);
    }

    @Override
    public long getLong(String columnLabel) throws SQLException {
        return getLong(findColumn(columnLabel));
    }

    @Override
    public float getFloat(int columnIndex) throws SQLException {
        return parseNumber(columnIndex, Float::parseFloat, 0F);
    }

    @Override
    public float getFloat(String columnLabel) throws SQLException {
        return getFloat(findColumn(columnLabel));
    }

    @Override
    public double getDouble(int columnIndex) throws SQLException {
        return parseNumber(columnIndex, Double::parseDouble, 0D);
    }

    @Override
    public double getDouble(String columnLabel) throws SQLException {
        return getDouble(findColumn(columnLabel));
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        return parseNumber(columnIndex, BigDecimal::new, null);
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
        return getBigDecimal(findColumn(columnLabel));
    }

    @Override
    @Deprecated
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        return parseNumber(columnIndex, value -> new BigDecimal(value).setScale(scale, BigDecimal.ROUND_FLOOR), null);
    }

    @Override
    @Deprecated
    public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
        return getBigDecimal(findColumn(columnLabel), scale);
    }

    @Override
    public byte[] getBytes(int columnIndex) throws SQLException {
        return getColumnValue(columnIndex, bytesValue -> bytesValue);
    }

    @Override
    public byte[] getBytes(String columnLabel) throws SQLException {
        return getBytes(findColumn(columnLabel));
    }

    @Override
    public Date getDate(int columnIndex) throws SQLException {
        return getDate(columnIndex, Calendar.getInstance());
    }

    @Override
    public Date getDate(String columnLabel) throws SQLException {
        return getDate(findColumn(columnLabel));
    }

    @Override
    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        String value = getString(columnIndex);
        if (value != null) {
            try {
                if (cal == null) {
                    cal = Calendar.getInstance();
                }
                int year = Integer.parseInt(value.substring(0, 4));
                int month = 1;
                int day = 1;
                if (value.length() > 5) {
                    month = Integer.parseInt(value.substring(5, 7));
                    day = Integer.parseInt(value.substring(8, 10));
                }
                cal.set(year, month - 1, day);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                return new Date(cal.getTimeInMillis());
            } catch (Exception e) {
                String errorMessage = "Get column date value failed: `parse date error`. Column index: `" + columnIndex
                        + "`. Current row: `" + getRow() + "`. Invalid column value: `" + value + "`. Connection info: `"
                        + connectionInfo + "`.";
                LOG.error(errorMessage, e);
                throw new SQLException(errorMessage, e);
            }
        } else {
            return null;
        }
    }

    @Override
    public Date getDate(String columnLabel, Calendar cal) throws SQLException {
        return getDate(findColumn(columnLabel), cal);
    }

    @Override
    public Time getTime(int columnIndex) throws SQLException {
        return getTime(columnIndex, Calendar.getInstance());
    }

    @Override
    public Time getTime(String columnLabel) throws SQLException {
        return getTime(findColumn(columnLabel));
    }

    @Override
    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        String value = getString(columnIndex);
        if (value != null) {
            try {
                if (cal == null) {
                    cal = Calendar.getInstance();
                }
                String[] timeParts = value.split(":");
                int hour = Integer.parseInt(timeParts[0]);
                int minute = Integer.parseInt(timeParts[1]);
                String[] secondsParts = timeParts[2].split("\\.");
                int second = Integer.parseInt(secondsParts[0]);
                int millisecond = 0;
                if (secondsParts.length > 1) {
                    if (secondsParts[1].length() > 3) {
                        secondsParts[1] = secondsParts[1].substring(0, 3);
                    }
                    millisecond = Integer.parseInt(secondsParts[1]);
                }
                cal.set(1970, 0, 1);
                cal.set(Calendar.HOUR_OF_DAY, hour);
                cal.set(Calendar.MINUTE, minute);
                cal.set(Calendar.SECOND, second);
                cal.set(Calendar.MILLISECOND, millisecond);
                return new Time(cal.getTimeInMillis());
            } catch (Exception e) {
                String errorMessage = "Get column time value failed: `parse time error`. Column index: `" + columnIndex
                        + "`. Current row: `" + getRow() + "`. Invalid column value: `" + value + "`. Connection info: `"
                        + connectionInfo + "`.";
                LOG.error(errorMessage, e);
                throw new SQLException(errorMessage, e);
            }
        } else {
            return null;
        }
    }

    @Override
    public Time getTime(String columnLabel, Calendar cal) throws SQLException {
        return getTime(findColumn(columnLabel), cal);
    }

    @Override
    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        return getTimestamp(columnIndex, Calendar.getInstance());
    }

    @Override
    public Timestamp getTimestamp(String columnLabel) throws SQLException {
        return getTimestamp(findColumn(columnLabel));
    }

    @Override
    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        String value = getString(columnIndex);
        if (value != null) {
            try {
                if (cal == null) {
                    cal = Calendar.getInstance();
                }
                int year = Integer.parseInt(value.substring(0, 4));
                int month = 1;
                int day = 1;
                if (value.length() > 5) {
                    month = Integer.parseInt(value.substring(5, 7));
                    day = Integer.parseInt(value.substring(8, 10));
                }
                int hour = 0;
                int minute = 0;
                int second = 0;
                int millisecond = 0;
                if (value.length() > 10) {
                    hour = Integer.parseInt(value.substring(11, 13));
                    minute = Integer.parseInt(value.substring(14, 16));
                    second = Integer.parseInt(value.substring(17, 19));
                }
                if (value.length() > 19) {
                    millisecond = Integer.parseInt(value.substring(20, 23));
                }
                cal.set(year, month - 1, day);
                cal.set(Calendar.HOUR_OF_DAY, hour);
                cal.set(Calendar.MINUTE, minute);
                cal.set(Calendar.SECOND, second);
                cal.set(Calendar.MILLISECOND, millisecond);
                return new Timestamp(cal.getTimeInMillis());
            } catch (Exception e) {
                String errorMessage = "Get column timestamp value failed: `parse timestamp error`. Column index: `" + columnIndex
                        + "`. Current row: `" + getRow() + "`. Invalid column value: `" + value + "`. Connection info: `"
                        + connectionInfo + "`.";
                LOG.error(errorMessage, e);
                throw new SQLException(errorMessage, e);
            }
        } else {
            return null;
        }
    }

    @Override
    public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
        return getTimestamp(findColumn(columnLabel), cal);
    }

    @Override
    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        return getBinaryStream(columnIndex);
    }

    @Override
    public InputStream getAsciiStream(String columnLabel) throws SQLException {
        return getAsciiStream(findColumn(columnLabel));
    }

    @Override
    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        return getBinaryStream(columnIndex);
    }

    @Override
    public InputStream getUnicodeStream(String columnLabel) throws SQLException {
        return getUnicodeStream(findColumn(columnLabel));
    }

    @Override
    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        return getColumnValue(columnIndex, bytesValue -> {
            if (bytesValue != null) {
                return new ByteArrayInputStream(bytesValue);
            } else {
                return null;
            }
        });
    }

    @Override
    public InputStream getBinaryStream(String columnLabel) throws SQLException {
        return getBinaryStream(findColumn(columnLabel));
    }

    @Override
    public Reader getCharacterStream(int columnIndex) throws SQLException {
        String value = getString(columnIndex);
        if (value != null) {
            return new StringReader(value);
        } else {
            return null;
        }
    }

    @Override
    public Reader getCharacterStream(String columnLabel) throws SQLException {
        return getCharacterStream(findColumn(columnLabel));
    }

    @Override
    public Reader getNCharacterStream(int columnIndex) throws SQLException {
        return getCharacterStream(columnIndex);
    }

    @Override
    public Reader getNCharacterStream(String columnLabel) throws SQLException {
        return getNCharacterStream(findColumn(columnLabel));
    }

    @Override
    public URL getURL(int columnIndex) throws SQLException {
        String value = getString(columnIndex);
        if (value != null) {
            try {
                return new URL(value);
            } catch (Exception e) {
                String errorMessage = "Get column URL value failed: `parse URL error`. Column index: `" + columnIndex
                        + "`. Current row: `" + getRow() + "`. Invalid column value: `" + value + "`. Connection info: `"
                        + connectionInfo + "`.";
                LOG.error(errorMessage, e);
                throw new SQLException(errorMessage, e);
            }
        } else {
            return null;
        }
    }

    @Override
    public URL getURL(String columnLabel) throws SQLException {
        return getURL(findColumn(columnLabel));
    }

    @Override
    public Object getObject(int columnIndex) throws SQLException {
        if (columnIndex >= 0 && columnIndex < columnDefinition41ResponsePacketList.size()) {
            ColumnDefinition41ResponsePacket columnDefinition41ResponsePacket = columnDefinition41ResponsePacketList.get(columnIndex);
            Class<?> javaType = ColumnTypeMappingUtil.getJavaType(columnDefinition41ResponsePacket.getColumnType(),
                    columnDefinition41ResponsePacket.getColumnDefinitionFlags());
            return getObject(columnIndex, javaType);
        } else {
            String errorMessage = "Get column object value failed: `columnIndex out of range`. Invalid column index: `" +
                    columnIndex + "`. Columns size: `" + columnDefinition41ResponsePacketList.size() + "`. Current row: `" +
                    getRow() + "`. Rows size: `" + rowsSize + "`. Connection info: `" + connectionInfo + "`.";
            LOG.error(errorMessage);
            throw new SQLException(errorMessage);
        }
    }

    @Override
    public Object getObject(String columnLabel) throws SQLException {
        return getObject(findColumn(columnLabel));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        if (type == null) {
            String errorMessage = "Get column object value failed: `type could not be null`. Column index: `" + columnIndex
                    + "`. type: `null`. Current row: `" + getRow() + "`. Connection info: `" + connectionInfo + "`.";
            LOG.error(errorMessage);
            throw new SQLException(errorMessage);
        }
        T columnValue;
        if (String.class == type) {
            columnValue = (T) getString(columnIndex);
        } else if (Integer.class == type || int.class == type) {
            columnValue = (T) (Integer) getInt(columnIndex);
        } else if (Long.class == type || long.class == type) {
            columnValue = (T) (Long) getLong(columnIndex);
        } else if (Boolean.class == type || boolean.class == type) {
            columnValue = (T) (Boolean) getBoolean(columnIndex);
        } else if (Double.class == type || double.class == type) {
            columnValue = (T) (Double) getDouble(columnIndex);
        } else if (Float.class == type || float.class == type) {
            columnValue = (T) (Float) getFloat(columnIndex);
        } else if (Short.class == type || short.class == type) {
            columnValue = (T) (Short) getShort(columnIndex);
        } else if (BigDecimal.class == type) {
            columnValue = (T) getBigDecimal(columnIndex);
        } else if (Date.class == type) {
            columnValue = (T) getDate(columnIndex);
        } else if (Time.class == type) {
            columnValue = (T) getTime(columnIndex);
        } else if (Timestamp.class == type || java.util.Date.class == type) {
            columnValue = (T) getTimestamp(columnIndex);
        } else if (byte[].class == type) {
            columnValue = (T) getBytes(columnIndex);
        } else if (URL.class == type) {
            columnValue = (T) getURL(columnIndex);
        } else if (Byte.class == type || byte.class == type) {
            columnValue = (T) (Byte) getByte(columnIndex);
        } else {
            String errorMessage = "Get column object value failed: `unsupported java type`. Column index: `" + columnIndex
                    + "`. type: `" + type + "`. Current row: `" + getRow() + "`. Connection info: `" + connectionInfo + "`.";
            LOG.error(errorMessage);
            throw new SQLException(errorMessage);
        }
        if (!wasNull()) {
            return columnValue;
        } else {
            return null;
        }
    }

    @Override
    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        return getObject(findColumn(columnLabel), type);
    }

    @Override
    public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
        if (columnIndex >= 0 && columnIndex < columnDefinition41ResponsePacketList.size()) {
            ColumnDefinition41ResponsePacket definition4Packet = columnDefinition41ResponsePacketList.get(columnIndex);
            String columnTypeName = ColumnTypeMappingUtil.getTypeName(definition4Packet.getColumnType(),
                    definition4Packet.getColumnDefinitionFlags(), definition4Packet.getMaximumColumnLength());
            Class<?> javaType = map.get(columnTypeName);
            if (javaType == null) {
                javaType = ColumnTypeMappingUtil.getJavaType(definition4Packet.getColumnType(),
                        definition4Packet.getColumnDefinitionFlags());
            }
            return getObject(columnIndex, javaType);
        } else {
            String errorMessage = "Get column object value failed: `columnIndex out of range`. Invalid column index: `" +
                    columnIndex + "`. Columns size: `" + columnDefinition41ResponsePacketList.size() + "`. Current row: `" +
                    getRow() + "`. Rows size: `" + rowsSize + "`. javaClassMap: `" + map + "`. Connection info: `" +
                    connectionInfo + "`.";
            LOG.error(errorMessage);
            throw new SQLException(errorMessage);
        }
    }

    @Override
    public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
        return getObject(findColumn(columnLabel), map);
    }

    @Override
    public Statement getStatement() throws SQLException {
        return statement;
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return new TextResultSetMetaData(columnDefinition41ResponsePacketList);
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {
        // do nothing
    }

    @Override
    public String getCursorName() throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyTextResultSet#getCursorName()");
    }

    @Override
    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyTextResultSet#getSQLXML(int columnIndex)");
    }

    @Override
    public SQLXML getSQLXML(String columnLabel) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyTextResultSet#getSQLXML(String columnLabel)");
    }

    @Override
    public RowId getRowId(int columnIndex) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyTextResultSet#getRowId(int columnIndex)");
    }

    @Override
    public RowId getRowId(String columnLabel) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyTextResultSet#getRowId(String columnLabel)");
    }

    @Override
    public Ref getRef(int columnIndex) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyTextResultSet#getRef(int columnIndex)");
    }

    @Override
    public Ref getRef(String columnLabel) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyTextResultSet#getRef(String columnLabel)");
    }

    @Override
    public Blob getBlob(int columnIndex) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyTextResultSet#getBlob(int columnIndex)");
    }

    @Override
    public Blob getBlob(String columnLabel) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyTextResultSet#getBlob(String columnLabel)");
    }

    @Override
    public Clob getClob(int columnIndex) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyTextResultSet#getClob(int columnIndex)");
    }

    @Override
    public Clob getClob(String columnLabel) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyTextResultSet#getClob(String columnLabel)");
    }

    @Override
    public NClob getNClob(int columnIndex) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyTextResultSet#getNClob(int columnIndex)");
    }

    @Override
    public NClob getNClob(String columnLabel) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyTextResultSet#getNClob(String columnLabel)");
    }

    @Override
    public Array getArray(int columnIndex) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyTextResultSet#getArray(int columnIndex)");
    }

    @Override
    public Array getArray(String columnLabel) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyTextResultSet#getArray(String columnLabel)");
    }

    @Override
    public int getHoldability() throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyTextResultSet#getHoldability()");
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return (T) this;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return ReadonlyTextResultSet.class == iface;
    }
}
