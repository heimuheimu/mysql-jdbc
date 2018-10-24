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

import java.util.Arrays;

/**
 * {@code BytesUtil} 提供字符串转换的工具方法，例如将对 MYSQL 特殊字符进行转义。
 *
 * <p><strong>说明：</strong>{@code BytesUtil} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class StringUtil {

    private StringUtil() {
        // private constructor
    }

    /**
     * 对字符串中包含的特殊字符进行转义后返回，更多信息请参考：
     * <a href="https://dev.mysql.com/doc/refman/8.0/en/string-literals.html">
     *     String Literals
     * </a>
     *
     * @param value 需要转义的字符串
     * @return 转义后的字符串
     */
    public static String escape(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        int specialCharacterCount = 0;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\0':
                case '\'':
                case '\b':
                case '\n':
                case '\r':
                case '\t':
                case '\032':
                case '\\':
                    specialCharacterCount++;
                    break;
            }
        }
        if (specialCharacterCount > 0) {
            StringBuilder buffer = new StringBuilder(value.length() + specialCharacterCount);
            for (int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);
                switch (c) {
                    case '\0':
                        buffer.append('\\');
                        buffer.append('0');
                        break;
                    case '\'':
                        buffer.append('\\');
                        buffer.append('\'');
                        break;
                    case '\b':
                        buffer.append('\\');
                        buffer.append('b');
                        break;
                    case '\n':
                        buffer.append('\\');
                        buffer.append('n');
                        break;
                    case '\r':
                        buffer.append('\\');
                        buffer.append('r');
                        break;
                    case '\t':
                        buffer.append('\\');
                        buffer.append('t');
                        break;
                    case '\032':
                        buffer.append('\\');
                        buffer.append('Z');
                        break;
                    case '\\':
                        buffer.append('\\');
                        buffer.append('\\');
                        break;
                    default:
                        buffer.append(c);
                        break;
                }
            }
            return buffer.toString();
        } else {
            return value;
        }
    }

    /**
     * 隐藏密码关键信息，返回隐藏关键信息后的密码字符串。
     *
     * @param password 密码字符串
     * @return 隐藏关键信息后的密码字符串
     */
    public static String hidePassword(String password) {
        if (password == null || password.isEmpty()) {
            return password;
        }
        char[] chars = password.toCharArray();
        int passwordLength = chars.length;
        if (passwordLength <= 2) {
            Arrays.fill(chars, '*');
        } else if (passwordLength < 8) {
            Arrays.fill(chars, 1, passwordLength - 1, '*');
        } else {
            Arrays.fill(chars, 2, passwordLength - 2, '*');
        }
        return new String(chars);
    }
}
