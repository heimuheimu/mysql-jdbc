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
import com.heimuheimu.mysql.jdbc.monitor.ExecutionMonitorFactory;
import com.heimuheimu.mysql.jdbc.packet.MysqlPacket;
import com.heimuheimu.mysql.jdbc.packet.generic.ErrorPacket;
import com.heimuheimu.mysql.jdbc.result.ReadonlyTextResultSet;
import com.heimuheimu.naivemonitor.monitor.ExecutionMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * {@link Statement} 实现类，通过 {@link SQLCommand} 执行 SQL 语句并返回结果。
 *
 * @author heimuheimu
 */
public class TextStatement implements Statement {

    private static final Logger LOG = LoggerFactory.getLogger(TextStatement.class);

    /**
     * Mysql 命令慢执行日志
     */
    protected static final Logger MYSQL_SLOW_EXECUTION_LOG = LoggerFactory.getLogger("MYSQL_SLOW_EXECUTION_LOG");

    /**
     * 与 Mysql 服务进行数据交互的管道
     */
    private final MysqlChannel mysqlChannel;

    /**
     * Mysql 数据库连接信息
     */
    private final ConnectionInfo connectionInfo;

    /**
     * Mysql 命令执行信息监控器
     */
    private final ExecutionMonitor executionMonitor;

    /**
     * 执行 Mysql 命令过慢最小时间，单位：纳秒，不能小于等于 0，执行 Mysql 命令时间大于该值时，将进行慢执行日志打印
     */
    private final long slowExecutionThreshold;

    /**
     * SQL 执行超时时间，单位：秒，如果等于 0，则不进行超时时间设置
     */
    private volatile int queryTimeout = 0;

    /**
     * SQL 语句变更的记录行数，如果执行的是查询语句，则为 0
     */
    private volatile long affectedRows = 0;

    /**
     * 最后插入的主键 ID，如果执行的是查询语句，则为 -1
     */
    private volatile long lastInsertId = -1;

    /**
     * SQL 查询结果，如果执行的是非查询语句，则为 {@code null}
     */
    private volatile ResultSet resultSet = null;

