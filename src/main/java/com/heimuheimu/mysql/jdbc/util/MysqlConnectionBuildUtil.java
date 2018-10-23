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

package com.heimuheimu.mysql.jdbc.util;

import com.heimuheimu.mysql.jdbc.ConnectionConfiguration;
import com.heimuheimu.mysql.jdbc.MysqlConnection;
import com.heimuheimu.mysql.jdbc.channel.MysqlChannel;
import com.heimuheimu.mysql.jdbc.facility.UnusableServiceNotifier;
import com.heimuheimu.mysql.jdbc.net.BuildSocketException;
import com.heimuheimu.mysql.jdbc.net.SocketConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据库 URL 解析工具类。
 *
 * @author heimuheimu
 */
public class MysqlConnectionBuildUtil {

    /**
     * JDBC 参数名称：Mysql 地址，由主机名和端口组成，":" 符号分割，例如：localhost:3306
     */
    public static final String PROPERTY_HOST = "host";

    /**
     * JDBC 参数名称：Mysql 数据库名称
     */
    public static final String PROPERTY_DATABASE_NAME = "databaseName";

    /**
     * JDBC 参数名称：Mysql 数据库用户名
     */
    public static final String PROPERTY_USER = "user";

    /**
     * JDBC 参数名称：Mysql 数据库密码
     */
    public static final String PROPERTY_PASSWORD = "password";

    /**
     * JDBC 参数名称：Mysql 连接需要支持的特性数值，每个比特位可代表不同的特性是否支持(需 MYSQL 服务端也支持该属性才可生效)
     */
    public static final String PROPERTY_CAPABILITIES_FLAGS = "capabilitiesFlags";

    /**
     * JDBC 参数名称：Mysql 连接使用的字符集编码 ID，ID 对应的编码可通过数据库表 "information_schema.collations" 进行查询
     */
    public static final String PROPERTY_CHARACTER_ID = "characterId";

    /**
     * JDBC 参数名称：Mysql 连接在空闲时，PING 命令发送时间间隔，单位：秒，如果该值小于等于 0，则不进行心跳检测
     */
    public static final String PROPERTY_PING_PERIOD = "pingPeriod";

    /**
     * JDBC 参数名称：Mysql 连接使用的 Socket 配置信息
     */
    public static final String PROPERTY_SOCKET_CONFIGURATION = "socketConfiguration";

    /**
     * JDBC 参数名称：SQL 执行超时时间，单位：毫秒，如果等于 0，则没有超时时间限制，不允许设置小于 0 的值
     */
    public static final String PROPERTY_TIMEOUT = "timeout";

    /**
     * JDBC 参数名称：执行 Mysql 命令过慢最小时间，单位：毫秒，不能小于等于 0
     */
    public static final String PROPERTY_SLOW_EXECUTION_THRESHOLD = "slowExecutionThreshold";

    /**
     * JDBC 参数名称：{@code MysqlChannel} 不可用通知器
     */
    public static final String PROPERTY_UNUSABLE_SERVICE_NOTIFIER = "unusableServiceNotifier";

    private static final Logger LOG = LoggerFactory.getLogger(MysqlConnectionBuildUtil.class);

    private MysqlConnectionBuildUtil() {
        // private constructor
    }

