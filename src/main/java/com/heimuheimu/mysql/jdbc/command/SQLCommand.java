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

package com.heimuheimu.mysql.jdbc.command;

import com.heimuheimu.mysql.jdbc.ConnectionInfo;
import com.heimuheimu.mysql.jdbc.packet.MysqlPacket;
import com.heimuheimu.mysql.jdbc.packet.command.text.CommandQueryPacket;
import com.heimuheimu.mysql.jdbc.packet.generic.EOFPacket;
import com.heimuheimu.mysql.jdbc.packet.generic.ErrorPacket;
import com.heimuheimu.mysql.jdbc.packet.generic.OKPacket;

/**
 * Mysql SQL 命令，用于执行任何合法的 SQL 语句。
 *
 * <p><strong>注意：</strong>{@code SQLCommand} 不支持 "LOAD DATA INFILE" 语句。</p>
 *
 * <p><strong>说明：</strong>{@code SQLCommand} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 * @see CommandQueryPacket
 */
public class SQLCommand extends AbstractCommand {

    /**
     * SQL 语句
     */
    private final String sql;

    /**
     * 执行 SQL 语句的 Mysql 数据库连接信息
     */
    private final ConnectionInfo connectionInfo;

    /**
     * SQL 命令数据包字节数组
     */
    private final byte[] requestByteArray;

    /**
     * 已接受的 EOF 数据包数量
     */
    private volatile int receivedEOFPacketCount = 0;

    /**
     * SQL 语句执行完成后，返回的是否为 {@code TextResultsetResponsePacket} 数据包
     */
    private volatile boolean hasTextResultSet = false;

    /**
     * SQL 语句变更的记录行数
     */
    private volatile long affectedRows = 0;

    /**
     * 最后插入的主键 ID
     */
    private volatile long lastInsertId = -1;

    /**
     * SQL 语句执行出错返回的错误响应数据包
     */
    private volatile ErrorPacket errorPacket = null;

    /**
     * SQL 语句执行完成后 Mysql 服务端状态信息
     */
    private volatile MysqlServerStatusInfo serverStatusInfo = null;

    /**
     * 构造一个 Mysql SQL 命令，用于执行任何合法的 SQL 语句。
     *
     * @param sql SQL 语句，不允许为 {@code null} 或空字符串
     * @param connectionInfo 执行 SQL 语句的 Mysql 数据库连接信息，不允许为 {@code null}
     * @throws IllegalArgumentException 如果 {@code sql} 为 {@code null} 或空字符串
     * @throws IllegalArgumentException 如果 {@code connectionInfo} 为 {@code null}
     */
    public SQLCommand(String sql, ConnectionInfo connectionInfo) throws IllegalArgumentException {
        if (sql == null || sql.isEmpty()) {
            throw new IllegalArgumentException("Create SQLCommand failed: `sql is null or empty`. Sql: `" + sql
                    + "`. Connection info: `" + connectionInfo + "`.");
        }
        if (connectionInfo == null) {
            throw new IllegalArgumentException("Create SQLCommand failed: `connectionInfo is null`. Sql: `" + sql
                    + "`. Connection info: `null`.");
        }
        this.sql = sql;
        this.connectionInfo = connectionInfo;
        CommandQueryPacket commandQueryPacket = new CommandQueryPacket(sql);
        this.requestByteArray = commandQueryPacket.buildMysqlPacketBytes(connectionInfo.getJavaCharset());
    }

    @Override
    public byte[] getRequestByteArray() {
        return requestByteArray;
    }

    /**
     * 判断 SQL 语句执行完成后，返回的是否为 {@code TextResultsetResponsePacket} 数据包。
     *
     * @return 是否为 {@code TextResultsetResponsePacket} 数据包
     * @throws IllegalStateException 如果 SQL 语句尚未执行完成，将会抛出此异常
     */
    public boolean hasTextResultSet() throws IllegalStateException {
        if (!isCompleted()) {
            throw new IllegalStateException("Check has text result set failed: `command is not finished`. Sql: `" +
                    sql + "`. ConnectionInfo:`" + connectionInfo + "`.");
        }
        return hasTextResultSet;
    }

