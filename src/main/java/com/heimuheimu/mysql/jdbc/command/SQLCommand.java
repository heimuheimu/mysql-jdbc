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

import java.sql.SQLException;

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
     * @param sql SQL 语句
     * @param connectionInfo 执行 SQL 语句的 Mysql 数据库连接信息
     */
    public SQLCommand(String sql, ConnectionInfo connectionInfo) {
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
     * 获得 SQL 语句执行出错返回的错误响应包数据，如果执行成功或执行未完成，将会返回 {@code null}。
     *
     * @return 错误响应包数据，可能为 {@code null}
     */
    public ErrorPacket getErrorPacket() {
        return errorPacket;
    }

    @Override
    protected boolean isLastPacket(MysqlPacket responsePacket) throws SQLException {
        if (responsePacket.getPayload()[0] == 0xFB) {
            throw new SQLException("Receive response packet for `SQLCommand` failed: `LOCAL INFILE Request is not supported`. Sql: `" +
                    sql + "`. ConnectionInfo:`" + connectionInfo + "`. Invalid response packet:`" + responsePacket + "`.");
        }
        if (OKPacket.isOkPacket(responsePacket)) {
            OKPacket okPacket = OKPacket.parse(responsePacket, connectionInfo.getCapabilitiesFlags(), connectionInfo.getJavaCharset());
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
                ", connectionInfo=" + connectionInfo +
                '}';
    }
}
