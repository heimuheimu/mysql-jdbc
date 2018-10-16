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
import com.heimuheimu.mysql.jdbc.command.SQLCommand;
import com.heimuheimu.mysql.jdbc.facility.SQLFeatureNotSupportedExceptionBuilder;
import com.heimuheimu.mysql.jdbc.monitor.DatabaseMonitor;
import com.heimuheimu.mysql.jdbc.monitor.DatabaseMonitorFactory;
import com.heimuheimu.mysql.jdbc.monitor.ExecutionMonitorFactory;
import com.heimuheimu.mysql.jdbc.packet.MysqlPacket;
import com.heimuheimu.mysql.jdbc.packet.generic.ErrorPacket;
import com.heimuheimu.mysql.jdbc.result.AutoGenerateKeysResultSet;
import com.heimuheimu.mysql.jdbc.result.ReadonlyTextResultSet;
import com.heimuheimu.mysql.jdbc.util.SQLUtil;
import com.heimuheimu.naivemonitor.monitor.ExecutionMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * {@link Statement} 实现类，通过 {@link SQLCommand} 执行 SQL 语句并返回结果。
 *
 * <p><strong>说明：</strong>{@code TextStatement} 类是非线程安全的，不允许多个线程使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class TextStatement implements Statement {

    /**
     * {@code TextStatement} 错误信息日志
     */
    private static final Logger LOG = LoggerFactory.getLogger(TextStatement.class);

    /**
     * Mysql 命令慢执行日志
     */
    private static final Logger MYSQL_SLOW_EXECUTION_LOG = LoggerFactory.getLogger("MYSQL_SLOW_EXECUTION_LOG");

    /**
     * Mysql 命令 DEBUG 执行日志
     */
    private static final Logger MYSQL_EXECUTION_DEBUG_LOG = LoggerFactory.getLogger("MYSQL_EXECUTION_DEBUG_LOG");

    /**
     * 创建当前 {@code TextStatement} 实例的 Mysql 数据库连接
     */
    protected final MysqlConnection mysqlConnection;

    /**
     * 与 Mysql 服务进行数据交互的管道
     */
    protected final MysqlChannel mysqlChannel;

    /**
     * Mysql 数据库连接信息
     */
    protected final ConnectionInfo connectionInfo;

    /**
     * Mysql 命令执行信息监控器
     */
    protected final ExecutionMonitor executionMonitor;

    /**
     * SQL 查询结果，如果执行的是非查询语句，则为 {@code null}
     */
    protected volatile ReadonlyTextResultSet resultSet = null;

    /**
     * 执行 Mysql 命令过慢最小时间，单位：纳秒，不能小于等于 0，执行 Mysql 命令时间大于该值时，将进行慢执行日志打印
     */
    private final long slowExecutionThreshold;

    /**
     * SQL 执行超时时间，单位：毫秒
     */
    private volatile long queryMillisecondsTimeout = Long.MAX_VALUE;

    /**
     * SQL 语句变更的记录行数，如果执行的是查询语句，则为 -1
     */
    private volatile long affectedRows = -1;

    /**
     * 最后插入的主键 ID，如果执行的是查询语句，则为 -1
     */
    private volatile long lastInsertId = -1;

    /**
     * 获取行数据顺序
     */
    private volatile int fetchDirection = ResultSet.FETCH_FORWARD;

    /**
     * Mysql 数据库信息监控器
     */
    private final DatabaseMonitor databaseMonitor;

    /**
     * 构造一个 {@code TextStatement} 实例。
     *
     * @param mysqlConnection 创建当前 {@code TextStatement} 实例的 Mysql 数据库连接
     * @param executionMonitor Mysql 命令执行信息监控器，不允许为 {@code null}
     * @param slowExecutionThreshold 执行 Mysql 命令过慢最小时间，单位：纳秒，不能小于等于 0
     */
    public TextStatement(MysqlConnection mysqlConnection, ExecutionMonitor executionMonitor, long slowExecutionThreshold) {
        this.mysqlConnection = mysqlConnection;
        this.mysqlChannel = mysqlConnection.getMysqlChannel();
        this.connectionInfo = mysqlConnection.getMysqlChannel().getConnectionInfo();
        this.executionMonitor = executionMonitor;
        this.slowExecutionThreshold = slowExecutionThreshold;
        ConnectionConfiguration configuration = mysqlConnection.getMysqlChannel().getConnectionConfiguration();
        this.databaseMonitor = DatabaseMonitorFactory.get(configuration.getHost(), configuration.getDatabaseName());
    }

    @Override
    public long getLargeMaxRows() throws SQLException {
        return 0; // no limit
    }

    @Override
    public int getMaxRows() throws SQLException {
        return 0; // no limit
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        return 0; // no limit
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        if (queryMillisecondsTimeout == Long.MAX_VALUE) { // 如果没有超时时间，则返回 0
            return 0;
        } else {
            long seconds = queryMillisecondsTimeout / 1000;
            int intSeconds = seconds > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) seconds;
            return Math.max(1, intSeconds);
        }
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        setQueryMillisecondsTimeout(seconds * 1000);
    }

    /**
     * 获得 SQL 执行超时时间，单位：毫秒。
     *
     * @return SQL 执行超时时间，单位：毫秒
     */
    public long getQueryMillisecondsTimeout() {
        return queryMillisecondsTimeout;
    }

    /**
     * 设置 SQL 执行超时时间，单位：毫秒，如果等于 0，则没有超时时间限制，不允许设置小于 0 的值。
     *
     * @param queryMillisecondsTimeout SQL 执行超时时间，单位：毫秒，如果等于 0，则没有超时时间限制，不允许设置小于 0 的值
     * @throws SQLException 如果 SQL 执行超时时间小于 0，将会抛出此异常
     */
    public void setQueryMillisecondsTimeout(long queryMillisecondsTimeout) throws SQLException {
        if (queryMillisecondsTimeout > 0) {
            this.queryMillisecondsTimeout = queryMillisecondsTimeout;
        } else if (queryMillisecondsTimeout == 0) {
            this.queryMillisecondsTimeout = Long.MAX_VALUE;
        } else {
            executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_UNEXPECTED_ERROR);
            String errorMessage = "Set query timeout failed: `could not less than zero`. invalidQueryTimeout: `"
                    + queryMillisecondsTimeout + "ms`. Connection info: `" + connectionInfo + "`. Host: `"
                    + mysqlChannel.getConnectionConfiguration().getHost() + "`.";
            LOG.error(errorMessage);
            throw new SQLException(errorMessage);
        }
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        long startTime = System.nanoTime();
        try {
            reset();
            SQLCommand sqlCommand = new SQLCommand(sql, connectionInfo);
            List<MysqlPacket> mysqlPacketList = mysqlChannel.send(sqlCommand, queryMillisecondsTimeout);
            ErrorPacket errorPacket = sqlCommand.getErrorPacket();
            if (errorPacket != null) {
                String errorMessage = "Execute sql failed: `" + errorPacket.getErrorMessage() + "`. Error code: `"
                        + errorPacket.getErrorCode() + "`. Sql state: `" + errorPacket.getSqlState() + "`. Sql: `" + sql
                        + "`. Connection info: `" + connectionInfo + "`. Host: `"
                        + mysqlChannel.getConnectionConfiguration().getHost() + ".";
                throw new SQLException(errorMessage, errorPacket.getSqlState(), errorPacket.getErrorCode());
            }
            if (sqlCommand.hasTextResultSet()) {
                resultSet = new ReadonlyTextResultSet(mysqlPacketList, connectionInfo, this, executionMonitor);
                if (fetchDirection != ResultSet.FETCH_FORWARD) {
                    resultSet.setFetchDirection(fetchDirection);
                }
                mysqlConnection.setLastServerStatusInfo(sqlCommand.getServerStatusInfo());
                databaseMonitor.onSelectExecuted(resultSet.getRowsSize());
                if (MYSQL_EXECUTION_DEBUG_LOG.isDebugEnabled()) { // print debug log
                    StringBuilder queryResult = new StringBuilder();
                    int columnCount = resultSet.getMetaData().getColumnCount();
                    while (resultSet.next()) {
                        queryResult.append("[");
                        for (int i = 1; i <= columnCount; i++) {
                            if (i > 1) {
                                queryResult.append(", ");
                            }
                            queryResult.append("`").append(resultSet.getObject(i)).append("`");
                        }
                        queryResult.append("]\n\r");
                    }
                    MYSQL_EXECUTION_DEBUG_LOG.debug("[{}] {}\n\r{}\n\rRows size: {}\n\r{}", mysqlConnection.getSchema(),
                            sql.replace('\n', ' ').replace('\r', ' '),
                            sqlCommand.getServerStatusInfo(), resultSet.getRowsSize(), queryResult.toString());
                }
                return true;
            } else {
                affectedRows = sqlCommand.getAffectedRows();
                lastInsertId = sqlCommand.getLastInsertId();
                mysqlConnection.setLastServerStatusInfo(sqlCommand.getServerStatusInfo());
                databaseMonitor.onExecuted(SQLUtil.getSQLType(sql), sqlCommand.getAffectedRows());
                if (MYSQL_EXECUTION_DEBUG_LOG.isDebugEnabled()) {
                    MYSQL_EXECUTION_DEBUG_LOG.debug("[{}] {}\n\r{}\n\rAffected rows: {}. Last insert id: {}.", mysqlConnection.getSchema(),
                            sql.replace('\n', ' ').replace('\r', ' '),
                            sqlCommand.getServerStatusInfo(), affectedRows, lastInsertId);
                }
                return false;
            }
        } catch (IllegalStateException e) {
            executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_ILLEGAL_STATE);
            String errorMessage = "Execute sql failed: `illegal state`. Cost: `" + (System.nanoTime() - startTime) + "ns`. Sql: `"
                    + sql + "`. Connection info: `" + connectionInfo + "`. Host: `"
                    + mysqlChannel.getConnectionConfiguration().getHost() + "`.";
            LOG.error(errorMessage, e);
            throw new SQLException(errorMessage, e);
        } catch (SQLTimeoutException e) {
            executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_TIMEOUT);
            LOG.error("Execute sql failed: `timeout`. Cost: `" + (System.nanoTime() - startTime) + "ns`. Sql: `"
                    + sql + "`. Query timeout: `" + queryMillisecondsTimeout + "ms`. Connection info: `" + connectionInfo
                    + "`. Host: `" + mysqlChannel.getConnectionConfiguration().getHost() + "`.", e);
            throw e;
        } catch (SQLException e) {
            executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_MYSQL_ERROR);
            LOG.error("Execute sql failed: `sql exception`. Cost: `" + (System.nanoTime() - startTime) + "ns`. Sql: `"
                    + sql + "`. Connection info: `" + connectionInfo + "`. Host: `"
                    + mysqlChannel.getConnectionConfiguration().getHost() + "`.", e);
            throw e;
        } catch (Exception e) {
            executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_UNEXPECTED_ERROR);
            String errorMessage = "Execute sql failed: `unexpected exception`. Sql: `" + sql + "`. Connection info: `"
                    + connectionInfo + "`. Host: `" + mysqlChannel.getConnectionConfiguration().getHost() + "`.";
            LOG.error(errorMessage, e);
            throw new SQLException(errorMessage, e);
        } finally {
            long executedNanoTime = System.nanoTime() - startTime;
            if (executedNanoTime > slowExecutionThreshold) {
                MYSQL_SLOW_EXECUTION_LOG.info("`Cost`:`{}ns ({}ms)`. `Sql`:`{}`. `Database`:`{}`. `Host`:`{}`. `{}`.", executedNanoTime,
                        TimeUnit.MILLISECONDS.convert(executedNanoTime, TimeUnit.NANOSECONDS), sql, mysqlConnection.getSchema(),
                        mysqlChannel.getConnectionConfiguration().getHost(), mysqlConnection.getLastServerStatusInfo());
            }
            executionMonitor.onExecuted(startTime);
        }
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        return execute(sql);
    }

    /**
     * 清空上一次 SQL 执行的状态。
     */
    private void reset() {
        affectedRows = -1;
        lastInsertId = -1;
        resultSet = null;
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        execute(sql);
        if (resultSet == null) {
            executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_UNEXPECTED_ERROR);
            String errorMessage = "Execute query sql failed: `not query sql`. Sql: `" + sql + "`. Connection info: `"
                    + connectionInfo + "`. Host: `" + mysqlChannel.getConnectionConfiguration().getHost() + "`.";
            LOG.error(errorMessage);
            throw new SQLException(errorMessage);
        }
        return resultSet;
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        return resultSet;
    }

    @Override
    public long executeLargeUpdate(String sql) throws SQLException {
        execute(sql);
        if (affectedRows == -1) {
            executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_UNEXPECTED_ERROR);
            String errorMessage = "Execute update sql failed: `not update sql`. Sql: `" + sql + "`. Connection info: `"
                    + connectionInfo + "`. Host: `" + mysqlChannel.getConnectionConfiguration().getHost() + "`.";
            LOG.error(errorMessage);
            throw new SQLException(errorMessage);
        }
        return affectedRows;
    }

    @Override
    public long executeLargeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return executeLargeUpdate(sql);
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        long affectedRows = executeLargeUpdate(sql);
        return affectedRows > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) affectedRows;
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return executeUpdate(sql);
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return new AutoGenerateKeysResultSet(lastInsertId, this);
    }

    @Override
    public long getLargeUpdateCount() throws SQLException {
        return affectedRows;
    }

    @Override
    public int getUpdateCount() throws SQLException {
        return affectedRows > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) affectedRows;
    }

    @Override
    public void close() throws SQLException {
        reset();
    }

    @Override
    public void cancel() throws SQLException {
        reset();
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        // this is a no-op
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
    public void setCursorName(String name) throws SQLException {
        // this is a no-op
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        if (direction == ResultSet.FETCH_REVERSE) {
            fetchDirection = ResultSet.FETCH_REVERSE;
        } else {
            fetchDirection = ResultSet.FETCH_FORWARD;
        }
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return fetchDirection;
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        // this is a no-op
    }

    @Override
    public int getFetchSize() throws SQLException {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return ResultSet.CONCUR_READ_ONLY;
    }

    @Override
    public int getResultSetType() throws SQLException {
        return ResultSet.TYPE_SCROLL_INSENSITIVE;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return mysqlConnection;
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return false; // always false
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        return false; // always false
    }

    @Override
    public void closeOnCompletion() throws SQLException {
        // this is a no-op
    }

    @Override
    public boolean isClosed() throws SQLException {
        return false;
    }

    @Override
    public boolean isPoolable() throws SQLException {
        return false;
    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return (T) this;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return TextStatement.class == iface;
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("TextStatement#execute(String sql, int[] columnIndexes)");
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("TextStatement#execute(String sql, String[] columnNames)");
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("TextStatement#executeUpdate(String sql, int[] columnIndexes)");
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("TextStatement#executeUpdate(String sql, String[] columnNames)");
    }

    @Override
    public long executeLargeUpdate(String sql, int[] columnIndexes) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("TextStatement#executeLargeUpdate(String sql, int[] columnIndexes)");
    }

    @Override
    public long executeLargeUpdate(String sql, String[] columnNames) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("TextStatement#executeLargeUpdate(String sql, String[] columnNames)");
    }

    @Override
    public long[] executeLargeBatch() throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("TextStatement#executeLargeBatch()",
                "mysql-jdbc does not support batch statements.");
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("TextStatement#addBatch(String sql)",
                "mysql-jdbc does not support batch statements.");
    }

    @Override
    public void clearBatch() throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("TextStatement#clearBatch()",
                "mysql-jdbc does not support batch statements.");
    }

    @Override
    public int[] executeBatch() throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("TextStatement#executeBatch()",
                "mysql-jdbc does not support batch statements.");
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("TextStatement#setMaxRows(int max)");
    }

    @Override
    public void setLargeMaxRows(long max) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("TextStatement#setLargeMaxRows(long max)");
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("TextStatement#setMaxFieldSize(int max)");
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        if (poolable) {
            throw SQLFeatureNotSupportedExceptionBuilder.build("TextStatement#setPoolable(boolean poolable)",
                    "Pool TextStatement could not improve any performance. Please don't do that.");
        }
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        throw SQLFeatureNotSupportedExceptionBuilder.build("TextStatement#getResultSetHoldability()");
    }
}
