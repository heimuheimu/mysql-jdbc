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

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 支持滚动读取行数据的只读 {@link ResultSet} 抽象实现类，{@link #getType()} 方法永远返回
 * {@link ResultSet#TYPE_SCROLL_INSENSITIVE}。
 *
 * <p><strong>说明：</strong>{@code ReadonlyScrollResultSet} 类是非线程安全的。</p>
 *
 * @author heimuheimu
 */
public abstract class ReadonlyScrollResultSet extends ReadonlyResultSet {

    /**
     * 获取行数据顺序
     */
    private int fetchDirection = ResultSet.FETCH_FORWARD;

    /**
     * 当前正在读取的行数，第一行为 1，第二行为 2，以此类推
     */
    private int currentRow = 0;

    @Override
    public boolean next() throws SQLException {
        return relative(1);
    }

    @Override
    public boolean isBeforeFirst() throws SQLException {
        int rowsSize = getRowsSize();
        if (rowsSize > 0) {
            return currentRow == 0;
        } else {
            return false;
        }
    }

    @Override
    public boolean isAfterLast() throws SQLException {
        int rowsSize = getRowsSize();
        if (rowsSize > 0) {
            return currentRow == (rowsSize + 1);
        } else {
            return false;
        }
    }

    @Override
    public boolean isFirst() throws SQLException {
        return currentRow == 1;
    }

    @Override
    public boolean isLast() throws SQLException {
        int rowsSize = getRowsSize();
        if (rowsSize > 0) {
            return currentRow == rowsSize;
        } else {
            return false;
        }
    }

    @Override
    public void beforeFirst() throws SQLException {
        int rowsSize = getRowsSize();
        if (rowsSize > 0) {
            currentRow = 0;
        }
    }

    @Override
    public void afterLast() throws SQLException {
        int rowsSize = getRowsSize();
        if (rowsSize > 0) {
            currentRow = rowsSize + 1;
        }
    }

    @Override
    public boolean first() throws SQLException {
        return absolute(1);
    }

    @Override
    public boolean last() throws SQLException {
        return absolute(-1);
    }

    @Override
    public int getRow() throws SQLException {
        int rowsSize = getRowsSize();
        if (currentRow > 0 && currentRow <= rowsSize) {
            return currentRow;
        } else {
            return 0;
        }
    }

    @Override
    public boolean absolute(int row) throws SQLException {
        int rowsSize = getRowsSize();
        if (rowsSize > 0) {
            if (row < 0) {
                row = rowsSize + 1 + row;
            }
            row = Math.max(0, row);
            row = Math.min(rowsSize + 1, row);
            currentRow = row;
            return currentRow > 0 && currentRow <= rowsSize;
        } else {
            return false;
        }
    }

    @Override
    public boolean relative(int rows) throws SQLException {
        int targetRow = currentRow + rows;
        targetRow = Math.max(0, targetRow);
        return absolute(targetRow);
    }

    @Override
    public boolean previous() throws SQLException {
        return relative(-1);
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        if (direction == ResultSet.FETCH_REVERSE) {
            fetchDirection = ResultSet.FETCH_REVERSE;
        } else {
            fetchDirection = ResultSet.FETCH_FORWARD;
        }
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return fetchDirection;
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        // do nothing
    }

    @Override
    public int getFetchSize() throws SQLException {
        return getRowsSize();
    }

    @Override
    public int getType() throws SQLException {
        return ResultSet.TYPE_SCROLL_INSENSITIVE;
    }

    public abstract int getRowsSize();
}
