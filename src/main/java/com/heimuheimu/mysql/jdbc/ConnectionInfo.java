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

import java.nio.charset.Charset;

/**
 * Mysql 数据库连接信息。
 *
 * <p><strong>说明：</strong>{@code ConnectionInfo} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class ConnectionInfo {

    /**
     * 当前 Mysql 连接 ID
     */
    private final long connectionId;

    /**
     * Mysql 服务端版本号
     */
    private final String serverVersion;

    /**
     * Mysql 服务端默认字符集编码 ID，ID 对应的编码可通过数据库表 "information_schema.collations" 进行查询
     */
    private final int serverCharacterId;

    /**
     * Mysql 服务端支持的特性数值，每个比特位可代表不同的特性是否支持
     */
    private final long serverCapabilitiesFlags;

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
     * @param characterId 当前 Mysql 连接使用的字符集编码 ID
     * @param capabilitiesFlags 当前 Mysql 连接支持的特性数值
     * @param databaseName 当前 Mysql 连接使用的数据库名称
     */
    public ConnectionInfo(long connectionId, String serverVersion, int serverCharacterId, long serverCapabilitiesFlags,
                          int characterId, long capabilitiesFlags, String databaseName) {
        this.connectionId = connectionId;
        this.serverVersion = serverVersion;
        this.serverCharacterId = serverCharacterId;
        this.serverCapabilitiesFlags = serverCapabilitiesFlags;
        this.characterId = characterId;
        this.capabilitiesFlags = capabilitiesFlags;
        this.databaseName = databaseName;
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

    @Override
    public String toString() {
        return "ConnectionInfo{" +
                "connectionId=" + connectionId +
                ", serverVersion='" + serverVersion + '\'' +
                ", serverCharacterId=" + serverCharacterId +
                ", serverCapabilitiesFlags=" + serverCapabilitiesFlags +
                ", characterId=" + characterId +
                ", javaCharset='" + getJavaCharset() + '\'' +
                ", capabilitiesFlags=" + capabilitiesFlags +
                ", databaseName='" + databaseName + '\'' +
                '}';
    }
}
