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

import com.heimuheimu.mysql.jdbc.net.SocketConfiguration;
import com.heimuheimu.mysql.jdbc.util.MysqlConnectionBuildUtil;
import com.heimuheimu.mysql.jdbc.util.StringUtil;

import java.net.MalformedURLException;
import java.util.Map;

/**
 * 建立 Mysql 数据库连接使用的配置信息。
 *
 * <p><strong>说明：</strong>{@code ConnectionConfiguration} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class ConnectionConfiguration {

    /**
     * Mysql 地址，由主机名和端口组成，":" 符号分割，例如：localhost:3306
     */
    private final String host;

    /**
     * Mysql 数据库名称
     */
    private final String databaseName;

    /**
     * Mysql 数据库用户名
     */
    private final String username;

    /**
     * Mysql 数据库密码
     */
    private final String password;

    /**
     * Mysql 连接使用的字符集编码 ID，ID 对应的编码可通过数据库表 "information_schema.collations" 进行查询
     */
    private final int characterId;

    /**
     * Mysql 连接需要支持的特性数值，每个比特位可代表不同的特性是否支持(需 MYSQL 服务端也支持该属性才可生效)
     */
    private final long capabilitiesFlags;

    /**
     * Mysql 连接在空闲时，PING 命令发送时间间隔，单位：秒，如果该值小于等于 0，则不进行心跳检测
     */
    private final int pingPeriod;

    /**
     * Mysql 连接使用的 Socket 配置信息，允许为 {@code null}
     */
    private final SocketConfiguration socketConfiguration;

    /**
     * 构造一个建立 Mysql 数据库连接使用的配置信息，字符集编码 ID 默认为 45（utf8mb4_general_ci），PING 命令发送时间间隔默认为 30 秒，
     * Socket 配置信息默认使用 {@link SocketConfiguration#DEFAULT} 配置信息。
     *
     * @param jdbcURL Mysql JDBC URL，例如：jdbc:mysql://localhost:3306/demo
     * @param username Mysql 数据库用户名
     * @param password Mysql 数据库密码
     * @throws IllegalArgumentException 如果 JDBC URL 不符合规则，将抛出此异常
     */
    public ConnectionConfiguration(String jdbcURL, String username, String password) throws IllegalArgumentException {
        try {
            Map<String, Object> properties = MysqlConnectionBuildUtil.parseURL(jdbcURL);
            this.host = (String) properties.get(MysqlConnectionBuildUtil.PROPERTY_HOST);
            this.databaseName = (String) properties.get(MysqlConnectionBuildUtil.PROPERTY_DATABASE_NAME);
            this.username = username;
            this.password = password;
            this.characterId = (int) properties.getOrDefault(MysqlConnectionBuildUtil.PROPERTY_CHARACTER_ID, 45);
            this.capabilitiesFlags = (long) properties.getOrDefault(MysqlConnectionBuildUtil.PROPERTY_CAPABILITIES_FLAGS, 0);
            this.pingPeriod = (int) properties.getOrDefault(MysqlConnectionBuildUtil.PROPERTY_PING_PERIOD, 30);
            this.socketConfiguration = null;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Create `ConnectionConfiguration` failed: `invalid jdbc url`. `jdbcURL`:`"
                + jdbcURL + "`.", e);
        }
    }

    /**
     * 构造一个建立 Mysql 数据库连接使用的配置信息，字符集编码 ID 默认为 45（utf8mb4_general_ci），PING 命令发送时间间隔默认为 30 秒，
     * Socket 配置信息默认使用 {@link SocketConfiguration#DEFAULT} 配置信息。
     *
     * @param host Mysql 地址，由主机名和端口组成，":" 符号分割，例如：localhost:3306
     * @param databaseName Mysql 数据库名称
     * @param username Mysql 数据库用户名
     * @param password Mysql 数据库密码
     */
    public ConnectionConfiguration(String host, String databaseName, String username, String password) {
        this(host, databaseName, username, password, 45, 0, 30, null);
    }

    /**
     * 构造一个建立 Mysql 数据库连接使用的配置信息。
     *
     * @param host Mysql 地址，由主机名和端口组成，":" 符号分割，例如：localhost:3306
     * @param databaseName Mysql 数据库名称
     * @param username Mysql 数据库用户名
     * @param password Mysql 数据库密码
     * @param characterId Mysql 连接使用的字符集编码 ID，ID 对应的编码可通过数据库表 "information_schema.collations" 进行查询
     * @param capabilitiesFlags Mysql 连接需要支持的特性数值，每个比特位可代表不同的特性是否支持(需 MYSQL 服务端也支持该属性才可生效)
     * @param pingPeriod Mysql 连接在空闲时，PING 命令发送时间间隔，单位：秒，如果该值小于等于 0，则不进行心跳检测
     * @param socketConfiguration Mysql 连接使用的 Socket 配置信息，如果传 {@code null}，将会使用 {@link SocketConfiguration#DEFAULT} 配置信息
     */
    public ConnectionConfiguration(String host, String databaseName, String username, String password, int characterId,
                                   long capabilitiesFlags, int pingPeriod, SocketConfiguration socketConfiguration) {
        this.host = host;
        this.databaseName = databaseName;
        this.username = username;
        this.password = password;
        this.characterId = characterId;
        this.capabilitiesFlags = capabilitiesFlags;
        this.pingPeriod = pingPeriod;
        this.socketConfiguration = socketConfiguration;
    }

    /**
     * 获得 Mysql 地址，由主机名和端口组成，":" 符号分割，例如：localhost:3306。
     *
     * @return Mysql 地址，由主机名和端口组成，":" 符号分割，例如：localhost:3306
     */
    public String getHost() {
        return host;
    }

    /**
     * 获得 Mysql 数据库名称。
     *
     * @return Mysql 数据库名称
     */
    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * 获得 Mysql 数据库用户名。
     * @return  Mysql 数据库用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 获得 Mysql 数据库密码。
     *
     * @return Mysql 数据库密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 获得 Mysql 连接使用的字符集编码 ID，ID 对应的编码可通过数据库表 "information_schema.collations" 进行查询。
     *
     * @return Mysql 连接使用的字符集编码 ID
     */
    public int getCharacterId() {
        return characterId;
    }

    /**
     * 获得 Mysql 连接需要支持的特性数值，每个比特位可代表不同的特性是否支持(需 MYSQL 服务端也支持该属性才可生效)。
     *
     * @return Mysql 连接需要支持的特性数值
     */
    public long getCapabilitiesFlags() {
        return capabilitiesFlags;
    }

    /**
     * 获得 Mysql 连接在空闲时，PING 命令发送时间间隔，单位：秒，如果该值小于等于 0，则不进行心跳检测。
     *
     * @return PING 命令发送时间间隔，单位：秒
     */
    public int getPingPeriod() {
        return pingPeriod;
    }

    /**
     * Mysql 连接使用的 Socket 配置信息，可能返回 {@code null}
     *
     * @return Socket 配置信息，可能为 {@code null}
     */
    public SocketConfiguration getSocketConfiguration() {
        return socketConfiguration;
    }

    @Override
    public String toString() {
        return "ConnectionConfiguration{" +
                "host='" + host + '\'' +
                ", databaseName='" + databaseName + '\'' +
                ", username='" + username + '\'' +
                ", password='" + StringUtil.hidePassword(password) + '\'' +
                ", characterId=" + characterId +
                ", capabilitiesFlags=" + capabilitiesFlags +
                ", pingPeriod=" + pingPeriod +
                ", socketConfiguration=" + socketConfiguration +
                '}';
    }
}
