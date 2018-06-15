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

package com.heimuheimu.mysql.jdbc.channel;

import com.heimuheimu.mysql.jdbc.ConnectionConfiguration;
import com.heimuheimu.mysql.jdbc.ConnectionInfo;
import com.heimuheimu.mysql.jdbc.packet.CharsetMappingUtil;
import com.heimuheimu.mysql.jdbc.packet.MysqlPacket;
import com.heimuheimu.mysql.jdbc.packet.MysqlPacketReader;
import com.heimuheimu.mysql.jdbc.packet.connection.HandshakeResponse41Packet;
import com.heimuheimu.mysql.jdbc.packet.connection.HandshakeV10Packet;
import com.heimuheimu.mysql.jdbc.packet.connection.auth.AuthenticationPlugin;
import com.heimuheimu.mysql.jdbc.packet.connection.auth.AuthenticationPluginFactory;
import com.heimuheimu.mysql.jdbc.packet.generic.ErrorPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Mysql 客户端授权认证处理器。
 *
 * @author heimuheimu
 */
public class HandshakeProcessor {
    
    private static final Logger LOG = LoggerFactory.getLogger(HandshakeProcessor.class);
    
    private final ConnectionConfiguration connectionConfiguration;

    private final OutputStream outputStream;

    private final MysqlPacketReader reader;

    /**
     * 构造一个 Mysql 客户端授权认证处理器。
     *
     * @param connectionConfiguration 建立 Mysql 数据库连接使用的配置信息
     * @param outputStream Mysql 客户端输出流
     * @param reader {@link MysqlPacket} 读取器
     */
    public HandshakeProcessor(ConnectionConfiguration connectionConfiguration, OutputStream outputStream, MysqlPacketReader reader) {
        this.connectionConfiguration = connectionConfiguration;
        this.outputStream = outputStream;
        this.reader = reader;
    }

    /**
     * 与 Mysql 服务端进行握手，并进行客户端授权认证。
     *
     * @return Mysql 数据库连接信息
     * @throws IOException 如果握手过程中出现 IO 错误或 Mysql 服务端返回 {@link ErrorPacket} 数据包，将会抛出此异常
     */
    public ConnectionInfo doHandshake() throws IOException {
        MysqlPacket packet = reader.read();
        HandshakeV10Packet handshakeV10Packet = HandshakeV10Packet.parse(packet);
        LOG.debug("[handshake] receive HandshakeV10Packet: `{}`. Connection config: `{}`.", handshakeV10Packet, connectionConfiguration);

        AuthenticationPlugin plugin = AuthenticationPluginFactory.get(handshakeV10Packet.getAuthPluginName());
        byte[] authResponse = plugin.encode(connectionConfiguration.getPassword(), handshakeV10Packet.getAuthPluginData());

        HandshakeResponse41Packet handshakeResponse41Packet = new HandshakeResponse41Packet();
        handshakeResponse41Packet.setClientCharacterId(connectionConfiguration.getCharacterId());
        handshakeResponse41Packet.setCapabilitiesFlags(connectionConfiguration.getCapabilitiesFlags());
        handshakeResponse41Packet.setUsername(connectionConfiguration.getUsername());
        handshakeResponse41Packet.setAuthResponse(authResponse);
        handshakeResponse41Packet.setAuthPluginName(plugin.getName());
        handshakeResponse41Packet.setDatabaseName(connectionConfiguration.getDatabaseName());

        byte[] handshakeResponse41PacketBytes = handshakeResponse41Packet.buildMysqlPacketBytes(handshakeV10Packet.getCapabilitiesFlags());
        outputStream.write(handshakeResponse41PacketBytes);
        outputStream.flush();
        LOG.debug("[handshake] send HandshakeResponse41Packet: `{}`. Connection config: `{}`.", handshakeResponse41Packet, connectionConfiguration);

        packet = reader.read();
        if (ErrorPacket.isErrorPacket(packet)) {
            Charset charset = CharsetMappingUtil.getJavaCharset(handshakeResponse41Packet.getClientCharacterId());
            ErrorPacket errorPacket = ErrorPacket.parse(packet, charset);
            LOG.error("Connect to mysql failed: `handshake failed`. Error packet: `{}`. Connection config: `{}`.",
                    errorPacket, connectionConfiguration);
            throw new IOException("Connect to mysql failed: `handshake failed`. Error packet: `" + errorPacket
                    + "`. Connection config: `" + connectionConfiguration + "`.");
        } else {
            ConnectionInfo connectionInfo = new ConnectionInfo(handshakeV10Packet.getConnectionId(),
                    handshakeV10Packet.getServerVersion(), handshakeV10Packet.getServerCharacterId(),
                    handshakeV10Packet.getCapabilitiesFlags(), handshakeResponse41Packet.getClientCharacterId(),
                    handshakeResponse41Packet.getCapabilitiesFlags(), handshakeResponse41Packet.getDatabaseName());
            return connectionInfo;
        }
    }
}
