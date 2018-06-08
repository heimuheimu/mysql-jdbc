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

import com.heimuheimu.mysql.jdbc.packet.MysqlPacket;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * "TextResultsetRow" 数据包信息，与 "TextResultset" 数据包一起发送，更多信息请参考：
 * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_query_response_text_resultset_row.html">
 *     TextResultsetRow
 * </a>
 *
 * <p><strong>说明：</strong>{@code TextResultsetRowResponsePacket} 类是非线程安全的，不允许多个线程使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class TextResultsetRowResponsePacket {

    /**
     * 一行记录所有列的值对应的字节数组列表
     */
    private final List<byte[]> columnsValues = new ArrayList<>();

    /**
     * 新增一列值对应的字节数组，允许为 {@code null}。
     *
     * @param columnValue 一列值对应的字节数组，允许为 {@code null}
     */
    public void addColumnValue(byte[] columnValue) {
        columnsValues.add(columnValue);
    }

    /**
     * 获得一行记录所有列的值对应的字节数组列表，该方法不会返回 {@code null}。
     *
     * @return 一行记录所有列的值对应的字节数组列表，不会为 {@code null}
     */
    public List<byte[]> getColumnsValues() {
        return columnsValues;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("TextResultsetRowResponsePacket{columnsValues=[");
        if (!columnsValues.isEmpty()) {
            for (byte[] value : columnsValues) {
                if (value != null) {
                    buffer.append("\"").append(new String(value, StandardCharsets.UTF_8)).append("\", ");
                } else {
                    buffer.append("null");
                }
            }
            buffer.delete(buffer.length() - 2, buffer.length());
        }
        buffer.append("]}");
        return buffer.toString();
    }

    /**
     * 对 Mysql "TextResultsetRow" 数据包进行解析，生成对应的 {@code TextResultsetRowResponsePacket} 实例，
     * "TextResultsetRow" 数据包格式定义：
     * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_query_response_text_resultset_row.html">
     *     TextResultsetRow
     * </a>
     *
     * @param packet "TextResultsetRow" 数据包
     * @return {@code TextResultsetRowResponsePacket} 实例
     * @throws IllegalArgumentException 如果 Mysql 数据包不是正确的 "TextResultsetRow" 数据包，将会抛出此异常
     */
    public static TextResultsetRowResponsePacket parse(MysqlPacket packet) {
        try {
            packet.setPosition(0);
            TextResultsetRowResponsePacket textResultsetRowResponsePacket = new TextResultsetRowResponsePacket();
            while (packet.hasRemaining()) {
                int valueBytesLength = (int) packet.readLengthEncodedInteger();
                if (valueBytesLength != 0xFB) {
                    textResultsetRowResponsePacket.addColumnValue(packet.readFixedLengthBytes(valueBytesLength));
                } else {
                    textResultsetRowResponsePacket.addColumnValue(null);
                }
            }
            return textResultsetRowResponsePacket;
        } catch (Exception e) {
            throw new IllegalArgumentException("Parse TextResultsetRowResponsePacket failed: `invalid format`. " + packet, e);
        }
    }
}
