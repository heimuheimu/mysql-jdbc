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

import com.heimuheimu.mysql.jdbc.ConnectionConfiguration;
import com.heimuheimu.mysql.jdbc.DatabaseInfoProvider;
import com.heimuheimu.mysql.jdbc.channel.MysqlChannel;
import com.heimuheimu.mysql.jdbc.command.SQLCommand;
import com.heimuheimu.mysql.jdbc.packet.command.text.ColumnDefinition41ResponsePacket;
import com.heimuheimu.mysql.jdbc.packet.command.text.TextResultsetResponsePacket;
import com.heimuheimu.mysql.jdbc.packet.generic.ErrorPacket;
import com.heimuheimu.mysql.jdbc.packet.generic.OKPacket;
import org.junit.*;

import java.math.BigDecimal;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * {@link ColumnTypeMappingUtil} 单元测试类。
 *
 * <p><strong>说明：</strong>该单元测试需访问 Mysql 数据库才可执行，默认为 Ignore，需手动执行。</p>
 *
 * @author heimuheimu
 */
@Ignore
public class TestColumnTypeMappingUtil {

    /**
     * 创建表 SQL 语句
     */
    private static final String CREATE_TABLE_SQL = "CREATE TABLE `heimuheimu_columns_type_test` (" +
            "  `c1` tinyint(4)," +
            "  `c2` smallint(6)," +
            "  `c3` mediumint(9)," +
            "  `c4` int(11)," +
            "  `c5` bigint(20)," +
            "  `c6` decimal(10,10)," +
            "  `c7` float," +
            "  `c8` double," +
            "  `c9` bit(64)," +
            "  `c10` date," +
            "  `c11` datetime," +
            "  `c12` timestamp," +
            "  `c13` time," +
            "  `c14` year(4)," +
            "  `c15` char(16)," +
            "  `c16` varchar(255)," +
            "  `c17` binary(16)," +
            "  `c18` varbinary(255)," +
            "  `c19` tinyblob," +
            "  `c20` blob," +
            "  `c21` mediumblob," +
            "  `c22` longblob," +
            "  `c23` tinytext," +
            "  `c24` text," +
            "  `c25` mediumtext," +
            "  `c26` longtext," +
            "  `c27` enum('Mercury','Venus','Earth')," +
            "  `c28` set('one','two','three')" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

    /**
     * 删除表 SQL 语句
     */
    private static final String DELETE_TABLE_SQL = "DROP TABLE `heimuheimu_columns_type_test`";

    /**
     * 查询 SQL 语句
     */
    private static final String SELECT_SQL = "select * from `heimuheimu_columns_type_test` limit 1";

    /**
     * 期望的列数量
     */
    private static final int EXPECTED_COLUMN_COUNT = 28;

    /**
     * 期望的列类型名称 Map，Key 为列名，Value 为列类型名称
     */
    private static final HashMap<String, String> EXPECTED_TYPE_NAME_MAP;

    /**
     * 期望的列类型对应的 Java Class Map，Key 为列名，Value 为列类型对应的 Java Class
     */
    private static final HashMap<String, Class<?>> EXPECTED_JAVA_CLASS_MAP;

    /**
     * 期望的列类型 JDBC Type Map，Key 为列名，Value 为 JDBC Type
     * @see Types
     */
    private static final HashMap<String, Integer> EXPECTED_JDBC_TYPE_MAP;

