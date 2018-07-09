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

import java.util.ArrayList;
import java.util.List;

/**
 * 提供列定义数值解析方法，更多信息请参考：
 * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/group__group__cs__column__definition__flags.html">
 *     Column Definition Flags
 * </a>
 *
 * <p><strong>说明：</strong>{@code ColumnDefinitionFlagsUtil} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class ColumnDefinitionFlagsUtil {

    /**
     * "NOT_NULL_FLAG" 状态对应的比特位索引位置
     */
    public static final int INDEX_NOT_NULL_FLAG = 0;

    /**
     * "PRI_KEY_FLAG" 状态对应的比特位索引位置
     */
    public static final int INDEX_PRI_KEY_FLAG = 1;

    /**
     * "UNIQUE_KEY_FLAG" 状态对应的比特位索引位置
     */
    public static final int INDEX_UNIQUE_KEY_FLAG = 2;

    /**
     * "MULTIPLE_KEY_FLAG " 状态对应的比特位索引位置
     */
    public static final int INDEX_MULTIPLE_KEY_FLAG = 3;

    /**
     * "BLOB_FLAG" 状态对应的比特位索引位置
     */
    public static final int INDEX_BLOB_FLAG = 4;

    /**
     * "UNSIGNED_FLAG" 状态对应的比特位索引位置
     */
    public static final int INDEX_UNSIGNED_FLAG = 5;

    /**
     * "ZEROFILL_FLAG" 状态对应的比特位索引位置
     */
    public static final int INDEX_ZEROFILL_FLAG = 6;

    /**
     * "BINARY_FLAG" 状态对应的比特位索引位置
     */
    public static final int INDEX_BINARY_FLAG = 7;

    /**
     * "ENUM_FLAG" 状态对应的比特位索引位置
     */
    public static final int INDEX_ENUM_FLAG = 8;

    /**
     * "AUTO_INCREMENT_FLAG" 状态对应的比特位索引位置
     */
    public static final int INDEX_AUTO_INCREMENT_FLAG = 9;

    /**
     * "TIMESTAMP_FLAG" 状态对应的比特位索引位置
     */
    public static final int INDEX_TIMESTAMP_FLAG = 10;

    /**
     * "SET_FLAG" 状态对应的比特位索引位置
     */
    public static final int INDEX_SET_FLAG = 11;

    /**
     * "NO_DEFAULT_VALUE_FLAG" 状态对应的比特位索引位置
     */
    public static final int INDEX_NO_DEFAULT_VALUE_FLAG = 12;

    /**
     * "ON_UPDATE_NOW_FLAG" 状态对应的比特位索引位置
     */
    public static final int INDEX_ON_UPDATE_NOW_FLAG = 13;

    /**
     * "NUM_FLAG" 状态对应的比特位索引位置
     */
    public static final int INDEX_NUM_FLAG = 15;

    /**
     * 判断指定的列定义是否开启。
     *
     * @param columnDefinitionFlags 列定义数值
     * @param columnDefinitionIndex 状态对应的比特位索引位置，允许的值为：[0, 15]
     * @return 是否开启
     * @throws IllegalArgumentException 如果状态对应的比特位索引位置没有在允许的范围内，将会抛出此异常
     */
    public static boolean isColumnDefinitionEnabled(int columnDefinitionFlags, int columnDefinitionIndex)
            throws IllegalArgumentException {
        if (columnDefinitionIndex < 0 || columnDefinitionIndex > 15) {
            throw new IllegalArgumentException("Check column definition flag failed: `invalid column definition index`. `columnDefinitionFlags`:`"
                    + columnDefinitionFlags + "`. `columnDefinitionIndex`:`" + columnDefinitionIndex + "`.");
        }
        return (columnDefinitionFlags & (1L << columnDefinitionIndex)) != 0;
    }

    /**
     * 根据列定义状态比特位索引位置获得对应的名称，该方法不会返回 {@code null}。
     *
     * @param columnDefinitionIndex 状态对应的比特位索引位置，允许的值为：[0, 15]
     * @return 状态名称
     * @throws IllegalArgumentException 如果状态对应的比特位索引位置没有在允许的范围内，将会抛出此异常
     */
    public static String getColumnDefinitionName(int columnDefinitionIndex) throws IllegalArgumentException {
        switch (columnDefinitionIndex) {
            case INDEX_NOT_NULL_FLAG:
                return "NOT_NULL_FLAG";
            case INDEX_PRI_KEY_FLAG:
                return "PRI_KEY_FLAG";
            case INDEX_UNIQUE_KEY_FLAG:
                return "UNIQUE_KEY_FLAG";
            case INDEX_MULTIPLE_KEY_FLAG:
                return "MULTIPLE_KEY_FLAG";
            case INDEX_BLOB_FLAG:
                return "BLOB_FLAG";
            case INDEX_UNSIGNED_FLAG:
                return "UNSIGNED_FLAG";
            case INDEX_ZEROFILL_FLAG:
                return "ZEROFILL_FLAG";
            case INDEX_BINARY_FLAG:
                return "BINARY_FLAG";
            case INDEX_ENUM_FLAG:
                return "ENUM_FLAG";
            case INDEX_AUTO_INCREMENT_FLAG:
                return "AUTO_INCREMENT_FLAG";
            case INDEX_TIMESTAMP_FLAG:
                return "TIMESTAMP_FLAG";
            case INDEX_SET_FLAG:
                return "SET_FLAG";
            case INDEX_NO_DEFAULT_VALUE_FLAG:
                return "NO_DEFAULT_VALUE_FLAG";
            case INDEX_ON_UPDATE_NOW_FLAG:
                return "ON_UPDATE_NOW_FLAG";
            case INDEX_NUM_FLAG:
                return "NUM_FLAG";
            default:
                throw new IllegalArgumentException("Get column definition flag name failed: `invalid column definition index`. `columnDefinitionIndex`:`"
                        + columnDefinitionIndex + "`.");
        }
    }

    /**
     * 返回列定义数值中开启的状态名称列表，该方法不会返回 {@code null}。
     *
     * @param columnDefinitionFlags 列定义数值
     * @return 开启的列定义名称列表
     */
    public static List<String> getEnabledColumnDefinitionNames(int columnDefinitionFlags) {
        List<String> enabledColumnDefinitionNames = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            if (i == 14) {// 该状态位暂未使用
                continue;
            } else {
                if (isColumnDefinitionEnabled(columnDefinitionFlags, i)) {
                    enabledColumnDefinitionNames.add(getColumnDefinitionName(i));
                }
            }
        }
        return enabledColumnDefinitionNames;
    }
}
