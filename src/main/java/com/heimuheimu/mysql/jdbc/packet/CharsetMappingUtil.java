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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 提供工具方法用于 Mysql 字符集编码和 Java 字符集编码之间的映射，Mysql 字符集定义请参考：
 * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_character_set.html">
 *     Character Set
 * </a>
 *
 * <p><strong>说明：</strong>{@code CharsetMappingUtil} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class CharsetMappingUtil {

    /**
     * 判断 Mysql 字符集编码 ID 对应的编码是否为 binary。
     *
     * @param mysqlCharacterId Mysql 字符集编码 ID
     * @return 是否为 binary 编码
     */
    public static boolean isBinary(int mysqlCharacterId) {
        return mysqlCharacterId == 63;
    }

    /**
     * 根据 Mysql 字符集编码 ID 获得对应的 Java 字符集编码，该方法不会返回 {@code null}。
     *
     * @param mysqlCharacterId Mysql 字符集编码 ID
     * @return Java 字符集编码，不会为 {@code null}
     */
    public static Charset getJavaCharset(int mysqlCharacterId) {
        return StandardCharsets.UTF_8;
    }
}
