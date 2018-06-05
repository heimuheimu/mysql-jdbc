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

package com.heimuheimu.mysql.jdbc.packet.connection.auth;

import java.util.HashMap;
import java.util.Map;

/**
 * Mysql 客户端授权插件工厂类。
 *
 * <p><strong>说明：</strong>{@code AuthenticationPluginFactory} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class AuthenticationPluginFactory {

    private static final Map<String, AuthenticationPlugin> AUTHENTICATION_PLUGIN_MAP;

    static {
        AUTHENTICATION_PLUGIN_MAP = new HashMap<>();
        // add plugin: mysql_native_password
        MysqlNativePasswordAuthPlugin mysqlNativePasswordAuthPlugin = new MysqlNativePasswordAuthPlugin();
        AUTHENTICATION_PLUGIN_MAP.put(mysqlNativePasswordAuthPlugin.getName(), mysqlNativePasswordAuthPlugin);
    }

    /**
     * 根据名称获得对应的 Mysql 客户端授权插件，该方法不会返回 {@code null}
     *
     * @param authPluginName 插件名称
     * @return Mysql 客户端授权插件
     * @throws IllegalArgumentException 如果无法找到该名称对应的 Mysql 客户端授权插件，将会抛出此异常
     */
    public AuthenticationPlugin get(String authPluginName) throws IllegalArgumentException {
        AuthenticationPlugin plugin = AUTHENTICATION_PLUGIN_MAP.get(authPluginName);
        if (plugin == null) {
            throw new IllegalArgumentException("There is no authentication plugin for name: `" + authPluginName + "`.");
        }
        return plugin;
    }
}
