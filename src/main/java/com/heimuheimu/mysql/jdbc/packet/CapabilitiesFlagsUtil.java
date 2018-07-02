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
 * 提供 Mysql 客户端可使用的特性数值解析和生成方法。
 *
 * <p>
 * 更多信息请参考：
 * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/group__group__cs__capabilities__flags.html">
 *     Capabilities Flags
 * </a>
 * </p>
 *
 * <p><strong>说明：</strong>{@code CapabilitiesFlagsUtil} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class CapabilitiesFlagsUtil {

    /**
     * "CLIENT_LONG_PASSWORD" 特性对应的比特位索引位置
     */
    public static final int INDEX_CLIENT_LONG_PASSWORD = 0;

    /**
     * "CLIENT_FOUND_ROWS" 特性对应的比特位索引位置
     */
    public static final int INDEX_CLIENT_FOUND_ROWS = 1;

    /**
     * "CLIENT_LONG_FLAG" 特性对应的比特位索引位置
     */
    public static final int INDEX_CLIENT_LONG_FLAG = 2;

    /**
     * "CLIENT_CONNECT_WITH_DB" 特性对应的比特位索引位置
     */
    public static final int INDEX_CLIENT_CONNECT_WITH_DB = 3;

    /**
     * "CLIENT_NO_SCHEMA " 特性对应的比特位索引位置
     */
    public static final int INDEX_CLIENT_NO_SCHEMA = 4;

    /**
     * "CLIENT_COMPRESS " 特性对应的比特位索引位置
     */
    public static final int INDEX_CLIENT_COMPRESS = 5;

    /**
     * "CLIENT_ODBC" 特性对应的比特位索引位置
     */
    public static final int INDEX_CLIENT_ODBC = 6;

    /**
     * "CLIENT_LOCAL_FILES" 特性对应的比特位索引位置
     */
    public static final int INDEX_CLIENT_LOCAL_FILES = 7;

    /**
     * "CLIENT_IGNORE_SPACE" 特性对应的比特位索引位置
     */
    public static final int INDEX_CLIENT_IGNORE_SPACE = 8;

    /**
     * "CLIENT_PROTOCOL_41" 特性对应的比特位索引位置
     */
    public static final int INDEX_CLIENT_PROTOCOL_41 = 9;

    /**
     * "CLIENT_INTERACTIVE" 特性对应的比特位索引位置
     */
    public static final int INDEX_CLIENT_INTERACTIVE = 10;

    /**
     * "CLIENT_SSL" 特性对应的比特位索引位置
     */
    public static final int INDEX_CLIENT_SSL = 11;

    /**
     * "CLIENT_IGNORE_SIGPIPE" 特性对应的比特位索引位置
     */
    public static final int INDEX_CLIENT_IGNORE_SIGPIPE = 12;

    /**
     * "CLIENT_TRANSACTIONS" 特性对应的比特位索引位置
     */
    public static final int INDEX_CLIENT_TRANSACTIONS = 13;

    /**
     * "CLIENT_RESERVED" 特性对应的比特位索引位置
     */
    public static final int INDEX_CLIENT_RESERVED = 14;

    /**
     * "CLIENT_RESERVED2" 特性对应的比特位索引位置
     */
    public static final int INDEX_CLIENT_RESERVED2 = 15;

    /**
     * "CLIENT_MULTI_STATEMENTS" 特性对应的比特位索引位置
     */
    public static final int INDEX_CLIENT_MULTI_STATEMENTS = 16;

    /**
     * "CLIENT_MULTI_RESULTS" 特性对应的比特位索引位置
     */
    public static final int INDEX_CLIENT_MULTI_RESULTS = 17;

    /**
     * "CLIENT_PS_MULTI_RESULTS" 特性对应的比特位索引位置
     */
    public static final int INDEX_CLIENT_PS_MULTI_RESULTS = 18;

    /**
     * "CLIENT_PLUGIN_AUTH" 特性对应的比特位索引位置
     */
    public static final int INDEX_CLIENT_PLUGIN_AUTH = 19;

    /**
     * "CLIENT_CONNECT_ATTRS" 特性对应的比特位索引位置
     */
    public static final int INDEX_CLIENT_CONNECT_ATTRS = 20;

    /**
     * "CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA" 特性对应的比特位索引位置
     */
    public static final int INDEX_CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA = 21;

    /**
     * "CLIENT_CAN_HANDLE_EXPIRED_PASSWORDS" 特性对应的比特位索引位置
     */
    public static final int INDEX_CLIENT_CAN_HANDLE_EXPIRED_PASSWORDS = 22;

    /**
     * "CLIENT_SESSION_TRACK" 特性对应的比特位索引位置
     */
    public static final int INDEX_CLIENT_SESSION_TRACK = 23;

    /**
     * "CLIENT_DEPRECATE_EOF" 特性对应的比特位索引位置
     */
    public static final int INDEX_CLIENT_DEPRECATE_EOF = 24;

    /**
     * "CLIENT_OPTIONAL_RESULTSET_METADATA" 特性对应的比特位索引位置
     */
    public static final int INDEX_CLIENT_OPTIONAL_RESULTSET_METADATA = 25;

    /**
     * "CLIENT_SSL_VERIFY_SERVER_CERT" 特性对应的比特位索引位置
     */
    public static final int INDEX_CLIENT_SSL_VERIFY_SERVER_CERT = 30;

    /**
     * "CLIENT_REMEMBER_OPTIONS" 特性对应的比特位索引位置
     */
    public static final int INDEX_CLIENT_REMEMBER_OPTIONS = 31;

    /**
     * 判断指定的 Mysql 客户端特性是否开启。
     *
     * @param capabilitiesFlags Mysql 客户端可使用的特性数值
     * @param capabilityIndex 特性对应的比特位索引位置，允许的值为：[0, 31]
     * @return 是否开启
     * @throws IllegalArgumentException 如果特性对应的比特位索引位置没有在允许的范围内，将会抛出此异常
     */
    public static boolean isCapabilityEnabled(long capabilitiesFlags, int capabilityIndex) throws IllegalArgumentException {
        if (capabilityIndex < 0 || capabilityIndex > 31) {
            throw new IllegalArgumentException("Check capability flag failed: `invalid capability index`. `capabilitiesFlags`:`"
                + capabilitiesFlags + "`. `capabilityIndex`:`" + capabilityIndex + "`.");
        }
        return (capabilitiesFlags & (1L << capabilityIndex)) != 0;
    }

    /**
     * 开启指定的 Mysql 客户端特性，并保留特性数值的其它特性设置。
     *
     * @param capabilitiesFlags Mysql 客户端可使用的特性数值
     * @param capabilityIndex 特性对应的比特位索引位置，允许的值为：[0, 31]
     * @return 开启指定的 Mysql 客户端特性后的特性数值
     * @throws IllegalArgumentException 如果特性对应的比特位索引位置没有在允许的范围内，将会抛出此异常
     */
    public static long enableCapability(long capabilitiesFlags, int capabilityIndex) throws IllegalArgumentException {
        if (capabilityIndex < 0 || capabilityIndex > 31) {
            throw new IllegalArgumentException("Enable capability flag failed: `invalid capability index`. `capabilitiesFlags`:`"
                    + capabilitiesFlags + "`. `capabilityIndex`:`" + capabilityIndex + "`.");
        }
        return capabilitiesFlags | (1L << capabilityIndex);
    }

    /**
     * 关闭指定的 Mysql 客户端特性，并保留特性数值的其它特性设置。
     *
     * @param capabilitiesFlags Mysql 客户端可使用的特性数值
     * @param capabilityIndex 特性对应的比特位索引位置，允许的值为：[0, 31]
     * @return 关闭指定的 Mysql 客户端特性后的特性数值
     * @throws IllegalArgumentException 如果特性对应的比特位索引位置没有在允许的范围内，将会抛出此异常
     */
    public static long disableCapability(long capabilitiesFlags, int capabilityIndex) throws IllegalArgumentException {
        if (capabilityIndex < 0 || capabilityIndex > 31) {
            throw new IllegalArgumentException("Disable capability flag failed: `invalid capability index`. `capabilitiesFlags`:`"
                    + capabilitiesFlags + "`. `capabilityIndex`:`" + capabilityIndex + "`.");
        }
        long maskCapability = ~enableCapability(0, capabilityIndex);
        return capabilitiesFlags & maskCapability;
    }

    /**
     * 根据 Mysql 客户端特性比特位索引位置获得对应的名称，该方法不会返回 {@code null}。
     *
     * @param capabilityIndex 特性对应的比特位索引位置，允许的值为：[0, 31]
     * @return 特性名称
     * @throws IllegalArgumentException 如果特性对应的比特位索引位置没有在允许的范围内，将会抛出此异常
     */
    public static String getCapabilityName(int capabilityIndex) throws IllegalArgumentException {
        switch (capabilityIndex) {
            case INDEX_CLIENT_LONG_PASSWORD:
                return "CLIENT_LONG_PASSWORD";
            case INDEX_CLIENT_FOUND_ROWS:
                return "CLIENT_FOUND_ROWS";
            case INDEX_CLIENT_LONG_FLAG:
                return "CLIENT_LONG_FLAG";
            case INDEX_CLIENT_CONNECT_WITH_DB:
                return "CLIENT_CONNECT_WITH_DB";
            case INDEX_CLIENT_NO_SCHEMA:
                return "CLIENT_NO_SCHEMA";
            case INDEX_CLIENT_COMPRESS:
                return "CLIENT_COMPRESS";
            case INDEX_CLIENT_ODBC:
                return "CLIENT_ODBC";
            case INDEX_CLIENT_LOCAL_FILES:
                return "CLIENT_LOCAL_FILES";
            case INDEX_CLIENT_IGNORE_SPACE:
                return "CLIENT_IGNORE_SPACE";
            case INDEX_CLIENT_PROTOCOL_41:
                return "CLIENT_PROTOCOL_41";
            case INDEX_CLIENT_INTERACTIVE:
                return "CLIENT_INTERACTIVE";
            case INDEX_CLIENT_SSL:
                return "CLIENT_SSL";
            case INDEX_CLIENT_IGNORE_SIGPIPE:
                return "CLIENT_IGNORE_SIGPIPE";
            case INDEX_CLIENT_TRANSACTIONS:
                return "CLIENT_TRANSACTIONS";
            case INDEX_CLIENT_RESERVED:
                return "CLIENT_RESERVED";
            case INDEX_CLIENT_RESERVED2:
                return "CLIENT_RESERVED2";
            case INDEX_CLIENT_MULTI_STATEMENTS:
                return "CLIENT_MULTI_STATEMENTS";
            case INDEX_CLIENT_MULTI_RESULTS:
                return "CLIENT_MULTI_RESULTS";
            case INDEX_CLIENT_PS_MULTI_RESULTS:
                return "CLIENT_PS_MULTI_RESULTS";
            case INDEX_CLIENT_PLUGIN_AUTH:
                return "CLIENT_PLUGIN_AUTH";
            case INDEX_CLIENT_CONNECT_ATTRS:
                return "CLIENT_CONNECT_ATTRS";
            case INDEX_CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA:
                return "CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA";
            case INDEX_CLIENT_CAN_HANDLE_EXPIRED_PASSWORDS:
                return "CLIENT_CAN_HANDLE_EXPIRED_PASSWORDS";
            case INDEX_CLIENT_SESSION_TRACK:
                return "CLIENT_SESSION_TRACK";
            case INDEX_CLIENT_DEPRECATE_EOF:
                return "CLIENT_DEPRECATE_EOF";
            case INDEX_CLIENT_OPTIONAL_RESULTSET_METADATA:
                return "CLIENT_OPTIONAL_RESULTSET_METADATA";
            case INDEX_CLIENT_SSL_VERIFY_SERVER_CERT:
                return "CLIENT_SSL_VERIFY_SERVER_CERT";
            case INDEX_CLIENT_REMEMBER_OPTIONS:
                return "CLIENT_REMEMBER_OPTIONS";
            default:
                throw new IllegalArgumentException("Get capability name failed: `invalid capability index`. `capabilityIndex`:`"
                        + capabilityIndex + "`.");
        }
    }

    /**
     * 返回 Mysql 客户端可使用的特性数值中已开启的特性名称列表，该方法不会返回 {@code null}。
     *
     * @param capabilitiesFlags Mysql 客户端可使用的特性数值
     * @return 开启的特性名称列表
     */
    public static List<String> getEnabledCapabilitiesNames(long capabilitiesFlags) {
        List<String> enabledCapabilitiesNames = new ArrayList<>();
        for (int i = 0; i < 32; i++) {
            if (i > 25 && i < 30) {
                continue;
            } else {
                if (isCapabilityEnabled(capabilitiesFlags, i)) {
                    enabledCapabilitiesNames.add(getCapabilityName(i));
                }
            }
        }
        return enabledCapabilitiesNames;
    }
}
