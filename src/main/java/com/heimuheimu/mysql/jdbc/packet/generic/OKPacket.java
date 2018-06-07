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
import com.heimuheimu.mysql.jdbc.packet.ServerStatusFlagsUtil;

import java.nio.charset.Charset;

/**
 * "OK_Packet" 数据包信息，当 Mysql 服务端执行成功时，将通过该数据包进行响应，更多信息请参考：
 * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_ok_packet.html">
 *     OK_Packet
 * </a>
 *
 * <p><strong>说明：</strong>{@code OKPacket} 类是非线程安全的，不允许多个线程使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class OKPacket {

    /**
     * 变更的记录行数
     */
    private long affectedRows = 0;

    /**
     * 最后插入的主键 ID
     */
    private long lastInsertId = -1;

    /**
     * Mysql 服务端状态数值，每个比特位可代表不同的服务端状态
     */
    private int serverStatusFlags = 0;

    /**
     * 警告信息数量
     */
    private int warnings = 0;

    /**
     * Mysql 服务端状态信息
     */
    private String info = "";

    /**
     * Mysql 服务端 Session 状态信息
     */
    private String sessionStateInfo = "";

    /**
     * 获得变更的记录行数。
     *
     * @return 变更的记录行数
     */
    public long getAffectedRows() {
        return affectedRows;
    }

    /**
     * 设置变更的记录行数。
     *
     * @param affectedRows 变更的记录行数
     */
    public void setAffectedRows(long affectedRows) {
        this.affectedRows = affectedRows;
    }

    /**
     * 获得最后插入的主键 ID。
     *
     * @return 最后插入的主键 ID
     */
    public long getLastInsertId() {
        return lastInsertId;
    }

    /**
     * 设置最后插入的主键 ID。
     *
     * @param lastInsertId 最后插入的主键 ID
     */
    public void setLastInsertId(long lastInsertId) {
        this.lastInsertId = lastInsertId;
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
     * 设置 Mysql 服务端状态数值。
     *
     * @param serverStatusFlags Mysql 服务端状态数值
     */
    public void setServerStatusFlags(int serverStatusFlags) {
        this.serverStatusFlags = serverStatusFlags;
    }

    /**
     * 获得警告信息数量。
     *
     * @return 警告信息数量
     */
    public int getWarnings() {
        return warnings;
    }

    /**
     * 设置警告信息数量。
     *
     * @param warnings 警告信息数量
     */
    public void setWarnings(int warnings) {
        this.warnings = warnings;
    }

    /**
     * 获得 Mysql 服务端状态信息。
     *
     * @return Mysql 服务端状态信息
     */
    public String getInfo() {
        return info;
    }

    /**
     * 设置 Mysql 服务端状态信息。
     *
     * @param info Mysql 服务端状态信息
     */
    public void setInfo(String info) {
        this.info = info;
    }

    /**
     * 获得 Mysql 服务端 Session 状态信息。
     *
     * @return Mysql 服务端 Session 状态信息
     */
    public String getSessionStateInfo() {
        return sessionStateInfo;
    }

    /**
     * 设置 Mysql 服务端 Session 状态信息。
     *
     * @param sessionStateInfo Mysql 服务端 Session 状态信息
     */
    public void setSessionStateInfo(String sessionStateInfo) {
        this.sessionStateInfo = sessionStateInfo;
    }

    @Override
    public String toString() {
        return "OKPacket{" +
                "affectedRows=" + affectedRows +
                ", lastInsertId=" + lastInsertId +
                ", serverStatusFlags=" + serverStatusFlags +
                ", warnings=" + warnings +
                ", info='" + info + '\'' +
                ", sessionStateInfo='" + sessionStateInfo + '\'' +
                '}';
    }

    /**
     * 判断该 Mysql 数据包是否为 "OK_Packet" 数据包。
     *
     * @param packet Mysql 数据包
     * @return 是否为 "OK_Packet" 数据包
     */
    public static boolean isOkPacket(MysqlPacket packet) {
        if (packet.getPayload().length >= 7) {
            int initialPosition = packet.getPosition();
            packet.setPosition(0);
            int firstByte = (int) packet.readFixedLengthInteger(1);
            packet.setPosition(initialPosition);
            return firstByte == 0x00 || firstByte == 0xFE;
        }
        return false;
    }

    /**
     * 对 Mysql "OK_Packet" 数据包进行解析，生成对应的 {@code OKPacket} 实例，"OK_Packet" 数据包格式定义：
     * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_ok_packet.html">
     *     OK_Packet
     * </a>
     *
     * @param packet "OK_Packet" 数据包
     * @param capabilitiesFlags Mysql 客户端可使用的特性数值，每个比特位可代表不同的特性是否支持
     * @param charset 字符集编码
     * @return {@code OKPacket} 实例
     * @throws IllegalArgumentException 如果 Mysql 数据包不是正确的 "OK_Packet" 数据包，将会抛出此异常
     */
    public static OKPacket parse(MysqlPacket packet, long capabilitiesFlags, Charset charset) {
        if (isOkPacket(packet)) {
            try {
                packet.setPosition(1);
                OKPacket okPacket = new OKPacket();
                okPacket.setAffectedRows(packet.readLengthEncodedInteger());
                okPacket.setLastInsertId(packet.readLengthEncodedInteger());
                if (CapabilitiesFlagsUtil.isCapabilityEnabled(capabilitiesFlags, CapabilitiesFlagsUtil.INDEX_CLIENT_PROTOCOL_41)) {
                    okPacket.setServerStatusFlags((int) packet.readFixedLengthInteger(2));
                    okPacket.setWarnings((int) packet.readFixedLengthInteger(2));
                } else if (CapabilitiesFlagsUtil.isCapabilityEnabled(capabilitiesFlags, CapabilitiesFlagsUtil.INDEX_CLIENT_TRANSACTIONS)) {
                    okPacket.setServerStatusFlags((int) packet.readFixedLengthInteger(2));
                }
                if (packet.hasRemaining()) {
                    if (CapabilitiesFlagsUtil.isCapabilityEnabled(capabilitiesFlags, CapabilitiesFlagsUtil.INDEX_CLIENT_SESSION_TRACK)) {
                        okPacket.setInfo(packet.readLengthEncodedString(charset));
                        if (packet.hasRemaining() && ServerStatusFlagsUtil.isServerStatusEnabled(okPacket.getServerStatusFlags(),
                                ServerStatusFlagsUtil.INDEX_SERVER_SESSION_STATE_CHANGED)) {
                            okPacket.setSessionStateInfo(packet.readLengthEncodedString(charset));
                        }
                    } else {
                        okPacket.setInfo(packet.readRestOfPacketString(charset));
                    }
                }
                return okPacket;
            } catch (Exception e) {
                throw new IllegalArgumentException("Parse OK_Packet failed: `invalid format`. " + packet, e);
            }
        } else {
            throw new IllegalArgumentException("Parse OK_Packet failed: `invalid first byte[0x" +
                    Integer.toString(packet.getPayload()[0], 16) + "]`. Expected value: `0x00 or 0xFE`. " + packet);
        }
    }
}