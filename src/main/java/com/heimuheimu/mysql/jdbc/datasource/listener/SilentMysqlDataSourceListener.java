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

package com.heimuheimu.mysql.jdbc.datasource.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link MysqlDataSourceListener} 方法静默执行封装类，监听器中的方法执行遇到异常时，仅打印日志。
 *
 * <p><strong>说明：</strong>{@code SilentMysqlDataSourceListener} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class SilentMysqlDataSourceListener implements MysqlDataSourceListener {
    
    private static final Logger LOG = LoggerFactory.getLogger(SilentMysqlDataSourceListener.class);

    private final MysqlDataSourceListener target;

    /**
     * 构造一个 {@link MysqlDataSourceListener} 方法静默执行封装类。
     *
     * @param target 目标监听器，允许为 {@code null}
     */
    public SilentMysqlDataSourceListener(MysqlDataSourceListener target) {
        this.target = target;
    }

    @Override
    public void onCreated(String host, String databaseName) {
        if (target != null) {
            try {
                target.onCreated(host, databaseName);
            } catch (Exception e) {
                LOG.error("Execute `MysqlDataSourceListener#onCreated(String host, String databaseName)` failed: `unexpected error`. `host`:`"
                        + host + "`. `databaseName`:`" + databaseName + "`.", e);
            }
        }
    }

    @Override
    public void onRecovered(String host, String databaseName) {
        if (target != null) {
            try {
                target.onRecovered(host, databaseName);
            } catch (Exception e) {
                LOG.error("Execute `MysqlDataSourceListener#onRecovered(String host, String databaseName)` failed: `unexpected error`. `host`:`"
                        + host + "`. `databaseName`:`" + databaseName + "`.", e);
            }
        }
    }

    @Override
    public void onClosed(String host, String databaseName) {
        if (target != null) {
            try {
                target.onClosed(host, databaseName);
            } catch (Exception e) {
                LOG.error("Execute `MysqlDataSourceListener#onClosed(String host, String databaseName)` failed: `unexpected error`. `host`:`"
                        + host + "`. `databaseName`:`" + databaseName + "`.", e);
            }
        }
    }
}
