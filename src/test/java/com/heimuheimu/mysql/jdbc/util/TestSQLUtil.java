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
import org.junit.Assert;
import org.junit.Test;

/**
 * {@link SQLUtil} 单元测试类。
 *
 * @author heimuheimu
 */
public class TestSQLUtil {

    private static final String[] SELECT_SQL_ARRAY = new String[] {
            "select * from test",
            " select * from test",
            "\tselect\t* from test",
            "\nselect\n* from test",
            "\rselect\r* from test",
            " \n\r\tselect \n\r\t* from test"
    };

    private static final String[] INSERT_SQL_ARRAY = new String[] {
            "insert into test (`val`) values ('demo')",
            " insert into test (`val`) values ('demo')",
            "\tinsert\tinto test (`val`) values ('demo')",
            "\ninsert\ninto test (`val`) values ('demo')",
            "\rinsert\ninto test (`val`) values ('demo')",
            " \n\r\tinsert \n\r\tinto test (`val`) values ('demo')"
    };

    private static final String[] UPDATE_SQL_ARRAY = new String[] {
            "update test set val = 'demo' where id = 1",
            " update test set val = 'demo' where id = 1",
            "\tupdate\ttest set val = 'demo' where id = 1",
            "\nupdate\ntest set val = 'demo' where id = 1",
            "\rupdate\rtest set val = 'demo' where id = 1",
            " \n\r\tupdate \n\r\ttest set val = 'demo' where id = 1"
    };

    private static final String[] DELETE_SQL_ARRAY = new String[] {
            "delete from test where id = 1",
            " delete from test where id = 1",
            "\tdelete\tfrom test where id = 1",
            "\ndelete\nfrom test where id = 1",
            "\rdelete\rfrom test where id = 1",
            " \n\r\tdelete \n\r\tfrom test where id = 1"
    };

    private static final String[] OTHER_SQL_ARRAY = new String[] {
            "",
            "truncate test",
            "inserd",
            "selecd",
            "updatd",
            "deletd"
    };

    @Test
    public void testGetSQLType() {
        for (String selectSQL : SELECT_SQL_ARRAY) {
            Assert.assertEquals("Invalid select sql: `" + selectSQL + "`.", SQLType.SELECT, SQLUtil.getSQLType(selectSQL));
            Assert.assertEquals("Invalid select sql: `" + selectSQL + "`.", SQLType.SELECT, SQLUtil.getSQLType(selectSQL.toUpperCase()));
        }
        for (String insertSQL : INSERT_SQL_ARRAY) {
            Assert.assertEquals("Invalid insert sql: `" + insertSQL + "`.", SQLType.INSERT, SQLUtil.getSQLType(insertSQL));
            Assert.assertEquals("Invalid insert sql: `" + insertSQL + "`.", SQLType.INSERT, SQLUtil.getSQLType(insertSQL.toUpperCase()));
        }
        for (String updateSQL : UPDATE_SQL_ARRAY) {
            Assert.assertEquals("Invalid update sql: `" + updateSQL + "`.", SQLType.UPDATE, SQLUtil.getSQLType(updateSQL));
            Assert.assertEquals("Invalid update sql: `" + updateSQL + "`.", SQLType.UPDATE, SQLUtil.getSQLType(updateSQL.toUpperCase()));
        }
        for (String deleteSQL : DELETE_SQL_ARRAY) {
            Assert.assertEquals("Invalid delete sql: `" + deleteSQL + "`.", SQLType.DELETE, SQLUtil.getSQLType(deleteSQL));
            Assert.assertEquals("Invalid delete sql: `" + deleteSQL + "`.", SQLType.DELETE, SQLUtil.getSQLType(deleteSQL.toUpperCase()));
        }
        for (String otherSQL : OTHER_SQL_ARRAY) {
            Assert.assertEquals("Invalid other sql: `" + otherSQL + "`.", SQLType.OTHER, SQLUtil.getSQLType(otherSQL));
            Assert.assertEquals("Invalid other sql: `" + otherSQL + "`.", SQLType.OTHER, SQLUtil.getSQLType(otherSQL.toUpperCase()));
        }
    }
}