    /**
     * 根据 JDBC URL 和 Mysql 数据库连接参数 Map 生成对应的 Mysql 数据库连接并返回，该方法不会返回 {@code null}。
     *
     * <p><b>注意：</b>参数 Map 中定义的参数值优先级高于 JDBC URL 中定义的参数值。</p>
     *
     * @param url JDBC URL，例如：jdbc:mysql://localhost:3306/demo，允许为 {@code null}
     * @param connectionInfo Mysql 数据库连接参数 Map，允许为 {@code null}
     * @return Mysql 数据库连接，不会为 {@code null}
     * @throws MalformedURLException 如果 JDBC URL 解析失败，将抛出此异常
     * @throws IllegalArgumentException 如果 {@code timeout} 存在且值小于 0，将会抛出此异常
     * @throws IllegalArgumentException 如果 {@code slowExecutionThreshold} 存在且值小于等于 0，将会抛出此异常
     * @throws BuildSocketException 如果创建与 Mysql 服务器的 Socket 连接失败，将会抛出此异常
     */
    @SuppressWarnings("unchecked")
    public static MysqlConnection build(String url, Map<String, Object> connectionInfo) throws MalformedURLException,
            IllegalArgumentException, BuildSocketException {
        try {
            Map<String, Object> properties = new HashMap<>();
            properties.putAll(parseURL(url));
            properties.putAll(connectionInfo);

            ConnectionConfiguration configuration = getConnectionConfiguration(properties);

            int timeout = 5000; // 默认 SQL 执行超时时间为 5 秒
            if (connectionInfo.containsKey(PROPERTY_TIMEOUT)) {
                timeout = (int) connectionInfo.get(PROPERTY_TIMEOUT);
            }

            int slowExecutionThreshold = 500; // 默认 Mysql 命令过慢最小时间为 500 毫秒
            if (connectionInfo.containsKey(PROPERTY_SLOW_EXECUTION_THRESHOLD)) {
                slowExecutionThreshold = (int) connectionInfo.get(PROPERTY_SLOW_EXECUTION_THRESHOLD);
            }

            UnusableServiceNotifier<MysqlChannel> notifier = null;
            if (connectionInfo.containsKey(PROPERTY_UNUSABLE_SERVICE_NOTIFIER)) {
                notifier = (UnusableServiceNotifier<MysqlChannel>) connectionInfo.get(PROPERTY_UNUSABLE_SERVICE_NOTIFIER);
            }

            return new MysqlConnection(configuration, timeout, slowExecutionThreshold, notifier);
        } catch (Exception e) {
            LOG.error("Build MysqlConnection failed. url: `" + url + "`. connectionInfo: `" + connectionInfo + "`.", e);
            throw e;
        }
    }

    /**
     * 根据 Mysql 数据库连接参数 Map 生成 {@link ConnectionConfiguration} 实例后返回，该方法不会返回 {@code null}。
     *
     * @param connectionInfo Mysql 数据库连接参数 Map，不允许为 {@code null}
     * @return 建立 Mysql 数据库连接使用的配置信息
     */
    private static ConnectionConfiguration getConnectionConfiguration(Map<String, Object> connectionInfo) {
        String host = (String) connectionInfo.get(PROPERTY_HOST);
        String databaseName = "";
        if (connectionInfo.containsKey(PROPERTY_DATABASE_NAME)) {
            databaseName = (String) connectionInfo.get(PROPERTY_DATABASE_NAME);
        }
        String username = (String) connectionInfo.get(PROPERTY_USER);
        String password = "";
        if (connectionInfo.containsKey(PROPERTY_PASSWORD)) {
            password = (String) connectionInfo.get(PROPERTY_PASSWORD);
        }
        int characterId = 45; // 默认为 utf8mb4 编码
        if (connectionInfo.containsKey(PROPERTY_CHARACTER_ID)) {
            characterId = (int) connectionInfo.get(PROPERTY_CHARACTER_ID);
        }
        int capabilitiesFlags = 0; // 默认为 0
        if (connectionInfo.containsKey(PROPERTY_CAPABILITIES_FLAGS)) {
            capabilitiesFlags = (int) connectionInfo.get(PROPERTY_CAPABILITIES_FLAGS);
        }
        int pingPeriod = 30; // 默认为 30 秒
        if (connectionInfo.containsKey(PROPERTY_PING_PERIOD)) {
            pingPeriod = (int) connectionInfo.get(PROPERTY_PING_PERIOD);
        }
        SocketConfiguration socketConfiguration = null; // 默认为 null
        if (connectionInfo.containsKey(PROPERTY_SOCKET_CONFIGURATION)) {
            socketConfiguration = (SocketConfiguration) connectionInfo.get(PROPERTY_SOCKET_CONFIGURATION);
        }
        return new ConnectionConfiguration(host, databaseName,
                username, password, characterId, capabilitiesFlags, pingPeriod, socketConfiguration);
    }

