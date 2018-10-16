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

package com.heimuheimu.mysql.jdbc.facility;

import com.heimuheimu.mysql.jdbc.monitor.SQLFeatureNotSupportedMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLFeatureNotSupportedException;

/**
 * {@link SQLFeatureNotSupportedException} 构造器。
 *
 * <p><strong>说明：</strong>{@code SQLFeatureNotSupportedExceptionBuilder} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class SQLFeatureNotSupportedExceptionBuilder {
    
    private static final Logger LOG = LoggerFactory.getLogger("SQL_FEATURE_NOT_SUPPORTED_LOG");

    /**
     * 构造一个 {@link SQLFeatureNotSupportedException} 异常实例，异常描述信息使用默认值。
     *
     * @param methodName 抛出该异常的方法名称
     * @return {@code SQLFeatureNotSupportedException} 实例
     */
    public static SQLFeatureNotSupportedException build(String methodName) {
        return build(methodName, "");
    }

    /**
     * 构造一个 {@link SQLFeatureNotSupportedException} 异常实例。
     *
     * @param methodName 抛出该异常的方法名称
     * @param errorMessage 异常描述信息
     * @return {@code SQLFeatureNotSupportedException} 实例
     */
    public static SQLFeatureNotSupportedException build(String methodName, String errorMessage) {
        SQLFeatureNotSupportedMonitor.getInstance().increaseCount();
        if (errorMessage == null || errorMessage.isEmpty()) {
            errorMessage = "mysql-jdbc does not support this method.";
        }
        errorMessage += " method: `" + methodName + "`.";
        LOG.error(errorMessage);
        return new SQLFeatureNotSupportedException(errorMessage);
    }
}
