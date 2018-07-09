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

import com.heimuheimu.mysql.jdbc.packet.command.text.ColumnDefinition41ResponsePacket;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * 提供工具方法用于 Mysql 列类型和 Java 类型之间的映射，Mysql 类型定义请参考：
 * <a href="https://dev.mysql.com/doc/internals/en/com-query-response.html#column-type">
 * Column Types
 * </a>
 *
 * <p><strong>说明：</strong>{@code ColumnTypeMappingUtil} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 * @see ColumnDefinition41ResponsePacket#getColumnType()
 */
public class ColumnTypeMappingUtil {

    /**
     * Mysql data type: TINYINT
     */
    public static final int MYSQL_TYPE_TINY = 0x01;

    /**
     * Mysql data type: SMALLINT
     */
    public static final int MYSQL_TYPE_SHORT = 0x02;

    /**
     * Mysql data type: INT
     */
    public static final int MYSQL_TYPE_LONG = 0x03;

    /**
     * Mysql data type: FLOAT
     */
    public static final int MYSQL_TYPE_FLOAT = 0x04;

    /**
     * Mysql data type: DOUBLE、REAL
     */
    public static final int MYSQL_TYPE_DOUBLE = 0x05;

    /**
     * Mysql data type: TIMESTAMP
     */
    public static final int MYSQL_TYPE_TIMESTAMP = 0x07;

    /**
     * Mysql data type: BIGINT
     */
    public static final int MYSQL_TYPE_LONGLONG = 0x08;

    /**
     * Mysql data type: MEDIUMINT
     */
    public static final int MYSQL_TYPE_INT24 = 0x09;

    /**
     * Mysql data type: DATE
     */
    public static final int MYSQL_TYPE_DATE = 0x0a;

    /**
     * Mysql data type: TIME
     */
    public static final int MYSQL_TYPE_TIME = 0x0b;

    /**
     * Mysql data type: DATETIME
     */
    public static final int MYSQL_TYPE_DATETIME = 0x0c;

    /**
     * Mysql data type: YEAR
     */
    public static final int MYSQL_TYPE_YEAR = 0x0d;

    /**
     * Mysql data type: BIT
     */
    public static final int MYSQL_TYPE_BIT = 0x10;

    /**
     * Mysql data type: DECIMAL
     */
    public static final int MYSQL_TYPE_NEWDECIMAL = 0xf6;

    /**
     * Mysql data type: BLOB、TINYBLOB、MEDIUMBLOB、LONGBLOB、TEXT、TINYTEXT、MEDIUMTEXT、LONGTEXT
     */
    public static final int MYSQL_TYPE_BLOB = 0xfc;

    /**
     * Mysql data type: VARCHAR、VARBINARY
     */
    public static final int MYSQL_TYPE_VAR_STRING = 0xfd;

    /**
     * Mysql data type: CHAR、BINARY、ENUM、SET
     */
    public static final int MYSQL_TYPE_STRING = 0xfe;

    private static final Map<Integer, String> MYSQL_TYPE_NAME_MAP;

    static {
        MYSQL_TYPE_NAME_MAP = new HashMap<>();
        //Numeric type
        MYSQL_TYPE_NAME_MAP.put(MYSQL_TYPE_TINY, "TINYINT");
        MYSQL_TYPE_NAME_MAP.put(MYSQL_TYPE_SHORT, "SMALLINT");
        MYSQL_TYPE_NAME_MAP.put(MYSQL_TYPE_INT24, "MEDIUMINT");
        MYSQL_TYPE_NAME_MAP.put(MYSQL_TYPE_LONG, "INT");
        MYSQL_TYPE_NAME_MAP.put(MYSQL_TYPE_LONGLONG, "BIGINT");
        MYSQL_TYPE_NAME_MAP.put(MYSQL_TYPE_FLOAT, "FLOAT");
        MYSQL_TYPE_NAME_MAP.put(MYSQL_TYPE_DOUBLE, "DOUBLE");
        MYSQL_TYPE_NAME_MAP.put(MYSQL_TYPE_NEWDECIMAL, "DECIMAL");
        MYSQL_TYPE_NAME_MAP.put(MYSQL_TYPE_BIT, "BIT");

        //Date type
        MYSQL_TYPE_NAME_MAP.put(MYSQL_TYPE_YEAR, "YEAR");
        MYSQL_TYPE_NAME_MAP.put(MYSQL_TYPE_DATE, "DATE");
        MYSQL_TYPE_NAME_MAP.put(MYSQL_TYPE_TIME, "TIME");
        MYSQL_TYPE_NAME_MAP.put(MYSQL_TYPE_TIMESTAMP, "TIMESTAMP");
        MYSQL_TYPE_NAME_MAP.put(MYSQL_TYPE_DATETIME, "DATETIME");

        //String type
        MYSQL_TYPE_NAME_MAP.put(MYSQL_TYPE_BLOB, "BLOB");
        MYSQL_TYPE_NAME_MAP.put(MYSQL_TYPE_VAR_STRING, "VAR_STRING");
        MYSQL_TYPE_NAME_MAP.put(MYSQL_TYPE_STRING, "STRING");
    }

    /**
     * 根据 Mysql 列类型 ID，获得对应的名称，如果列类型 ID 未被定义，将会返回 "UNKNOWN"。
     *
     * @param columnType 列类型 ID
     * @param columnCharacterId 列使用的字符集编码 ID
     * @return 列类型名称
     */
    public static String getTypeName(int columnType, int columnCharacterId) {
        if (MYSQL_TYPE_NAME_MAP.containsKey(columnType)) {
            return MYSQL_TYPE_NAME_MAP.get(columnType);
        } else {
            return "UNKNOWN";
        }
    }

    /**
     * 根据 Mysql 列类型 ID 和该列使用的字符集编码 ID，获得对应的 Java 类型。
     *
     * @param columnType 列类型 ID
     * @param columnCharacterId 列使用的字符集编码 ID
     * @return 列对应的 Java 类型
     */
    public static Class<?> getJavaClass(int columnType, int columnCharacterId) {
        if (columnType == MYSQL_TYPE_TINY || columnType == MYSQL_TYPE_SHORT
                || columnType == MYSQL_TYPE_INT24 || columnType == MYSQL_TYPE_LONG) {
            return Integer.class;
        } else if (columnType == MYSQL_TYPE_LONGLONG) {
            return Long.class;
        } else if (columnType == MYSQL_TYPE_NEWDECIMAL) {
            return BigDecimal.class;
        } else if (columnType == MYSQL_TYPE_FLOAT || columnType == MYSQL_TYPE_DOUBLE) {
            return Double.class;
        } else if (columnType == MYSQL_TYPE_BIT) {
            return Boolean.class;
        } else if (columnType == MYSQL_TYPE_YEAR || columnType ==  MYSQL_TYPE_DATE) {
            return Date.class;
        } else if (columnType == MYSQL_TYPE_TIME) {
            return Time.class;
        } else if (columnType == MYSQL_TYPE_TIMESTAMP || columnType == MYSQL_TYPE_DATETIME) {
            return Timestamp.class;
        } else {
            if (CharsetMappingUtil.isBinary(columnCharacterId)) {
                return byte[].class;
            } else {
                return String.class;
            }
        }
    }
}
