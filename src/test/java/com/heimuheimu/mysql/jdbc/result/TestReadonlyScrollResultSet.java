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

import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;

/**
 * {@link ReadonlyScrollResultSet} 单元测试类。
 *
 * @author heimuheimu
 */
public class TestReadonlyScrollResultSet {

    @Test
    public void testNext() {
        try {
            SimpleReadonlyScrollResultSet resultSet = new SimpleReadonlyScrollResultSet(0);
            Assert.assertFalse(resultSet.next());
            Assert.assertEquals(0, resultSet.getRow());
            Assert.assertFalse(resultSet.isAfterLast());
            Assert.assertFalse(resultSet.isBeforeFirst());
            Assert.assertFalse(resultSet.isFirst());
            Assert.assertFalse(resultSet.isLast());

            int rowsSize = 10;
            resultSet = new SimpleReadonlyScrollResultSet(rowsSize);
            Assert.assertEquals(0, resultSet.getRow());
            Assert.assertTrue(resultSet.isBeforeFirst());
            Assert.assertFalse(resultSet.isAfterLast());
            Assert.assertFalse(resultSet.isFirst());
            Assert.assertFalse(resultSet.isLast());

            for (int i = 0; i < rowsSize; i++) {
                Assert.assertTrue("Test #next() failed. row: " + i, resultSet.next());
                Assert.assertEquals("Test #next() failed. row: " + i, i + 1, resultSet.getRow());

                Assert.assertFalse("Test #next() failed. row: " + i, resultSet.isBeforeFirst());
                Assert.assertFalse("Test #next() failed. row: " + i, resultSet.isAfterLast());
                if (i == 0) {
                    Assert.assertTrue("Test #next() failed. row: " + i, resultSet.isFirst());
                } else {
                    Assert.assertFalse("Test #next() failed. row: " + i, resultSet.isFirst());
                }
                if (i == (rowsSize - 1)) {
                    Assert.assertTrue("Test #next() failed. row: " + i, resultSet.isLast());
                } else {
                    Assert.assertFalse("Test #next() failed. row: " + i, resultSet.isLast());
                }
            }

            Assert.assertFalse(resultSet.next());
            Assert.assertEquals(0, resultSet.getRow());
            Assert.assertFalse(resultSet.isBeforeFirst());
            Assert.assertTrue(resultSet.isAfterLast());
            Assert.assertFalse(resultSet.isFirst());
            Assert.assertFalse(resultSet.isLast());

            Assert.assertFalse(resultSet.next());
            Assert.assertEquals(0, resultSet.getRow());
            Assert.assertFalse(resultSet.isBeforeFirst());
            Assert.assertTrue(resultSet.isAfterLast());
            Assert.assertFalse(resultSet.isFirst());
            Assert.assertFalse(resultSet.isLast());
        } catch (SQLException e) {
            Assert.fail("Test #next() failed.");
        }
    }

    @Test
    public void testAbsolute() {
        try {
            SimpleReadonlyScrollResultSet resultSet = new SimpleReadonlyScrollResultSet(0);
            Assert.assertFalse(resultSet.absolute(0));
            Assert.assertFalse(resultSet.absolute(-1));
            Assert.assertFalse(resultSet.absolute(1));

            int rowsSize = 10;
            resultSet = new SimpleReadonlyScrollResultSet(rowsSize);

            for (int i = 1; i <= rowsSize; i++) {
                Assert.assertTrue("Test #absolute(int row) failed. row: " + i, resultSet.absolute(i));
                Assert.assertEquals("Test #absolute(int row) failed. row: " + i, i, resultSet.getRow());
            }
            Assert.assertFalse(resultSet.absolute(0));
            Assert.assertTrue(resultSet.isBeforeFirst());
            Assert.assertFalse(resultSet.absolute(rowsSize + 1));
            Assert.assertTrue(resultSet.isAfterLast());
            Assert.assertFalse(resultSet.absolute(rowsSize + 2));
            Assert.assertTrue(resultSet.isAfterLast());

            for (int i = -1; i >= -rowsSize; i--) {
                Assert.assertTrue("Test #absolute() failed. row: " + i, resultSet.absolute(i));
                Assert.assertEquals("Test #absolute() failed. row: " + i, rowsSize + 1 + i, resultSet.getRow());
            }
            Assert.assertFalse(resultSet.absolute(-rowsSize - 1));
            Assert.assertTrue(resultSet.isBeforeFirst());
            Assert.assertFalse(resultSet.absolute(-rowsSize - 2));
            Assert.assertTrue(resultSet.isBeforeFirst());
        } catch (SQLException e) {
            Assert.fail("Test #absolute(int row) failed.");
        }
    }

