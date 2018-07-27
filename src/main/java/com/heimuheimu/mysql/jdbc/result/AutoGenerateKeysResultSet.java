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

import com.heimuheimu.mysql.jdbc.facility.SQLFeatureNotSupportedExceptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Mysql 执行 Insert 语句后返回的自增主键只读 {@link ResultSet} 实现类，结果集最多包含一个自增主键值。
 *
 * <p><strong>说明：</strong>{@code AutoGenerateKeysResultSet} 类是非线程安全的，不允许多个线程使用同一个实例。</p>
 *
 * @author heimuheimu
 * @see Statement#getGeneratedKeys()
 */
public class AutoGenerateKeysResultSet extends ReadonlyScrollResultSet {

    private static final Logger LOG = LoggerFactory.getLogger(AutoGenerateKeysResultSet.class);

    private final List<Long> autoGenerateKeyList;

    private final Statement statement;

    /**
     * 构造一个自增主键查询结果 {@link ResultSet} 实现类。
     *
     * @param autoGenerateKey 自增主键，如果为 -1，则不存在自增主键
     * @param statement SQL 语句执行器
     */
    public AutoGenerateKeysResultSet(long autoGenerateKey, Statement statement) {
        autoGenerateKeyList = new ArrayList<>();
        if (autoGenerateKey != -1) {
            autoGenerateKeyList.add(autoGenerateKey);
        }
        this.statement = statement;
    }

    @Override
    public int getRowsSize() {
        return autoGenerateKeyList.size();
    }

    @Override
    public void close() throws SQLException {
        // no resource need to be released
    }

    private long getAutoGenerateKey() throws SQLException {
        int currentRow = getRow();
        if (currentRow >= 1 && currentRow <= getRowsSize()) {
            return autoGenerateKeyList.get(currentRow - 1);
        } else {
            String errorMessage = "Get auto generate key failed: `invalid row`. Current row: `" + currentRow
                    + "`. Rows size: `" + getRowsSize() + "`.";
            LOG.error(errorMessage);
            throw new SQLException(errorMessage);
        }
    }

    @Override
    public boolean wasNull() throws SQLException {
        return false;
    }

    @Override
    public String getString(int columnIndex) throws SQLException {
        return String.valueOf(getAutoGenerateKey());
    }

    @Override
    public String getString(String columnLabel) throws SQLException {
        return String.valueOf(getAutoGenerateKey());
    }

    @Override
    public short getShort(int columnIndex) throws SQLException {
        return (short) getAutoGenerateKey();
    }

    @Override
    public short getShort(String columnLabel) throws SQLException {
        return (short) getAutoGenerateKey();
    }

    @Override
    public int getInt(int columnIndex) throws SQLException {
        return (int) getAutoGenerateKey();
    }

    @Override
    public int getInt(String columnLabel) throws SQLException {
        return (int) getAutoGenerateKey();
    }

    @Override
    public long getLong(int columnIndex) throws SQLException {
        return getAutoGenerateKey();
    }

    @Override
    public long getLong(String columnLabel) throws SQLException {
        return getAutoGenerateKey();
    }

    @Override
    public float getFloat(int columnIndex) throws SQLException {
        return getAutoGenerateKey();
    }

    @Override
    public float getFloat(String columnLabel) throws SQLException {
        return getAutoGenerateKey();
    }

    @Override
    public double getDouble(int columnIndex) throws SQLException {
        return getAutoGenerateKey();
    }

    @Override
    public double getDouble(String columnLabel) throws SQLException {
        return getAutoGenerateKey();
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        return new BigDecimal(getAutoGenerateKey());
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
        return new BigDecimal(getAutoGenerateKey());
    }

    @Override
    public Object getObject(int columnIndex) throws SQLException {
        return getAutoGenerateKey();
    }

    @Override
    public Object getObject(String columnLabel) throws SQLException {
        return getAutoGenerateKey();
    }

    @Override
    public int findColumn(String columnLabel) throws SQLException {
        return 1; // always 1
    }

    @Override
    public Statement getStatement() throws SQLException {
        return statement;
    }

