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

import java.nio.charset.Charset;

/**
 * "COM_QUERY" 数据包信息，用于向 Mysql 服务端发送 SQL 命令，更多信息请参考：
 * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_query.html">
 *     COM_QUERY
 * </a>
 *
 * <p><strong>说明：</strong>{@code CommandQueryPacket} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class CommandQueryPacket {

    /**
     * SQL 命令
     */
    private final String sql;

    /**
     * 构造一个 Mysql "COM_QUERY" 数据包。
     *
     * @param sql SQL 命令，不允许为 {@code null} 或空
     * @throws IllegalArgumentException 如果 SQL 命令为 {@code null} 或空，将会抛出此异常
     */
    public CommandQueryPacket(String sql) throws IllegalArgumentException {
        if (sql == null || sql.isEmpty()) {
            throw new IllegalArgumentException("Construct `CommandQueryPacket` failed: `sql is empty`. `invalidSql`:`" + sql + "`.");
        }
        this.sql = sql;
    }

    /**
     * 获得 SQL 命令。
     *
     * @return SQL 命令
     */
    public String getSql() {
        return sql;
    }

    @Override
    public String toString() {
        return "CommandQueryPacket{" +
                "sql='" + sql + '\'' +
                '}';
    }

    /**
     * 根据当前 {@code CommandQueryPacket} 实例信息，生成对应的 Mysql "COM_QUERY" 数据包字节数组，
     * "COM_QUERY" 数据包格式定义：
     * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_query.html">
     *     COM_QUERY
     * </a>
     *
     * @param charset 字符集编码
     * @return "COM_QUERY" 数据包字节数组
     */
    public byte[] buildMysqlPacketBytes(Charset charset) {
        byte[] sqlBytes = sql.getBytes(charset);
        MysqlPacket mysqlPacket = new MysqlPacket(0, new byte[1 + sqlBytes.length]);
        mysqlPacket.writeFixedLengthInteger(1, 0x03);
        mysqlPacket.writeFixedLengthBytes(sqlBytes);
        return mysqlPacket.buildMysqlPacketBytes();
    }
}