    @Test
    public void testRelative() {
        try {
            SimpleReadonlyScrollResultSet resultSet = new SimpleReadonlyScrollResultSet(0);
            Assert.assertFalse(resultSet.relative(0));
            Assert.assertFalse(resultSet.relative(-1));
            Assert.assertFalse(resultSet.relative(1));

            int rowsSize = 10;
            resultSet = new SimpleReadonlyScrollResultSet(rowsSize);
            resultSet.absolute(5);
            Assert.assertTrue(resultSet.relative(-1));
            Assert.assertEquals(4, resultSet.getRow());

            resultSet.absolute(5);
            Assert.assertTrue(resultSet.relative(-3));
            Assert.assertEquals(2, resultSet.getRow());

            resultSet.absolute(5);
            Assert.assertFalse(resultSet.relative(-5));
            Assert.assertTrue(resultSet.isBeforeFirst());
            Assert.assertEquals(0, resultSet.getRow());

            resultSet.absolute(5);
            Assert.assertFalse(resultSet.relative(-100));
            Assert.assertTrue(resultSet.isBeforeFirst());
            Assert.assertEquals(0, resultSet.getRow());

            resultSet.absolute(5);
            Assert.assertTrue(resultSet.relative(1));
            Assert.assertEquals(6, resultSet.getRow());

            resultSet.absolute(5);
            Assert.assertTrue(resultSet.relative(2));
            Assert.assertEquals(7, resultSet.getRow());

            resultSet.absolute(5);
            Assert.assertTrue(resultSet.relative(5));
            Assert.assertEquals(10, resultSet.getRow());

            resultSet.absolute(5);
            Assert.assertFalse(resultSet.relative(6));
            Assert.assertTrue(resultSet.isAfterLast());
            Assert.assertEquals(0, resultSet.getRow());

            resultSet.absolute(5);
            Assert.assertFalse(resultSet.relative(100));
            Assert.assertTrue(resultSet.isAfterLast());
            Assert.assertEquals(0, resultSet.getRow());
        } catch (SQLException e) {
            Assert.fail("Test #relative(int rows) failed.");
        }
    }

    @Test
    public void testPrevious() {
        try {
            SimpleReadonlyScrollResultSet resultSet = new SimpleReadonlyScrollResultSet(0);
            Assert.assertFalse(resultSet.previous());
            Assert.assertFalse(resultSet.isAfterLast());
            Assert.assertFalse(resultSet.isBeforeFirst());
            Assert.assertFalse(resultSet.isFirst());
            Assert.assertFalse(resultSet.isLast());

            int rowsSize = 10;
            resultSet = new SimpleReadonlyScrollResultSet(rowsSize);
            Assert.assertFalse(resultSet.previous());

            resultSet.afterLast();
            for (int i = 1; i <= rowsSize; i++) {
                Assert.assertTrue(resultSet.previous());
                Assert.assertEquals(rowsSize + 1 - i, resultSet.getRow());
            }
            Assert.assertFalse(resultSet.previous());
            Assert.assertTrue(resultSet.isBeforeFirst());
            Assert.assertEquals(0, resultSet.getRow());
        } catch (SQLException e) {
            Assert.fail("Test #previous() failed.");
        }
    }

    @Test
    public void testFirst() {
        try {
            SimpleReadonlyScrollResultSet resultSet = new SimpleReadonlyScrollResultSet(0);
            Assert.assertFalse(resultSet.first());
            Assert.assertFalse(resultSet.isFirst());

            int rowsSize = 10;
            resultSet = new SimpleReadonlyScrollResultSet(rowsSize);
            Assert.assertFalse(resultSet.isFirst());
            Assert.assertTrue(resultSet.first());
            Assert.assertTrue(resultSet.isFirst());
        } catch (SQLException e) {
            Assert.fail("Test #first() failed.");
        }
    }

    @Test
    public void testLast() {
        try {
            SimpleReadonlyScrollResultSet resultSet = new SimpleReadonlyScrollResultSet(0);
            Assert.assertFalse(resultSet.last());
            Assert.assertFalse(resultSet.isLast());

            int rowsSize = 10;
            resultSet = new SimpleReadonlyScrollResultSet(rowsSize);
            Assert.assertFalse(resultSet.isLast());
            Assert.assertTrue(resultSet.last());
            Assert.assertTrue(resultSet.isLast());
        } catch (SQLException e) {
            Assert.fail("Test #last() failed.");
        }
    }

    @Test
    public void testBeforeFirst() {
        try {
            SimpleReadonlyScrollResultSet resultSet = new SimpleReadonlyScrollResultSet(0);
            Assert.assertFalse(resultSet.isBeforeFirst());
            resultSet.beforeFirst();
            Assert.assertFalse(resultSet.isBeforeFirst());

            int rowsSize = 10;
            resultSet = new SimpleReadonlyScrollResultSet(rowsSize);
            Assert.assertTrue(resultSet.isBeforeFirst());
            Assert.assertTrue(resultSet.next());
            Assert.assertFalse(resultSet.isBeforeFirst());
            resultSet.beforeFirst();
            Assert.assertTrue(resultSet.isBeforeFirst());
        } catch (SQLException e) {
            Assert.fail("Test #beforeFirst() failed.");
        }
    }

