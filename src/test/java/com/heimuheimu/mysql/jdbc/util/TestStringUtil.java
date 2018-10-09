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

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link TestStringUtil} 单元测试类。
 *
 * @author heimuheimu
 */
public class TestStringUtil {

    private static final Map<String, String> ESCAPE_TEST_CASE_MAP;

    static {
        ESCAPE_TEST_CASE_MAP = new HashMap<>();
        ESCAPE_TEST_CASE_MAP.put(null, null);
        ESCAPE_TEST_CASE_MAP.put("", "");
        ESCAPE_TEST_CASE_MAP.put("heimuheimu", "heimuheimu");
        ESCAPE_TEST_CASE_MAP.put("\0", "\\0");
        ESCAPE_TEST_CASE_MAP.put("'", "\\'");
        ESCAPE_TEST_CASE_MAP.put("\b", "\\b");
        ESCAPE_TEST_CASE_MAP.put("\n", "\\n");
        ESCAPE_TEST_CASE_MAP.put("\r", "\\r");
        ESCAPE_TEST_CASE_MAP.put("\t", "\\t");
        ESCAPE_TEST_CASE_MAP.put("\032", "\\Z");
        ESCAPE_TEST_CASE_MAP.put("\\", "\\\\");
        ESCAPE_TEST_CASE_MAP.put("a \0 ' \b \n \r \t \032 \\ \\s c", "a \\0 \\' \\b \\n \\r \\t \\Z \\\\ \\\\s c");
    }

    @Test
    public void testEscape() {
        for (String value : ESCAPE_TEST_CASE_MAP.keySet()) {
            String expectedValue = ESCAPE_TEST_CASE_MAP.get(value);
            Assert.assertEquals("Escape failed.", expectedValue, StringUtil.escape(value));
        }
    }
}
