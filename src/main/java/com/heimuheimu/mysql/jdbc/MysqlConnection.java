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
     * 当前连接使用的数据库名称
     */
    private volatile String currentDatabaseName;

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
        this.mysqlChannel.init();
        this.lastServerStatusInfo = new MysqlServerStatusInfo(mysqlChannel.getConnectionInfo().getServerStatusFlags());
        this.executionMonitor = ExecutionMonitorFactory.get(configuration.getHost(), configuration.getDatabaseName());
        this.timeout = timeout;
        this.slowExecutionThreshold = TimeUnit.NANOSECONDS.convert(slowExecutionThreshold, TimeUnit.MILLISECONDS); // 将毫秒转换为纳秒
        this.currentDatabaseName = mysqlChannel.getConnectionInfo().getDatabaseName();
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        checkClosed("setNetworkTimeout(Executor executor, int milliseconds)");
        this.timeout = milliseconds;
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        checkClosed("getNetworkTimeout()");
        return timeout > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) timeout;
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
    public void setSchema(String schema) throws SQLException {
        checkClosed("setSchema(String schema)");
        executeSql("USE " + schema);
        this.currentDatabaseName = schema;
    }

    @Override
    public String getSchema() throws SQLException {
        checkClosed("getSchema()");
        return currentDatabaseName != null ? currentDatabaseName : "";
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
        checkClosed("createStatement(int resultSetType, int resultSetConcurrency)");
        if (resultSetType != ResultSet.TYPE_SCROLL_INSENSITIVE || resultSetConcurrency != ResultSet.CONCUR_READ_ONLY) {
            executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_UNEXPECTED_ERROR);
            Map<String, Object> parameterMap = new LinkedHashMap<>();
            parameterMap.put("resultSetType", resultSetType);
            parameterMap.put("resultSetConcurrency", resultSetConcurrency);
            parameterMap.putAll(getCommonParameterMap());
            String errorMessage = LogBuildUtil.buildMethodExecuteFailedLog("MysqlConnection#createStatement(int resultSetType, int resultSetConcurrency)",
                    "invalid resultSetType or resultSetConcurrency", parameterMap);
            LOG.error(errorMessage);
            throw new SQLException(errorMessage);
        }
        return createStatement();
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        checkClosed("prepareStatement(String sql)");
        TextPreparedStatement preparedStatement = new TextPreparedStatement(sql, this, executionMonitor, slowExecutionThreshold);
        preparedStatement.setQueryMillisecondsTimeout(timeout);
        return preparedStatement;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        checkClosed("prepareStatement(String sql, int autoGeneratedKeys)");
        return prepareStatement(sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        checkClosed("prepareStatement(String sql, int resultSetType, int resultSetConcurrency)");
        if (resultSetType != ResultSet.TYPE_SCROLL_INSENSITIVE || resultSetConcurrency != ResultSet.CONCUR_READ_ONLY) {
            executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_UNEXPECTED_ERROR);
            Map<String, Object> parameterMap = new LinkedHashMap<>();
            parameterMap.put("resultSetType", resultSetType);
            parameterMap.put("resultSetConcurrency", resultSetConcurrency);
            parameterMap.putAll(getCommonParameterMap());
            String errorMessage = LogBuildUtil.buildMethodExecuteFailedLog("MysqlConnection#prepareStatement(String sql, int resultSetType, int resultSetConcurrency)",
                    "invalid resultSetType or resultSetConcurrency", parameterMap);
            LOG.error(errorMessage);
            throw new SQLException(errorMessage);
        }
        return prepareStatement(sql);
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        checkClosed("nativeSQL(String sql)");
        return sql;
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        checkClosed("setReadOnly(boolean readOnly)");
        boolean currentIsReadOnly = isReadOnly();
        if (readOnly != currentIsReadOnly) {
            if (mysqlChannel.getConnectionInfo().versionMeetsMinimum(5, 6, 5)) {
                if (readOnly) {
                    executeSql("SET TRANSACTION READ ONLY");
                } else {
                    executeSql("SET TRANSACTION READ WRITE");
                }
            } else { // Mysql 版本小于 5.6.5 不支持 ReadOnly 设置
                if (readOnly) {
                    executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_UNEXPECTED_ERROR);
                    String errorMessage = LogBuildUtil.buildMethodExecuteFailedLog("MysqlConnection#setReadOnly(boolean readOnly)",
                            "mysql version too low, minimal version: 5.6.5", getCommonParameterMap());
                    LOG.error(errorMessage);
                    throw new SQLException(errorMessage);
                }
            }
        }
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        checkClosed("isReadOnly()");
        return lastServerStatusInfo.isInReadonlyTransaction();
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        checkClosed("setAutoCommit(boolean autoCommit)");
        boolean currentIsAutoCommit = getAutoCommit();
        if (autoCommit != currentIsAutoCommit) {
            if (autoCommit) {
                executeSql("SET autocommit=1");
            } else {
                executeSql("SET autocommit=0");
            }
        }
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        checkClosed("getAutoCommit()");
        return lastServerStatusInfo.isAutoCommit();
    }

    @Override
    public void commit() throws SQLException {
        checkClosed("commit()");
        executeSql("COMMIT");
    }

    @Override
    public void rollback() throws SQLException {
        checkClosed("rollback()");
        executeSql("ROLLBACK");
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        close();
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

    private void executeSql(String sql) throws SQLException {
        Statement statement = createStatement();
        statement.execute(sql);
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
     * @throws SQLException 如果当前 Mysql 连接已关闭，则抛出 {@code SQLException} 异常
     */
    private void checkClosed(String methodName) throws SQLException {
        if (isClosed()) {
            executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_ILLEGAL_STATE);
            Map<String, Object> parameterMap = getCommonParameterMap();
            String errorMessage = LogBuildUtil.buildMethodExecuteFailedLog("MysqlConnection#" + methodName,
                    "Mysql connection is closed", parameterMap);
            LOG.error(errorMessage);
            throw new SQLException(errorMessage);
        }
    }

    /**
     * 获得通用参数 {@code Map}。
     *
     * @return 通用参数 {@code Map}
     */
    private Map<String, Object> getCommonParameterMap() {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("timeout", timeout);
        parameterMap.put("slowExecutionThreshold", slowExecutionThreshold);
        parameterMap.put("mysqlChannel", mysqlChannel);
        return parameterMap;
    }
}
