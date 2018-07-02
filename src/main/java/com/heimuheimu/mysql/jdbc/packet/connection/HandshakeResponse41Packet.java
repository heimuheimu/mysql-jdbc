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
import com.heimuheimu.mysql.jdbc.packet.CharsetMappingUtil;
import com.heimuheimu.mysql.jdbc.packet.MysqlPacket;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;

/**
 * "HandshakeResponse41Packet" 数据包信息，Mysql 客户端在收到 {@link HandshakeV10Packet} 数据包后，将发送该响应数据包至服务端，更多信息请参考：
 * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_connection_phase_packets_protocol_handshake_response.html">
 *     Protocol::HandshakeResponse41
 * </a>
 *
 * <p><strong>说明：</strong>{@code HandshakeResponse41Packet} 类是非线程安全的，不允许多个线程使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class HandshakeResponse41Packet {

    /**
     * 数据包最大字节数量
     */
    private static final int MAX_PACKET_SIZE = 256 * 256 * 256 - 1;

    /**
     * Mysql 客户端可使用的特性数值，每个比特位可代表不同的特性是否支持
     */
    private long capabilitiesFlags = 0;

    /**
     * Mysql 客户端字符集编码 ID，ID 对应的编码可通过数据库表 "information_schema.collations" 进行查询
     *
     * <p>默认为：45 (utf8mb4_general_ci)</p>
     */
    private int clientCharacterId = 45;

    /**
     * Mysql 数据库用户名
     */
    private String username = "";

    /**
     * Mysql 数据库密码加密字节数组
     */
    private byte[] authResponse = null;

    /**
     * Mysql 数据库名称
     */
    private String databaseName = "";

    /**
     * Mysql 客户端可使用的授权插件名称
     */
    private String authPluginName = "";

    /**
     * Mysql 客户端属性键值对 Map，Key 为属性名称，Value 为属性值
     */
    private LinkedHashMap<String, String> clientConnectionAttributeMap = new LinkedHashMap<>();

    /**
     * 获得 Mysql 客户端可使用的特性数值。
     *
     * @return Mysql 客户端可使用的特性数值
     */
    public long getCapabilitiesFlags() {
        return capabilitiesFlags;
    }

    /**
     * 设置 Mysql 客户端可使用的特性数值。
     *
     * @param capabilitiesFlags Mysql 客户端可使用的特性数值
     */
    public void setCapabilitiesFlags(long capabilitiesFlags) {
        this.capabilitiesFlags = capabilitiesFlags;
    }

    /**
     * 获得 Mysql 客户端字符集编码 ID，ID 对应的编码可通过数据库表 "information_schema.collations" 进行查询。
     *
     * <p>默认为：45 (utf8mb4_general_ci)</p>
     *
     * @return Mysql 客户端字符集编码 ID
     */
    public int getClientCharacterId() {
        return clientCharacterId;
    }

    /**
     * 设置 Mysql 客户端字符集编码 ID。
     *
     * @param clientCharacterId Mysql 客户端字符集编码 ID，值允许的范围为：[0, 255]
     * @throws IllegalArgumentException 如果 {@code clientCharacterId} 的值不在允许的范围内，将会抛出此异常
     */
    public void setClientCharacterId(int clientCharacterId) throws IllegalArgumentException {
        if (clientCharacterId < 0 || clientCharacterId > 255) {
            throw new IllegalArgumentException("Set client character id failed: `out of range`. `invalidClientCharacterId`:`"
                    + clientCharacterId + "`. " + this);
        }
        this.clientCharacterId = clientCharacterId;
    }

    /**
     * 获得 Mysql 数据库用户名。
     *
     * @return Mysql 数据库用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置 Mysql 数据库用户名。
     *
     * @param username Mysql 数据库用户名，不允许为 {@code null} 或空
     * @throws IllegalArgumentException 如果 {@code username} 为 {@code null} 或空，将会抛出此异常
     */
    public void setUsername(String username) throws IllegalArgumentException {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Set username failed: `username should not be empty`. " + this);
        }
        this.username = username;
    }

    /**
     * 获得 Mysql 数据库密码加密字节数组。
     *
     * @return Mysql 数据库密码加密字节数组
     */
    public byte[] getAuthResponse() {
        return authResponse;
    }

    /**
     * 设置 Mysql 数据库密码加密字节数组。
     *
     * @param authResponse Mysql 数据库密码加密字节数组，不允许为 {@code null}
     * @throws NullPointerException 如果 {@code authResponse} 为 {@code null}，将抛出此异常
     */
    public void setAuthResponse(byte[] authResponse) throws NullPointerException {
        if (authResponse == null) {
            throw new NullPointerException("Set auth response failed: `authResponse should not be null`. " + this);
        }
        this.authResponse = authResponse;
    }

    /**
     * 获得 Mysql 数据库名称。
     *
     * @return Mysql 数据库名称
     */
    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * 设置 Mysql 数据库名称。
     *
     * @param databaseName Mysql 数据库名称，允许为 {@code null} 或空
     */
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
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
     * @param authPluginName Mysql 客户端可使用的授权插件名称，不允许为 {@code null} 或空
     * @throws IllegalArgumentException 如果 {@code authPluginName} 为 {@code null} 或空，将会抛出此异常
     */
    public void setAuthPluginName(String authPluginName) throws IllegalArgumentException {
        if (authPluginName == null || authPluginName.isEmpty()) {
            throw new IllegalArgumentException("Set auth plugin name failed: `authPluginName should not be empty`. " + this);
        }
        this.authPluginName = authPluginName;
    }

    /**
     * 获得 Mysql 客户端属性键值对 Map，Key 为属性名称，Value 为属性值。
     *
     * @return Mysql 客户端属性键值对 Map
     */
    public LinkedHashMap<String, String> getClientConnectionAttributeMap() {
        return clientConnectionAttributeMap;
    }

    /**
     * 添加一个 Mysql 客户端属性键值对。
     *
     * @param key 属性 Key，不允许为 {@code null} 或空
     * @param value 属性 Value，不允许为 {@code null} 或空
     * @throws IllegalArgumentException 如果 {@code key} 或 {@code value} 为 {@code null} 或空，将会抛出此异常
     */
    public void addClientConnectionAttribute(String key, String value) throws IllegalArgumentException {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Add client connection attribute failed: `key should not be empty`. `key`:`"
                    + key + "`. `value`:`" + value + "`. " + this);
        }
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Add client connection attribute failed: `value should not be empty`. `key`:`"
                    + key + "`. `value`:`" + value + "`. " + this);
        }
        clientConnectionAttributeMap.put(key, value);
    }

    /**
     * 根据当前 {@code HandshakeResponse41Packet} 实例信息，生成对应的 Mysql "HandshakeResponse41Packet" 数据包字节数组，
     * "HandshakeResponse41Packet" 数据包格式定义：
     * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_connection_phase_packets_protocol_handshake_response.html">
     *     Protocol::HandshakeResponse41
     * </a>
     *
     * <p><strong>注意：</strong>调用该方法后，{@link #capabilitiesFlags} 将会被重新赋值，默认特性将被开启，已指定开启但 Mysql 服务端不支持的特性将会被关闭。</p>
     *
     * @return "HandshakeResponse41Packet" 数据包字节数组
     * @throws IllegalArgumentException 如果 {@code HandshakeResponse41Packet} 中关键字段信息尚未设置，将会抛出此异常
     * @throws IllegalStateException 如果指定的 Mysql 客户端特性在 Mysql 服务端未启用，将会抛出此异常
     */
    public byte[] buildMysqlPacketBytes(long serverCapabilitiesFlags) throws IllegalArgumentException, IllegalStateException {
        initializeCapabilitiesFlags(serverCapabilitiesFlags);

        Charset charset = CharsetMappingUtil.getJavaCharset(clientCharacterId);
        int mysqlPacketPayloadLength = 32;

        byte[] usernameBytes;
        if (username != null && !username.isEmpty()) {
            usernameBytes = username.getBytes(charset);
            mysqlPacketPayloadLength += usernameBytes.length + 1;
        } else {
            throw new IllegalArgumentException("Build `HandshakeResponse41` packet failed: `username is empty`. " + this);
        }

        if (authResponse != null) {
            mysqlPacketPayloadLength += authResponse.length + 9;
        } else {
            throw new IllegalArgumentException("Build `HandshakeResponse41` packet failed: `authResponse is null`. " + this);
        }

        byte[] databaseNameBytes = new byte[0];
        if (databaseName != null && !databaseName.isEmpty()) {
            databaseNameBytes = databaseName.getBytes(charset);
            mysqlPacketPayloadLength += databaseNameBytes.length + 1;
        }

        byte[] authPluginNameBytes;
        if (authPluginName != null && !authPluginName.isEmpty()) {
            // the Authentication Method used by the client to generate auth-response value in this packet. This is an UTF-8 string.
            authPluginNameBytes = authPluginName.getBytes(StandardCharsets.UTF_8);
            mysqlPacketPayloadLength += authPluginNameBytes.length + 1;
        } else {
            throw new IllegalArgumentException("Build `HandshakeResponse41` packet failed: `authPluginName is empty`. " + this);
        }

        byte[][] clientConnectionAttributeBytes = null;
        int bytesLengthOfAllAttributes = 0;
        if (CapabilitiesFlagsUtil.isCapabilityEnabled(capabilitiesFlags, CapabilitiesFlagsUtil.INDEX_CLIENT_CONNECT_ATTRS)) {
            clientConnectionAttributeBytes = new byte[clientConnectionAttributeMap.size() * 2][];
            int attributeBytesIndex = 0;
            for (String key : clientConnectionAttributeMap.keySet()) {
                byte[] attributeKeyBytes = key.getBytes(charset);
                byte[] attributeValueBytes = clientConnectionAttributeMap.get(key).getBytes(charset);
                clientConnectionAttributeBytes[attributeBytesIndex++] = attributeKeyBytes;
                clientConnectionAttributeBytes[attributeBytesIndex++] = attributeValueBytes;
                bytesLengthOfAllAttributes += attributeKeyBytes.length + MysqlPacket.getBytesLengthForLengthEncodedInteger(attributeKeyBytes.length);
                bytesLengthOfAllAttributes += attributeValueBytes.length + MysqlPacket.getBytesLengthForLengthEncodedInteger(attributeValueBytes.length);
            }
            mysqlPacketPayloadLength += bytesLengthOfAllAttributes + 9;
        }

        MysqlPacket handshakeResponse41Packet = new MysqlPacket(1, new byte[mysqlPacketPayloadLength]);
        handshakeResponse41Packet.writeFixedLengthInteger(4, capabilitiesFlags);
        handshakeResponse41Packet.writeFixedLengthInteger(4, MAX_PACKET_SIZE);
        handshakeResponse41Packet.writeFixedLengthInteger(1, clientCharacterId);
        handshakeResponse41Packet.setPosition(handshakeResponse41Packet.getPosition() + 23); // filler to the size of the handshake response packet. All 0s.
        handshakeResponse41Packet.writeNullTerminatedString(usernameBytes);
        if (CapabilitiesFlagsUtil.isCapabilityEnabled(capabilitiesFlags,
                CapabilitiesFlagsUtil.INDEX_CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA)) {
            handshakeResponse41Packet.writeLengthEncodedString(authResponse);
        } else {
            handshakeResponse41Packet.writeFixedLengthInteger(1, authResponse.length);
            handshakeResponse41Packet.writeFixedLengthBytes(authResponse);
        }
        if (CapabilitiesFlagsUtil.isCapabilityEnabled(capabilitiesFlags,
                CapabilitiesFlagsUtil.INDEX_CLIENT_CONNECT_WITH_DB)) {
            handshakeResponse41Packet.writeNullTerminatedString(databaseNameBytes);
        }
        if (CapabilitiesFlagsUtil.isCapabilityEnabled(capabilitiesFlags,
                CapabilitiesFlagsUtil.INDEX_CLIENT_PLUGIN_AUTH)) {
            handshakeResponse41Packet.writeNullTerminatedString(authPluginNameBytes);
        }
        if (CapabilitiesFlagsUtil.isCapabilityEnabled(capabilitiesFlags,
                CapabilitiesFlagsUtil.INDEX_CLIENT_CONNECT_ATTRS)) {
            handshakeResponse41Packet.writeLengthEncodedInteger(bytesLengthOfAllAttributes);
            for (int i = 0; i < clientConnectionAttributeBytes.length; i++) {
                handshakeResponse41Packet.writeLengthEncodedString(clientConnectionAttributeBytes[i]);
            }
        }
        return handshakeResponse41Packet.buildMysqlPacketBytes();
    }

    private void initializeCapabilitiesFlags(long serverCapabilitiesFlags) {
        this.capabilitiesFlags = CapabilitiesFlagsUtil.disableCapability(this.capabilitiesFlags,
                CapabilitiesFlagsUtil.INDEX_CLIENT_DEPRECATE_EOF); // 关闭不使用 EOF 包特性，简化 ResultSet 判断

        enableClientCapability(serverCapabilitiesFlags, CapabilitiesFlagsUtil.INDEX_CLIENT_PROTOCOL_41, true);
        enableClientCapability(serverCapabilitiesFlags, CapabilitiesFlagsUtil.INDEX_CLIENT_PLUGIN_AUTH, true);
        enableClientCapability(serverCapabilitiesFlags, CapabilitiesFlagsUtil.INDEX_CLIENT_RESERVED, false);
        enableClientCapability(serverCapabilitiesFlags, CapabilitiesFlagsUtil.INDEX_CLIENT_RESERVED2, true);

        enableClientCapability(serverCapabilitiesFlags, CapabilitiesFlagsUtil.INDEX_CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA, false);

        if (!clientConnectionAttributeMap.isEmpty()) {
            enableClientCapability(serverCapabilitiesFlags, CapabilitiesFlagsUtil.INDEX_CLIENT_CONNECT_ATTRS, false);
        }

        if (databaseName != null && !databaseName.isEmpty()) {
            enableClientCapability(serverCapabilitiesFlags, CapabilitiesFlagsUtil.INDEX_CLIENT_CONNECT_WITH_DB, true);
        }

        // capabilitiesFlags 可能由外部进行设置，将 Mysql 服务端不支持的特性关闭后重新赋值
        long clientCapabilitiesFlags = 0;
        for (int i = 0; i < 32; i++) {
            if (CapabilitiesFlagsUtil.isCapabilityEnabled(serverCapabilitiesFlags, i) &&
                    CapabilitiesFlagsUtil.isCapabilityEnabled(capabilitiesFlags, i)) {
                clientCapabilitiesFlags = CapabilitiesFlagsUtil.enableCapability(clientCapabilitiesFlags, i);
            }
        }
        this.capabilitiesFlags = clientCapabilitiesFlags;
    }

    private void enableClientCapability(long serverCapabilitiesFlags, int capabilityIndex, boolean required) throws IllegalStateException {
        if (CapabilitiesFlagsUtil.isCapabilityEnabled(serverCapabilitiesFlags, capabilityIndex)) {
            capabilitiesFlags = CapabilitiesFlagsUtil.enableCapability(capabilitiesFlags, capabilityIndex);
        } else {
            if (required) {
                throw new IllegalStateException("Enable capability `" + CapabilitiesFlagsUtil.getCapabilityName(capabilityIndex)
                        + "` failed: `the capability is disabled at mysql server side`. `capabilityIndex`:`" + capabilityIndex
                        + "`. `serverCapabilitiesFlags`:`" + serverCapabilitiesFlags + "`. " + this);
            }
        }
    }

    @Override
    public String toString() {
        return "HandshakeResponse41Packet{" +
                "capabilitiesFlags=" + capabilitiesFlags +
                ", clientCharacterId=" + clientCharacterId +
                ", username='" + username + '\'' +
                ", authResponse=" + Arrays.toString(authResponse) +
                ", databaseName='" + databaseName + '\'' +
                ", authPluginName='" + authPluginName + '\'' +
                ", clientConnectionAttributeMap=" + clientConnectionAttributeMap +
                '}';
    }
}
