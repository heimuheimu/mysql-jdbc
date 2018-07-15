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

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.*;

/**
 * 只读 {@link ResultSet} 抽象实现类，所有的变更操作都会直接抛出 {@link SQLFeatureNotSupportedException} 异常，
 * {@link #getConcurrency()} 方法永远返回 {@link ResultSet#CONCUR_READ_ONLY}。
 *
 * <p><strong>说明：</strong>{@code ReadonlyResultSet} 类是线程安全的。</p>
 *
 * @author heimuheimu
 */
public abstract class ReadonlyResultSet implements ResultSet {

    @Override
    public int getConcurrency() throws SQLException {
        return ResultSet.CONCUR_READ_ONLY;
    }

    @Override
    public void updateObject(int columnIndex, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateObject(int columnIndex, Object x, SQLType targetSqlType, int scaleOrLength)");
    }

    @Override
    public void updateObject(String columnLabel, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateObject(String columnLabel, Object x, SQLType targetSqlType, int scaleOrLength)");
    }

    @Override
    public void updateObject(int columnIndex, Object x, SQLType targetSqlType) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateObject(int columnIndex, Object x, SQLType targetSqlType)");
    }

    @Override
    public void updateObject(String columnLabel, Object x, SQLType targetSqlType) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateObject(String columnLabel, Object x, SQLType targetSqlType)");
    }

    @Override
    public boolean rowUpdated() throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#rowUpdated()");
    }

    @Override
    public boolean rowInserted() throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#rowInserted()");
    }

    @Override
    public boolean rowDeleted() throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#rowDeleted()");
    }

    @Override
    public void updateNull(int columnIndex) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateNull(int columnIndex)");
    }

    @Override
    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateBoolean(int columnIndex, boolean x)");
    }

    @Override
    public void updateByte(int columnIndex, byte x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateByte(int columnIndex, byte x)");
    }

    @Override
    public void updateShort(int columnIndex, short x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateShort(int columnIndex, short x)");
    }

    @Override
    public void updateInt(int columnIndex, int x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateInt(int columnIndex, int x)");
    }

    @Override
    public void updateLong(int columnIndex, long x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateLong(int columnIndex, long x)");
    }

    @Override
    public void updateFloat(int columnIndex, float x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateFloat(int columnIndex, float x)");
    }

    @Override
    public void updateDouble(int columnIndex, double x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateDouble(int columnIndex, double x)");
    }

    @Override
    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateBigDecimal(int columnIndex, BigDecimal x)");
    }

    @Override
    public void updateString(int columnIndex, String x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateString(int columnIndex, String x)");
    }

    @Override
    public void updateBytes(int columnIndex, byte[] x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateBytes(int columnIndex, byte[] x)");
    }

    @Override
    public void updateDate(int columnIndex, Date x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateDate(int columnIndex, Date x)");
    }

    @Override
    public void updateTime(int columnIndex, Time x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateTime(int columnIndex, Time x)");
    }

    @Override
    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateTimestamp(int columnIndex, Timestamp x)");
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateAsciiStream(int columnIndex, InputStream x, int length)");
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateBinaryStream(int columnIndex, InputStream x, int length)");
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateCharacterStream(int columnIndex, Reader x, int length)");
    }

    @Override
    public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateObject(int columnIndex, Object x, int scaleOrLength)");
    }

    @Override
    public void updateObject(int columnIndex, Object x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateObject(int columnIndex, Object x)");
    }

    @Override
    public void updateNull(String columnLabel) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateNull(String columnLabel)");
    }

    @Override
    public void updateBoolean(String columnLabel, boolean x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateBoolean(String columnLabel, boolean x)");
    }

    @Override
    public void updateByte(String columnLabel, byte x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateByte(String columnLabel, byte x)");
    }

    @Override
    public void updateShort(String columnLabel, short x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateShort(String columnLabel, short x)");
    }

    @Override
    public void updateInt(String columnLabel, int x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateInt(String columnLabel, int x)");
    }

    @Override
    public void updateLong(String columnLabel, long x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateLong(String columnLabel, long x)");
    }

    @Override
    public void updateFloat(String columnLabel, float x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateFloat(String columnLabel, float x)");
    }

    @Override
    public void updateDouble(String columnLabel, double x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateDouble(String columnLabel, double x)");
    }

    @Override
    public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateBigDecimal(String columnLabel, BigDecimal x)");
    }

    @Override
    public void updateString(String columnLabel, String x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateString(String columnLabel, String x)");
    }

    @Override
    public void updateBytes(String columnLabel, byte[] x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateBytes(String columnLabel, byte[] x)");
    }

    @Override
    public void updateDate(String columnLabel, Date x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateDate(String columnLabel, Date x)");
    }

    @Override
    public void updateTime(String columnLabel, Time x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateTime(String columnLabel, Time x)");
    }

    @Override
    public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateTimestamp(String columnLabel, Timestamp x)");
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateAsciiStream(String columnLabel, InputStream x, int length)");
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateBinaryStream(String columnLabel, InputStream x, int length)");
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateCharacterStream(String columnLabel, Reader reader, int length)");
    }

    @Override
    public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateObject(String columnLabel, Object x, int scaleOrLength)");
    }

    @Override
    public void updateObject(String columnLabel, Object x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateObject(String columnLabel, Object x)");
    }

    @Override
    public void insertRow() throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#insertRow()");
    }

    @Override
    public void updateRow() throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateRow()");
    }

    @Override
    public void deleteRow() throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#deleteRow()");
    }

    @Override
    public void refreshRow() throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#refreshRow()");
    }

    @Override
    public void cancelRowUpdates() throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#cancelRowUpdates()");
    }

    @Override
    public void moveToInsertRow() throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#moveToInsertRow()");
    }

    @Override
    public void moveToCurrentRow() throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#moveToCurrentRow()");
    }

    @Override
    public void updateRef(int columnIndex, Ref x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateRef(int columnIndex, Ref x)");
    }

    @Override
    public void updateRef(String columnLabel, Ref x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateRef(String columnLabel, Ref x)");
    }

    @Override
    public void updateBlob(int columnIndex, Blob x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateBlob(int columnIndex, Blob x)");
    }

    @Override
    public void updateBlob(String columnLabel, Blob x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateBlob(String columnLabel, Blob x)");
    }

    @Override
    public void updateClob(int columnIndex, Clob x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateClob(int columnIndex, Clob x)");
    }

    @Override
    public void updateClob(String columnLabel, Clob x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateClob(String columnLabel, Clob x)");
    }

    @Override
    public void updateArray(int columnIndex, Array x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateArray(int columnIndex, Array x)");
    }

    @Override
    public void updateArray(String columnLabel, Array x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateArray(String columnLabel, Array x)");
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateNCharacterStream(int columnIndex, Reader x, long length)");
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateNCharacterStream(String columnLabel, Reader reader, long length)");
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateAsciiStream(int columnIndex, InputStream x, long length)");
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateBinaryStream(int columnIndex, InputStream x, long length)");
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateCharacterStream(int columnIndex, Reader x, long length)");
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateAsciiStream(String columnLabel, InputStream x, long length)");
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateBinaryStream(String columnLabel, InputStream x, long length)");
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateCharacterStream(String columnLabel, Reader reader, long length)");
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateBlob(int columnIndex, InputStream inputStream, long length)");
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateBlob(String columnLabel, InputStream inputStream, long length)");
    }

    @Override
    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateClob(int columnIndex, Reader reader, long length)");
    }

    @Override
    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateClob(String columnLabel, Reader reader, long length)");
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateNClob(int columnIndex, Reader reader, long length)");
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateNClob(String columnLabel, Reader reader, long length)");
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateNCharacterStream(int columnIndex, Reader x)");
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateNCharacterStream(String columnLabel, Reader reader)");
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateAsciiStream(int columnIndex, InputStream x)");
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateBinaryStream(int columnIndex, InputStream x)");
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateCharacterStream(int columnIndex, Reader x)");
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateAsciiStream(String columnLabel, InputStream x)");
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateBinaryStream(String columnLabel, InputStream x)");
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateCharacterStream(String columnLabel, Reader reader)");
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateBlob(int columnIndex, InputStream inputStream)");
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateBlob(String columnLabel, InputStream inputStream)");
    }

    @Override
    public void updateClob(int columnIndex, Reader reader) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateClob(int columnIndex, Reader reader)");
    }

    @Override
    public void updateClob(String columnLabel, Reader reader) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateClob(String columnLabel, Reader reader)");
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateNClob(int columnIndex, Reader reader)");
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateNClob(String columnLabel, Reader reader)");
    }

    @Override
    public void updateRowId(int columnIndex, RowId x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateRowId(int columnIndex, RowId x)");
    }

    @Override
    public void updateRowId(String columnLabel, RowId x) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateRowId(String columnLabel, RowId x)");
    }

    @Override
    public void updateNString(int columnIndex, String nString) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateNString(int columnIndex, String nString)");
    }

    @Override
    public void updateNString(String columnLabel, String nString) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateNString(String columnLabel, String nString)updateNString(String columnLabel, String nString)");
    }

    @Override
    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateNClob(int columnIndex, NClob nClob)");
    }

    @Override
    public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateNClob(String columnLabel, NClob nClob)");
    }

    @Override
    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateSQLXML(int columnIndex, SQLXML xmlObject)");
    }

    @Override
    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("ReadonlyResultSet#updateSQLXML(String columnLabel, SQLXML xmlObject)");
    }
}
