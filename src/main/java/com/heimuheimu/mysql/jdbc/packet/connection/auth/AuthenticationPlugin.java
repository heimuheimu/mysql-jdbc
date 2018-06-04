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

/**
 * Mysql 客户端授权插件，用于对数据库密码进行加密。
 *
 * <p>
 * 更多信息请参考：
 * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_connection_phase_authentication_methods.html">
 *     Authentication Methods
 * </a>
 * </p>
 *
 * @author heimuheimu
 */
public interface AuthenticationPlugin {

    /**
     * 获得插件名称。
     *
     * @return 插件名称
     */
    String getName();

    /**
     * 根据数据库密码生成的加密字节数组。
     *
     * @param password 数据库密码，允许为 {@code null} 或空
     * @param authPluginData 插件用于密码加密的字节数组
     * @return 根据数据库密码生成的加密字节数组
     * @throws IllegalArgumentException 如果 {@code authPluginData} 不正确，将抛出此异常
     */
    byte[] encode(String password, byte[] authPluginData) throws IllegalArgumentException;
}
