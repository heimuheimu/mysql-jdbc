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

public class SQLCommand extends AbstractCommand {

    /**
     * SQL 命令
     */
    private final String sql;

    /**
     * 执行当前 SQL 命令的 Mysql 数据库连接信息
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
     * @param sql SQL 命令
     * @param connectionInfo 执行当前 SQL 命令的 Mysql 数据库连接信息
     */
    public SQLCommand(String sql, ConnectionInfo connectionInfo) {
        this.sql = sql;
        this.connectionInfo = connectionInfo;
        CommandQueryPacket commandQueryPacket = new CommandQueryPacket(sql);
        this.requestByteArray = commandQueryPacket.buildMysqlPacketBytes(connectionInfo.getJavaCharset());
    }

    @Override
    protected boolean isLastPacket(MysqlPacket responsePacket) throws SQLException {
        if (responsePacket.getPayload()[0] == 0xFB) {
            throw new SQLException("Receive response packet for `SQLCommand` failed: `LOCAL INFILE Request is not supported`. Sql: `" +
                    sql + "`. Connection info:`" + connectionInfo + "`. Invalid response packet:`" + responsePacket + "`.");
        }
        if (OKPacket.isOkPacket(responsePacket)) {
            return true;
        } else if (EOFPacket.isEOFPacket(responsePacket)) {
            receivedEOFPacketCount ++;
            if (receivedEOFPacketCount == 2) {
                return true;
            }
        } else if (ErrorPacket.isErrorPacket(responsePacket)) {
            return true;
        }
        return false;
    }

    @Override
    public byte[] getRequestByteArray() {
        return requestByteArray;
    }
}
