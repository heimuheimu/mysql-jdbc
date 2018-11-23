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

package com.heimuheimu.mysql.jdbc.monitor;

import com.heimuheimu.mysql.jdbc.constant.SQLType;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Mysql 数据库信息监控器。
 *
 * @author heimuheimu
 */
public class DatabaseMonitor {

    /**
     * SELECT 语句返回的记录总数
     */
    private final AtomicLong selectRowsCount = new AtomicLong(0);

    /**
     * 单条 SELECT 语句返回的最大记录数
     */
    private volatile long maxSelectRowsCount = 0;

    /**
     * INSERT 语句插入的记录总数
     */
    private final AtomicLong insertRowsCount = new AtomicLong(0);

    /**
     * 单条 INSERT 语句插入的最大记录数
     */
    private volatile long maxInsertRowsCount = 0;

    /**
     * UPDATE 语句更新的记录总数
     */
    private final AtomicLong updateRowsCount = new AtomicLong(0);

    /**
     * 单条 UPDATE 语句更新的最大记录数
     */
    private volatile long maxUpdateRowsCount = 0;

    /**
     * DELETE 语句删除的记录总数
     */
    private final AtomicLong deleteRowsCount = new AtomicLong(0);

    /**
     * 单条 DELETE 语句删除的最大记录数
     */
    private volatile long maxDeleteRowsCount = 0;

    /**
     * 当 SELECT 语句执行完成后，调用此方法进行监控。
     *
     * @param rowsCount SELECT 语句返回的记录总数
     */
    public void onSelectExecuted(long rowsCount) {
        selectRowsCount.addAndGet(rowsCount);
        if (maxSelectRowsCount < rowsCount) {
            maxSelectRowsCount = rowsCount;
        }
    }

    /**
     * 当 SQL 语句执行完成后，调用此方法进行监控。
     *
     * @param sqlType SQL 语句类型
     * @param rowsCount SQL 语句影响的记录总数
     */
    public void onExecuted(SQLType sqlType, long rowsCount) {
        switch (sqlType) {
            case INSERT:
                insertRowsCount.addAndGet(rowsCount);
                //仅使用了 volatile 来保证可见性，并没有保证操作的原子性，极端情况下，真正的最大值可能会被覆盖
                if (maxInsertRowsCount < rowsCount) {
                    maxInsertRowsCount = rowsCount;
                }
                break;
            case UPDATE:
                updateRowsCount.addAndGet(rowsCount);
                if (maxUpdateRowsCount < rowsCount) {
                    maxUpdateRowsCount = rowsCount;
                }
                break;
            case DELETE:
                deleteRowsCount.addAndGet(rowsCount);
                if (maxDeleteRowsCount < rowsCount) {
                    maxDeleteRowsCount = rowsCount;
                }
                break;
            case SELECT:
                onSelectExecuted(rowsCount);
                break;
            default:
                break;
        }
    }

    /**
     * 获得 SELECT 语句返回的记录总数。
     *
     * @return SELECT 语句返回的记录总数
     */
    public long getSelectRowsCount() {
        return selectRowsCount.get();
    }

    /**
     * 获得单条 SELECT 语句返回的最大记录数。
     *
     * @return 单条 SELECT 语句返回的最大记录数
     */
    public long getMaxSelectRowsCount() {
        return maxSelectRowsCount;
    }

    /**
     * 重置单条 SELECT 语句返回的最大记录数，将其设置为 0。
     */
    public void resetMaxSelectRowsCount() {
        this.maxSelectRowsCount = 0;
    }

    /**
     * 获得 INSERT 语句插入的记录总数。
     *
     * @return INSERT 语句插入的记录总数
     */
    public long getInsertRowsCount() {
        return insertRowsCount.get();
    }

    /**
     * 获得单条 INSERT 语句插入的最大记录数。
     *
     * @return 单条 INSERT 语句插入的最大记录数
     */
    public long getMaxInsertRowsCount() {
        return maxInsertRowsCount;
    }

    /**
     * 重置单条 INSERT 语句插入的最大记录数，将其设置为 0。
     */
    public void resetMaxInsertRowsCount() {
        this.maxInsertRowsCount = 0;
    }

    /**
     * 获得 UPDATE 语句更新的记录总数。
     *
     * @return UPDATE 语句更新的记录总数
     */
    public long getUpdateRowsCount() {
        return updateRowsCount.get();
    }

    /**
     * 获得单条 UPDATE 语句更新的最大记录数。
     *
     * @return 单条 UPDATE 语句更新的最大记录数
     */
    public long getMaxUpdateRowsCount() {
        return maxUpdateRowsCount;
    }

    /**
     * 重置单条 UPDATE 语句更新的最大记录数，将其设置为 0。
     */
    public void resetMaxUpdateRowsCount() {
        this.maxUpdateRowsCount = 0;
    }

    /**
     * 获得 DELETE 语句删除的记录总数。
     *
     * @return DELETE 语句删除的记录总数
     */
    public long getDeleteRowsCount() {
        return deleteRowsCount.get();
    }

    /**
     * 获得单条 DELETE 语句删除的最大记录数。
     *
     * @return 单条 DELETE 语句删除的最大记录数
     */
    public long getMaxDeleteRowsCount() {
        return maxDeleteRowsCount;
    }

    /**
     * 重置单条 DELETE 语句删除的最大记录数，将其设置为 0。
     */
    public void resetMaxDeleteRowsCount() {
        this.maxDeleteRowsCount = 0;
    }

    @Override
    public String toString() {
        return "DatabaseMonitor{" +
                "selectRowsCount=" + selectRowsCount +
                ", maxSelectRowsCount=" + maxSelectRowsCount +
                ", insertRowsCount=" + insertRowsCount +
                ", maxInsertRowsCount=" + maxInsertRowsCount +
                ", updateRowsCount=" + updateRowsCount +
                ", maxUpdateRowsCount=" + maxUpdateRowsCount +
                ", deleteRowsCount=" + deleteRowsCount +
                ", maxDeleteRowsCount=" + maxDeleteRowsCount +
                '}';
    }
}