    /**
     * 将 URL 解析成创建 Mysql 数据库连接需要使用的参数 Map 并返回，该方法不会返回 {@code null}。
     *
     * @param url JDBC URL，例如：jdbc:mysql://localhost:3306/demo，允许为 {@code null}
     * @return Mysql 数据库连接参数 Map，不会为 {@code null}
     * @throws MalformedURLException 如果 JDBC URL 解析失败，将抛出此异常
     */
    private static Map<String, Object> parseURL(String url) throws MalformedURLException {
        Map<String, Object> urlConnectionInfo = new HashMap<>();
        if (url != null) {
            url = url.trim();
            if (!url.isEmpty()) {
                String protocolName = "";
                String hostName = "";
                String databaseName = "";
                String queryString = "";
                int port = 3306;
                int protocolSplitCharIndex = url.indexOf("//");
                if (protocolSplitCharIndex >= 0) {
                    protocolName = url.substring(0, protocolSplitCharIndex);
                    url = url.substring(protocolSplitCharIndex + 2);
                    int querySplitCharIndex = url.indexOf("?");
                    if (querySplitCharIndex >= 0) {
                        queryString = url.substring(querySplitCharIndex + 1);
                        url = url.substring(0, querySplitCharIndex);
                    }
                    int databaseNameSplitCharIndex = url.indexOf("/");
                    if (databaseNameSplitCharIndex >= 0) {
                        databaseName = url.substring(databaseNameSplitCharIndex + 1);
                        url = url.substring(0, databaseNameSplitCharIndex);
                    }
                    int portSplitCharIndex = url.indexOf(":");
                    if (portSplitCharIndex >= 0) {
                        try {
                            port = Integer.parseInt(url.substring(portSplitCharIndex + 1));
                        } catch (Exception e) {
                            throw new MalformedURLException("Parse mysql jdbc url failed: `invalid port,must be number`. url: `"
                                    + url + "`.");
                        }
                        url = url.substring(0, portSplitCharIndex);
                    }
                    hostName = url;
                }
                if (!protocolName.equals("jdbc:mysql:")) {
                    throw new MalformedURLException("Parse mysql jdbc url failed: `invalid protocol, must start with 'jdbc:mysql://'`. url: `"
                            + url + "`.");
                }
                if (hostName.isEmpty()) {
                    throw new MalformedURLException("Parse mysql jdbc url failed: `empty host`. url: `" + url + "`.");
                }
                urlConnectionInfo.put(PROPERTY_HOST, hostName + ":" + port);
                urlConnectionInfo.put(PROPERTY_DATABASE_NAME, databaseName);
                try {
                    urlConnectionInfo.putAll(parseQueryString(queryString));
                } catch (Exception e) {
                    throw new MalformedURLException("Parse mysql jdbc url failed: `" + e.getMessage() + "`. url: `" + url + "`.");
                }
            }
        }
        return urlConnectionInfo;
    }

    /**
     * 将 JDBC URL 中的查询参数字符串解析为 JDBC 参数 Map 后返回，该方法不会返回 {@code null}。
     *
     * @param queryString JDBC URL 中包含的查询参数字符串
     * @return JDBC 参数 Map，不会为 {@code null}
     * @throws MalformedURLException 如果 JDBC URL 中的查询参数字符串解析失败，将抛出此异常
     */
    @SuppressWarnings("ConstantConditions")
    private static Map<String, Object> parseQueryString(String queryString) throws MalformedURLException {
        Map<String, Object> params = new HashMap<>();
        if (queryString != null && !queryString.isEmpty()) {
            String[] parameterPairs = queryString.split("&");
            for (String parameterPair : parameterPairs) {
                String[] parameterParts = parameterPair.split("=");
                if (parameterParts.length == 2) {
                    String parameterName = parameterParts[0];
                    String parameterValue = parameterParts[1];
                    if (parameterValue != null && !parameterValue.isEmpty()) {
                        try {
                            parameterValue = URLDecoder.decode(parameterValue, "UTF-8");
                        } catch (UnsupportedEncodingException ignored) {}
                    }
                    switch (parameterName) {
                        case PROPERTY_USER:
                            params.put(PROPERTY_USER, parameterValue);
                            break;
                        case PROPERTY_PASSWORD:
                            params.put(PROPERTY_PASSWORD, parameterValue);
                            break;
                        case PROPERTY_CHARACTER_ID:
                            try {
                                params.put(PROPERTY_CHARACTER_ID, Integer.parseInt(parameterValue));
                                break;
                            } catch (Exception e) {
                                throw new MalformedURLException("invalid characterId");
                            }
                        case PROPERTY_CAPABILITIES_FLAGS:
                            try {
                                params.put(PROPERTY_CAPABILITIES_FLAGS, Integer.parseInt(parameterValue));
                                break;
                            } catch (Exception e) {
                                throw new MalformedURLException("invalid capabilitiesFlags");
                            }
                        case PROPERTY_PING_PERIOD:
                            try {
                                params.put(PROPERTY_PING_PERIOD, Integer.parseInt(parameterValue));
                                break;
                            } catch (Exception e) {
                                throw new MalformedURLException("invalid pingPeriod");
                            }
                        default:
                            break;
                    }
                }
            }
        }
        return params;
    }
}
