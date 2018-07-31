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

import com.heimuheimu.mysql.jdbc.channel.MysqlChannel;
import com.heimuheimu.mysql.jdbc.command.MysqlServerStatusInfo;
import com.heimuheimu.mysql.jdbc.facility.SQLFeatureNotSupportedExceptionBuilder;
import com.heimuheimu.mysql.jdbc.facility.UnusableServiceNotifier;
import com.heimuheimu.mysql.jdbc.facility.parameter.ConstructorParameterChecker;
import com.heimuheimu.mysql.jdbc.facility.parameter.Parameters;
import com.heimuheimu.mysql.jdbc.monitor.ExecutionMonitorFactory;
import com.heimuheimu.mysql.jdbc.net.BuildSocketException;
import com.heimuheimu.mysql.jdbc.util.LogBuildUtil;
import com.heimuheimu.naivemonitor.monitor.ExecutionMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * Mysql 数据库连接。
 *
 * <p><strong>说明：</strong>{@code MysqlConnection} 类是非线程安全的，不允许多个线程使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class MysqlConnection implements Connection {

    /**
     * {@code MysqlConnection} 错误日志
     */
    private static final Logger LOG = LoggerFactory.getLogger(MysqlConnection.class);

    /**
     * 与 Mysql 服务进行数据交互的管道
     */
    private final MysqlChannel mysqlChannel;

    /**
     * 当前连接最新的 Mysql 服务端状态信息
     */
    private volatile MysqlServerStatusInfo lastServerStatusInfo;

    /**
     * 当前连接使用的 SQL 操作执行信息监控器
     */
    private final ExecutionMonitor executionMonitor;

    /**
     * SQL 执行超时时间，单位：毫秒，如果等于 0，则没有超时时间限制
     */
    private volatile long timeout;

    /**
     * 执行 Mysql 命令过慢最小时间，单位：纳秒
     */
    private final long slowExecutionThreshold;

    /**
     * 构造一个 Mysql 数据库连接。
     *
     * @param configuration 建立 Mysql 数据库连接使用的配置信息，不允许为 {@code null}
     * @param timeout SQL 执行超时时间，单位：毫秒，如果等于 0，则没有超时时间限制，不允许设置小于 0 的值
     * @param slowExecutionThreshold 执行 Mysql 命令过慢最小时间，单位：毫秒，不能小于等于 0
     * @param unusableServiceNotifier {@code MysqlChannel} 不可用通知器，允许为 {@code null}
     * @throws IllegalArgumentException 如果 {@code configuration} 为 {@code null}，将会抛出此异常
     * @throws IllegalArgumentException 如果 {@code timeout} 小于 0，将会抛出此异常
     * @throws IllegalArgumentException 如果 {@code slowExecutionThreshold} 小于等于 0，将会抛出此异常
     * @throws BuildSocketException 如果创建与 Mysql 服务器的 Socket 连接失败，将会抛出此异常
     */
    public MysqlConnection(ConnectionConfiguration configuration, int timeout, int slowExecutionThreshold,
                           UnusableServiceNotifier<MysqlChannel> unusableServiceNotifier)
            throws IllegalArgumentException, BuildSocketException {
        ConstructorParameterChecker checker = new ConstructorParameterChecker("MysqlConnection", LOG);
        checker.addParameter("configuration", configuration);
        checker.addParameter("timeout", timeout);
        checker.addParameter("slowExecutionThreshold", slowExecutionThreshold);
        checker.addParameter("unusableServiceNotifier", unusableServiceNotifier);

        checker.check("configuration", "isNull", Parameters::isNull);
        checker.check("timeout", "isLessThanZero", Parameters::isLessThanZero);
        checker.check("slowExecutionThreshold", "isEqualOrLessThanZero", Parameters::isEqualOrLessThanZero);

        this.mysqlChannel = new MysqlChannel(configuration, unusableServiceNotifier);
        this.lastServerStatusInfo = new MysqlServerStatusInfo(mysqlChannel.getConnectionInfo().getServerStatusFlags());
        this.executionMonitor = ExecutionMonitorFactory.get(configuration.getHost(), configuration.getDatabaseName());
        this.timeout = timeout;
        this.slowExecutionThreshold = TimeUnit.NANOSECONDS.convert(slowExecutionThreshold, TimeUnit.MILLISECONDS); // 将毫秒转换为纳
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        return getMysqlChannel().isAvailable();
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        checkClosed("getMetaData()");
        return new MysqlDatabaseMetaData(this);
    }

    @Override
    public Statement createStatement() throws SQLException {
        checkClosed("createStatement()");
        TextStatement statement = new TextStatement(this, executionMonitor, slowExecutionThreshold);
        statement.setQueryMillisecondsTimeout(timeout);
        return statement;
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        if (resultSetType != ResultSet.TYPE_SCROLL_INSENSITIVE) {

        }
        return createStatement();
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return null;
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        checkClosed("createStatement()");
        return sql;
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {

    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return lastServerStatusInfo.isAutoCommit();
    }

    @Override
    public void commit() throws SQLException {

    }

    @Override
    public void rollback() throws SQLException {

    }

    @Override
    public void close() throws SQLException {
        mysqlChannel.close();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return !mysqlChannel.isAvailable();
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {

    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return false;
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        // do nothing
    }

    @Override
    public String getCatalog() throws SQLException {
        return "def";
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {

    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        return 0;
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {

    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return null;
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {

    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        return null;
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        return null;
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {

    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {

    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return null;
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {

    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {

    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        return null;
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        return null;
    }

    @Override
    public void setSchema(String schema) throws SQLException {

    }

    @Override
    public String getSchema() throws SQLException {
        return null;
    }

    @Override
    public void abort(Executor executor) throws SQLException {

    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {

    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        return 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return (T) this;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return MysqlConnection.class == iface;
    }

    @Override
    public Clob createClob() throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlConnection#createClob()");
    }

    @Override
    public Blob createBlob() throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlConnection#createBlob()");
    }

    @Override
    public NClob createNClob() throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlConnection#createNClob()");
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlConnection#createSQLXML()");
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlConnection#createArrayOf(String typeName, Object[] elements)");
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlConnection#createStruct(String typeName, Object[] attributes)");
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlConnection#setHoldability(int holdability)");
    }

    @Override
    public int getHoldability() throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlConnection#getHoldability()");
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlConnection#createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)");
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlConnection#prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)");
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlConnection#prepareStatement(String sql, int[] columnIndexes)");
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlConnection#prepareStatement(String sql, String[] columnNames)");
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlConnection#prepareCall(String sql)");
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlConnection#prepareCall(String sql, int resultSetType, int resultSetConcurrency)");
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlConnection#prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)");
    }

    /**
     * 获得当前连接使用的与 Mysql 服务进行数据交互的管道。
     *
     * @return 与 Mysql 服务进行数据交互的管道
     */
    public MysqlChannel getMysqlChannel() {
        return mysqlChannel;
    }

    /**
     * 获得当前连接最新的 Mysql 服务端状态信息。
     *
     * @return 当前连接最新的 Mysql 服务端状态信息
     */
    public MysqlServerStatusInfo getLastServerStatusInfo() {
        return lastServerStatusInfo;
    }

    /**
     * 设置当前连接最新的 Mysql 服务端状态信息。
     *
     * @param lastServerStatusInfo
     */
    public void setLastServerStatusInfo(MysqlServerStatusInfo lastServerStatusInfo) {
        this.lastServerStatusInfo = lastServerStatusInfo;
    }

    /**
     * 检查当前 Mysql 连接是否已关闭，如果已关闭，则抛出 {@code SQLException} 异常。
     *
     * @param methodName 调用该检查方法的方法名
     * @throws SQLException 如果当前 Mysql 连接已关闭，则抛出 {@code SQLException} 异常。
     */
    private void checkClosed(String methodName) throws SQLException {
        if (isClosed()) {
            Map<String, Object> parameterMap = new LinkedHashMap<>();
            parameterMap.put("timeout", timeout);
            parameterMap.put("slowExecutionThreshold", slowExecutionThreshold);
            parameterMap.put("mysqlChannel", mysqlChannel);
            String errorMessage = LogBuildUtil.buildMethodExecuteFailedLog("MysqlConnection#" + methodName,
                    "Mysql connection is closed", parameterMap);
            LOG.error(errorMessage);
            throw new SQLException(errorMessage);
        }
    }
}
