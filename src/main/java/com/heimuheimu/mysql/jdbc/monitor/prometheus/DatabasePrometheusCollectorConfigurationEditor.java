/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 heimuheimu
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

package com.heimuheimu.mysql.jdbc.monitor.prometheus;

import java.beans.PropertyEditorSupport;

/**
 * DatabasePrometheusCollectorConfiguration 类型转换器，支持在 Spring 配置文件中通过字符串形式配置 DatabasePrometheusCollectorConfiguration，
 * 字符串支持以下两种格式：
 * <ul>
 *     <li>jdbcURL, databaseAlias</li>
 *     <li>host, databaseName, databaseAlias</li>
 * </ul>
 *
 * <p><strong>注意：</strong>字符串使用 "," 作为分隔符，各个变量不应包含此字符。</p>
 *
 * @author heimuheimu
 * @since 1.1
 */
public class DatabasePrometheusCollectorConfigurationEditor extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Fails to parse `DatabasePrometheusCollectorConfiguration`: `text could not be null or empty`.");
        }
        String[] parts = text.split(",");
        if (parts.length == 2) {
            try {
                this.setValue(new DatabasePrometheusCollectorConfiguration(parts[0].trim(), parts[1].trim()));
            } catch (Exception e) {
                throw new IllegalArgumentException("Fails to parse `DatabasePrometheusCollectorConfiguration`: `invalid text`. `text`:`"
                        + text + "`.", e);
            }
        } else if (parts.length == 3) {
            try {
                this.setValue(new DatabasePrometheusCollectorConfiguration(parts[0].trim(), parts[1].trim(), parts[2].trim()));
            } catch (Exception e) {
                throw new IllegalArgumentException("Fails to parse `DatabasePrometheusCollectorConfiguration`: `invalid text`. `text`:`"
                        + text + "`.", e);
            }
        } else {
            throw new IllegalArgumentException("Fails to parse `DatabasePrometheusCollectorConfiguration`: `invalid text`. `text`:`"
                    + text + "`.");
        }
    }

    @Override
    public String getAsText() {
        DatabasePrometheusCollectorConfiguration configuration = (DatabasePrometheusCollectorConfiguration) this.getValue();
        return configuration == null ? "" : configuration.getHost() + ", " + configuration.getDatabaseName() + ", "
                + configuration.getDatabaseAlias();
    }
}
