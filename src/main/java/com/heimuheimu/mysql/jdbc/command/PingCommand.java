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

import com.heimuheimu.mysql.jdbc.packet.MysqlPacket;
import com.heimuheimu.mysql.jdbc.packet.command.utility.CommandPingPacket;
import com.heimuheimu.mysql.jdbc.packet.generic.OKPacket;

import java.sql.SQLException;
import java.sql.SQLTimeoutException;

/**
 * Mysql PING 命令，用于检测 Mysql 服务端是否存活。
 *
 * <p><strong>说明：</strong>{@code PingCommand} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 * @see CommandPingPacket
 */
public class PingCommand extends AbstractCommand {

    /**
     * PING 命令数据包字节数组
     */
    private final byte[] requestByteArray;

    /**
     * 构造一个 Mysql PING 命令，用于检测 Mysql 服务端是否存活。
     */
    public PingCommand() throws NullPointerException {
        CommandPingPacket pingPacket = new CommandPingPacket();
        this.requestByteArray = pingPacket.buildMysqlPacketBytes();
    }

    /**
     * 判断 PING 命令是否成功返回，返回值永远为 {@code true}。
     *
     * @param timeout 超时时间，单位：毫秒
     * @return PING 命令是否成功返回，返回值永远为 {@code true}
     * @throws SQLException 响应数据错误或等待响应数据过程中，命令被关闭，将抛出此异常
     * @throws SQLTimeoutException 等待响应数据超时，将抛出此异常
     */
    public boolean isSuccess(long timeout) throws SQLException {
        getResponsePacketList(timeout); // 等待 Ping 命令的 OK_Packet 响应包到达
        return true;
    }

    @Override
    protected boolean isLastPacket(MysqlPacket responsePacket) throws IllegalStateException {
        if (OKPacket.isOkPacket(responsePacket)) {
            return true;
        } else {
            throw new IllegalStateException("Receive response packet for `PingCommand` failed: `invalid OK_Packet`. Invalid response packet:`"
                + responsePacket + "`.");
        }
    }

    @Override
    public byte[] getRequestByteArray() {
        return requestByteArray;
    }
}