    /**
     * 构造一个 {@code TextStatement} 实例。
     *
     * @param mysqlChannel 与 Mysql 服务进行数据交互的管道，不允许为 {@code null}
     * @param connectionInfo Mysql 数据库连接信息，不允许为 {@code null}
     * @param executionMonitor Mysql 命令执行信息监控器，不允许为 {@code null}
     * @param slowExecutionThreshold 执行 Mysql 命令过慢最小时间，单位：纳秒，不能小于等于 0
     */
    public TextStatement(MysqlChannel mysqlChannel, ConnectionInfo connectionInfo,
                         ExecutionMonitor executionMonitor, long slowExecutionThreshold) {
        this.mysqlChannel = mysqlChannel;
        this.connectionInfo = connectionInfo;
        this.executionMonitor = executionMonitor;
        this.slowExecutionThreshold = slowExecutionThreshold;
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
        return queryTimeout;
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        if (seconds < 0) {
            String errorMessage = "Set query timeout failed: `could not less than zero`. invalidQueryTimeout: `"
                    + seconds + "`. Connection info: `" + connectionInfo + "`.";
            LOG.error(errorMessage);
            throw new SQLException(errorMessage);
        }
        this.queryTimeout = seconds;
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        long startTime = System.nanoTime();
        try {
            reset();
            SQLCommand sqlCommand = new SQLCommand(sql, connectionInfo);
            long millisecondTimeout = queryTimeout > 0 ? queryTimeout * 1000 : Long.MAX_VALUE;
            List<MysqlPacket> mysqlPacketList = mysqlChannel.send(sqlCommand, millisecondTimeout);
            ErrorPacket errorPacket = sqlCommand.getErrorPacket();
            if (errorPacket != null) {
                String errorMessage = "Execute sql failed: `" + errorPacket.getErrorMessage() + "`. Error code: `"
                        + errorPacket.getErrorCode() + "`. Sql state: `" + errorPacket.getSqlState() + "`. Sql: `" + sql
                        + "`. Connection info: `" + connectionInfo + "`.";
                throw new SQLException(errorMessage, errorPacket.getSqlState(), errorPacket.getErrorCode());
            }
            if (sqlCommand.hasTextResultSet()) {
                resultSet = new ReadonlyTextResultSet(mysqlPacketList, connectionInfo);
                return true;
            } else {
                affectedRows = sqlCommand.getAffectedRows();
                lastInsertId = sqlCommand.getLastInsertId();
                return false;
            }
        } catch (IllegalStateException e) {
            executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_ILLEGAL_STATE);
            String errorMessage = "Execute sql failed: `illegal state`. Cost: `" + (System.nanoTime() - startTime) + "ns`. Sql: `"
                    + sql + "`. Connection info: `" + connectionInfo + "`.";
            LOG.error(errorMessage, e);
            throw new SQLException(errorMessage, e);
        } catch (SQLTimeoutException e) {
            executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_TIMEOUT);
            LOG.error("Execute sql failed: `timeout`. Cost: `" + (System.nanoTime() - startTime) + "ns`. Sql: `"
                    + sql + "`. Query timeout: `" + queryTimeout + "s`. Connection info: `" + connectionInfo + "`.", e);
            throw e;
        } catch (SQLException e) {
            executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_MYSQL_ERROR);
            LOG.error("Execute sql failed: `sql exception`. Cost: `" + (System.nanoTime() - startTime) + "ns`. Sql: `"
                    + sql + "`. Connection info: `" + connectionInfo + "`.", e);
            throw e;
        } catch (Exception e) {
            executionMonitor.onError(ExecutionMonitorFactory.ERROR_CODE_UNEXPECTED_ERROR);
            String errorMessage = "Execute sql failed: `unexpected exception`. Sql: `" + sql + "`. Connection info: `"
                    + connectionInfo + "`.";
            LOG.error(errorMessage, e);
            throw new SQLException(errorMessage, e);
        } finally {
            long executedNanoTime = System.nanoTime() - startTime;
            if (executedNanoTime > slowExecutionThreshold) {
                MYSQL_SLOW_EXECUTION_LOG.info("`Cost`:`{}ns ({}ms)`. `Sql`:`{}`. `Database`:`{}`. `Host`:`{}`.", executedNanoTime,
                        TimeUnit.MILLISECONDS.convert(executedNanoTime, TimeUnit.NANOSECONDS), sql, connectionInfo.getDatabaseName(),
                        mysqlChannel.getConnectionConfiguration().getHost());
            }
            executionMonitor.onExecuted(startTime);
        }
    }

    /**
     * 清空上一次 SQL 执行的状态。
     */
    private void reset() {
        affectedRows = 0;
        lastInsertId = -1;
        resultSet = null;
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        return null;
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        long affectedRows = executeLargeUpdate(sql);
        return affectedRows > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) affectedRows;
    }

    @Override
    public long executeLargeUpdate(String sql) throws SQLException {
        return 0;
    }

    @Override
    public long getLargeUpdateCount() throws SQLException {
        return 0;
    }

    @Override
    public long executeLargeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return 0;
    }

    @Override
    public long executeLargeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return 0;
    }

    @Override
    public long executeLargeUpdate(String sql, String[] columnNames) throws SQLException {
        return 0;
    }

    @Override
    public void close() throws SQLException {
        reset();
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {

    }

    @Override
    public void cancel() throws SQLException {

    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {

    }

    @Override
    public void setCursorName(String name) throws SQLException {

    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        return resultSet;
    }

    @Override
    public int getUpdateCount() throws SQLException {
        return 0;
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return false;
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {

    }

    @Override
    public int getFetchDirection() throws SQLException {
        return 0;
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {

    }

    @Override
    public int getFetchSize() throws SQLException {
        return 0;
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return 0;
    }

    @Override
    public int getResultSetType() throws SQLException {
        return 0;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return null;
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        return false;
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return null;
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return 0;
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return 0;
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        return 0;
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        return false;
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        return false;
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        return false;
    }

    @Override
    public void closeOnCompletion() throws SQLException {
        // this method has no effect
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

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return (T) this;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return TextStatement.class == iface;
    }
}
