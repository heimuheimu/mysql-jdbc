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

package com.heimuheimu.mysql.jdbc.packet;

import com.heimuheimu.mysql.jdbc.util.BytesUtil;

import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * MYSQL 服务端与客户端之间进行通信的数据包，由 4 个字节的头部信息和可变长度的内容信息组成。
 *
 * <p>
 * 更多信息请参考：
 * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_packets.html">
 *     https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_packets.html
 * </a>
 * </p>
 *
 * <p><strong>说明：</strong>{@code MysqlPacket} 类是非线程安全的，不允许多个线程使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class MysqlPacket {

    /**
     * 数据包序号
     */
    private final int sequenceId;

    /**
     * 内容信息
     */
    private final byte[] payload;

    /**
     * 下一个被读取字节的索引位置
     */
    private int position = 0;

    /**
     * 构造一个 MYSQL 服务端与客户端之间进行通信的数据包。
     *
     * @param sequenceId 数据包序号
     * @param payload 内容信息
     */
    public MysqlPacket(int sequenceId, byte[] payload) {
        this.sequenceId = sequenceId;
        this.payload = payload;
    }

    /**
     * 获得数据包序号。
     *
     * @return 数据包序号
     */
    public int getSequenceId() {
        return sequenceId;
    }

    /**
     * 获得内容信息字节数组。
     *
     * <p><strong>注意：</strong>请勿直接修改内容信息字节数组中的内容。</p>
     *
     * @return
     */
    public byte[] getPayload() {
        return payload;
    }

    /**
     * 获得下一个被读取字节的索引位置。
     *
     * @return 下一个被读取字节的索引位置
     */
    public int getPosition() {
        return position;
    }

    /**
     * 设置下一个被读取字节的索引位置。
     *
     * @param position 下一个被读取字节的索引位置
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * 读取固定长度的字节数组。
     *
     * @param length 字节长度
     * @return 固定长度的字节数组
     * @throws ArrayIndexOutOfBoundsException 如果访问数组越界，将会抛出此异常
     * @throws IllegalArgumentException 如果 {@code length} 小于等于 0，将会抛出此异常
     */
    public byte[] readFixedLengthBytes(int length) throws IndexOutOfBoundsException, IllegalArgumentException {
        if (length <= 0) {
            throw new IllegalArgumentException("Read fixed length bytes failed: `invalid length`. `length`:`" + length + "`.");
        }
        byte[] value = new byte[length];
        System.arraycopy(payload, position, value, 0, length);
        position += length;
        return value;
    }

    /**
     * 读取 N 字节长度的无符号整数，长度允许的范围为: [1, 8]
     *
     * @param length 字节长度
     * @return 无符号整数
     * @throws ArrayIndexOutOfBoundsException 如果访问数组越界，将会抛出此异常
     * @throws IllegalArgumentException 如果 {@code length} 的值没有在允许的范围内，将会抛出此异常
     */
    public long readFixedLengthInteger(int length) throws ArrayIndexOutOfBoundsException, IllegalArgumentException {
        long value = BytesUtil.toUnsignedInteger(payload, position, length);
        position += length;
        return value;
    }

    /**
     * 读取以 '0x0000' 作为结束标志位的字符串。
     *
     * @param charset 字符编码
     * @return 以 '0x0000' 作为结束标志位的字符串
     */
    public String readNullTerminatedString(Charset charset) {
        int length = 0;
        int startPosition = position;
        while (payload[position++] != 0) {
            length++;
        }
        return new String(payload, startPosition, length, charset);
    }

    /**
     * 读取固定长度的字符串。
     *
     * @param length 字符串长度
     * @param charset 字符编码
     * @return 固定长度的字符串
     * @throws IllegalArgumentException 如果 {@code length} 小于等于 0，将会抛出此异常
     */
    public String readFixedLengthString(int length, Charset charset) throws IllegalArgumentException {
        if (length <= 0) {
            throw new IllegalArgumentException("Read fixed length string failed: `invalid length`. `length`:`" + length + "`.");
        }
        int startPosition = position;
        position += length;
        return new String(payload, startPosition, length, charset);
    }

    /**
     * 读取所有剩余字节的字符串。
     *
     * @param charset 字符编码
     * @return 所有剩余字节的字符串
     */
    public String readRestOfPacketString(Charset charset) {
        if (position < payload.length) {
            int startPosition = position;
            position = payload.length;
            int length = payload.length - startPosition;
            return new String(payload, startPosition, length, charset);
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return "MysqlPacket{" +
                "sequenceId=" + sequenceId +
                ", payload=" + Arrays.toString(payload) +
                ", position=" + position +
                '}';
    }
}