    static {
        EXPECTED_TYPE_NAME_MAP = new HashMap<>();
        EXPECTED_JAVA_CLASS_MAP = new HashMap<>();
        EXPECTED_JDBC_TYPE_MAP = new HashMap<>();

        EXPECTED_TYPE_NAME_MAP.put("c1", "TINYINT");
        EXPECTED_JAVA_CLASS_MAP.put("c1", Integer.class);
        EXPECTED_JDBC_TYPE_MAP.put("c1", Types.TINYINT);

        EXPECTED_TYPE_NAME_MAP.put("c2", "SMALLINT");
        EXPECTED_JAVA_CLASS_MAP.put("c2", Integer.class);
        EXPECTED_JDBC_TYPE_MAP.put("c2", Types.SMALLINT);

        EXPECTED_TYPE_NAME_MAP.put("c3", "MEDIUMINT");
        EXPECTED_JAVA_CLASS_MAP.put("c3", Integer.class);
        EXPECTED_JDBC_TYPE_MAP.put("c3", Types.INTEGER);

        EXPECTED_TYPE_NAME_MAP.put("c4", "INT");
        EXPECTED_JAVA_CLASS_MAP.put("c4", Integer.class);
        EXPECTED_JDBC_TYPE_MAP.put("c4", Types.INTEGER);

        EXPECTED_TYPE_NAME_MAP.put("c5", "BIGINT");
        EXPECTED_JAVA_CLASS_MAP.put("c5", Long.class);
        EXPECTED_JDBC_TYPE_MAP.put("c5", Types.BIGINT);

        EXPECTED_TYPE_NAME_MAP.put("c6", "DECIMAL");
        EXPECTED_JAVA_CLASS_MAP.put("c6", BigDecimal.class);
        EXPECTED_JDBC_TYPE_MAP.put("c6", Types.DECIMAL);

        EXPECTED_TYPE_NAME_MAP.put("c7", "FLOAT");
        EXPECTED_JAVA_CLASS_MAP.put("c7", Double.class);
        EXPECTED_JDBC_TYPE_MAP.put("c7", Types.FLOAT);

        EXPECTED_TYPE_NAME_MAP.put("c8", "DOUBLE");
        EXPECTED_JAVA_CLASS_MAP.put("c8", Double.class);
        EXPECTED_JDBC_TYPE_MAP.put("c8", Types.DOUBLE);

        EXPECTED_TYPE_NAME_MAP.put("c9", "BIT");
        EXPECTED_JAVA_CLASS_MAP.put("c9", Boolean.class);
        EXPECTED_JDBC_TYPE_MAP.put("c9", Types.BIT);

        EXPECTED_TYPE_NAME_MAP.put("c10", "DATE");
        EXPECTED_JAVA_CLASS_MAP.put("c10", Date.class);
        EXPECTED_JDBC_TYPE_MAP.put("c10", Types.DATE);

        EXPECTED_TYPE_NAME_MAP.put("c11", "DATETIME");
        EXPECTED_JAVA_CLASS_MAP.put("c11", Timestamp.class);
        EXPECTED_JDBC_TYPE_MAP.put("c11", Types.TIMESTAMP);

        EXPECTED_TYPE_NAME_MAP.put("c12", "TIMESTAMP");
        EXPECTED_JAVA_CLASS_MAP.put("c12", Timestamp.class);
        EXPECTED_JDBC_TYPE_MAP.put("c12", Types.TIMESTAMP);

        EXPECTED_TYPE_NAME_MAP.put("c13", "TIME");
        EXPECTED_JAVA_CLASS_MAP.put("c13", Time.class);
        EXPECTED_JDBC_TYPE_MAP.put("c13", Types.TIME);

        EXPECTED_TYPE_NAME_MAP.put("c14", "YEAR");
        EXPECTED_JAVA_CLASS_MAP.put("c14", Date.class);
        EXPECTED_JDBC_TYPE_MAP.put("c14", Types.DATE);

        EXPECTED_TYPE_NAME_MAP.put("c15", "CHAR");
        EXPECTED_JAVA_CLASS_MAP.put("c15", String.class);
        EXPECTED_JDBC_TYPE_MAP.put("c15", Types.CHAR);

        EXPECTED_TYPE_NAME_MAP.put("c16", "VARCHAR");
        EXPECTED_JAVA_CLASS_MAP.put("c16", String.class);
        EXPECTED_JDBC_TYPE_MAP.put("c16", Types.VARCHAR);

        EXPECTED_TYPE_NAME_MAP.put("c17", "BINARY");
        EXPECTED_JAVA_CLASS_MAP.put("c17", byte[].class);
        EXPECTED_JDBC_TYPE_MAP.put("c17", Types.BINARY);

        EXPECTED_TYPE_NAME_MAP.put("c18", "VARBINARY");
        EXPECTED_JAVA_CLASS_MAP.put("c18", byte[].class);
        EXPECTED_JDBC_TYPE_MAP.put("c18", Types.VARBINARY);

        EXPECTED_TYPE_NAME_MAP.put("c19", "TINYBLOB");
        EXPECTED_JAVA_CLASS_MAP.put("c19", byte[].class);
        EXPECTED_JDBC_TYPE_MAP.put("c19", Types.VARBINARY);

        EXPECTED_TYPE_NAME_MAP.put("c20", "BLOB");
        EXPECTED_JAVA_CLASS_MAP.put("c20", byte[].class);
        EXPECTED_JDBC_TYPE_MAP.put("c20", Types.LONGVARBINARY);

        EXPECTED_TYPE_NAME_MAP.put("c21", "MEDIUMBLOB");
        EXPECTED_JAVA_CLASS_MAP.put("c21", byte[].class);
        EXPECTED_JDBC_TYPE_MAP.put("c21", Types.LONGVARBINARY);

        EXPECTED_TYPE_NAME_MAP.put("c22", "LONGBLOB");
        EXPECTED_JAVA_CLASS_MAP.put("c22", byte[].class);
        EXPECTED_JDBC_TYPE_MAP.put("c22", Types.LONGVARBINARY);

        EXPECTED_TYPE_NAME_MAP.put("c23", "TINYTEXT");
        EXPECTED_JAVA_CLASS_MAP.put("c23", String.class);
        EXPECTED_JDBC_TYPE_MAP.put("c23", Types.VARCHAR);

        EXPECTED_TYPE_NAME_MAP.put("c24", "TEXT");
        EXPECTED_JAVA_CLASS_MAP.put("c24", String.class);
        EXPECTED_JDBC_TYPE_MAP.put("c24", Types.LONGVARCHAR);

        EXPECTED_TYPE_NAME_MAP.put("c25", "MEDIUMTEXT");
        EXPECTED_JAVA_CLASS_MAP.put("c25", String.class);
        EXPECTED_JDBC_TYPE_MAP.put("c25", Types.LONGVARCHAR);

        EXPECTED_TYPE_NAME_MAP.put("c26", "LONGTEXT");
        EXPECTED_JAVA_CLASS_MAP.put("c26", String.class);
        EXPECTED_JDBC_TYPE_MAP.put("c26", Types.LONGVARCHAR);

        EXPECTED_TYPE_NAME_MAP.put("c27", "ENUM");
        EXPECTED_JAVA_CLASS_MAP.put("c27", String.class);
        EXPECTED_JDBC_TYPE_MAP.put("c27", Types.CHAR);

        EXPECTED_TYPE_NAME_MAP.put("c28", "SET");
        EXPECTED_JAVA_CLASS_MAP.put("c28", String.class);
        EXPECTED_JDBC_TYPE_MAP.put("c28", Types.CHAR);
    }

