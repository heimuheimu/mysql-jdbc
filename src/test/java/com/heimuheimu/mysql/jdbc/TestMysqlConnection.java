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

package com.heimuheimu.mysql.jdbc;

import com.heimuheimu.mysql.jdbc.packet.ColumnTypeMappingUtil;
import org.junit.*;

import java.sql.Connection;
import java.sql.SQLException;
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
public class TestMysqlConnection {

    /**
     * Mysql 数据库连接列表
     */
    private static List<MysqlConnection> MYSQL_CONNECTION_LIST;

    @BeforeClass
    public static void init() throws SQLException {
        MYSQL_CONNECTION_LIST = new CopyOnWriteArrayList<>();
        for (Map<String, String> databaseInfo : DatabaseInfoProvider.getDatabaseInfoList()) {
            String host = databaseInfo.get("host");
            String databaseName = databaseInfo.get("databaseName");
            String username = databaseInfo.get("username");
            String password = databaseInfo.get("password");
            ConnectionConfiguration connectionConfiguration = new ConnectionConfiguration(host, databaseName, username, password,
                    45, 0, 30, null);
            MysqlConnection connection = new MysqlConnection(connectionConfiguration, 5000, 5, null);
            MYSQL_CONNECTION_LIST.add(connection);
        }
    }

    @AfterClass
    public static void clean() throws SQLException {
        for (MysqlConnection connection : MYSQL_CONNECTION_LIST) {
            connection.close();
        }
    }

    @Test
    public void testAutoCommit() throws SQLException {
        for (MysqlConnection connection : MYSQL_CONNECTION_LIST) {
            connection.setNetworkTimeout(null, 5000);
            connection.setAutoCommit(true);
            Assert.assertTrue(connection.getAutoCommit());
            connection.setAutoCommit(false);
            Assert.assertFalse(connection.getAutoCommit());
            connection.setAutoCommit(false);
            Assert.assertFalse(connection.getAutoCommit());
            connection.setAutoCommit(true);
            Assert.assertTrue(connection.getAutoCommit());
        }
    }

    public void testReadOnly() throws SQLException {
        for (MysqlConnection connection : MYSQL_CONNECTION_LIST) {
            connection.setNetworkTimeout(null, 5000);
            connection.setReadOnly(false);
            Assert.assertFalse(connection.isReadOnly());
            connection.setReadOnly(true);
            Assert.assertTrue(connection.isReadOnly());
        }
    }

    @Test
    public void testTransactionIsolation() throws SQLException {
        for (MysqlConnection connection : MYSQL_CONNECTION_LIST) {
            connection.setNetworkTimeout(null, 5000);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            Assert.assertTrue(connection.getTransactionIsolation() == Connection.TRANSACTION_READ_COMMITTED);

            connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            Assert.assertTrue(connection.getTransactionIsolation() == Connection.TRANSACTION_READ_UNCOMMITTED);

            connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            Assert.assertTrue(connection.getTransactionIsolation() == Connection.TRANSACTION_REPEATABLE_READ);

            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            Assert.assertTrue(connection.getTransactionIsolation() == Connection.TRANSACTION_SERIALIZABLE);
        }
    }
}
