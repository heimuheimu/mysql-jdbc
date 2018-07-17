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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 为 Mysql 命令提供响应数据包接收、获取等基础操作。
 *
 * <p><strong>说明：</strong>{@code AbstractCommand} 类是线程安全的。</p>
 *
 * @author heimuheimu
 */
public abstract class AbstractCommand implements Command {

    private final CountDownLatch latch = new CountDownLatch(1);

    private final List<MysqlPacket> responsePacketList = new ArrayList<>();

    private final Object responseLock = new Object();

    private volatile boolean isCompleted = false;

    @Override
    public boolean hasResponsePacket() {
        return !isCompleted;
    }

    @Override
    public void receiveResponsePacket(MysqlPacket responsePacket) throws SQLException {
        boolean isLastPacket = isLastPacket(responsePacket);

        synchronized (responseLock) {
            responsePacketList.add(responsePacket);
        }

        if (isLastPacket) {
            isCompleted = true;
            latch.countDown();
        }
    }

    @Override
    public List<MysqlPacket> getResponsePacketList(long timeout) throws SQLException {
        boolean latchFlag;
        try {
            latchFlag = latch.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) { // never happened
            throw new SQLException("Get mysql command response packet failed: `command has been interrupted`. Timeout: `"
                    + timeout + "ms`. Command: `" + this + "`.", e);
        }

        if (latchFlag) {
            if (isCompleted) {
                synchronized (responseLock) {
                    return responsePacketList;
                }
            } else {
                throw new SQLException("Get mysql command response packet failed: `command has been closed`. Timeout: `"
                        + timeout + "ms`. Command: `" + this + "`.");
            }
        } else {
            throw new SQLTimeoutException("Get mysql command response packet failed: `wait response timeout`. Timeout: `"
                + timeout + "ms`. Command: `" + this + "`.");
        }
    }

    @Override
    public void close() {
        latch.countDown();
    }

    @Override
    public MysqlServerStatusInfo getServerStatusInfo() {
        return null;
    }

    /**
     * 判断收到的响应数据包是否为该命令的最后一个数据包。
     *
     * @param responsePacket 收到的响应数据包
     * @return 是否为该命令的最后一个响应数据包
     * @throws SQLException 当接收到非预期响应包时，将抛出此异常
     */
    protected abstract boolean isLastPacket(MysqlPacket responsePacket) throws SQLException;
}