    /**
     * 获得 SQL 语句变更的记录行数，如果 SQL 为查询语句，将返回 0。
     *
     * @return SQL 语句变更的记录行数
     * @throws IllegalStateException 如果 SQL 语句尚未执行完成，将会抛出此异常
     */
    public long getAffectedRows() throws IllegalStateException {
        if (!isCompleted()) {
            throw new IllegalStateException("Get affected rows failed: `command is not finished`. Sql: `" +
                    sql + "`. ConnectionInfo:`" + connectionInfo + "`.");
        }
        return affectedRows;
    }

    /**
     * 获得 INSERT SQL 语句执行后，最后插入的主键 ID，如果 SQL 为查询语句，将返回 -1。
     *
     * @return 最后插入的主键 ID
     * @throws IllegalStateException 如果 SQL 语句尚未执行完成，将会抛出此异常
     */
    public long getLastInsertId() throws IllegalStateException {
        if (!isCompleted()) {
            throw new IllegalStateException("Get last insert id failed: `command is not finished`. Sql: `" +
                    sql + "`. ConnectionInfo:`" + connectionInfo + "`.");
        }
        return lastInsertId;
    }

    /**
     * 获得 SQL 语句执行出错返回的错误响应包数据，如果执行成功，将会返回 {@code null}。
     *
     * @return 错误响应包数据，可能为 {@code null}
     * @throws IllegalStateException 如果 SQL 语句尚未执行完成，将会抛出此异常
     */
    public ErrorPacket getErrorPacket() throws IllegalStateException {
        if (!isCompleted()) {
            throw new IllegalStateException("Get error packet failed: `command is not finished`. Sql: `" +
                    sql + "`. ConnectionInfo:`" + connectionInfo + "`.");
        }
        return errorPacket;
    }

    @Override
    protected boolean isLastPacket(MysqlPacket responsePacket) throws IllegalStateException {
        if (OKPacket.isOkPacket(responsePacket)) {
            OKPacket okPacket = OKPacket.parse(responsePacket, connectionInfo.getCapabilitiesFlags(), connectionInfo.getJavaCharset());
            affectedRows = okPacket.getAffectedRows();
            lastInsertId = okPacket.getLastInsertId();
            if (okPacket.getServerStatusFlags() != -1) {
                serverStatusInfo = new MysqlServerStatusInfo(okPacket.getServerStatusFlags());
            }
            return true;
        } else if (EOFPacket.isEOFPacket(responsePacket)) {
            receivedEOFPacketCount ++;
            if (receivedEOFPacketCount == 2) {
                EOFPacket eofPacket = EOFPacket.parse(responsePacket, connectionInfo.getCapabilitiesFlags());
                if (eofPacket.getServerStatusFlags() != -1) {
                    serverStatusInfo = new MysqlServerStatusInfo(eofPacket.getServerStatusFlags());
                }
                hasTextResultSet = true;
                return true;
            }
        } else if (ErrorPacket.isErrorPacket(responsePacket)) {
            errorPacket = ErrorPacket.parse(responsePacket, connectionInfo.getJavaCharset());
            return true;
        }
        return false;
    }

    @Override
    public MysqlServerStatusInfo getServerStatusInfo() {
        return serverStatusInfo;
    }

    @Override
    public String toString() {
        return "SQLCommand{" +
                "sql='" + sql + '\'' +
                ", receivedEOFPacketCount=" + receivedEOFPacketCount +
                ", hasTextResultSet=" + hasTextResultSet +
                ", affectedRows=" + affectedRows +
                ", lastInsertId=" + lastInsertId +
                ", errorPacket=" + errorPacket +
                ", serverStatusInfo=" + serverStatusInfo +
                '}';
    }
}