    /**
     * Mysql 数据库数据交互管道列表
     */
    private static List<MysqlChannel> mysqlChannelList;

    @BeforeClass
    public static void init() throws SQLException {
        mysqlChannelList = new CopyOnWriteArrayList<>();
        for (Map<String, String> databaseInfo : DatabaseInfoProvider.getDatabaseInfoList()) {
            String host = databaseInfo.get("host");
            String databaseName = databaseInfo.get("databaseName");
            String username = databaseInfo.get("username");
            String password = databaseInfo.get("password");
            ConnectionConfiguration connectionConfiguration = new ConnectionConfiguration(host, databaseName, username, password,
                    45, 0, 30, null);
            MysqlChannel mysqlChannel = new MysqlChannel(connectionConfiguration, null);
            mysqlChannel.init();
            SQLCommand createTableCommand = new SQLCommand(CREATE_TABLE_SQL, mysqlChannel.getConnectionInfo().getJavaCharset());
            List<MysqlPacket> mysqlPacketList = mysqlChannel.send(createTableCommand, 5000);
            if (!OKPacket.isOkPacket(mysqlPacketList.get(0))) {
                ErrorPacket errorPacket = null;
                if (ErrorPacket.isErrorPacket(mysqlPacketList.get(0))) {
                    errorPacket = ErrorPacket.parse(mysqlPacketList.get(0), mysqlChannel.getConnectionInfo().getJavaCharset());
                }
                Assert.fail("Create table `heimuheimu_columns_type_test` failed. Error packet: " + errorPacket + ". Database info: "
                    + databaseInfo);
            }
            mysqlChannelList.add(mysqlChannel);
        }
    }

