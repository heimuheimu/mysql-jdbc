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

import com.heimuheimu.mysql.jdbc.constant.SQLType;

/**
 * SQL 语句工具类。
 *
 * @author heimuheimu
 */
public class SQLUtil {

    private SQLUtil() {
        // private constructor
    }

    /**
     * 根据 SQL 语句获得其对应的语句类型。
     *
     * @param sql SQL 语句
     * @return SQL 语句类型
     */
    public static SQLType getSQLType(String sql) {
        int firstKeywordIndex = -1;
        for (int i = 0; i < sql.length(); i++) {
            if (!isWhitespaceChar(sql.charAt(i))) {
                firstKeywordIndex = i;
                break;
            }
        }
        if (firstKeywordIndex >= 0) {
            if ((sql.charAt(firstKeywordIndex) == 'i' || sql.charAt(firstKeywordIndex) == 'I') &&
                    firstKeywordIndex + 5 < sql.length()) {
                if ((sql.charAt(firstKeywordIndex + 1) == 'n' || sql.charAt(firstKeywordIndex + 1) == 'N') &&
                        (sql.charAt(firstKeywordIndex + 2) == 's' || sql.charAt(firstKeywordIndex + 2) == 'S') &&
                        (sql.charAt(firstKeywordIndex + 3) == 'e' || sql.charAt(firstKeywordIndex + 3) == 'E') &&
                        (sql.charAt(firstKeywordIndex + 4) == 'r' || sql.charAt(firstKeywordIndex + 4) == 'R') &&
                        (sql.charAt(firstKeywordIndex + 5) == 't' || sql.charAt(firstKeywordIndex + 5) == 'T')) {
                    return SQLType.INSERT;
                }
            } else if ((sql.charAt(firstKeywordIndex) == 'u' || sql.charAt(firstKeywordIndex) == 'U') &&
                    firstKeywordIndex + 5 < sql.length()) {
                if ((sql.charAt(firstKeywordIndex + 1) == 'p' || sql.charAt(firstKeywordIndex + 1) == 'P') &&
                        (sql.charAt(firstKeywordIndex + 2) == 'd' || sql.charAt(firstKeywordIndex + 2) == 'D') &&
                        (sql.charAt(firstKeywordIndex + 3) == 'a' || sql.charAt(firstKeywordIndex + 3) == 'A') &&
                        (sql.charAt(firstKeywordIndex + 4) == 't' || sql.charAt(firstKeywordIndex + 4) == 'T') &&
                        (sql.charAt(firstKeywordIndex + 5) == 'e' || sql.charAt(firstKeywordIndex + 5) == 'E')) {
                    return SQLType.UPDATE;
                }
            } else if ((sql.charAt(firstKeywordIndex) == 'd' || sql.charAt(firstKeywordIndex) == 'D') &&
                    firstKeywordIndex + 5 < sql.length()) {
                if ((sql.charAt(firstKeywordIndex + 1) == 'e' || sql.charAt(firstKeywordIndex + 1) == 'E') &&
                        (sql.charAt(firstKeywordIndex + 2) == 'l' || sql.charAt(firstKeywordIndex + 2) == 'L') &&
                        (sql.charAt(firstKeywordIndex + 3) == 'e' || sql.charAt(firstKeywordIndex + 3) == 'E') &&
                        (sql.charAt(firstKeywordIndex + 4) == 't' || sql.charAt(firstKeywordIndex + 4) == 'T') &&
                        (sql.charAt(firstKeywordIndex + 5) == 'e' || sql.charAt(firstKeywordIndex + 5) == 'E')) {
                    return SQLType.DELETE;
                }
            } else if ((sql.charAt(firstKeywordIndex) == 's' || sql.charAt(firstKeywordIndex) == 'S') &&
                    firstKeywordIndex + 5 < sql.length()) {
                if ((sql.charAt(firstKeywordIndex + 1) == 'e' || sql.charAt(firstKeywordIndex + 1) == 'E') &&
                        (sql.charAt(firstKeywordIndex + 2) == 'l' || sql.charAt(firstKeywordIndex + 2) == 'L') &&
                        (sql.charAt(firstKeywordIndex + 3) == 'e' || sql.charAt(firstKeywordIndex + 3) == 'E') &&
                        (sql.charAt(firstKeywordIndex + 4) == 'c' || sql.charAt(firstKeywordIndex + 4) == 'C') &&
                        (sql.charAt(firstKeywordIndex + 5) == 't' || sql.charAt(firstKeywordIndex + 5) == 'T')) {
                    return SQLType.SELECT;
                }
            }
        }
        return SQLType.OTHER;
    }

    /**
     * 判断该字符是否为空格字符。
     *
     * @return 是否为空格字符
     */
    private static boolean isWhitespaceChar(char c) {
        return c == ' ' || c == '\t' || c == '\n' || c == '\r';
    }
}
