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

import com.heimuheimu.mysql.jdbc.packet.ServerStatusFlagsUtil;

/**
 * Mysql 服务端状态信息。
 *
 * <p><strong>说明：</strong>{@code MysqlServerStatusInfo} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class MysqlServerStatusInfo {

    /**
     * Mysql 服务端状态数值，每个比特位可代表不同的服务端状态
     */
    private final int serverStatusFlags;

    /**
     * Mysql 服务端状态信息。
     *
     * @param serverStatusFlags Mysql 服务端状态信息
     */
    public MysqlServerStatusInfo(int serverStatusFlags) {
        this.serverStatusFlags = serverStatusFlags;
    }

    /**
     * 判断当前 Mysql 连接是否为自动提交事务。
     *
     * @return 是否为自动提交事务
     */
    public boolean isAutoCommit() {
        return ServerStatusFlagsUtil.isServerStatusEnabled(serverStatusFlags, ServerStatusFlagsUtil.INDEX_SERVER_STATUS_AUTOCOMMIT);
    }

    /**
     * 判断当前 Mysql 连接在服务端是否处于事务中。
     *
     * @return 是否处于事务中
     */
    public boolean isInTransaction() {
        return ServerStatusFlagsUtil.isServerStatusEnabled(serverStatusFlags, ServerStatusFlagsUtil.INDEX_SERVER_STATUS_IN_TRANS);
    }

    /**
     * 判断当前 Mysql 连接在服务端是否处于只读事务中。
     *
     * @return 是否处于只读事务中
     */
    public boolean isInReadonlyTransaction() {
        return ServerStatusFlagsUtil.isServerStatusEnabled(serverStatusFlags, ServerStatusFlagsUtil.INDEX_SERVER_STATUS_IN_TRANS_READONLY);
    }

    /**
     * 判断当前 Mysql 连接执行的 SQL 查询语句是否未使用最优的索引。
     *
     * @return SQL 查询语句是否未使用最优的索引
     */
    public boolean isQueryNoGoodIndexUsed() {
        return ServerStatusFlagsUtil.isServerStatusEnabled(serverStatusFlags, ServerStatusFlagsUtil.INDEX_SERVER_QUERY_NO_GOOD_INDEX_USED);
    }

    /**
     * 判断当前 Mysql 连接执行的 SQL 查询语句是否未使用索引。
     *
     * @return SQL 查询语句是否未使用索引
     */
    public boolean isQueryNoIndexUsed() {
        return ServerStatusFlagsUtil.isServerStatusEnabled(serverStatusFlags, ServerStatusFlagsUtil.INDEX_SERVER_QUERY_NO_INDEX_USED);
    }

    /**
     * 判断当前 Mysql 连接执行的 SQL 语句是否为慢查。
     *
     * @return SQL 语句是否为慢查
     */
    public boolean isSlow() {
        return ServerStatusFlagsUtil.isServerStatusEnabled(serverStatusFlags, ServerStatusFlagsUtil.INDEX_SERVER_QUERY_WAS_SLOW);
    }

    @Override
    public String toString() {
        return "MysqlServerStatusInfo{" +
                "serverStatusFlags=" + serverStatusFlags +
                ", isAutoCommit=" + isAutoCommit() +
                ", isInTransaction=" + isInTransaction() +
                ", isInReadonlyTransaction=" + isInReadonlyTransaction() +
                ", isQueryNoGoodIndexUsed=" + isQueryNoGoodIndexUsed() +
                ", isQueryNoIndexUsed=" + isQueryNoIndexUsed() +
                ", isSlow=" + isSlow() +
                '}';
    }
}
