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

import com.heimuheimu.mysql.jdbc.packet.MysqlPacket;

/**
 * "ERR_Packet" 数据包信息，当 Mysql 服务端执行错误时，将通过该数据包进行响应，更多信息请参考：
 * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_err_packet.html">
 *     ERR_Packet
 * </a>
 *
 * <p>
 * Mysql 服务端错误代码和消息定义请参考（建议选择与 Mysql 服务端版本一致的手册进行参考）：
 * <a href="https://dev.mysql.com/doc/refman/8.0/en/error-messages-server.html">
 *     Server Error Codes and Messages
 * </a>
 * </p>
 *
 * <p><strong>说明：</strong>{@code ErrorPacket} 类是非线程安全的，不允许多个线程使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class ErrorPacket {

    /**
     * Mysql 错误代码
     */
    private final int errorCode;

    /**
     * 标准化的 SQL 错误状态码，可能为空
     */
    private final String sqlState;

    /**
     * Mysql 错误描述信息
     */
    private final String errorMessage;

    /**
     * 构造一个 Mysql Error 响应数据包信息。
     *
     * @param errorCode Mysql 错误代码
     * @param sqlState 标准化的 SQL 错误状态码
     * @param errorMessage Mysql 错误描述信息
     */
    public ErrorPacket(int errorCode, String sqlState, String errorMessage) {
        this.errorCode = errorCode;
        this.sqlState = sqlState;
        this.errorMessage = errorMessage;
    }

    /**
     * 获得 Mysql 错误代码。
     *
     * @return Mysql 错误代码
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * 获得标准化的 SQL 错误状态码，可能为空。
     *
     * @return 标准化的 SQL 错误状态码，可能为空
     */
    public String getSqlState() {
        return sqlState;
    }

    /**
     * 获得 Mysql 错误描述信息。
     *
     * @return Mysql 错误描述信息
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        return "ErrorPacket{" +
                "errorCode=" + errorCode +
                ", sqlState='" + sqlState + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }

    /**
     * 判断该 Mysql 数据包是否为 "ERR_Packet" 数据包。
     *
     * @param mysqlPacket Mysql 数据包
     * @return 是否为 "ERR_Packet" 数据包
     */
    public static boolean isErrorPacket(MysqlPacket mysqlPacket) {
        int initialPosition = mysqlPacket.getPosition();
        mysqlPacket.setPosition(0);
        int header = (int) mysqlPacket.readFixedLengthInteger(1);
        mysqlPacket.setPosition(initialPosition);
        return header == 0xFF;
    }

    /**
     * 对 Mysql "ERR_Packet" 数据包进行解析，生成对应的 {@code ErrorPacket} 实例，"ERR_Packet" 数据包格式定义：
     * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_err_packet.html">
     *     ERR_Packet
     * </a>
     *
     * @param packet "ERR_Packet" 数据包
     * @return {@code ErrorPacket} 实例
     * @throws IllegalArgumentException 如果 Mysql 数据包不是正确的 "ERR_Packet" 数据包，将会抛出此异常
     */
    public static ErrorPacket parse(MysqlPacket packet) throws IllegalArgumentException {
        return null;
    }
}
