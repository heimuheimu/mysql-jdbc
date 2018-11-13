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

import java.util.concurrent.atomic.AtomicLong;

/**
 * Mysql 数据库连接池信息监控器。
 *
 * @author heimuheimu
 */
public class DataSourceMonitor {

    /**
     * 连接池发生连接泄漏的次数
     */
    private final AtomicLong connectionLeakedCount = new AtomicLong(0);

    /**
     * 连接池获取不到连接的次数
     */
    private final AtomicLong getConnectionFailedCount = new AtomicLong(0);

    /**
     * 连接池当前被使用的连接数量
     */
    private final AtomicLong acquiredConnectionCount = new AtomicLong(0);

    /**
     * 连接池被使用的最大连接数量
     */
    private volatile long maxAcquiredConnectionCount = 0;

    /**
     * 当连接池发生连接泄漏时，调用此方法进行监控
     */
    public void onConnectionLeaked() {
        connectionLeakedCount.incrementAndGet();
    }

    /**
     * 当从连接池中获取连接失败时，调用此方法进行监控。
     */
    public void onGetConnectionFailed() {
        getConnectionFailedCount.incrementAndGet();
    }

    /**
     * 当一个连接从连接池中被获取时，调用此方法进行监控。
     */
    public void onConnectionAcquired() {
        long currentAcquiredCount = acquiredConnectionCount.incrementAndGet();
        if (currentAcquiredCount > maxAcquiredConnectionCount) {
            maxAcquiredConnectionCount = currentAcquiredCount;
        }
    }

    /**
     * 当一个连接使用完成后被释放到连接池中时，调用此方法进行监控。
     */
    public void onConnectionReleased() {
        acquiredConnectionCount.decrementAndGet();
    }

    /**
     * 重制连接池被使用的最大连接数量。
     */
    public void resetMaxAcquiredConnectionCount() {
        maxAcquiredConnectionCount = 0;
    }

    /**
     * 获得连接池发生连接泄漏的次数。
     *
     * @return 连接池发生连接泄漏的次数
     */
    public long getConnectionLeakedCount() {
        return connectionLeakedCount.get();
    }

    /**
     * 获得连接池获取不到连接的次数。
     *
     * @return 连接池获取不到连接的次数
     */
    public long getGetConnectionFailedCount() {
        return getConnectionFailedCount.get();
    }

    /**
     * 获得连接池当前被使用的连接数量。
     *
     * @return 连接池当前被使用的连接数量
     */
    public long getAcquiredConnectionCount() {
        return acquiredConnectionCount.get();
    }

    /**
     * 获得连接池被使用的最大连接数量。
     *
     * @return 连接池被使用的最大连接数量
     */
    public long getMaxAcquiredConnectionCount() {
        return maxAcquiredConnectionCount;
    }

    @Override
    public String toString() {
        return "DataSourceMonitor{" +
                "connectionLeakedCount=" + connectionLeakedCount +
                ", getConnectionFailedCount=" + getConnectionFailedCount +
                ", acquiredConnectionCount=" + acquiredConnectionCount +
                ", maxAcquiredConnectionCount=" + maxAcquiredConnectionCount +
                '}';
    }
}
