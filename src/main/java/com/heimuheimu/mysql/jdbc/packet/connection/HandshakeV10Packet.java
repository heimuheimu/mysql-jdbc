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

package com.heimuheimu.mysql.jdbc.packet.connection;

import com.heimuheimu.mysql.jdbc.packet.CapabilitiesFlagsUtil;
import com.heimuheimu.mysql.jdbc.packet.MysqlPacket;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * "HandshakeV10" 数据包信息，Mysql 服务端将在客户端连接成功后，发送该数据包至客户端，更多信息请参考：
 * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_connection_phase_packets_protocol_handshake_v10.html">
 *     Protocol::HandshakeV10
 * </a>
 *
 * <p><strong>说明：</strong>{@code HandshakeV10Packet} 类是非线程安全的，不允许多个线程使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class HandshakeV10Packet {

    /**
     * Mysql 服务端版本号
     */
    private String serverVersion = "";

    /**
     * Mysql 连接 ID
     */
    private long connectionId = -1;

    /**
     * Mysql 服务端默认字符集编码 ID，ID 对应的编码可通过数据库表 "information_schema.collations" 进行查询
     */
    private int serverCharacterId = -1;

    /**
     * Mysql 服务端状态数值，每个比特位可代表不同的服务端状态
     */
    private int serverStatusFlags = 0;

    /**
     * Mysql 服务端支持的特性数值，每个比特位可代表不同的特性是否支持
     */
    private long capabilitiesFlags = 0;

    /**
     * Mysql 客户端可使用的授权插件名称
     */
    private String authPluginName = null;

    /**
     * Mysql 客户端授权插件用于密码加密的字节数组
     */
    private byte[] authPluginData = null;

    /**
     * 获得 Mysql 服务端版本号。
     *
     * @return Mysql 服务端版本号
     */
    public String getServerVersion() {
        return serverVersion;
    }

    /**
     * 设置 Mysql 服务端版本号。
     *
     * @param serverVersion Mysql 服务端版本号
     */
    public void setServerVersion(String serverVersion) {
        this.serverVersion = serverVersion;
    }

    /**
     * 获得 Mysql 连接 ID。
     *
     * @return Mysql 连接 ID
     */
    public long getConnectionId() {
        return connectionId;
    }

    /**
     * 设置 Mysql 连接 ID。
     *
     * @param connectionId Mysql 连接 ID
     */
    public void setConnectionId(long connectionId) {
        this.connectionId = connectionId;
    }

    /**
     * 获得 Mysql 服务端默认字符集编码 ID，ID 对应的编码可通过数据库表 "information_schema.collations" 进行查询。
     *
     * @return Mysql 服务端默认字符集编码 ID
     */
    public int getServerCharacterId() {
        return serverCharacterId;
    }

    /**
     * 设置 Mysql 服务端默认字符集编码 ID。
     *
     * @param serverCharacterId Mysql 服务端默认字符集编码 ID
     */
    public void setServerCharacterId(int serverCharacterId) {
        this.serverCharacterId = serverCharacterId;
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
     * 设置 Mysql 服务端状态数值，每个比特位可代表不同的服务端状态。
     *
     * @param serverStatusFlags Mysql 服务端状态数值
     */
    public void setServerStatusFlags(int serverStatusFlags) {
        this.serverStatusFlags = serverStatusFlags;
    }

    /**
     * 获得 Mysql 服务端支持的特性数值，每个比特位可代表不同的特性是否支持。
     *
     * @return Mysql 服务端支持的特性数值
     */
    public long getCapabilitiesFlags() {
        return capabilitiesFlags;
    }

    /**
     * 设置 Mysql 服务端支持的特性数值，每个比特位可代表不同的特性是否支持。
     *
     * @param capabilitiesFlags Mysql 服务端支持的特性数值
     */
    public void setCapabilitiesFlags(long capabilitiesFlags) {
        this.capabilitiesFlags = capabilitiesFlags;
    }

    /**
     * 获得 Mysql 客户端可使用的授权插件名称。
     *
     * @return Mysql 客户端可使用的授权插件名称
     */
    public String getAuthPluginName() {
        return authPluginName;
    }

    /**
     * 设置 Mysql 客户端可使用的授权插件名称。
     *
     * @param authPluginName Mysql 客户端可使用的授权插件名称
     */
    public void setAuthPluginName(String authPluginName) {
        this.authPluginName = authPluginName;
    }

    /**
     * 获得 Mysql 客户端授权插件用于密码加密的字节数组。
     *
     * @return Mysql 客户端授权插件用于密码加密的字节数组
     */
    public byte[] getAuthPluginData() {
        return authPluginData;
    }

    /**
     * 设置 Mysql 客户端授权插件用于密码加密的字节数组。
     *
     * @param authPluginData Mysql 客户端授权插件用于密码加密的字节数组
     */
    public void setAuthPluginData(byte[] authPluginData) {
        this.authPluginData = authPluginData;
    }

    @Override
    public String toString() {
        return "HandshakeV10Packet{" +
                "serverVersion='" + serverVersion + '\'' +
                ", connectionId=" + connectionId +
                ", serverCharacterId=" + serverCharacterId +
                ", serverStatusFlags=" + serverStatusFlags +
                ", capabilitiesFlags=" + capabilitiesFlags +
                ", authPluginName='" + authPluginName + '\'' +
                ", authPluginData=" + Arrays.toString(authPluginData) +
                '}';
    }

    /**
     * 对 Mysql "HandshakeV10Packet" 数据包进行解析，生成对应的 {@code HandshakeV10Packet} 实例，"HandshakeV10Packet" 数据包格式定义：
     * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_connection_phase_packets_protocol_handshake_v10.html">
     *     Protocol::HandshakeV10
     * </a>
     *
     * @param packet "HandshakeV10Packet" 数据包
     * @return {@code HandshakeV10Packet} 实例
     * @throws IllegalArgumentException 如果 Mysql 数据包不是正确的 "HandshakeV10Packet" 数据包，将会抛出此异常
     */
    public static HandshakeV10Packet parse(MysqlPacket packet) throws IllegalArgumentException {
        packet.setPosition(0);
        HandshakeV10Packet handshakeV10Packet = new HandshakeV10Packet();
        long protocolVersion = packet.readFixedLengthInteger(1);
        if (protocolVersion != 10) {
            throw new IllegalArgumentException("Invalid HandshakeV10Packet protocol version: `" + protocolVersion + "`. Expected value: `10`. " + packet);
        }
        handshakeV10Packet.setServerVersion(packet.readNullTerminatedString(StandardCharsets.US_ASCII));
        handshakeV10Packet.setConnectionId(packet.readFixedLengthInteger(4));
        byte[] authPluginDataPart1 = packet.readFixedLengthBytes(8);
        long filter = packet.readFixedLengthInteger(1);
        if (filter != 0) {
            throw new IllegalArgumentException("Invalid HandshakeV10Packet filter: `0x" + Long.toString(filter, 16)
                    + "`. Expected value: `0x00`. " + packet);
        }
        long capabilitiesFlags = packet.readFixedLengthInteger(2);
        handshakeV10Packet.setServerCharacterId((int) packet.readFixedLengthInteger(1));
        handshakeV10Packet.setServerStatusFlags((int) packet.readFixedLengthInteger(2));
        capabilitiesFlags |= packet.readFixedLengthInteger(2) << 16;
        handshakeV10Packet.setCapabilitiesFlags(capabilitiesFlags);
        int authPluginDataLength = 0;
        if (CapabilitiesFlagsUtil.isCapabilityEnabled(capabilitiesFlags, CapabilitiesFlagsUtil.INDEX_CLIENT_PLUGIN_AUTH)) {
            authPluginDataLength = (int) packet.readFixedLengthInteger(1);
        } else {
            long constantAuthPluginDataLength = packet.readFixedLengthInteger(1);
            if ( constantAuthPluginDataLength != 0) {
                throw new IllegalArgumentException("Invalid HandshakeV10Packet auth plugin data length constant value: `0x"
                        + Long.toString(constantAuthPluginDataLength, 16) + "`. Expected value: `0x00`. " + packet);
            }
        }
        packet.setPosition(packet.getPosition() + 10); // read reserved string. All 0s.
        authPluginDataLength = Math.max(13, authPluginDataLength - 8);
        byte[] authPluginDataPart2 = packet.readFixedLengthBytes(authPluginDataLength);
        if (CapabilitiesFlagsUtil.isCapabilityEnabled(capabilitiesFlags, CapabilitiesFlagsUtil.INDEX_CLIENT_PLUGIN_AUTH)) {
            byte[] authPluginData = new byte[authPluginDataPart1.length + authPluginDataPart2.length];
            System.arraycopy(authPluginDataPart1, 0, authPluginData, 0, authPluginDataPart1.length);
            System.arraycopy(authPluginDataPart2, 0, authPluginData, authPluginDataPart1.length, authPluginDataPart2.length);
            handshakeV10Packet.setAuthPluginData(authPluginData);

            // the Authentication Method used by the client to generate auth-response value in this packet. This is an UTF-8 string.
            String restOfPacketString = packet.readRestOfPacketString(StandardCharsets.UTF_8);
            int nullTerminatedIndex = restOfPacketString.indexOf(0x0000);
            if (nullTerminatedIndex < 0) {
                handshakeV10Packet.setAuthPluginName(restOfPacketString);
            } else {
                handshakeV10Packet.setAuthPluginName(restOfPacketString.substring(0, nullTerminatedIndex));
            }
        }
        return handshakeV10Packet;
    }
}
