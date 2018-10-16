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

import java.sql.SQLFeatureNotSupportedException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * {@link SQLFeatureNotSupportedException} 异常次数监控器。
 *
 * @author heimuheimu
 */
public class SQLFeatureNotSupportedMonitor {

    private static final SQLFeatureNotSupportedMonitor INSTANCE = new SQLFeatureNotSupportedMonitor();

    /**
     * {@link SQLFeatureNotSupportedException} 异常累计发生次数
     */
    private final AtomicLong count = new AtomicLong();

    /**
     * 当 {@link SQLFeatureNotSupportedException} 异常发生时，调用此方法进行监控。
     */
    public void increaseCount() {
        count.incrementAndGet();
    }

    /**
     * 获得 {@link SQLFeatureNotSupportedException} 异常累计发生次数。
     *
     * @return {@link SQLFeatureNotSupportedException} 异常累计发生次数
     */
    public long getCount() {
        return count.get();
    }

    @Override
    public String toString() {
        return "SQLFeatureNotSupportedMonitor{" +
                "count=" + count +
                '}';
    }

    /**
     * 获得 {@code SQLFeatureNotSupportedMonitor} 监控器实例。
     *
     * @return {@code SQLFeatureNotSupportedMonitor} 监控器实例
     */
    public static SQLFeatureNotSupportedMonitor getInstance() {
        return INSTANCE;
    }
}
