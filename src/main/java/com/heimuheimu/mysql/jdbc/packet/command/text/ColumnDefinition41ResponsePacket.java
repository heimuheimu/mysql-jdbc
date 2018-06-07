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

package com.heimuheimu.mysql.jdbc.packet.command.text;

import com.heimuheimu.mysql.jdbc.packet.MysqlPacket;

import java.nio.charset.Charset;

/**
 * "ColumnDefinition41" 数据包信息，与 "TextResultset" 数据包一起发送，更多信息请参考：
 * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_query_response_text_resultset_column_definition.html">
 *     ColumnDefinition41
 * </a>
 *
 * <p><strong>说明：</strong>{@code ColumnDefinition41ResponsePacket} 类是非线程安全的，不允许多个线程使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class ColumnDefinition41ResponsePacket {

    /**
     * 列使用的 catalog 值（The catalog used. Currently always "def"）
     */
    private String catalog = "";

    /**
     * 列所在的数据库名称（schema name）
     */
    private String databaseName = "";

    /**
     * 列所在的虚拟表名称（virtual table name）
     */
    private String tableName = "";

    /**
     * 列所在的物理表名称（physical table name）
     */
    private String originalTableName = "";

    /**
     * 列虚拟名称（virtual column name）
     */
    private String columnName = "";

    /**
     * 列物理名称（physical column name）
     */
    private String originalColumnName = "";

    /**
     * 固定长度列的长度（length of fixed length fields：[0x0c]）
     */
    private int fixedColumnLength = 0;

    /**
     * 列使用的字符集编码 ID（the column character set as defined in Character Set）
     */
    private int columnCharacterId = -1;

    /**
     * 列最大长度（maximum length of the field）
     */
    private long maximumColumnLength = 0;

    /**
     * 列类型（type of the column as defined in enum_field_types）
     */
    private int columnType = 0;

    /**
     * 列定义数值，每个比特位可代表不同的列定义（Flags as defined in Column Definition Flags）
     */
    private int columnDefinitionFlags = 0;

    /**
     * 小数显示位数（max shown decimal digits）
     */
    private int decimals = 0;

    /**
     * 获得列使用的 catalog 值（The catalog used. Currently always "def"）。
     *
     * @return 列使用的 catalog 值
     */
    public String getCatalog() {
        return catalog;
    }

    /**
     * 设置列使用的 catalog 值。
     *
     * @param catalog 列使用的 catalog 值
     */
    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    /**
     * 获得列所在的数据库名称（schema name）。
     *
     * @return 列所在的数据库名称
     */
    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * 设置列所在的数据库名称。
     *
     * @param databaseName 列所在的数据库名称
     */
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    /**
     * 获得列所在的虚拟表名称（virtual table name）。
     *
     * @return 列所在的虚拟表名称
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * 设置列所在的虚拟表名称。
     *
     * @param tableName 列所在的虚拟表名称
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * 获得列所在的物理表名称（physical table name）。
     *
     * @return 列所在的物理表名称
     */
    public String getOriginalTableName() {
        return originalTableName;
    }

    /**
     * 设置列所在的物理表名称。
     *
     * @param originalTableName 列所在的物理表名称
     */
    public void setOriginalTableName(String originalTableName) {
        this.originalTableName = originalTableName;
    }

    /**
     * 获得列虚拟名称（virtual column name）。
     *
     * @return 列虚拟名称
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * 设置列虚拟名称。
     *
     * @param columnName 列虚拟名称
     */
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    /**
     * 获得列物理名称（physical column name）。
     *
     * @return 列物理名称
     */
    public String getOriginalColumnName() {
        return originalColumnName;
    }

    /**
     * 设置列物理名称。
     *
     * @param originalColumnName 列物理名称
     */
    public void setOriginalColumnName(String originalColumnName) {
        this.originalColumnName = originalColumnName;
    }

    /**
     * 获得固定长度列的长度（length of fixed length fields：[0x0c]）。
     *
     * @return 固定长度列的长度
     */
    public int getFixedColumnLength() {
        return fixedColumnLength;
    }

    /**
     * 设置固定长度列的长度。
     *
     * @param fixedColumnLength 固定长度列的长度
     */
    public void setFixedColumnLength(int fixedColumnLength) {
        this.fixedColumnLength = fixedColumnLength;
    }

    /**
     * 获得列使用的字符集编码 ID（the column character set as defined in Character Set。
     *
     * @return 列使用的字符集编码 ID
     */
    public int getColumnCharacterId() {
        return columnCharacterId;
    }

    /**
     * 设置列使用的字符集编码 ID。
     *
     * @param columnCharacterId 列使用的字符集编码 ID
     */
    public void setColumnCharacterId(int columnCharacterId) {
        this.columnCharacterId = columnCharacterId;
    }

    /**
     * 获得列最大长度（maximum length of the field）。
     *
     * @return 列最大长度
     */
    public long getMaximumColumnLength() {
        return maximumColumnLength;
    }

    /**
     * 设置列最大长度。
     *
     * @param maximumColumnLength 列最大长度
     */
    public void setMaximumColumnLength(long maximumColumnLength) {
        this.maximumColumnLength = maximumColumnLength;
    }

    /**
     * 获得列类型（type of the column as defined in enum_field_types）。
     *
     * @return 列类型
     */
    public int getColumnType() {
        return columnType;
    }

    /**
     * 设置列类型。
     *
     * @param columnType 列类型
     */
    public void setColumnType(int columnType) {
        this.columnType = columnType;
    }

    /**
     * 获得列定义数值，每个比特位可代表不同的列定义（Flags as defined in Column Definition Flags）。
     *
     * @return 列定义数值
     */
    public int getColumnDefinitionFlags() {
        return columnDefinitionFlags;
    }

    /**
     * 设置列定义数值，每个比特位可代表不同的列定义。
     *
     * @param columnDefinitionFlags 列定义数值
     */
    public void setColumnDefinitionFlags(int columnDefinitionFlags) {
        this.columnDefinitionFlags = columnDefinitionFlags;
    }

    /**
     * 获得小数显示位数（max shown decimal digits）。
     *
     * @return 小数显示位数
     */
    public int getDecimals() {
        return decimals;
    }

    /**
     * 设置小数显示位数。
     *
     * @param decimals 小数显示位数
     */
    public void setDecimals(int decimals) {
        this.decimals = decimals;
    }

    @Override
    public String toString() {
        return "ColumnDefinition41ResponsePacket{" +
                "catalog='" + catalog + '\'' +
                ", databaseName='" + databaseName + '\'' +
                ", tableName='" + tableName + '\'' +
                ", originalTableName='" + originalTableName + '\'' +
                ", columnName='" + columnName + '\'' +
                ", originalColumnName='" + originalColumnName + '\'' +
                ", fixedColumnLength=" + fixedColumnLength +
                ", columnCharacterId=" + columnCharacterId +
                ", maximumColumnLength=" + maximumColumnLength +
                ", columnType=" + columnType +
                ", columnDefinitionFlags=" + columnDefinitionFlags +
                ", decimals=" + decimals +
                '}';
    }

    /**
     * 对 Mysql "ColumnDefinition41" 数据包进行解析，生成对应的 {@code ColumnDefinition41ResponsePacket} 实例，
     * "ColumnDefinition41" 数据包格式定义：
     * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_query_response_text_resultset_column_definition.html">
     *     ColumnDefinition41
     * </a>
     *
     * @param packet "ColumnDefinition41" 数据包
     * @param charset 字符集编码
     * @return {@code ColumnDefinition41ResponsePacket} 实例
     * @throws IllegalArgumentException 如果 Mysql 数据包不是正确的 "ColumnDefinition41" 数据包，将会抛出此异常
     */
    public static ColumnDefinition41ResponsePacket parse(MysqlPacket packet, Charset charset) {
        try {
            packet.setPosition(0);
            ColumnDefinition41ResponsePacket columnDefinition41ResponsePacket = new ColumnDefinition41ResponsePacket();
            columnDefinition41ResponsePacket.setCatalog(packet.readLengthEncodedString(charset));
            columnDefinition41ResponsePacket.setDatabaseName(packet.readLengthEncodedString(charset));
            columnDefinition41ResponsePacket.setTableName(packet.readLengthEncodedString(charset));
            columnDefinition41ResponsePacket.setOriginalTableName(packet.readLengthEncodedString(charset));
            columnDefinition41ResponsePacket.setColumnName(packet.readLengthEncodedString(charset));
            columnDefinition41ResponsePacket.setOriginalColumnName(packet.readLengthEncodedString(charset));
            columnDefinition41ResponsePacket.setFixedColumnLength((int) packet.readLengthEncodedInteger());
            columnDefinition41ResponsePacket.setColumnCharacterId((int) packet.readFixedLengthInteger(2));
            columnDefinition41ResponsePacket.setMaximumColumnLength(packet.readFixedLengthInteger(4));
            columnDefinition41ResponsePacket.setColumnType((int) packet.readFixedLengthInteger(1));
            columnDefinition41ResponsePacket.setColumnDefinitionFlags((int) packet.readFixedLengthInteger(2));
            columnDefinition41ResponsePacket.setDecimals((int) packet.readFixedLengthInteger(1));
            return columnDefinition41ResponsePacket;
        } catch (Exception e) {
            throw new IllegalArgumentException("Parse ColumnDefinition41 failed: `invalid format`. " + packet, e);
        }
    }
}