    @AfterClass
    public static void clean() throws SQLException {
        for (MysqlChannel mysqlChannel : mysqlChannelList) {
            SQLCommand deleteTableCommand = new SQLCommand(DELETE_TABLE_SQL, mysqlChannel.getConnectionInfo().getJavaCharset());
            List<MysqlPacket> mysqlPacketList = mysqlChannel.send(deleteTableCommand, 5000);
            if (!OKPacket.isOkPacket(mysqlPacketList.get(0))) {
                ErrorPacket errorPacket = null;
                if (ErrorPacket.isErrorPacket(mysqlPacketList.get(0))) {
                    errorPacket = ErrorPacket.parse(mysqlPacketList.get(0), mysqlChannel.getConnectionInfo().getJavaCharset());
                }
                Assert.fail("Delete table `heimuheimu_columns_type_test` failed. Error packet: " + errorPacket + ". MysqlChannel: "
                    + mysqlChannel);
            }
            mysqlChannel.close();
        }
    }

    @Test
    public void testGetTypeName() throws SQLException {
        if (mysqlChannelList == null || mysqlChannelList.isEmpty()) {
            Assert.fail("There is no active MysqlChannel.");
        }
        for (MysqlChannel mysqlChannel : mysqlChannelList) {
            HashMap<String, ColumnDefinition41ResponsePacket> definitionMap = getColumnDefinition41ResponsePacketMap(mysqlChannel);
            if (definitionMap == null) {
                Assert.fail("There is no metadata. `INDEX_CLIENT_OPTIONAL_RESULTSET_METADATA` is active. " + mysqlChannel);
            }
            for (int i = 1; i <= EXPECTED_COLUMN_COUNT; i++) {
                String columnName = "c" + i;
                if (definitionMap.containsKey(columnName)) {
                    ColumnDefinition41ResponsePacket definition41ResponsePacket = definitionMap.get(columnName);
                    String expectedTypeName = EXPECTED_TYPE_NAME_MAP.get(columnName);
                    if (expectedTypeName != null) {
                        Assert.assertEquals("Get column `" + columnName + "` type failed: "
                                        + definition41ResponsePacket + ". " + mysqlChannel,
                                expectedTypeName, ColumnTypeMappingUtil.getTypeName(definition41ResponsePacket.getColumnType(),
                                        definition41ResponsePacket.getColumnDefinitionFlags(),
                                        definition41ResponsePacket.getMaximumColumnLength()));
                    } else {
                        Assert.fail("`" + columnName + "` is not expected column. " + mysqlChannel);
                    }
                } else {
                    Assert.fail("Column `" + columnName + "` is not exist. " + mysqlChannel);
                }
            }
        }
    }

