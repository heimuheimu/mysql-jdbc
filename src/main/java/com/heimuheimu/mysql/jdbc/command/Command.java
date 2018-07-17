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

package com.heimuheimu.mysql.jdbc.command;

import com.heimuheimu.mysql.jdbc.packet.MysqlPacket;

import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.List;

/**
 * Mysql 命令，提供获取该命令的请求数据包、解析该命令的响应数据包等操作。
 *
 * <p><strong>说明：</strong>{@code Command} 的实现类必须是线程安全的。</p>
 *
 * @author heimuheimu
 */
public interface Command {

    /**
     * 获得该命令对应的请求数据包。
     *
     * @return 该命令对应的请求数据包
     */
    byte[] getRequestByteArray();

    /**
     * {@link com.heimuheimu.mysql.jdbc.channel.MysqlChannel} 在发送完命令对应的请求数据包后，
     * 会通过该方法判断该命令是否需要继续接收响应数据包。
     *
     * @return 该命令是否需要继续接收响应数据包
     * @see #receiveResponsePacket(MysqlPacket)
     */
    boolean hasResponsePacket();

    /**
     * 在 {@link #hasResponsePacket()} 方法返回 {@code true} 后，{@link com.heimuheimu.mysql.jdbc.channel.MysqlChannel}
     * 将会把下一个接收到响应数据包传入该方法。
     *
     * @param responsePacket 响应数据包
     * @throws SQLException 当接收到非预期响应包时，将抛出此异常
     * @see #hasResponsePacket()
     */
    void receiveResponsePacket(MysqlPacket responsePacket) throws SQLException;

    /**
     * 获得该命令对应的响应数据包列表，该方法不会返回 {@code null}。
     *
     * @param timeout 超时时间，单位：毫秒
     * @return 该命令对应的响应数据包列表
     * @throws SQLException 等待响应数据过程中，命令被关闭，将抛出此异常
     * @throws SQLTimeoutException 等待响应数据超时，将抛出此异常
     */
    List<MysqlPacket> getResponsePacketList(long timeout) throws SQLException;

    /**
     * 关闭该命令，如果该命令处于等待响应数据包状态，应立刻释放。
     */
    void close();

    /**
     * Mysql 命令执行完后，返回的 Mysql 服务端状态信息，该方法允许返回 {@code null}。
     *
     * @return Mysql 服务端状态信息，允许返回 {@code null}
     */
    MysqlServerStatusInfo getServerStatusInfo();
}
