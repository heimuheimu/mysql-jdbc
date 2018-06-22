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

package com.heimuheimu.mysql.jdbc.packet.command.utility;

import com.heimuheimu.mysql.jdbc.packet.MysqlPacket;

/**
 * "COM_PING" 数据包信息，用于向 Mysql 服务端发送 PING 命令，更多信息请参考：
 * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_ping.html">
 *     COM_PING
 * </a>
 *
 * <p><strong>说明：</strong>{@code CommandPingPacket} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class CommandPingPacket {

    public byte[] buildMysqlPacketBytes() {
        MysqlPacket mysqlPacket = new MysqlPacket(0, new byte[1]);
        mysqlPacket.writeFixedLengthInteger(1, 0x0E);
        return mysqlPacket.buildMysqlPacketBytes();
    }
}
