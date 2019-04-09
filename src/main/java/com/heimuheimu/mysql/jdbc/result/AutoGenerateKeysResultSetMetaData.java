/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 heimuheimu
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

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

/**
 * {@link AutoGenerateKeysResultSet} 使用的 {@link ResultSetMetaData} 信息。
 *
 * <p><strong>说明：</strong>{@code AutoGenerateKeysResultSetMetaData} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class AutoGenerateKeysResultSetMetaData implements ResultSetMetaData {

    @Override
    public int getColumnCount() {
        return 1;
    }

    @Override
    public boolean isAutoIncrement(int column) {
        return false;
    }

    @Override
    public boolean isCaseSensitive(int column) {
        return false;
    }

    @Override
    public boolean isSearchable(int column) {
        return false;
    }

    @Override
    public boolean isCurrency(int column) {
        return false;
    }

    @Override
    public int isNullable(int column) {
        return columnNoNulls;
    }

    @Override
    public boolean isSigned(int column) {
        return false;
    }

    @Override
    public int getColumnDisplaySize(int column) {
        return 19;
    }

    @Override
    public String getColumnLabel(int column) {
        return "GENERATED_KEY";
    }

    @Override
    public String getColumnName(int column) {
        return "GENERATED_KEY";
    }

    @Override
    public int getPrecision(int column) {
        return 19;
    }

    @Override
    public int getScale(int column) {
        return 0;
    }

    @Override
    public int getColumnType(int column) {
        return Types.BIGINT;
    }

    @Override
    public String getColumnTypeName(int column) {
        return "BIGINT";
    }

    @Override
    public boolean isReadOnly(int column) {
        return true;
    }

    @Override
    public boolean isWritable(int column) {
        return false;
    }

    @Override
    public boolean isDefinitelyWritable(int column) {
        return false;
    }

    @Override
    public String getColumnClassName(int column) {
        return Long.class.getName();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T unwrap(Class<T> iface) {
        return (T) this;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return AutoGenerateKeysResultSetMetaData.class == iface;
    }

    @Override
    public String getSchemaName(int column) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSetMetaData#getSchemaName(int column)");
    }

    @Override
    public String getTableName(int column) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSetMetaData#getTableName(int column)");
    }

    @Override
    public String getCatalogName(int column) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("AutoGenerateKeysResultSetMetaData#getCatalogName(int column)");
    }
}
