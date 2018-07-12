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
import java.sql.Types;
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
     * @param columnDefinitionFlags 列定义数值
     * @param maximumColumnLength 列最大长度
     * @return 列类型名称
     */
    public static String getTypeName(int columnType, int columnDefinitionFlags, long maximumColumnLength) {
        if (columnType == MYSQL_TYPE_STRING) {
            if (ColumnDefinitionFlagsUtil.isColumnDefinitionEnabled(columnDefinitionFlags,
                    ColumnDefinitionFlagsUtil.INDEX_ENUM_FLAG)) {
                return "ENUM";
            } else if (ColumnDefinitionFlagsUtil.isColumnDefinitionEnabled(columnDefinitionFlags,
                    ColumnDefinitionFlagsUtil.INDEX_SET_FLAG)) {
                return "SET";
            } else if (ColumnDefinitionFlagsUtil.isColumnDefinitionEnabled(columnDefinitionFlags,
                    ColumnDefinitionFlagsUtil.INDEX_BINARY_FLAG)) {
                return "BINARY";
            } else {
                return "CHAR";
            }
        } else if (columnType == MYSQL_TYPE_VAR_STRING) {
            if (ColumnDefinitionFlagsUtil.isColumnDefinitionEnabled(columnDefinitionFlags,
                    ColumnDefinitionFlagsUtil.INDEX_BINARY_FLAG)) {
                return "VARBINARY";
            } else {
                return "VARCHAR";
            }
        } else if (columnType == MYSQL_TYPE_BLOB) {
            String blobTypeName = ColumnDefinitionFlagsUtil.isColumnDefinitionEnabled(columnDefinitionFlags,
                    ColumnDefinitionFlagsUtil.INDEX_BINARY_FLAG) ? "BLOB" : "TEXT";
            String sizeName;
            if (maximumColumnLength < 65535L) {
                sizeName = "TINY";
            } else if (maximumColumnLength < 16777215L) {
                sizeName = "";
            } else if (maximumColumnLength < 4294967295L) {
                sizeName = "MEDIUM";
            } else {
                sizeName = "LONG";
            }
            return sizeName + blobTypeName;
        } else if (MYSQL_TYPE_NAME_MAP.containsKey(columnType)) {
            return MYSQL_TYPE_NAME_MAP.get(columnType);
        } else {
            return "UNKNOWN";
        }
    }

    /**
     * 根据 Mysql 列类型获得对应的 Java 类型。
     *
     * @param columnType 列类型 ID
     * @param columnDefinitionFlags 列定义数值
     * @return 列对应的 Java 类型
     */
    public static Class<?> getJavaType(int columnType, int columnDefinitionFlags) {
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
            if (ColumnDefinitionFlagsUtil.isColumnDefinitionEnabled(columnDefinitionFlags,
                    ColumnDefinitionFlagsUtil.INDEX_BINARY_FLAG)) {
                return byte[].class;
            } else {
                return String.class;
            }
        }
    }

    /**
     * 根据 Mysql 列类型获得对应的 JDBC 类型 ID。
     *
     * @param columnType 列类型 ID
     * @param columnDefinitionFlags 列定义数值
     * @param maximumColumnLength 列最大长度
     * @return JDBC 类型 ID
     * @see Types
     */
    public static int getJDBCType(int columnType, int columnDefinitionFlags, long maximumColumnLength) {
        String typeName = getTypeName(columnType, columnDefinitionFlags, maximumColumnLength);
        if ("TINYINT".equals(typeName)) {
            return Types.TINYINT;
        } else if ("SMALLINT".equals(typeName)) {
            return Types.SMALLINT;
        } else if ("MEDIUMINT".equals(typeName)) {
            return Types.INTEGER;
        } else if ("INT".equals(typeName)) {
            return Types.INTEGER;
        } else if ("BIGINT".equals(typeName)) {
            return Types.BIGINT;
        } else if ("FLOAT".equals(typeName)) {
            return Types.FLOAT;
        } else if ("DOUBLE".equals(typeName)) {
            return Types.DOUBLE;
        } else if ("DECIMAL".equals(typeName)) {
            return Types.DECIMAL;
        } else if ("BIT".equals(typeName)) {
            return Types.BIT;
        } else if ("YEAR".equals(typeName) || "DATE".equals(typeName)) {
            return Types.DATE;
        } else if ("TIME".equals(typeName)) {
            return Types.TIME;
        } else if ("TIMESTAMP".equals(typeName) || "DATETIME".equals(typeName)) {
            return Types.TIMESTAMP;
        } else if ("ENUM".equals(typeName) || "SET".equals(typeName) || "CHAR".equals(typeName)) {
            return Types.CHAR;
        } else if ("VARCHAR".equals(typeName) || "TINYTEXT".equals(typeName)) {
            return Types.VARCHAR;
        } else if ("TEXT".equals(typeName) || "MEDIUMTEXT".equals(typeName) || "LONGTEXT".equals(typeName)) {
            return Types.LONGVARCHAR;
        } else if ("BINARY".equals(typeName)) {
            return Types.BINARY;
        } else if ("VARBINARY".equals(typeName) || "TINYBLOB".equals(typeName)) {
            return Types.VARBINARY;
        } else if ("BLOB".equals(typeName) || "MEDIUMBLOB".equals(typeName) || "LONGBLOB".equals(typeName)) {
            return Types.LONGVARBINARY;
        } else {
            return Types.OTHER;
        }
    }
}
