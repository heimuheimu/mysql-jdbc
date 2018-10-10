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

package com.heimuheimu.mysql.jdbc;

import com.heimuheimu.mysql.jdbc.packet.CharsetMappingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

/**
 * Mysql 数据库连接信息。
 *
 * <p><strong>说明：</strong>{@code ConnectionInfo} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class ConnectionInfo {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionInfo.class);

    /**
     * 当前 Mysql 连接 ID
     */
    private final long connectionId;

    /**
     * Mysql 服务端版本号
     */
    private final String serverVersion;

    /**
     * 主版本号
     */
    private final int majorVersionNumber;

    /**
     * 副版本号
     */
    private final int minorVersionNumber;

    /**
     * 小版本号
     */
    private final int subMinorVersionNumber;

    /**
     * Mysql 服务端默认字符集编码 ID，ID 对应的编码可通过数据库表 "information_schema.collations" 进行查询
     */
    private final int serverCharacterId;

    /**
     * Mysql 服务端支持的特性数值，每个比特位可代表不同的特性是否支持
     */
    private final long serverCapabilitiesFlags;

    /**
     * Mysql 连接建立完成后，Mysql 服务端初始状态数值
     */
    private final int serverStatusFlags;

    /**
     * 当前 Mysql 连接使用的字符集编码 ID，ID 对应的编码可通过数据库表 "information_schema.collations" 进行查询
     */
    private final int characterId;

    /**
     * 当前 Mysql 连接支持的特性数值，每个比特位可代表不同的特性是否支持
     */
    private final long capabilitiesFlags;

    /**
     * 当前 Mysql 连接使用的数据库名称
     */
    private final String databaseName;

    /**
     * 构造一个 Mysql 数据库连接信息。
     *
     * @param connectionId 当前 Mysql 连接 ID
     * @param serverVersion Mysql 服务端版本号
     * @param serverCharacterId Mysql 服务端默认字符集编码 ID
     * @param serverCapabilitiesFlags Mysql 服务端支持的特性数值
     * @param serverStatusFlags Mysql 连接建立完成后，Mysql 服务端初始状态数值
     * @param characterId 当前 Mysql 连接使用的字符集编码 ID
     * @param capabilitiesFlags 当前 Mysql 连接支持的特性数值
     * @param databaseName 当前 Mysql 连接使用的数据库名称
     */
    public ConnectionInfo(long connectionId, String serverVersion, int serverCharacterId, long serverCapabilitiesFlags,
                          int serverStatusFlags, int characterId, long capabilitiesFlags, String databaseName) {
        this.connectionId = connectionId;
        this.serverVersion = serverVersion;
        this.serverCharacterId = serverCharacterId;
        this.serverCapabilitiesFlags = serverCapabilitiesFlags;
        this.serverStatusFlags = serverStatusFlags;
        this.characterId = characterId;
        this.capabilitiesFlags = capabilitiesFlags;
        this.databaseName = databaseName;

        int[] versionNumbers = parseServerVersion(serverVersion);
        this.majorVersionNumber = versionNumbers[0];
        this.minorVersionNumber = versionNumbers[1];
        this.subMinorVersionNumber = versionNumbers[2];
    }

    /**
     * 获得当前 Mysql 连接 ID。
     *
     * @return 当前 Mysql 连接 ID
     */
    public long getConnectionId() {
        return connectionId;
    }

    /**
     * 获得 Mysql 服务端版本号。
     *
     * @return Mysql 服务端版本号
     */
    public String getServerVersion() {
        return serverVersion;
    }

    /**
     * 获得主版本号。
     *
     * @return 主版本号
     */
    public int getMajorVersionNumber() {
        return majorVersionNumber;
    }

    /**
     * 获得副版本号。
     *
     * @return 副版本号
     */
    public int getMinorVersionNumber() {
        return minorVersionNumber;
    }

    /**
     * 获得小版本号。
     *
     * @return 小版本号
     */
    public int getSubMinorVersionNumber() {
        return subMinorVersionNumber;
    }

    /**
     * 获得 Mysql 服务端默认字符集编码 ID，ID 对应的编码可通过数据库表 "information_schema.collations" 进行查询。
     *
     * @return Mysql 服务端默认字符集编码 ID
     */
    public int getServerCharacterId() {
        return serverCharacterId;
    }

    /**
     * 获得 Mysql 服务端支持的特性数值，每个比特位可代表不同的特性是否支持。
     *
     * @return Mysql 服务端支持的特性数值
     */
    public long getServerCapabilitiesFlags() {
        return serverCapabilitiesFlags;
    }

    /**
     * 获得 Mysql 连接建立完成后，Mysql 服务端初始状态数值
     *
     * @return Mysql 连接建立完成后，Mysql 服务端初始状态数值
     */
    public int getServerStatusFlags() {
        return serverStatusFlags;
    }

    /**
     * 获得当前 Mysql 连接使用的字符集编码 ID，ID 对应的编码可通过数据库表 "information_schema.collations" 进行查询。
     *
     * @return 当前 Mysql 连接使用的字符集编码 ID
     */
    public int getCharacterId() {
        return characterId;
    }

    /**
     * 获得和当前 Mysql 连接使用的字符集编码 ID 对应的 Java 编码。
     *
     * @return 和当前 Mysql 连接使用的字符集编码 ID 对应的 Java 编码
     */
    public Charset getJavaCharset() {
        return CharsetMappingUtil.getJavaCharset(characterId);
    }

    /**
     * 获得当前 Mysql 连接支持的特性数值，每个比特位可代表不同的特性是否支持。
     *
     * @return 当前 Mysql 连接支持的特性数值
     */
    public long getCapabilitiesFlags() {
        return capabilitiesFlags;
    }

    /**
     * 获得当前 Mysql 连接使用的数据库名称。
     *
     * @return 当前 Mysql 连接使用的数据库名称
     */
    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * 判断当前 Mysql 服务端版本号是否满足指定的最小版本号要求。
     *
     * @param majorVersion 最小主版本号
     * @param minorVersion 最小副版本号
     * @param subMinorVersion 最小小版本号
     * @return 是否满足指定的最小版本号要求
     */
    public boolean versionMeetsMinimum(int majorVersion, int minorVersion, int subMinorVersion) {
        if (this.majorVersionNumber > majorVersion) {
            return true;
        } else if (this.majorVersionNumber == majorVersion) {
            if (this.minorVersionNumber > minorVersion) {
                return true;
            } else if (this.minorVersionNumber == minorVersion) {
                return this.subMinorVersionNumber >= subMinorVersion;
            }
        }
        return false;
    }

    /**
     * 将 Mysql 服务端版本号解析为各版本号数字后，以数组形式返回。
     *
     * @param serverVersion Mysql 服务端版本号
     * @return 各版本号数字数组
     */
    private int[] parseServerVersion(String serverVersion) {
        int majorVersionNumber = 0;
        int minorVersionNumber = 0;
        int subMinorVersionNumber = 0;
        try {
            int pointIndex = serverVersion.indexOf('.');
            if (pointIndex > 0) {
                majorVersionNumber = Integer.parseInt(serverVersion.substring(0, pointIndex));
                String remainingServerVersion = serverVersion.substring(pointIndex + 1);
                pointIndex = remainingServerVersion.indexOf('.');
                if (pointIndex > 0) {
                    minorVersionNumber = Integer.parseInt(remainingServerVersion.substring(0, pointIndex));
                    remainingServerVersion = remainingServerVersion.substring(pointIndex + 1);

                    int digitLength = 0;
                    while (digitLength < remainingServerVersion.length()) {
                        if ((remainingServerVersion.charAt(digitLength) < '0') || (remainingServerVersion.charAt(digitLength) > '9')) {
                            break;
                        }

                        digitLength++;
                    }
                    if (digitLength > 0) {
                        subMinorVersionNumber = Integer.parseInt(remainingServerVersion.substring(0, digitLength));
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Parse server version failed. `serverVersion`: `" + serverVersion + "`.", e);
        }
        return new int[]{majorVersionNumber, minorVersionNumber, subMinorVersionNumber};
    }

    @Override
    public String toString() {
        return "ConnectionInfo{" +
                "connectionId=" + connectionId +
                ", serverVersion='" + serverVersion + '\'' +
                ", majorVersionNumber=" + majorVersionNumber +
                ", minorVersionNumber=" + minorVersionNumber +
                ", subMinorVersionNumber=" + subMinorVersionNumber +
                ", serverCharacterId=" + serverCharacterId +
                ", serverCapabilitiesFlags=" + serverCapabilitiesFlags +
                ", serverStatusFlags=" + serverStatusFlags +
                ", characterId=" + characterId +
                ", capabilitiesFlags=" + capabilitiesFlags +
                ", databaseName='" + databaseName + '\'' +
                '}';
    }
}
