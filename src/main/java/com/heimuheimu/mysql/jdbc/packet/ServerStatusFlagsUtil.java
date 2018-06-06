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

import java.util.ArrayList;
import java.util.List;

/**
 * 提供 Mysql 服务端状态数值解析方法。
 *
 * <p>
 * 更多信息请参考：
 * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/mysql__com_8h.html#a1d854e841086925be1883e4d7b4e8cad">
 *     SERVER_STATUS_flags_enum
 * </a>、
 * <a href="https://dev.mysql.com/doc/internals/en/status-flags.html">
 *     Status Flags
 * </a>
 * </p>
 *
 * <p><strong>说明：</strong>{@code ServerStatusFlagsUtil} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class ServerStatusFlagsUtil {

    /**
     * "SERVER_STATUS_IN_TRANS" 状态对应的比特位索引位置
     */
    public static final int INDEX_SERVER_STATUS_IN_TRANS = 0;

    /**
     * "SERVER_STATUS_AUTOCOMMIT" 状态对应的比特位索引位置
     */
    public static final int INDEX_SERVER_STATUS_AUTOCOMMIT = 1;

    /**
     * "SERVER_MORE_RESULTS_EXISTS" 状态对应的比特位索引位置
     */
    public static final int INDEX_SERVER_MORE_RESULTS_EXISTS = 3;

    /**
     * "SERVER_QUERY_NO_GOOD_INDEX_USED" 状态对应的比特位索引位置
     */
    public static final int INDEX_SERVER_QUERY_NO_GOOD_INDEX_USED = 4;

    /**
     * "SERVER_QUERY_NO_INDEX_USED" 状态对应的比特位索引位置
     */
    public static final int INDEX_SERVER_QUERY_NO_INDEX_USED = 5;

    /**
     * "SERVER_STATUS_CURSOR_EXISTS" 状态对应的比特位索引位置
     */
    public static final int INDEX_SERVER_STATUS_CURSOR_EXISTS = 6;

    /**
     * "SERVER_STATUS_LAST_ROW_SENT" 状态对应的比特位索引位置
     */
    public static final int INDEX_SERVER_STATUS_LAST_ROW_SENT = 7;

    /**
     * "SERVER_STATUS_DB_DROPPED" 状态对应的比特位索引位置
     */
    public static final int INDEX_SERVER_STATUS_DB_DROPPED = 8;

    /**
     * "SERVER_STATUS_NO_BACKSLASH_ESCAPES" 状态对应的比特位索引位置
     */
    public static final int INDEX_SERVER_STATUS_NO_BACKSLASH_ESCAPES = 9;

    /**
     * "SERVER_STATUS_METADATA_CHANGED" 状态对应的比特位索引位置
     */
    public static final int INDEX_SERVER_STATUS_METADATA_CHANGED = 10;

    /**
     * "SERVER_QUERY_WAS_SLOW" 状态对应的比特位索引位置
     */
    public static final int INDEX_SERVER_QUERY_WAS_SLOW = 11;

    /**
     * "SERVER_PS_OUT_PARAMS" 状态对应的比特位索引位置
     */
    public static final int INDEX_SERVER_PS_OUT_PARAMS = 12;

    /**
     * "SERVER_STATUS_IN_TRANS_READONLY" 状态对应的比特位索引位置
     */
    public static final int INDEX_SERVER_STATUS_IN_TRANS_READONLY = 13;

    /**
     * "SERVER_SESSION_STATE_CHANGED" 状态对应的比特位索引位置
     */
    public static final int INDEX_SERVER_SESSION_STATE_CHANGED = 14;

    /**
     * 判断指定的 Mysql 服务端状态是否开启。
     *
     * @param serverStatusFlags Mysql 服务端状态数值
     * @param serverStatusIndex 状态对应的比特位索引位置，允许的值为：[0, 15]
     * @return 是否开启
     * @throws IllegalArgumentException 如果状态对应的比特位索引位置没有在允许的范围内，将会抛出此异常
     */
    public static boolean isServerStatusEnabled(int serverStatusFlags, int serverStatusIndex) throws IllegalArgumentException {
        if (serverStatusIndex < 0 || serverStatusIndex > 15) {
            throw new IllegalArgumentException("Check server status flag failed: `invalid server status index`. `serverStatusFlags`:`"
                    + serverStatusFlags + "`. `serverStatusIndex`:`" + serverStatusIndex + "`.");
        }
        return (serverStatusFlags & (1L << serverStatusIndex)) != 0;
    }

    /**
     * 根据 Mysql 服务端状态比特位索引位置获得对应的名称，该方法不会返回 {@code null}。
     *
     * @param serverStatusIndex 状态对应的比特位索引位置，允许的值为：[0, 15]
     * @return 状态名称
     * @throws IllegalArgumentException 如果状态对应的比特位索引位置没有在允许的范围内，将会抛出此异常
     */
    public static String getServerStatusName(int serverStatusIndex) throws IllegalArgumentException {
        switch (serverStatusIndex) {
            case INDEX_SERVER_STATUS_IN_TRANS:
                return "SERVER_STATUS_IN_TRANS";
            case INDEX_SERVER_STATUS_AUTOCOMMIT:
                return "SERVER_STATUS_AUTOCOMMIT";
            case INDEX_SERVER_MORE_RESULTS_EXISTS:
                return "SERVER_MORE_RESULTS_EXISTS";
            case INDEX_SERVER_QUERY_NO_GOOD_INDEX_USED:
                return "SERVER_QUERY_NO_GOOD_INDEX_USED";
            case INDEX_SERVER_QUERY_NO_INDEX_USED:
                return "SERVER_QUERY_NO_INDEX_USED";
            case INDEX_SERVER_STATUS_CURSOR_EXISTS:
                return "SERVER_STATUS_CURSOR_EXISTS";
            case INDEX_SERVER_STATUS_LAST_ROW_SENT:
                return "SERVER_STATUS_LAST_ROW_SENT";
            case INDEX_SERVER_STATUS_DB_DROPPED:
                return "SERVER_STATUS_DB_DROPPED";
            case INDEX_SERVER_STATUS_NO_BACKSLASH_ESCAPES:
                return "SERVER_STATUS_NO_BACKSLASH_ESCAPES";
            case INDEX_SERVER_STATUS_METADATA_CHANGED:
                return "SERVER_STATUS_METADATA_CHANGED";
            case INDEX_SERVER_QUERY_WAS_SLOW:
                return "SERVER_QUERY_WAS_SLOW";
            case INDEX_SERVER_PS_OUT_PARAMS:
                return "SERVER_PS_OUT_PARAMS";
            case INDEX_SERVER_STATUS_IN_TRANS_READONLY:
                return "SERVER_STATUS_IN_TRANS_READONLY";
            case INDEX_SERVER_SESSION_STATE_CHANGED:
                return "SERVER_SESSION_STATE_CHANGED";
            default:
                throw new IllegalArgumentException("Get server status name failed: `invalid server status index`. `serverStatusIndex`:`"
                        + serverStatusIndex + "`.");
        }
    }

    /**
     * 返回 Mysql 服务端状态数值中开启的状态名称列表，该方法不会返回 {@code null}。
     *
     * @param serverStatusFlags Mysql 服务端状态数值
     * @return 开启的状态名称列表
     */
    public static List<String> getEnabledServerStatusNames(int serverStatusFlags) {
        List<String> enabledServerStatusNames = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            if (i == 2 || i == 15) {// 该状态位暂未使用
                continue;
            } else {
                if (isServerStatusEnabled(serverStatusFlags, i)) {
                    enabledServerStatusNames.add(getServerStatusName(i));
                }
            }
        }
        return enabledServerStatusNames;
    }
}
