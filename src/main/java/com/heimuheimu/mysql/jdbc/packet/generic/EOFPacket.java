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

package com.heimuheimu.mysql.jdbc.packet.generic;

import com.heimuheimu.mysql.jdbc.packet.CapabilitiesFlagsUtil;
import com.heimuheimu.mysql.jdbc.packet.MysqlPacket;

/**
 * "EOF_Packet" 数据包信息，当 Mysql 服务端命令执行结束时，将通过该数据包进行响应，更多信息请参考：
 * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_eof_packet.html">
 *     EOF_Packet
 * </a>
 *
 * <p><strong>说明：</strong>{@code EOFPacket} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class EOFPacket {

    /**
     * Mysql 服务端状态数值，每个比特位可代表不同的服务端状态
     */
    private final int serverStatusFlags;

    /**
     * 警告信息数量
     */
    private final int warnings;

    /**
     * 构造一个 Mysql EOF 响应数据包信息。
     *
     * @param serverStatusFlags Mysql 服务端状态数值，每个比特位可代表不同的服务端状态
     * @param warnings 警告信息数量
     */
    public EOFPacket(int serverStatusFlags, int warnings) {
        this.serverStatusFlags = serverStatusFlags;
        this.warnings = warnings;
    }

    /**
     * 获得 Mysql 服务端状态数值，每个比特位可代表不同的服务端状态。
     *
     * @return Mysql 服务端状态数值
     */
    public int getServerStatusFlags() {
        return serverStatusFlags;
    }

    /**
     * 获得警告信息数量。
     *
     * @return 警告信息数量
     */
    public int getWarnings() {
        return warnings;
    }

    @Override
    public String toString() {
        return "EOFPacket{" +
                "serverStatusFlags=" + serverStatusFlags +
                ", warnings=" + warnings +
                '}';
    }

    /**
     * 判断该 Mysql 数据包是否为 "EOF_Packet" 数据包。
     *
     * @param packet Mysql 数据包
     * @return 是否为 "EOF_Packet" 数据包
     */
    public static boolean isEOFPacket(MysqlPacket packet) {
        if (packet.getPayload().length < 9) {
            int initialPosition = packet.getPosition();
            packet.setPosition(0);
            int firstByte = (int) packet.readFixedLengthInteger(1);
            packet.setPosition(initialPosition);
            return firstByte == 0xFE;
        }
        return false;
    }

    /**
     * 对 Mysql "EOF_Packet" 数据包进行解析，生成对应的 {@code EOFPacket} 实例，"EOF_Packet" 数据包格式定义：
     * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_eof_packet.html">
     *     EOF_Packet
     * </a>
     *
     * @param packet "EOF_Packet" 数据包
     * @param capabilitiesFlags Mysql 客户端可使用的特性数值，每个比特位可代表不同的特性是否支持
     * @return {@code EOFPacket} 实例
     * @throws IllegalArgumentException 如果 Mysql 数据包不是正确的 "EOF_Packet" 数据包，将会抛出此异常
     */
    public static EOFPacket parse(MysqlPacket packet, long capabilitiesFlags) throws IllegalArgumentException {
        if (isEOFPacket(packet)) {
            try {
                packet.setPosition(1);
                int warnings = 0;
                int serverStatusFlags = 0;
                if (CapabilitiesFlagsUtil.isCapabilityEnabled(capabilitiesFlags, CapabilitiesFlagsUtil.INDEX_CLIENT_PROTOCOL_41)) {
                    warnings = (int) packet.readFixedLengthInteger(2);
                    serverStatusFlags = (int) packet.readFixedLengthInteger(2);
                }
                return new EOFPacket(serverStatusFlags, warnings);
            } catch (Exception e) {
                throw new IllegalArgumentException("Parse EOF_Packet failed: `invalid format`. " + packet, e);
            }
        } else {
            throw new IllegalArgumentException("Parse EOF_Packet failed: `invalid first byte[0x"
                    + Integer.toString(packet.getPayload()[0], 16) + "]`. Expected value: `0xFE`. " + packet);
        }
    }
}