    @Override
    public boolean getBoolean(int columnIndex) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getBoolean(int columnIndex)");
    }

    @Override
    public boolean getBoolean(String columnLabel) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getBoolean(String columnLabel)");
    }

    @Override
    public byte getByte(int columnIndex) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getByte(int columnIndex)");
    }

    @Override
    public byte getByte(String columnLabel) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getByte(String columnLabel)");
    }

    @Override
    public byte[] getBytes(int columnIndex) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getBytes(int columnIndex)");
    }

    @Override
    public Date getDate(int columnIndex) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getDate(int columnIndex)");
    }

    @Override
    public Time getTime(int columnIndex) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getTime(int columnIndex)");
    }

    @Override
    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getTimestamp(int columnIndex)");
    }

    @Override
    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getAsciiStream(int columnIndex)");
    }

    @Override
    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getUnicodeStream(int columnIndex)");
    }

    @Override
    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getBinaryStream(int columnIndex)");
    }

    @Override
    public byte[] getBytes(String columnLabel) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getBytes(String columnLabel)");
    }

    @Override
    public Date getDate(String columnLabel) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getDate(String columnLabel)");
    }

    @Override
    public Time getTime(String columnLabel) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getTime(String columnLabel)");
    }

    @Override
    public Timestamp getTimestamp(String columnLabel) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getTimestamp(String columnLabel)");
    }

    @Override
    public InputStream getAsciiStream(String columnLabel) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getAsciiStream(String columnLabel)");
    }

    @Override
    public InputStream getUnicodeStream(String columnLabel) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getUnicodeStream(String columnLabel)");
    }

    @Override
    public InputStream getBinaryStream(String columnLabel) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getBinaryStream(String columnLabel)");
    }

    @Override
    public String getCursorName() throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getCursorName()");
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getMetaData()");
    }

    @Override
    public Reader getCharacterStream(int columnIndex) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getCharacterStream(int columnIndex)");
    }

    @Override
    public Reader getCharacterStream(String columnLabel) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getCharacterStream(String columnLabel)");
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getBigDecimal(int columnIndex)");
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getBigDecimal(String columnLabel)");
    }

    @Override
    public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getObject(int columnIndex, Map<String, Class<?>> map)");
    }

    @Override
    public Ref getRef(int columnIndex) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getRef(int columnIndex)");
    }

    @Override
    public Blob getBlob(int columnIndex) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getBlob(int columnIndex)");
    }

    @Override
    public Clob getClob(int columnIndex) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getClob(int columnIndex)");
    }

    @Override
    public Array getArray(int columnIndex) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getArray(int columnIndex)");
    }

    @Override
    public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getObject(String columnLabel, Map<String, Class<?>> map)");
    }

    @Override
    public Ref getRef(String columnLabel) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getRef(String columnLabel)");
    }

    @Override
    public Blob getBlob(String columnLabel) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getBlob(String columnLabel)");
    }

    @Override
    public Clob getClob(String columnLabel) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getClob(String columnLabel)");
    }

    @Override
    public Array getArray(String columnLabel) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getArray(String columnLabel)");
    }

    @Override
    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getDate(int columnIndex, Calendar cal)");
    }

    @Override
    public Date getDate(String columnLabel, Calendar cal) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getDate(String columnLabel, Calendar cal)");
    }

    @Override
    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getTime(int columnIndex, Calendar cal)");
    }

    @Override
    public Time getTime(String columnLabel, Calendar cal) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getTime(String columnLabel, Calendar cal)");
    }

    @Override
    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getTimestamp(int columnIndex, Calendar cal)");
    }

    @Override
    public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getTimestamp(String columnLabel, Calendar cal)");
    }

    @Override
    public URL getURL(int columnIndex) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getURL(int columnIndex)");
    }

    @Override
    public URL getURL(String columnLabel) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getURL(String columnLabel)");
    }

    @Override
    public RowId getRowId(int columnIndex) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getRowId(int columnIndex)");
    }

    @Override
    public RowId getRowId(String columnLabel) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getRowId(String columnLabel)");
    }

    @Override
    public int getHoldability() throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getHoldability()");
    }

    @Override
    public NClob getNClob(int columnIndex) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getNClob(int columnIndex)");
    }

    @Override
    public NClob getNClob(String columnLabel) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getNClob(String columnLabel)");
    }

    @Override
    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getSQLXML(int columnIndex)");
    }

    @Override
    public SQLXML getSQLXML(String columnLabel) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getSQLXML(String columnLabel)");
    }

    @Override
    public String getNString(int columnIndex) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getNString(int columnIndex)");
    }

    @Override
    public String getNString(String columnLabel) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getNString(String columnLabel)");
    }

    @Override
    public Reader getNCharacterStream(int columnIndex) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getNCharacterStream(int columnIndex)");
    }

    @Override
    public Reader getNCharacterStream(String columnLabel) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getNCharacterStream(String columnLabel)");
    }

    @Override
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getObject(int columnIndex, Class<T> type)");
    }

    @Override
    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSet#getObject(String columnLabel, Class<T> type)");
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {

    }

    @Override
    public boolean isClosed() throws SQLException {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return (T) this;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return AutoGenerateKeysResultSet.class == iface;
    }
}
