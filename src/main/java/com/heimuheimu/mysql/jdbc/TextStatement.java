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

import com.heimuheimu.mysql.jdbc.facility.SQLFeatureNotSupportedExceptionBuilder;

import java.sql.*;

/**
 *
 */
public class TextStatement implements Statement {

    @Override
    public long getLargeMaxRows() throws SQLException {
        return 0; // no limit
    }

    @Override
    public int getMaxRows() throws SQLException {
        return 0; // no limit
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        return 0; // no limit
    }

    @Override
    public long getLargeUpdateCount() throws SQLException {
        return 0;
    }

    @Override
    public long executeLargeUpdate(String sql) throws SQLException {
        return 0;
    }

    @Override
    public long executeLargeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return 0;
    }

    @Override
    public long executeLargeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return 0;
    }

    @Override
    public long executeLargeUpdate(String sql, String[] columnNames) throws SQLException {
        return 0;
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        return null;
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        return 0;
    }

    @Override
    public void close() throws SQLException {

    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {

    }

    @Override
    public int getQueryTimeout() throws SQLException {
        return 0;
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {

    }

    @Override
    public void cancel() throws SQLException {

    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {

    }

    @Override
    public void setCursorName(String name) throws SQLException {

    }

    @Override
    public boolean execute(String sql) throws SQLException {
        return false;
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        return null;
    }

    @Override
    public int getUpdateCount() throws SQLException {
        return 0;
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return false;
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {

    }

    @Override
    public int getFetchDirection() throws SQLException {
        return 0;
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {

    }

    @Override
    public int getFetchSize() throws SQLException {
        return 0;
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return 0;
    }

    @Override
    public int getResultSetType() throws SQLException {
        return 0;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return null;
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        return false;
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return null;
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return 0;
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return 0;
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        return 0;
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        return false;
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        return false;
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        return false;
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return 0;
    }

    @Override
    public boolean isClosed() throws SQLException {
        return false;
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {

    }

    @Override
    public boolean isPoolable() throws SQLException {
        return false;
    }

    @Override
    public void closeOnCompletion() throws SQLException {

    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return false;
    }

    @Override
    public long[] executeLargeBatch() throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("TextStatement#executeLargeBatch()",
                "mysql-jdbc does not support batch statements.");
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("TextStatement#addBatch(String sql)",
                "mysql-jdbc does not support batch statements.");
    }

    @Override
    public void clearBatch() throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("TextStatement#clearBatch()",
                "mysql-jdbc does not support batch statements.");
    }

    @Override
    public int[] executeBatch() throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("TextStatement#executeBatch()",
                "mysql-jdbc does not support batch statements.");
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("TextStatement#setMaxRows(int max)");
    }

    @Override
    public void setLargeMaxRows(long max) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("TextStatement#setLargeMaxRows(long max)");
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("TextStatement#setMaxFieldSize(int max)");
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return (T) this;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return TextStatement.class == iface;
    }
}
