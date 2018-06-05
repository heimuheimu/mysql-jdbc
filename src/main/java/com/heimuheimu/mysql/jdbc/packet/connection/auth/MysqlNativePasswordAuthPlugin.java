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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * "mysql_native_password" 客户端授权插件实现类。
 *
 * <p>
 * 更多信息请参考：
 * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_connection_phase_authentication_methods_native_password_authentication.html">
 *     Native Authentication
 * </a>
 * </p>
 *
 * <p><strong>说明：</strong>{@code MysqlNativePasswordAuthPlugin} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class MysqlNativePasswordAuthPlugin implements AuthenticationPlugin {

    /**
     * "mysql_native_password" 用于加密的字节数组长度
     */
    private static final int AUTH_PLUGIN_DATA_LENGTH = 20;

    @Override
    public String getName() {
        return "mysql_native_password";
    }

    @Override
    public byte[] encode(String password, byte[] authPluginData) throws IllegalArgumentException {
        if (password == null || password.isEmpty()) {
            return new byte[0];
        }
        if (authPluginData.length < AUTH_PLUGIN_DATA_LENGTH) {
            throw new IllegalArgumentException("Encode password failed: `insufficient auth plugin data`. `authPluginData`:`"
                    + Arrays.toString(authPluginData) + "`.");
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] passwordHashStage1 = md.digest(password.getBytes(StandardCharsets.UTF_8));
            md.reset();
            byte[] passwordHashStage2 = md.digest(passwordHashStage1);
            md.reset();
            md.update(authPluginData, 0, AUTH_PLUGIN_DATA_LENGTH);
            md.update(passwordHashStage2);
            byte[] encodedBytes = md.digest();
            for (int i = 0; i < encodedBytes.length; i++) {
                encodedBytes[i] = (byte) (encodedBytes[i] ^ passwordHashStage1[i]);
            }
            return encodedBytes;
        } catch (Exception e) {
            throw new RuntimeException("Encode password failed: `" + e.getMessage() + "`. `authPluginData`:`"
                    + Arrays.toString(authPluginData) + "`." , e);
        }
    }
}
