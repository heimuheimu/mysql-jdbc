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

package com.heimuheimu.mysql.jdbc.result;

import com.heimuheimu.mysql.jdbc.packet.CharsetMappingUtil;
import com.heimuheimu.mysql.jdbc.packet.ColumnDefinitionFlagsUtil;
import com.heimuheimu.mysql.jdbc.packet.ColumnTypeMappingUtil;
import com.heimuheimu.mysql.jdbc.packet.command.text.ColumnDefinition41ResponsePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

/**
 * 将 {@link ColumnDefinition41ResponsePacket} 信息转换为 {@link ResultSetMetaData}。
 *
 * <p><strong>说明：</strong>{@code TextResultSetMetaData} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class TextResultSetMetaData implements ResultSetMetaData {
    
    private static final Logger LOG = LoggerFactory.getLogger(TextResultSetMetaData.class);

    /**
     * "ColumnDefinition41" 数据包列表
     */
    private final List<ColumnDefinition41ResponsePacket> columnDefinition41ResponsePacketList;

    /**
     * 构造一个 {@code TextResultSetMetaData}。
     *
     * @param columnDefinition41ResponsePacketList "ColumnDefinition41" 数据包列表
     */
    public TextResultSetMetaData(List<ColumnDefinition41ResponsePacket> columnDefinition41ResponsePacketList) {
        this.columnDefinition41ResponsePacketList = columnDefinition41ResponsePacketList;
    }

    @Override
    public int getColumnCount() {
        return columnDefinition41ResponsePacketList.size();
    }

    @Override
    public boolean isAutoIncrement(int column) throws SQLException {
        ColumnDefinition41ResponsePacket columnDefinitionPacket = getColumnDefinition41ResponsePacket(column);
        return ColumnDefinitionFlagsUtil.isColumnDefinitionEnabled(columnDefinitionPacket.getColumnDefinitionFlags(),
                ColumnDefinitionFlagsUtil.INDEX_AUTO_INCREMENT_FLAG);
    }

    @Override
    public boolean isCaseSensitive(int column) throws SQLException {
        Class<?> javaType = getJavaType(column);
        if (javaType == String.class) {
            ColumnDefinition41ResponsePacket columnDefinitionPacket = getColumnDefinition41ResponsePacket(column);
            return CharsetMappingUtil.isCaseSensitive(columnDefinitionPacket.getColumnCharacterId());
        } else {
            return false;
        }
    }

    @Override
    public boolean isSearchable(int column) {
        return true;
    }

    @Override
    public boolean isCurrency(int column) throws SQLException {
        return getJavaType(column) == BigDecimal.class;
    }

    @Override
    public int isNullable(int column) throws SQLException {
        ColumnDefinition41ResponsePacket columnDefinitionPacket = getColumnDefinition41ResponsePacket(column);
        if (ColumnDefinitionFlagsUtil.isColumnDefinitionEnabled(columnDefinitionPacket.getColumnDefinitionFlags(),
                ColumnDefinitionFlagsUtil.INDEX_NOT_NULL_FLAG)) {
            return columnNoNulls;
        } else {
            return columnNullable;
        }
    }

    @Override
    public boolean isSigned(int column) throws SQLException {
        ColumnDefinition41ResponsePacket columnDefinitionPacket = getColumnDefinition41ResponsePacket(column);
        return !ColumnDefinitionFlagsUtil.isColumnDefinitionEnabled(columnDefinitionPacket.getColumnDefinitionFlags(),
                ColumnDefinitionFlagsUtil.INDEX_UNSIGNED_FLAG);
    }

    @Override
    public int getColumnDisplaySize(int column) throws SQLException {
        ColumnDefinition41ResponsePacket columnDefinitionPacket = getColumnDefinition41ResponsePacket(column);
        if (columnDefinitionPacket.getMaximumColumnLength() > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        } else {
            return (int) columnDefinitionPacket.getMaximumColumnLength();
        }
    }

    @Override
    public String getColumnLabel(int column) throws SQLException {
        ColumnDefinition41ResponsePacket columnDefinitionPacket = getColumnDefinition41ResponsePacket(column);
        return columnDefinitionPacket.getColumnName();
    }

    @Override
    public String getColumnName(int column) throws SQLException {
        ColumnDefinition41ResponsePacket columnDefinitionPacket = getColumnDefinition41ResponsePacket(column);
        return columnDefinitionPacket.getOriginalColumnName();
    }

    @Override
    public String getSchemaName(int column) throws SQLException {
        ColumnDefinition41ResponsePacket columnDefinitionPacket = getColumnDefinition41ResponsePacket(column);
        return columnDefinitionPacket.getDatabaseName();
    }

    @Override
    public int getPrecision(int column) throws SQLException {
        ColumnDefinition41ResponsePacket columnDefinitionPacket = getColumnDefinition41ResponsePacket(column);
        int maximumColumnLength = columnDefinitionPacket.getMaximumColumnLength() > Integer.MAX_VALUE ?
                Integer.MAX_VALUE : (int) columnDefinitionPacket.getMaximumColumnLength();
        Class<?> columnJavaType = getJavaType(column);
        if (columnJavaType == Float.class || columnJavaType == Double.class || columnJavaType == BigDecimal.class) {
            boolean isUnsigned = ColumnDefinitionFlagsUtil.isColumnDefinitionEnabled(columnDefinitionPacket.getColumnDefinitionFlags(),
                    ColumnDefinitionFlagsUtil.INDEX_UNSIGNED_FLAG);
            int adjustLength = 0;
            if (columnDefinitionPacket.getDecimals() > 0) {
                if (columnJavaType == BigDecimal.class) {
                    adjustLength = isUnsigned ? -1 : -2;
                }
            } else {
                if (columnJavaType == BigDecimal.class) {
                    adjustLength = isUnsigned ? 0 : -1;
                } else {
                    adjustLength = 1;
                }
            }
            return maximumColumnLength + adjustLength;
        } else if (columnJavaType == String.class) {
            return maximumColumnLength / CharsetMappingUtil.getMaxBytesPerChar(columnDefinitionPacket.getColumnCharacterId());
        }
        return maximumColumnLength;
    }

    @Override
    public int getScale(int column) throws SQLException {
        Class<?> javaType = getJavaType(column);
        if (Number.class.isAssignableFrom(javaType)) {
            ColumnDefinition41ResponsePacket columnDefinitionPacket = getColumnDefinition41ResponsePacket(column);
            return columnDefinitionPacket.getDecimals();
        } else {
            return 0;
        }
    }

    @Override
    public String getTableName(int column) throws SQLException {
        ColumnDefinition41ResponsePacket columnDefinitionPacket = getColumnDefinition41ResponsePacket(column);
        return columnDefinitionPacket.getTableName();
    }

    @Override
    public String getCatalogName(int column) throws SQLException {
        ColumnDefinition41ResponsePacket columnDefinitionPacket = getColumnDefinition41ResponsePacket(column);
        return columnDefinitionPacket.getCatalog();
    }

    @Override
    public int getColumnType(int column) throws SQLException {
        ColumnDefinition41ResponsePacket columnDefinitionPacket = getColumnDefinition41ResponsePacket(column);
        return ColumnTypeMappingUtil.getJDBCType(columnDefinitionPacket.getColumnType(),
                columnDefinitionPacket.getColumnDefinitionFlags(), columnDefinitionPacket.getMaximumColumnLength());
    }

    @Override
    public String getColumnTypeName(int column) throws SQLException {
        ColumnDefinition41ResponsePacket columnDefinitionPacket = getColumnDefinition41ResponsePacket(column);
        return ColumnTypeMappingUtil.getTypeName(columnDefinitionPacket.getColumnType(),
                columnDefinitionPacket.getColumnDefinitionFlags(), columnDefinitionPacket.getMaximumColumnLength());
    }

    @Override
    public boolean isReadOnly(int column) {
        return true;
    }

    @Override
    public boolean isWritable(int column) {
        return false;
    }

    @Override
    public boolean isDefinitelyWritable(int column) {
        return false;
    }

    @Override
    public String getColumnClassName(int column) throws SQLException {
        return getJavaType(column).getName();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T unwrap(Class<T> iface) {
        return (T) this;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return TextResultSetMetaData.class == iface;
    }

    /**
     * 获得指定位置的列定义信息。
     *
     * @param columnIndex 列位置，第一列为 1，第二列为 2，以此类推...
     * @return 列定义信息
     * @throws SQLException 如果列位置越界，将抛出此异常
     */
    private ColumnDefinition41ResponsePacket getColumnDefinition41ResponsePacket(int columnIndex) throws SQLException {
        if (columnIndex > 0 && columnIndex <= columnDefinition41ResponsePacketList.size()) {
            return columnDefinition41ResponsePacketList.get(columnIndex - 1);
        } else {
            String errorMessage = "Get column definition packet failed: `columnIndex out of range`. Invalid column index: `"
                    + columnIndex + "`. Columns size: `" + columnDefinition41ResponsePacketList.size() + "`.";
            LOG.error(errorMessage);
            throw new SQLException(errorMessage);
        }
    }

    /**
     * 获得指定位置的列对应的 Java 类型。
     *
     * @param columnIndex 列位置，第一列为 1，第二列为 2，以此类推...
     * @return 列对应的 Java 类型
     * @throws SQLException 如果列位置越界，将抛出此异常
     */
    private Class<?> getJavaType(int columnIndex) throws SQLException {
        ColumnDefinition41ResponsePacket columnDefinitionPacket = getColumnDefinition41ResponsePacket(columnIndex);
        return ColumnTypeMappingUtil.getJavaType(columnDefinitionPacket.getColumnType(),
                columnDefinitionPacket.getColumnDefinitionFlags());
    }
}
