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

package com.heimuheimu.mysql.jdbc.datasource;

import com.heimuheimu.mysql.jdbc.util.LogBuildUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Mysql 数据库连接池使用的配置信息。
 *
 * <p><strong>说明：</strong>{@code DataSourceConfiguration} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class DataSourceConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(DataSourceConfiguration.class);

    /**
     * 连接池拥有的数据库连接数量，不允许小于等于 0
     */
    private final int poolSize;

    /**
     * 从连接池获取数据库连接的超时时间，单位：毫秒，如果等于 0，则没有超时时间限制，不允许设置小于 0 的值
     */
    private final long checkoutTimeout;

    /**
     * 连接最大占用时间，单位：毫秒，如果为 0，则没有最大时间限制，不允许设置小于 0 的值
     */
    private final int maxOccupyTime;

    /**
     * SQL 执行超时时间，单位：毫秒，如果等于 0，则没有超时时间限制，不允许设置小于 0 的值
     */
    private final int timeout;

    /**
     * 执行 Mysql 命令过慢最小时间，单位：毫秒，不能小于等于 0
     */
    private final int slowExecutionThreshold;

    /**
     * 构造一个 Mysql 数据库连接池使用的配置信息，如果最大连接数量值小于最小连接数量值，将使用最小连接数量的值。
     *
     * @param poolSize 连接池拥有的数据库连接数量，不允许小于等于 0
     * @param checkoutTimeout 从连接池获取数据库连接的超时时间，单位：毫秒，如果等于 0，则没有超时时间限制，不允许设置小于 0 的值
     * @param maxOccupyTime 连接最大占用时间，单位：毫秒，如果为 0，则没有最大时间限制，不允许设置小于 0 的值
     * @param timeout SQL 执行超时时间，单位：毫秒，如果等于 0，则没有超时时间限制，不允许设置小于 0 的值
     * @param slowExecutionThreshold 执行 Mysql 命令过慢最小时间，单位：毫秒，不能小于等于 0
     * @throws IllegalArgumentException 如果 {@code minPoolSize} 或 {@code maxPoolSize} 或 {@code checkoutTimeout} 的值小于 0，将抛出此异常
     */
    public DataSourceConfiguration(int poolSize, long checkoutTimeout, int maxOccupyTime, int timeout,
                                   int slowExecutionThreshold) throws IllegalArgumentException {
        this.poolSize = poolSize;
        this.checkoutTimeout = checkoutTimeout;
        this.maxOccupyTime = maxOccupyTime;
        this.timeout = timeout;
        this.slowExecutionThreshold = slowExecutionThreshold;

        if (poolSize <= 0) {
            String errorLog = "Create `DataSourceConfiguration` failed: `poolSize could not be equal or less than 0`."
                    + buildLogForParameters();
            LOG.error(errorLog);
            throw new IllegalArgumentException(errorLog);
        }
        if (checkoutTimeout < 0) {
            String errorLog = "Create `DataSourceConfiguration` failed: `checkoutTimeout could not be less than 0`."
                    + buildLogForParameters();
            LOG.error(errorLog);
            throw new IllegalArgumentException(errorLog);
        }
        if (maxOccupyTime < 0) {
            String errorLog = "Create `DataSourceConfiguration` failed: `maxOccupyTime could not be less than 0`."
                    + buildLogForParameters();
            LOG.error(errorLog);
            throw new IllegalArgumentException(errorLog);
        }
        if (timeout < 0) {
            String errorLog = "Create `DataSourceConfiguration` failed: `timeout could not be less than 0`."
                    + buildLogForParameters();
            LOG.error(errorLog);
            throw new IllegalArgumentException(errorLog);
        }
        if (slowExecutionThreshold <= 0) {
            String errorLog = "Create `DataSourceConfiguration` failed: `slowExecutionThreshold could not be equal or less than 0`."
                    + buildLogForParameters();
            LOG.error(errorLog);
            throw new IllegalArgumentException(errorLog);
        }
    }

    /**
     * 获得连接池拥有的数据库连接数量，不允许小于等于 0。
     *
     * @return 连接池拥有的数据库连接数量，不允许小于等于 0
     */
    public int getPoolSize() {
        return poolSize;
    }

    /**
     * 获得从连接池获取数据库连接的超时时间，单位：毫秒，如果等于 0，则没有超时时间限制。
     *
     * @return 从连接池获取数据库连接的超时时间，单位：毫秒，如果等于 0，则没有超时时间限制
     */
    public long getCheckoutTimeout() {
        return checkoutTimeout;
    }

    /**
     * 获得连接最大占用时间，单位：毫秒，如果为 0，则没有最大时间限制。
     *
     * @return 连接最大占用时间，单位：毫秒，如果为 0，则没有最大时间限制
     */
    public int getMaxOccupyTime() {
        return maxOccupyTime;
    }

    /**
     * 获得 SQL 执行超时时间，单位：毫秒，如果等于 0，则没有超时时间限制。
     *
     * @return SQL 执行超时时间，单位：毫秒，如果等于 0，则没有超时时间限制
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * 获得执行 Mysql 命令过慢最小时间，单位：毫秒。
     *
     * @return 执行 Mysql 命令过慢最小时间，单位：毫秒
     */
    public int getSlowExecutionThreshold() {
        return slowExecutionThreshold;
    }

    /**
     * 返回当前 {@code DataSourceConfiguration} 相关参数信息，用于日志打印。
     *
     * @return 当前 {@code DataSourceConfiguration} 相关参数信息
     */
    private String buildLogForParameters() {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("poolSize", poolSize);
        parameterMap.put("checkoutTimeout", checkoutTimeout);
        parameterMap.put("maxOccupyTime", maxOccupyTime);
        parameterMap.put("timeout", timeout);
        parameterMap.put("slowExecutionThreshold", slowExecutionThreshold);
        return LogBuildUtil.build(parameterMap);
    }

    @Override
    public String toString() {
        return "DataSourceConfiguration{" +
                "poolSize=" + poolSize +
                ", checkoutTimeout=" + checkoutTimeout +
                ", maxOccupyTime=" + maxOccupyTime +
                ", timeout=" + timeout +
                ", slowExecutionThreshold=" + slowExecutionThreshold +
                '}';
    }
}