    @Test
    public void testGetJavaType() throws SQLException {
        if (mysqlChannelList == null || mysqlChannelList.isEmpty()) {
            Assert.fail("There is no active MysqlChannel.");
        }
        for (MysqlChannel mysqlChannel : mysqlChannelList) {
            HashMap<String, ColumnDefinition41ResponsePacket> definitionMap = getColumnDefinition41ResponsePacketMap(mysqlChannel);
            if (definitionMap == null) {
                Assert.fail("There is no metadata. `INDEX_CLIENT_OPTIONAL_RESULTSET_METADATA` is active. " + mysqlChannel);
            }
            for (int i = 1; i <= EXPECTED_COLUMN_COUNT; i++) {
                String columnName = "c" + i;
                if (definitionMap.containsKey(columnName)) {
                    ColumnDefinition41ResponsePacket definition41ResponsePacket = definitionMap.get(columnName);
                    Class<?> expectedJavaType = EXPECTED_JAVA_CLASS_MAP.get(columnName);
                    if (expectedJavaType != null) {
                        Assert.assertEquals("Get column `" + columnName + "` java type failed: "
                                        + definition41ResponsePacket + ". " + mysqlChannel, expectedJavaType,
                                ColumnTypeMappingUtil.getJavaType(definition41ResponsePacket.getColumnType(),
                                        definition41ResponsePacket.getColumnDefinitionFlags()));
                    } else {
                        Assert.fail("`" + columnName + "` is not expected column. " + mysqlChannel);
                    }
                } else {
                    Assert.fail("Column `" + columnName + "` is not exist. " + mysqlChannel);
                }
            }
        }
    }

    @Test
    public void testGetJDBCType() throws SQLException {
        if (mysqlChannelList == null || mysqlChannelList.isEmpty()) {
            Assert.fail("There is no active MysqlChannel.");
        }
        for (MysqlChannel mysqlChannel : mysqlChannelList) {
            HashMap<String, ColumnDefinition41ResponsePacket> definitionMap = getColumnDefinition41ResponsePacketMap(mysqlChannel);
            if (definitionMap == null) {
                Assert.fail("There is no metadata. `INDEX_CLIENT_OPTIONAL_RESULTSET_METADATA` is active. " + mysqlChannel);
            }
            for (int i = 1; i <= EXPECTED_COLUMN_COUNT; i++) {
                String columnName = "c" + i;
                if (definitionMap.containsKey(columnName)) {
                    ColumnDefinition41ResponsePacket definition41ResponsePacket = definitionMap.get(columnName);
                    Integer expectedJDBCType = EXPECTED_JDBC_TYPE_MAP.get(columnName);
                    if (expectedJDBCType != null) {
                        Assert.assertEquals("Get column `" + columnName + "` JDBC type failed: "
                                        + definition41ResponsePacket + ". " + mysqlChannel, (int) expectedJDBCType,
                                ColumnTypeMappingUtil.getJDBCType(definition41ResponsePacket.getColumnType(),
                                        definition41ResponsePacket.getColumnDefinitionFlags(),
                                        definition41ResponsePacket.getMaximumColumnLength()));
                    } else {
                        Assert.fail("`" + columnName + "` is not expected column. " + mysqlChannel);
                    }
                } else {
                    Assert.fail("Column `" + columnName + "` is not exist. " + mysqlChannel);
                }
            }
        }
    }

    private HashMap<String, ColumnDefinition41ResponsePacket> getColumnDefinition41ResponsePacketMap(MysqlChannel mysqlChannel) throws SQLException {
        SQLCommand selectCommand = new SQLCommand(SELECT_SQL, mysqlChannel.getConnectionInfo().getJavaCharset());
        List<MysqlPacket> mysqlPacketList = mysqlChannel.send(selectCommand, 5000);
        int i = 0;
        MysqlPacket packet = mysqlPacketList.get(i++);
        TextResultsetResponsePacket textResultsetResponsePacket = TextResultsetResponsePacket.parse(packet,
                mysqlChannel.getConnectionInfo().getCapabilitiesFlags());
        if (textResultsetResponsePacket.isMetadataFollows()) {
            HashMap<String, ColumnDefinition41ResponsePacket> result = new HashMap<>();
            for (int j = 0; j < textResultsetResponsePacket.getColumnCount(); j++) {
                ColumnDefinition41ResponsePacket definition41ResponsePacket = ColumnDefinition41ResponsePacket.parse(mysqlPacketList.get(i++),
                        mysqlChannel.getConnectionInfo().getJavaCharset());
                result.put(definition41ResponsePacket.getColumnName(), definition41ResponsePacket);
            }
            return result;
        } else {
            return null;
        }
    }
}
