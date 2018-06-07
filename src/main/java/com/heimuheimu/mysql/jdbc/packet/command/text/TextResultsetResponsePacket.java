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

package com.heimuheimu.mysql.jdbc.packet.command.text;

import com.heimuheimu.mysql.jdbc.packet.CapabilitiesFlagsUtil;
import com.heimuheimu.mysql.jdbc.packet.MysqlPacket;

/**
 * "TextResultset" 数据包信息，Mysql 服务端在收到 "COM_QUERY" 数据包后，可能会发送该响应数据包至客户端，更多信息请参考：
 * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_connection_phase_packets_protocol_handshake_response.html">
 *     TextResultset
 * </a>
 *
 *  <p><strong>说明：</strong>{@code TextResultsetResponsePacket} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 *  @author heimuheimu
 */
public class TextResultsetResponsePacket {

    /**
     * 是否跟有 "ColumnDefinitionResponsePacket" 数据包
     */
    private final boolean isMetadataFollows;

    /**
     * 列数量
     */
    private final int columnCount;

    /**
     * 构造一个 Mysql "TextResultset" 响应数据包信息。
     *
     * @param isMetadataFollows 是否跟有 "ColumnDefinitionResponsePacket" 数据包
     * @param columnCount 列数量
     */
    public TextResultsetResponsePacket(boolean isMetadataFollows, int columnCount) {
        this.isMetadataFollows = isMetadataFollows;
        this.columnCount = columnCount;
    }

    /**
     * 判断是否跟有 "ColumnDefinitionResponsePacket" 数据包。
     *
     * @return 是否跟有 "ColumnDefinitionResponsePacket" 数据包
     */
    public boolean isMetadataFollows() {
        return isMetadataFollows;
    }

    /**
     * 获得列数量。
     *
     * @return 列数量
     */
    public int getColumnCount() {
        return columnCount;
    }

    /**
     * 对 Mysql "TextResultset" 数据包进行解析，生成对应的 {@code TextResultsetResponsePacket} 实例，"TextResultset" 数据包格式定义：
     * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_connection_phase_packets_protocol_handshake_response.html">
     *     TextResultset
     * </a>
     *
     * @param packet "TextResultset" 数据包
     * @param capabilitiesFlags Mysql 客户端可使用的特性数值，每个比特位可代表不同的特性是否支持
     * @return {@code TextResultsetResponsePacket} 实例
     * @throws IllegalArgumentException 如果 Mysql 数据包不是正确的 "OK_Packet" 数据包，将会抛出此异常
     */
    public static TextResultsetResponsePacket parse(MysqlPacket packet, long capabilitiesFlags) {
        try {
            packet.setPosition(0);
            boolean isMetadataFollows = true;
            if (CapabilitiesFlagsUtil.isCapabilityEnabled(capabilitiesFlags, CapabilitiesFlagsUtil.INDEX_CLIENT_OPTIONAL_RESULTSET_METADATA)) {
                int metadataFollows = (int) packet.readFixedLengthInteger(1);
                isMetadataFollows = metadataFollows != 1;
            }
            return new TextResultsetResponsePacket(isMetadataFollows, (int) packet.readLengthEncodedInteger());
        } catch (Exception e) {
            throw new IllegalArgumentException("Parse TextResultset failed: `invalid format`. " + packet, e);
        }
    }
}
