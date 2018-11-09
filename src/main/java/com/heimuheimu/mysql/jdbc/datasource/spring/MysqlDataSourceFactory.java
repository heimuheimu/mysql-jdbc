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

package com.heimuheimu.mysql.jdbc.datasource.spring;

import com.heimuheimu.mysql.jdbc.ConnectionConfiguration;
import com.heimuheimu.mysql.jdbc.datasource.DataSourceConfiguration;
import com.heimuheimu.mysql.jdbc.datasource.MysqlDataSource;
import com.heimuheimu.mysql.jdbc.datasource.listener.MysqlDataSourceListener;
import org.springframework.beans.factory.FactoryBean;

import java.io.Closeable;

/**
 * {@link MysqlDataSource} Spring 工厂类，兼容 Spring 4.0 以下版本不支持 lambda 语法问题。
 */
public class MysqlDataSourceFactory implements FactoryBean<MysqlDataSource>, Closeable {

    private final MysqlDataSource mysqlDataSource;

    /**
     * 构造一个 Mysql 数据库连接池工厂类，用于创建 {@link MysqlDataSource} 实例。
     *
     * @param connectionConfiguration 建立 Mysql 数据库连接使用的配置信息，不允许为 {@code null}
     * @param dataSourceConfiguration 连接池使用的配置信息，不允许为 {@code null}
     * @param listener Mysql 数据库连接池事件监听器，允许为 {@code null}
     * @throws IllegalArgumentException 如果 {@code connectionConfiguration} 或 {@code dataSourceConfiguration} 为 {@code null}，将会抛出此异常
     */
    public MysqlDataSourceFactory(ConnectionConfiguration connectionConfiguration, DataSourceConfiguration dataSourceConfiguration,
                                  MysqlDataSourceListener listener) throws IllegalArgumentException {
        this.mysqlDataSource = new MysqlDataSource(connectionConfiguration, dataSourceConfiguration, listener);
    }

    @Override
    public MysqlDataSource getObject() throws Exception {
        return mysqlDataSource;
    }

    @Override
    public Class<?> getObjectType() {
        return MysqlDataSource.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void close() {
        this.mysqlDataSource.close();
    }
}
