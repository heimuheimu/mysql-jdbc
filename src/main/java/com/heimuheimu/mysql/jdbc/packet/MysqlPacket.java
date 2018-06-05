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
 *     MySQL Packets
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
     * @return 内容信息字节数组
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
     * 读取 "Protocol::FixedLengthInteger" 类型的无符号整数，长度允许的范围为: [1, 8]。
     *
     * <p>
     * 更多信息请参考：
     * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_dt_integers.html">
     *     Protocol::FixedLengthInteger
     * </a>
     * </p>
     *
     * @param length 字节长度
     * @return 无符号整数
     * @throws ArrayIndexOutOfBoundsException 如果访问数组越界，将会抛出此异常
     * @throws IllegalArgumentException 如果 {@code length} 的值没有在允许的范围内，将会抛出此异常
     */
    public long readFixedLengthInteger(int length) throws ArrayIndexOutOfBoundsException, IllegalArgumentException {
        long value = BytesUtil.decodeUnsignedInteger(payload, position, length);
        position += length;
        return value;
    }

    /**
     * 读取 "Protocol::LengthEncodedInteger" 类型的无符号整数。
     *
     * <p>
     * 更多信息请参考：
     * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_dt_integers.html">
     *     Protocol::LengthEncodedInteger
     * </a>
     * </p>
     *
     * @return 无符号整数
     * @throws ArrayIndexOutOfBoundsException 如果访问数组越界，将会抛出此异常
     */
    public long readLengthEncodedInteger() throws ArrayIndexOutOfBoundsException {
        long magicValue = readFixedLengthInteger(1);
        if (magicValue == 0xFC) {
            return readFixedLengthInteger(2);
        } else if (magicValue == 0xFD) {
            return readFixedLengthInteger(3);
        } else if (magicValue == 0xFE) {
            return readFixedLengthInteger(8);
        } else {
            return magicValue;
        }
    }

    /**
     * 读取 "Protocol::NullTerminatedString" 类型的字符串。
     *
     * <p>
     * 更多信息请参考：
     * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_dt_strings.html">
     *     Protocol::NullTerminatedString
     * </a>
     * </p>
     *
     * @param charset 字符集编码
     * @return "Protocol::NullTerminatedString" 类型的字符串
     * @throws ArrayIndexOutOfBoundsException 如果访问数组越界，将会抛出此异常
     */
    public String readNullTerminatedString(Charset charset) throws ArrayIndexOutOfBoundsException {
        int length = 0;
        int startPosition = position;
        while (payload[position++] != 0) {
            length++;
        }
        return new String(payload, startPosition, length, charset);
    }

    /**
     * 读取 "Protocol::LengthEncodedString" 类型的字符串，该方法不会返回 {@code null}。
     *
     * <p>
     * 更多信息请参考：
     * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_dt_strings.html">
     *     Protocol::LengthEncodedString
     * </a>
     * </p>
     *
     * @param charset 字符集编码
     * @return "Protocol::LengthEncodedString" 类型的字符串
     * @throws ArrayIndexOutOfBoundsException 如果访问数组越界，将会抛出此异常
     */
    public String readLengthEncodedString(Charset charset) throws ArrayIndexOutOfBoundsException {
        int length = (int) readLengthEncodedInteger();
        return readFixedLengthString(length, charset);
    }

    /**
     * 读取 "Protocol::FixedLengthString" 类型的字符串，该方法不会返回 {@code null}。
     *
     * <p>
     * 更多信息请参考：
     * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_dt_strings.html">
     *     Protocol::FixedLengthString
     * </a>
     * </p>
     *
     * @param length 字符串长度
     * @param charset 字符集编码
     * @return "Protocol::FixedLengthString" 类型的字符串
     * @throws ArrayIndexOutOfBoundsException 如果访问数组越界，将会抛出此异常
     * @throws IllegalArgumentException 如果 {@code length} 小于 0，将会抛出此异常
     */
    public String readFixedLengthString(int length, Charset charset) throws ArrayIndexOutOfBoundsException, IllegalArgumentException {
        if (length < 0) {
            throw new IllegalArgumentException("Read fixed length string failed: `invalid length`. `length`:`" + length + "`.");
        }
        if (length == 0) {
            return "";
        }
        int startPosition = position;
        position += length;
        return new String(payload, startPosition, length, charset);
    }

    /**
     * 读取 "Protocol::RestOfPacketString" 类型的字符串，可能返回 {@code null}。
     *
     * <p>
     * 更多信息请参考：
     * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_dt_strings.html">
     *     Protocol::RestOfPacketString
     * </a>
     * </p>
     *
     * @param charset 字符编码
     * @return "Protocol::FixedLengthString" 类型的字符串
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

    /**
     * 写入固定长度的字节数组。
     *
     * @param src 需要写入的字节数组，不允许为 {@code null}
     * @return 当前 {@code MysqlPacket} 实例
     * @throws IndexOutOfBoundsException 如果访问数组越界，将会抛出此异常
     * @throws NullPointerException 如果 {@code src} 为 {@code null}，将会抛出此异常
     */
    public MysqlPacket writeFixedLengthBytes(byte[] src) throws IndexOutOfBoundsException, NullPointerException {
        if (src == null) {
            throw new IllegalArgumentException("Write fixed length bytes failed: `src is null`.");
        } else if (src.length > 0) {
            System.arraycopy(src, 0, payload, position, src.length);
            position += src.length;
        }
        return this;
    }

    /**
     * 以字节形式写入 "Protocol::FixedLengthInteger" 类型的无符号整数。
     *
     * <p>
     * 更多信息请参考：
     * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_dt_integers.html">
     *     Protocol::FixedLengthInteger
     * </a>
     * </p>
     *
     * @param length 字节长度
     * @param value 无符号整数
     * @return 当前 {@code MysqlPacket} 实例
     * @throws ArrayIndexOutOfBoundsException 如果访问数组越界，将会抛出此异常
     * @throws IllegalArgumentException 如果 {@code length} 或 {@code value} 的值没有在允许的范围内，将会抛出此异常
     */
    public MysqlPacket writeFixedLengthInteger(int length, long value) throws ArrayIndexOutOfBoundsException,
            IllegalArgumentException {
        BytesUtil.encodeUnsignedInteger(payload, position, length, value);
        position += length;
        return this;
    }

    /**
     * 以字节形式写入 "Protocol::LengthEncodedInteger" 类型的无符号整数。
     *
     * <p>
     * 更多信息请参考：
     * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_dt_integers.html">
     *     Protocol::LengthEncodedInteger
     * </a>
     * </p>
     *
     * @param value 无符号整数
     * @return 当前 {@code MysqlPacket} 实例
     * @throws ArrayIndexOutOfBoundsException 如果访问数组越界，将会抛出此异常
     * @throws IllegalArgumentException 如果 {@code value} 的值为负数，将会抛出此异常
     */
    public MysqlPacket writeLengthEncodedInteger(long value) throws ArrayIndexOutOfBoundsException, IllegalArgumentException {
        if (value < 251) {
            BytesUtil.encodeUnsignedInteger(payload, position++, 1, value);
        } else if (value < 65536) {
            BytesUtil.encodeUnsignedInteger(payload, position++, 1, 0xFC);
            BytesUtil.encodeUnsignedInteger(payload, position, 2, value);
            position += 2;
        } else if (value < 16777216) {
            BytesUtil.encodeUnsignedInteger(payload, position++, 1, 0xFD);
            BytesUtil.encodeUnsignedInteger(payload, position, 3, value);
            position += 3;
        } else {
            BytesUtil.encodeUnsignedInteger(payload, position++, 1, 0xFE);
            BytesUtil.encodeUnsignedInteger(payload, position, 8, value);
            position += 8;
        }
        return this;
    }

    /**
     * 以字节形式写入 "Protocol::NullTerminatedString" 类型的字符串。
     *
     * <p>
     * 更多信息请参考：
     * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_dt_strings.html">
     *     Protocol::NullTerminatedString
     * </a>
     * </p>
     *
     * @param text 字符串字节数组
     * @return 当前 {@code MysqlPacket} 实例
     * @throws ArrayIndexOutOfBoundsException 如果访问数组越界，将会抛出此异常
     * @throws IllegalArgumentException 如果 {@code text} 为 {@code null}，将会抛出此异常
     */
    public MysqlPacket writeNullTerminatedString(byte[] text) throws ArrayIndexOutOfBoundsException, IllegalArgumentException {
        if (text == null) {
            throw new IllegalArgumentException("Write null terminated string failed: `text is null`.");
        }
        writeFixedLengthBytes(text);
        payload[position++] = 0;
        return this;
    }

    /**
     * 以字节形式写入 "Protocol::LengthEncodedString" 类型的字符串。
     *
     * @param text 字符串字节数组
     * @return 当前 {@code MysqlPacket} 实例
     * @throws ArrayIndexOutOfBoundsException 如果访问数组越界，将会抛出此异常
     * @throws IllegalArgumentException 如果 {@code text} 为 {@code null}，将会抛出此异常
     */
    public MysqlPacket writeLengthEncodedString(byte[] text) throws ArrayIndexOutOfBoundsException, IllegalArgumentException {
        if (text == null) {
            throw new IllegalArgumentException("Write length encoded string failed: `text is null`.");
        }
        writeLengthEncodedInteger(text.length);
        writeFixedLengthBytes(text);
        return this;
    }

    /**
     * 根据已写入的内容信息，组装成 MYSQL 数据包字节数组。
     *
     * <p>
     * 更多信息请参考：
     * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_packets.html">
     *     MySQL Packets
     * </a>
     * </p>
     *
     * @return MYSQL 数据包字节数组
     * @throws IllegalArgumentException 如果已写入的内容信息为空，即 {@link #getPosition()} 为 0，将会抛出此异常
     */
    public byte[] buildMysqlPacketBytes() throws IllegalArgumentException {
        if (position == 0) {
            throw new IllegalArgumentException("Build mysql packet bytes failed: `payload is empty`.");
        }
        byte[] packetBytes = new byte[4 + position];
        BytesUtil.encodeUnsignedInteger(packetBytes, 0, 3, position);
        System.arraycopy(payload, 0, packetBytes, 4, position);
        return packetBytes;
    }

    @Override
    public String toString() {
        return "MysqlPacket{" +
                "sequenceId=" + sequenceId +
                ", payload=" + Arrays.toString(payload) +
                ", position=" + position +
                '}';
    }

    /**
     * 将该无符号整数转换为 "Protocol::LengthEncodedInteger" 类型所需的字节长度。
     *
     * <p>
     * 更多信息请参考：
     * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_dt_integers.html">
     *     Protocol::LengthEncodedInteger
     * </a>
     * </p>
     *
     * @param value 无符号整数
     * @return 转换为 "Protocol::LengthEncodedInteger" 类型所需的字节长度
     * @see #writeLengthEncodedInteger(long)
     */
    public static int getBytesLengthForLengthEncodedInteger(long value) {
        if (value < 251) {
            return 1;
        } else if (value < 65536) {
            return 3;
        } else if (value < 16777216) {
            return 4;
        } else {
            return 9;
        }
    }
}
