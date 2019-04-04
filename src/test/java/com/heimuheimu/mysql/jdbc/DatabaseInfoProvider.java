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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 提供用于测试使用的 Mysql 数据库信息列表。
 *
 * @author heimuheimu
 */
public class DatabaseInfoProvider {

    /**
     * 获得用于测试使用的 Mysql 数据库信息列表，返回的 Map 包含以下 Key：`host`、`databaseName`、`username`、`password`，
     * 如果该方法返回 {@code null} 或空 Map，则需要访问 Mysql 数据库才可执行的测试用例将不会执行。
     *
     * @return 用于测试使用的 Mysql 数据库信息列表，不会返回 {@code null}
     */
    public static List<Map<String, String>> getDatabaseInfoList() {
        List<Map<String, String>> result = new ArrayList<>();

        HashMap<String, String> v5_5_56 = new HashMap<>();
        v5_5_56.put("host", "192.168.80.136:3306");
        v5_5_56.put("databaseName", "test");
        v5_5_56.put("username", "ts");
        v5_5_56.put("password", "");
        result.add(v5_5_56);

        HashMap<String, String> v5_6_25 = new HashMap<>();
        v5_6_25.put("host", "192.168.16.100:3306");
        v5_6_25.put("databaseName", "test");
        v5_6_25.put("username", "root");
        v5_6_25.put("password", "");
        result.add(v5_6_25);

        HashMap<String, String> v5_7_17 = new HashMap<>();
        v5_7_17.put("host", "192.168.16.176:3308");
        v5_7_17.put("databaseName", "green_dam_gril");
        v5_7_17.put("username", "dev");
        v5_7_17.put("password", "dev");
        result.add(v5_7_17);

        HashMap<String, String> v8_0_11 = new HashMap<>();
        v8_0_11.put("host", "192.168.80.140:3306");
        v8_0_11.put("databaseName", "test");
        v8_0_11.put("username", "dev");
        v8_0_11.put("password", "123qweQWE!@#");
        result.add(v8_0_11);
        return result;
    }
}
