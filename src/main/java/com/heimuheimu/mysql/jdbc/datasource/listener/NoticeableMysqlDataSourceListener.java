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

package com.heimuheimu.mysql.jdbc.datasource.listener;

import com.heimuheimu.naivemonitor.alarm.NaiveServiceAlarm;
import com.heimuheimu.naivemonitor.alarm.ServiceAlarmMessageNotifier;
import com.heimuheimu.naivemonitor.alarm.ServiceContext;
import com.heimuheimu.naivemonitor.util.MonitorUtil;

import java.util.List;
import java.util.Map;

/**
 * 该监听器可用于当 Mysql 数据库连接池中发生数据库连接不可用或者从不可用状态恢复时，进行实时通知。
 *
 * @author heimuheimu
 * @see NaiveServiceAlarm
 */
public class NoticeableMysqlDataSourceListener implements MysqlDataSourceListener {

    /**
     * 使用 Mysql 数据库连接池的项目名称
     */
    private final String project;

    /**
     * 使用 Mysql 数据库连接池的主机名称
     */
    private final String host;

    /**
     * 服务不可用报警器
     */
    private final NaiveServiceAlarm naiveServiceAlarm;

    /**
     * 构造一个 Mysql 数据库连接池事件监听器，可在数据库连接不可用或者从不可用状态恢复时，进行实时通知。
     *
     * @param project 使用 Mysql 数据库连接池的项目名称
     * @param notifierList 服务不可用或从不可用状态恢复的报警消息通知器列表，不允许 {@code null} 或空
     * @throws IllegalArgumentException 如果消息通知器列表为 {@code null} 或空时，抛出此异常
     */
    public NoticeableMysqlDataSourceListener(String project, List<ServiceAlarmMessageNotifier> notifierList)
            throws IllegalArgumentException {
        this(project, notifierList, null);
    }

    /**
     * 构造一个 Mysql 数据库连接池事件监听器，可在数据库连接不可用或者从不可用状态恢复时，进行实时通知。
     *
     * @param project 使用 Mysql 数据库连接池的项目名称
     * @param notifierList 服务不可用或从不可用状态恢复的报警消息通知器列表，不允许 {@code null} 或空
     * @param hostAliasMap 别名 Map，Key 为机器名， Value 为别名，允许为 {@code null}
     * @throws IllegalArgumentException 如果消息通知器列表为 {@code null} 或空时，抛出此异常
     */
    public NoticeableMysqlDataSourceListener(String project, List<ServiceAlarmMessageNotifier> notifierList,
                                                   Map<String, String> hostAliasMap) throws IllegalArgumentException {
        this.project = project;
        this.naiveServiceAlarm = new NaiveServiceAlarm(notifierList);
        String host = MonitorUtil.getLocalHostName();
        if (hostAliasMap != null && hostAliasMap.containsKey(host)) {
            this.host = hostAliasMap.get(host);
        } else {
            this.host = host;
        }
    }

    @Override
    public void onCreated(String host, String databaseName) {
        // do nothing
    }

    @Override
    public void onRecovered(String host, String databaseName) {
        naiveServiceAlarm.onRecovered(getServiceContext(host, databaseName));
    }

    @Override
    public void onClosed(String host, String databaseName) {
        naiveServiceAlarm.onCrashed(getServiceContext(host, databaseName));
    }

    protected ServiceContext getServiceContext(String databaseHost, String databaseName) {
        ServiceContext serviceContext = new ServiceContext();
        serviceContext.setName("MySQL Connection");
        serviceContext.setHost(host);
        serviceContext.setProject(project);
        serviceContext.setRemoteHost(databaseHost + "/" + databaseName);
        return serviceContext;
    }

    @Override
    public String toString() {
        return "NoticeableMysqlDataSourceListener{" +
                "project='" + project + '\'' +
                ", host='" + host + '\'' +
                ", naiveServiceAlarm=" + naiveServiceAlarm +
                '}';
    }
}
