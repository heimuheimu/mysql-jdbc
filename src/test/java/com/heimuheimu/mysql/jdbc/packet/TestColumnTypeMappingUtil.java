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
import com.heimuheimu.mysql.jdbc.channel.MysqlChannel;
import com.heimuheimu.mysql.jdbc.command.SQLCommand;
import com.heimuheimu.mysql.jdbc.packet.command.text.ColumnDefinition41ResponsePacket;
import com.heimuheimu.mysql.jdbc.packet.command.text.TextResultsetResponsePacket;
import com.heimuheimu.mysql.jdbc.packet.generic.ErrorPacket;
import com.heimuheimu.mysql.jdbc.packet.generic.OKPacket;
import org.junit.*;

import java.sql.SQLException;
import java.util.List;

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
     * 目标 Mysql 地址，由主机名和端口组成，":" 符号分割，例如：localhost:3306
     */
    private static final String host = "192.168.80.136:3306";

    /**
     * 目标 Mysql 数据库名称
     */
    private static final String databaseName = "test";

    /**
     * 目标 Mysql 数据库用户名
     */
    private static final String username = "ts";

    /**
     * 目标 Mysql 数据库密码
     */
    private static final String password = "";

    /**
     * 创建表 SQL 语句
     */
    private static final String CREATE_TABLE_SQL = "CREATE TABLE `heimuheimu_columns_type_test` (" +
            "  `c1_tinyint` tinyint(4)," +
            "  `c2_smallint` smallint(6)," +
            "  `c3_mediumint` mediumint(9)," +
            "  `c4_int` int(11)," +
            "  `c5_bigint` bigint(20)," +
            "  `c6_decimal` decimal(10,10)," +
            "  `c7_float` float," +
            "  `c8_double` double," +
            "  `c9_bit` bit(64)," +
            "  `c10_date` date," +
            "  `c11_datetime` datetime," +
            "  `c12_timestamp` timestamp," +
            "  `c13_time` time," +
            "  `c14_year` year(4)," +
            "  `c15_char` char(16)," +
            "  `c16_varchar` varchar(255)," +
            "  `c17_binary` binary(16)," +
            "  `c18_varbinary` varbinary(255)," +
            "  `c19_tinyblob` tinyblob," +
            "  `c20_blob` blob," +
            "  `c21_mediumblob` mediumblob," +
            "  `c22_longblob` longblob," +
            "  `c23_tinytext` tinytext," +
            "  `c24_text` text," +
            "  `c25_mediumtext` mediumtext," +
            "  `c26_longtext` longtext," +
            "  `c27_enum` enum('Mercury','Venus','Earth')," +
            "  `c28_set` set('one','two','three')," +
            "  `c29_real` double" +
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
     * Mysql 数据库数据交互管道
     */
    private static MysqlChannel mysqlChannel;

    @BeforeClass
    public static void init() throws SQLException {
        ConnectionConfiguration connectionConfiguration = new ConnectionConfiguration(host, databaseName, username, password,
                45, 0, 30, null);
        mysqlChannel = new MysqlChannel(connectionConfiguration, null);
        mysqlChannel.init();

        SQLCommand createTableCommand = new SQLCommand(CREATE_TABLE_SQL, mysqlChannel.getConnectionInfo().getJavaCharset());
        List<MysqlPacket> mysqlPacketList = mysqlChannel.send(createTableCommand, 5000);
        if (!OKPacket.isOkPacket(mysqlPacketList.get(0))) {
            ErrorPacket errorPacket = null;
            if (ErrorPacket.isErrorPacket(mysqlPacketList.get(0))) {
                errorPacket = ErrorPacket.parse(mysqlPacketList.get(0), mysqlChannel.getConnectionInfo().getJavaCharset());
            }
            Assert.fail("Create table `heimuheimu_columns_type_test` failed. Error packet: " + errorPacket);
        }
    }

    @AfterClass
    public static void clean() throws SQLException {
        SQLCommand deleteTableCommand = new SQLCommand(DELETE_TABLE_SQL, mysqlChannel.getConnectionInfo().getJavaCharset());
        List<MysqlPacket> mysqlPacketList = mysqlChannel.send(deleteTableCommand, 5000);
        if (!OKPacket.isOkPacket(mysqlPacketList.get(0))) {
            ErrorPacket errorPacket = null;
            if (ErrorPacket.isErrorPacket(mysqlPacketList.get(0))) {
                errorPacket = ErrorPacket.parse(mysqlPacketList.get(0), mysqlChannel.getConnectionInfo().getJavaCharset());
            }
            Assert.fail("Delete table `heimuheimu_columns_type_test` failed. Error packet: " + errorPacket);
        }
        mysqlChannel.close();
    }

    @Test
    public void testGetTypeName() throws SQLException {
        SQLCommand selectCommand = new SQLCommand(SELECT_SQL, mysqlChannel.getConnectionInfo().getJavaCharset());
        List<MysqlPacket> mysqlPacketList = mysqlChannel.send(selectCommand, 5000);
        int i = 0;
        MysqlPacket packet = mysqlPacketList.get(i++);
        TextResultsetResponsePacket textResultsetResponsePacket = TextResultsetResponsePacket.parse(packet,
                mysqlChannel.getConnectionInfo().getCapabilitiesFlags());
        if (textResultsetResponsePacket.isMetadataFollows()) {
            for (int j = 0; j < textResultsetResponsePacket.getColumnCount(); j++) {
                ColumnDefinition41ResponsePacket definition41ResponsePacket = ColumnDefinition41ResponsePacket.parse(mysqlPacketList.get(i++),
                        mysqlChannel.getConnectionInfo().getJavaCharset());
                Assert.assertEquals("Get column type failed: " + definition41ResponsePacket, getExpectedTypeName(definition41ResponsePacket),
                        ColumnTypeMappingUtil.getTypeName(definition41ResponsePacket.getColumnType(), definition41ResponsePacket.getColumnCharacterId()));
            }
        } else {
            Assert.fail("There is no metadata. `INDEX_CLIENT_OPTIONAL_RESULTSET_METADATA` is active.");
        }
    }

    private String getExpectedTypeName(ColumnDefinition41ResponsePacket definition41ResponsePacket) {
        String columnName = definition41ResponsePacket.getColumnName();
        String[] columnNameParts = columnName.split("_");
        return columnNameParts[1].toUpperCase();
    }
}
