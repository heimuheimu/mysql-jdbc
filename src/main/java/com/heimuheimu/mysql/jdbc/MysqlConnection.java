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
import java.util.UUID;
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
     * 当前连接使用的事务等级，在未实际获取前，值为 {@link Integer#MIN_VALUE}
     */
    private volatile int transactionIsolation = Integer.MIN_VALUE;

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
    public String getSchema() throws SQLException {
        checkClosed("getSchema()");
        return currentDatabaseName != null ? currentDatabaseName : "";
    }

    @Override
    public void setSchema(String schema) throws SQLException {
        checkClosed("setSchema(String schema)");
        try {
            createStatement().execute("USE " + schema);
            this.currentDatabaseName = schema;
        } catch (Exception e) {
            executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_UNEXPECTED_ERROR);
            Map<String, Object> parameterMap = new LinkedHashMap<>();
            parameterMap.put("schema", schema);
            parameterMap.putAll(getCommonParameterMap());
            String errorMessage = LogBuildUtil.buildMethodExecuteFailedLog("MysqlConnection#setSchema(String schema)",
                    "unexpected error", parameterMap);
            LOG.error(errorMessage, e);
            throw new SQLException(errorMessage, e);
        }
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
    public boolean getAutoCommit() throws SQLException {
        checkClosed("getAutoCommit()");
        return lastServerStatusInfo.isAutoCommit();
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        checkClosed("setAutoCommit(boolean autoCommit)");
        boolean currentIsAutoCommit = getAutoCommit();
        if (autoCommit != currentIsAutoCommit) {
            try {
                if (autoCommit) {
                    createStatement().execute("SET autocommit=1");
                } else {
                    createStatement().execute("SET autocommit=0");
                }
            } catch (Exception e) {
                executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_UNEXPECTED_ERROR);
                Map<String, Object> parameterMap = new LinkedHashMap<>();
                parameterMap.put("autoCommit", autoCommit);
                parameterMap.putAll(getCommonParameterMap());
                String errorMessage = LogBuildUtil.buildMethodExecuteFailedLog("MysqlConnection#setAutoCommit(boolean autoCommit)",
                        "unexpected error", parameterMap);
                LOG.error(errorMessage, e);
                throw new SQLException(errorMessage, e);
            }
        }
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        checkClosed("isReadOnly()");
        return lastServerStatusInfo.isInReadonlyTransaction();
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        checkClosed("setReadOnly(boolean readOnly)");
        boolean currentIsReadOnly = isReadOnly();
        if (readOnly != currentIsReadOnly) {
            if (mysqlChannel.getConnectionInfo().versionMeetsMinimum(5, 6, 5)) {
                try {
                    if (readOnly) {
                        createStatement().execute("SET SESSION TRANSACTION READ ONLY");
                    } else {
                        createStatement().execute("SET SESSION TRANSACTION READ WRITE");
                    }
                } catch (Exception e) {
                    executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_UNEXPECTED_ERROR);
                    Map<String, Object> parameterMap = new LinkedHashMap<>();
                    parameterMap.put("readOnly", readOnly);
                    parameterMap.putAll(getCommonParameterMap());
                    String errorMessage = LogBuildUtil.buildMethodExecuteFailedLog("MysqlConnection#setReadOnly(boolean readOnly)",
                            "unexpected error", parameterMap);
                    LOG.error(errorMessage, e);
                    throw new SQLException(errorMessage, e);
                }
            } else { // Mysql 版本小于 5.6.5 不支持 ReadOnly 设置
                if (readOnly) {
                    executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_UNEXPECTED_ERROR);
                    Map<String, Object> parameterMap = new LinkedHashMap<>();
                    parameterMap.put("readOnly", readOnly);
                    parameterMap.putAll(getCommonParameterMap());
                    String errorMessage = LogBuildUtil.buildMethodExecuteFailedLog("MysqlConnection#setReadOnly(boolean readOnly)",
                            "mysql version too low, minimal version: 5.6.5", parameterMap);
                    LOG.error(errorMessage);
                    throw new SQLException(errorMessage);
                }
            }
        }
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        checkClosed("getTransactionIsolation()");
        if (transactionIsolation == Integer.MIN_VALUE) {
            ConnectionInfo connection = mysqlChannel.getConnectionInfo();
            String variableName;
            if (connection.versionMeetsMinimum(8, 0 ,3) ||
                    (connection.versionMeetsMinimum(5, 7, 20)
                            && !connection.versionMeetsMinimum(8, 0, 0))) {
                variableName = "@@session.transaction_isolation";
            } else {
                variableName = "@@session.tx_isolation";
            }
            String transactionIsolationName = "";
            try {
                ResultSet resultSet = createStatement().executeQuery("SELECT " + variableName);
                while (resultSet.next()) {
                    transactionIsolationName = resultSet.getString(1);
                    break;
                }
            } catch (Exception e) {
                executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_UNEXPECTED_ERROR);
                String errorMessage = LogBuildUtil.buildMethodExecuteFailedLog("MysqlConnection#getTransactionIsolation()",
                        "unexpected error", getCommonParameterMap());
                LOG.error(errorMessage, e);
                throw new SQLException(errorMessage, e);
            }
            switch (transactionIsolationName) {
                case "READ-UNCOMMITED":
                case "READ-UNCOMMITTED":
                    transactionIsolation = TRANSACTION_READ_UNCOMMITTED;
                    break;
                case "READ-COMMITTED":
                    transactionIsolation = TRANSACTION_READ_COMMITTED;
                    break;
                case "REPEATABLE-READ":
                    transactionIsolation = TRANSACTION_REPEATABLE_READ;
                    break;
                case "SERIALIZABLE":
                    transactionIsolation = TRANSACTION_SERIALIZABLE;
                    break;
                default:
                    executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_UNEXPECTED_ERROR);
                    String errorMessage = LogBuildUtil.buildMethodExecuteFailedLog("MysqlConnection#getTransactionIsolation()",
                            "Could not map transaction isolation '" + transactionIsolationName + "' to a valid JDBC level", getCommonParameterMap());
                    LOG.error(errorMessage);
                    throw new SQLException(errorMessage);
            }
        }
        return transactionIsolation;
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        checkClosed("setTransactionIsolation(int level)");
        if (level != transactionIsolation) {
            String sql;
            switch (level) {
                case TRANSACTION_READ_COMMITTED:
                    sql = "SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED";
                    break;
                case TRANSACTION_READ_UNCOMMITTED:
                    sql = "SET SESSION TRANSACTION ISOLATION LEVEL READ UNCOMMITTED";
                    break;
                case TRANSACTION_REPEATABLE_READ:
                    sql = "SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ";
                    break;
                case TRANSACTION_SERIALIZABLE:
                    sql = "SET SESSION TRANSACTION ISOLATION LEVEL SERIALIZABLE";
                    break;
                default:
                    executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_UNEXPECTED_ERROR);
                    Map<String, Object> parameterMap = new LinkedHashMap<>();
                    parameterMap.put("level", level);
                    parameterMap.putAll(getCommonParameterMap());
                    String errorMessage = LogBuildUtil.buildMethodExecuteFailedLog("MysqlConnection#setTransactionIsolation(int level)",
                            "unsupported transaction isolation level '" + level + "'", parameterMap);
                    LOG.error(errorMessage);
                    throw new SQLException(errorMessage);
            }
            try {
                createStatement().execute(sql);
            } catch (Exception e) {
                transactionIsolation = Integer.MIN_VALUE; // 无法完全确定当前连接事务等级，将本地的事务等级设置为未初始化状态
                executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_UNEXPECTED_ERROR);
                Map<String, Object> parameterMap = new LinkedHashMap<>();
                parameterMap.put("level", level);
                parameterMap.put("sql", sql);
                parameterMap.putAll(getCommonParameterMap());
                String errorMessage = LogBuildUtil.buildMethodExecuteFailedLog("MysqlConnection#setTransactionIsolation(int level)",
                        "unexpected error", parameterMap);
                LOG.error(errorMessage, e);
                throw new SQLException(errorMessage, e);
            }
            this.transactionIsolation = level;
        }
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        String randomSavepointName = UUID.randomUUID().toString().replace("-", "");
        return setSavepoint(randomSavepointName);
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        String methodName = "setSavepoint(String name)";
        checkClosed(methodName);
        Savepoint savepoint = new MysqlSavepoint(name);
        checkSavepointName(methodName, savepoint);
        checkInAutoCommitMode(methodName, name);
        try {
            createStatement().execute("SAVEPOINT `" + name + "`");
            return savepoint;
        } catch (Exception e) {
            executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_UNEXPECTED_ERROR);
            Map<String, Object> parameterMap = new LinkedHashMap<>();
            parameterMap.put("savepointName", name);
            parameterMap.putAll(getCommonParameterMap());
            String errorMessage = LogBuildUtil.buildMethodExecuteFailedLog("MysqlConnection#setSavepoint(String name)",
                    "unexpected error", parameterMap);
            LOG.error(errorMessage, e);
            throw new SQLException(errorMessage, e);
        }
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        String methodName = "rollback(Savepoint savepoint)";
        checkClosed(methodName);
        checkSavepointName(methodName, savepoint);
        checkInAutoCommitMode(methodName, savepoint.getSavepointName());
        try {
            createStatement().execute("ROLLBACK TO SAVEPOINT `" + savepoint.getSavepointName() + "`");
        } catch (Exception e) {
            executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_UNEXPECTED_ERROR);
            Map<String, Object> parameterMap = new LinkedHashMap<>();
            parameterMap.put("savepointName", savepoint.getSavepointName());
            parameterMap.putAll(getCommonParameterMap());
            String errorMessage = LogBuildUtil.buildMethodExecuteFailedLog("MysqlConnection#rollback(Savepoint savepoint)",
                    "unexpected error", parameterMap);
            LOG.error(errorMessage, e);
            throw new SQLException(errorMessage, e);
        }
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        String methodName = "releaseSavepoint(Savepoint savepoint)";
        checkClosed(methodName);
        checkSavepointName(methodName, savepoint);
        checkInAutoCommitMode(methodName, savepoint.getSavepointName());
        try {
            createStatement().execute("RELEASE SAVEPOINT `" + savepoint.getSavepointName() + "`");
        } catch (Exception e) {
            executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_UNEXPECTED_ERROR);
            Map<String, Object> parameterMap = new LinkedHashMap<>();
            parameterMap.put("savepointName", savepoint.getSavepointName());
            parameterMap.putAll(getCommonParameterMap());
            String errorMessage = LogBuildUtil.buildMethodExecuteFailedLog("MysqlConnection#releaseSavepoint(Savepoint savepoint)",
                    "unexpected error", parameterMap);
            LOG.error(errorMessage, e);
            throw new SQLException(errorMessage, e);
        }
    }

    /**
     * 检查 Savepoint 名称，如果为 {@code null} 或空，将会抛出 {@link SQLException} 异常。
     *
     * @param methodName 方法名称
     * @param savepoint Savepoint 实例
     * @throws SQLException 如果 Savepoint 名称为 {@code null} 或空，将会抛出此异常
     */
    private void checkSavepointName(String methodName, Savepoint savepoint) throws SQLException {
        String savepointName = null;
        if (savepoint != null) {
            savepointName = savepoint.getSavepointName();
        }
        if (savepointName == null || savepointName.isEmpty()) {
            executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_UNEXPECTED_ERROR);
            Map<String, Object> parameterMap = new LinkedHashMap<>();
            parameterMap.put("savepointName", savepointName);
            parameterMap.putAll(getCommonParameterMap());
            String errorMessage = LogBuildUtil.buildMethodExecuteFailedLog("MysqlConnection#" + methodName,
                    "savepoint name could not be empty", parameterMap);
            LOG.error(errorMessage);
            throw new SQLException(errorMessage);
        }
    }

    /**
     * 检查当前连接是否处于 auto-commit 模式，如果是，将会抛出 {@link SQLException} 异常。
     *
     * @param methodName 方法名称
     * @param savepointName Savepoint 名称
     * @throws SQLException 如果当前连接处于 auto-commit 模式，将会抛出此异常
     */
    private void checkInAutoCommitMode(String methodName, String savepointName) throws SQLException {
        if (lastServerStatusInfo.isAutoCommit()) {
            executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_UNEXPECTED_ERROR);
            Map<String, Object> parameterMap = new LinkedHashMap<>();
            parameterMap.put("savepointName", savepointName);
            parameterMap.putAll(getCommonParameterMap());
            String errorMessage = LogBuildUtil.buildMethodExecuteFailedLog("MysqlConnection#" + methodName,
                    "connection object is currently in auto-commit mode", parameterMap);
            LOG.error(errorMessage);
            throw new SQLException(errorMessage);
        }
    }

    @Override
    public void commit() throws SQLException {
        checkClosed("commit()");
        try {
            createStatement().execute("COMMIT");
        } catch (Exception e) {
            executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_UNEXPECTED_ERROR);
            String errorMessage = LogBuildUtil.buildMethodExecuteFailedLog("MysqlConnection#commit()",
                    "unexpected error", getCommonParameterMap());
            LOG.error(errorMessage, e);
            throw new SQLException(errorMessage, e);
        }
    }

    @Override
    public void rollback() throws SQLException {
        checkClosed("rollback()");
        try {
            createStatement().execute("ROLLBACK");
        } catch (Exception e) {
            executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_UNEXPECTED_ERROR);
            String errorMessage = LogBuildUtil.buildMethodExecuteFailedLog("MysqlConnection#rollback()",
                    "unexpected error", getCommonParameterMap());
            LOG.error(errorMessage, e);
            throw new SQLException(errorMessage, e);
        }
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
        // this is a no-op
    }

    @Override
    public String getCatalog() throws SQLException {
        return "def";
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {
        // this is a no-op
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        // this is a no-op
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        // this is a no-op
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
     * @param lastServerStatusInfo 最新的 Mysql 服务端状态信息
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
     * 获得通用参数 {@code Map}，用于日志打印。
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

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlConnection#getTypeMap()");
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("MysqlConnection#setTypeMap(Map<String, Class<?>> map)");
    }
}