    @Test
    public void testAfterLast() {
        try {
            SimpleReadonlyScrollResultSet resultSet = new SimpleReadonlyScrollResultSet(0);
            Assert.assertFalse(resultSet.isAfterLast());
            resultSet.afterLast();
            Assert.assertFalse(resultSet.isAfterLast());

            int rowsSize = 10;
            resultSet = new SimpleReadonlyScrollResultSet(rowsSize);
            Assert.assertFalse(resultSet.isAfterLast());
            resultSet.afterLast();
            Assert.assertTrue(resultSet.isAfterLast());
        } catch (SQLException e) {
            Assert.fail("Test #afterLast() failed.");
        }
    }

    private static class SimpleReadonlyScrollResultSet extends ReadonlyScrollResultSet {

        public final int rowsSize;

        public SimpleReadonlyScrollResultSet(int rowsSize) {
            this.rowsSize = rowsSize;
        }

        @Override
        public int getRowsSize() {
            return rowsSize;
        }

        @Override
        public void close() throws SQLException {

        }

        @Override
        public boolean wasNull() throws SQLException {
            return false;
        }

        @Override
        public String getString(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public boolean getBoolean(int columnIndex) throws SQLException {
            return false;
        }

        @Override
        public byte getByte(int columnIndex) throws SQLException {
            return 0;
        }

        @Override
        public short getShort(int columnIndex) throws SQLException {
            return 0;
        }

        @Override
        public int getInt(int columnIndex) throws SQLException {
            return 0;
        }

        @Override
        public long getLong(int columnIndex) throws SQLException {
            return 0;
        }

        @Override
        public float getFloat(int columnIndex) throws SQLException {
            return 0;
        }

        @Override
        public double getDouble(int columnIndex) throws SQLException {
            return 0;
        }

        @Override
        public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
            return null;
        }

        @Override
        public byte[] getBytes(int columnIndex) throws SQLException {
            return new byte[0];
        }

        @Override
        public Date getDate(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public Time getTime(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public Timestamp getTimestamp(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public InputStream getAsciiStream(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public InputStream getUnicodeStream(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public InputStream getBinaryStream(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public String getString(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public boolean getBoolean(String columnLabel) throws SQLException {
            return false;
        }

        @Override
        public byte getByte(String columnLabel) throws SQLException {
            return 0;
        }

        @Override
        public short getShort(String columnLabel) throws SQLException {
            return 0;
        }

        @Override
        public int getInt(String columnLabel) throws SQLException {
            return 0;
        }

        @Override
        public long getLong(String columnLabel) throws SQLException {
            return 0;
        }

        @Override
        public float getFloat(String columnLabel) throws SQLException {
            return 0;
        }

        @Override
        public double getDouble(String columnLabel) throws SQLException {
            return 0;
        }

        @Override
        public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
            return null;
        }

        @Override
        public byte[] getBytes(String columnLabel) throws SQLException {
            return new byte[0];
        }

        @Override
        public Date getDate(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public Time getTime(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public Timestamp getTimestamp(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public InputStream getAsciiStream(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public InputStream getUnicodeStream(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public InputStream getBinaryStream(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public SQLWarning getWarnings() throws SQLException {
            return null;
        }

        @Override
        public void clearWarnings() throws SQLException {

        }

        @Override
        public String getCursorName() throws SQLException {
            return null;
        }

        @Override
        public ResultSetMetaData getMetaData() throws SQLException {
            return null;
        }

        @Override
        public Object getObject(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public Object getObject(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public int findColumn(String columnLabel) throws SQLException {
            return 0;
        }

        @Override
        public Reader getCharacterStream(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public Reader getCharacterStream(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public Statement getStatement() throws SQLException {
            return null;
        }

        @Override
        public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
            return null;
        }

        @Override
        public Ref getRef(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public Blob getBlob(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public Clob getClob(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public Array getArray(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
            return null;
        }

        @Override
        public Ref getRef(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public Blob getBlob(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public Clob getClob(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public Array getArray(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public Date getDate(int columnIndex, Calendar cal) throws SQLException {
            return null;
        }

        @Override
        public Date getDate(String columnLabel, Calendar cal) throws SQLException {
            return null;
        }

        @Override
        public Time getTime(int columnIndex, Calendar cal) throws SQLException {
            return null;
        }

        @Override
        public Time getTime(String columnLabel, Calendar cal) throws SQLException {
            return null;
        }

        @Override
        public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
            return null;
        }

        @Override
        public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
            return null;
        }

        @Override
        public URL getURL(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public URL getURL(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public RowId getRowId(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public RowId getRowId(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public int getHoldability() throws SQLException {
            return 0;
        }

        @Override
        public boolean isClosed() throws SQLException {
            return false;
        }

        @Override
        public NClob getNClob(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public NClob getNClob(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public SQLXML getSQLXML(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public SQLXML getSQLXML(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public String getNString(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public String getNString(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public Reader getNCharacterStream(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public Reader getNCharacterStream(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
            return null;
        }

        @Override
        public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
            return null;
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            return null;
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return false;
        }
    }
}
