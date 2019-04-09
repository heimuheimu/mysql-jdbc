# mysql-jdbc: 为高并发应用量身定制的 MySQL 数据库驱动，同时集成适用于生产环境的数据库连接池。

[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/heimuheimu/mysql-jdbc.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/heimuheimu/mysql-jdbc/context:java)

## 使用要求
* JDK 版本：1.8+ 
* 依赖类库：
  * [slf4j-log4j12 1.7.5+](https://mvnrepository.com/artifact/org.slf4j/slf4j-log4j12)
  * [naivemonitor 1.0+](https://github.com/heimuheimu/naivemonitor)
* MySQL 版本：5.5+

## 使用限制
* MySQL 用户认证插件应使用 [mysql_native_password](https://dev.mysql.com/doc/refman/8.0/en/native-pluggable-authentication.html)（MySQL 版本 >= 8.0.4 时，默认认证插件为 "caching_sha2_password"，需修改配置）
* mysql-jdbc 不支持存储过程调用以及批量 SQL 执行相关 API 接口。

## 注意事项
### 1、affected rows
在执行 UPDATE 语句时，MYSQL 默认返回的 affected-rows 值为实际变更的行数，如果开启 "CLIENT_FOUND_ROWS" 特性，则返回的值为匹配的行数，
MYSQL 官方 JDBC 默认开启该特性，在 mysql-jdbc 中，可通过 "capabilitiesFlags" 参数进行开启，例如：
```
jdbc:mysql://localhost:3306/demo?capabilitiesFlags=2
```
更多信息请参考：[https://dev.mysql.com/doc/refman/8.0/en/mysql-affected-rows.html](https://dev.mysql.com/doc/refman/8.0/en/mysql-affected-rows.html)

## mysql-jdbc 特色：
* SQL 执行超时时，会自动执行 KILL 命令，防止慢查长时间占用数据库资源。
* 自动关闭泄漏的连接。
* 在慢查日志中可通过 MySQL 服务端状态信息中的 "isQueryNoGoodIndexUsed"、"isQueryNoIndexUsed"、"isSlow" 值判断是 SQL 语句问题还是网络传输问题。
* 完善的监控信息。
* 连接不可用或恢复时，实时报警。

## Maven 配置
```xml
    <dependency>
        <groupId>com.heimuheimu</groupId>
        <artifactId>mysql-jdbc</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
```

## Log4J 配置
```
# MySQL JDBC 根日志
log4j.logger.com.heimuheimu.mysql=WARN, MYSQL_JDBC_LOG
log4j.additivity.com.heimuheimu.mysql=false
log4j.appender.MYSQL_JDBC_LOG=org.apache.log4j.DailyRollingFileAppender
log4j.appender.MYSQL_JDBC_LOG.file=${log.output.directory}/mysql-jdbc/mysql-jdbc.log
log4j.appender.MYSQL_JDBC_LOG.encoding=UTF-8
log4j.appender.MYSQL_JDBC_LOG.DatePattern=_yyyy-MM-dd
log4j.appender.MYSQL_JDBC_LOG.layout=org.apache.log4j.PatternLayout
log4j.appender.MYSQL_JDBC_LOG.layout.ConversionPattern=%d{ISO8601} %-5p [%F:%L] : %m%n

# MySQL 连接信息日志
log4j.logger.MYSQL_CONNECTION_LOG=INFO, MYSQL_CONNECTION_LOG
log4j.additivity.MYSQL_CONNECTION_LOG=false
log4j.appender.MYSQL_CONNECTION_LOG=org.apache.log4j.DailyRollingFileAppender
log4j.appender.MYSQL_CONNECTION_LOG.file=${log.output.directory}/mysql-jdbc/connection.log
log4j.appender.MYSQL_CONNECTION_LOG.encoding=UTF-8
log4j.appender.MYSQL_CONNECTION_LOG.DatePattern=_yyyy-MM-dd
log4j.appender.MYSQL_CONNECTION_LOG.layout=org.apache.log4j.PatternLayout
log4j.appender.MYSQL_CONNECTION_LOG.layout.ConversionPattern=%d{ISO8601} %-5p : %m%n

# MySQL 慢查日志，打印执行时间过慢的操作
log4j.logger.MYSQL_SLOW_EXECUTION_LOG=INFO, MYSQL_SLOW_EXECUTION_LOG
log4j.additivity.MYSQL_SLOW_EXECUTION_LOG=false
log4j.appender.MYSQL_SLOW_EXECUTION_LOG=org.apache.log4j.DailyRollingFileAppender
log4j.appender.MYSQL_SLOW_EXECUTION_LOG.file=${log.output.directory}/mysql-jdbc/slow_execution.log
log4j.appender.MYSQL_SLOW_EXECUTION_LOG.encoding=UTF-8
log4j.appender.MYSQL_SLOW_EXECUTION_LOG.DatePattern=_yyyy-MM-dd
log4j.appender.MYSQL_SLOW_EXECUTION_LOG.layout=org.apache.log4j.PatternLayout
log4j.appender.MYSQL_SLOW_EXECUTION_LOG.layout.ConversionPattern=%d{ISO8601} : %m%n

# 调用 MySQL JDBC 不支持的方法错误日志
log4j.logger.SQL_FEATURE_NOT_SUPPORTED_LOG=ERROR, SQL_FEATURE_NOT_SUPPORTED_LOG
log4j.additivity.SQL_FEATURE_NOT_SUPPORTED_LOG=false
log4j.appender.SQL_FEATURE_NOT_SUPPORTED_LOG=org.apache.log4j.DailyRollingFileAppender
log4j.appender.SQL_FEATURE_NOT_SUPPORTED_LOG.file=${log.output.directory}/mysql-jdbc/sql_feature_not_supported.log
log4j.appender.SQL_FEATURE_NOT_SUPPORTED_LOG.encoding=UTF-8
log4j.appender.SQL_FEATURE_NOT_SUPPORTED_LOG.DatePattern=_yyyy-MM-dd
log4j.appender.SQL_FEATURE_NOT_SUPPORTED_LOG.layout=org.apache.log4j.PatternLayout
log4j.appender.SQL_FEATURE_NOT_SUPPORTED_LOG.layout.ConversionPattern=%d{ISO8601} : %m%n

# SQL 执行 DEBUG 日志（请勿在生产环境中开启）
log4j.logger.MYSQL_EXECUTION_DEBUG_LOG=DEBUG, MYSQL_EXECUTION_DEBUG_LOG
log4j.additivity.MYSQL_EXECUTION_DEBUG_LOG=false
log4j.appender.MYSQL_EXECUTION_DEBUG_LOG=org.apache.log4j.DailyRollingFileAppender
log4j.appender.MYSQL_EXECUTION_DEBUG_LOG.file=${log.output.directory}/mysql-jdbc/sql_execution_debug.log
log4j.appender.MYSQL_EXECUTION_DEBUG_LOG.encoding=UTF-8
log4j.appender.MYSQL_EXECUTION_DEBUG_LOG.DatePattern=_yyyy-MM-dd
log4j.appender.MYSQL_EXECUTION_DEBUG_LOG.layout=org.apache.log4j.PatternLayout
log4j.appender.MYSQL_EXECUTION_DEBUG_LOG.layout.ConversionPattern=%d{ISO8601} : %m%n
```

## Spring 配置
```xml
    <!-- 在 MySQL 连接不可用或恢复时进行实时通知，线程安全，多个数据库连接池可共用此监听器 -->
    <bean id="noticeableMysqlDataSourceListener" class="com.heimuheimu.mysql.jdbc.datasource.listener.NoticeableMysqlDataSourceListener">
        <constructor-arg index="0" value="your-project-name" /> <!-- 当前项目名称 -->
        <constructor-arg index="1" ref="notifierList" /> <!-- 报警器列表，报警器的信息可查看 naivemonitor 项目 -->
    </bean>
    
    <!-- MySQL 数据库连接池配置信息，线程安全，多个数据库连接池可共用此配置 -->
    <bean id="dataSourceConfiguration" class="com.heimuheimu.mysql.jdbc.datasource.DataSourceConfiguration">
        <constructor-arg index="0" value="20" /> <!-- 连接池大小，可结合监控数据 "连接池被使用的最大连接数量" 进行设置 -->
        <constructor-arg index="1" value="3000" /> <!-- 从连接池获取连接的超时时间，单位：毫秒，建议 3 秒 -->
        <constructor-arg index="2" value="10000" /> <!-- 连接最大占用时间，单位：毫秒，建议 10 秒 -->
        <constructor-arg index="3" value="5000" /> <!-- SQL 执行超时时间，单位：毫秒，建议 5 秒 -->
        <constructor-arg index="4" value="200" /> <!-- 执行 SQL 过慢最小时间，单位：毫秒，建议 200 毫秒-->
    </bean>
    
    <!-- MySQL 数据库连接池配置 -->
    <bean id="demoDataSource" class="com.heimuheimu.mysql.jdbc.datasource.spring.MysqlDataSourceFactory" destroy-method="close">
        <constructor-arg index="0">
            <bean class="com.heimuheimu.mysql.jdbc.ConnectionConfiguration"> <!-- 数据库连接使用的配置信息 -->
                <constructor-arg index="0" value="jdbc:mysql://localhost:3306/demo" /> <!-- JDBC URL 地址-->
                <constructor-arg index="1" value="root" /> <!-- 数据库用户名 -->
                <constructor-arg index="2" value="" /> <!-- 数据库密码 -->
            </bean>
        </constructor-arg>
        <constructor-arg index="1" ref="dataSourceConfiguration" /> <!-- 连接池配置信息 -->
        <constructor-arg index="2" ref="noticeableMysqlDataSourceListener" /> <!-- 连接池事件监听器 -->
    </bean>
```

## Falcon 监控数据上报 Spring 配置
```xml
    <!-- 监控数据采集器列表 -->
    <util:list id="falconDataCollectorList">
        <!-- MySQL 数据库数据采集器，每个数据库应独立配置 -->
        <bean class="com.heimuheimu.mysql.jdbc.monitor.falcon.DatabaseDataCollector">
            <constructor-arg index="0" value="jdbc:mysql://localhost:3306/demo" /> <!-- JDBC URL 地址，与数据库连接使用的地址一致 -->
            <constructor-arg index="1" value="demo_master" /> <!-- 采集器名称，生成 Falcon 的 Metric 名称时使用，每个库唯一 -->
        </bean>
    </util:list>
    
    <!-- Falcon 监控数据上报器 -->
    <bean id="falconReporter" class="com.heimuheimu.naivemonitor.falcon.FalconReporter" init-method="init" destroy-method="close">
        <constructor-arg index="0" value="http://127.0.0.1:1988/v1/push" /> <!-- Falcon 监控数据推送地址 -->
        <constructor-arg index="1" ref="falconDataCollectorList" />
    </bean>
```

## Falcon 上报数据项说明（上报周期：30秒，示例采集器名称 "demo_master"）
### 连接池数据项：
 * mysql_jdbc_demo_master_datasource_connection_leaked_count/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内连接池发生的连接泄漏次数
 * mysql_jdbc_demo_master_datasource_get_connection_failed_count/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内连接池发生的获取连接失败次数
 * mysql_jdbc_demo_master_datasource_acquired_connection_count/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 连接池当前被使用的连接数量
 * mysql_jdbc_demo_master_datasource_max_acquired_connection_count/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内连接池被使用的最大连接数量（应根据此值设置连接池大小）
 
### SQL 执行错误数据项： 
 * mysql_jdbc_demo_master_mysql_error/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内执行 SQL 发生的 MySQL 执行错误的次数
 * mysql_jdbc_demo_master_illegal_state/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内执行 SQL 发生的连接已被关闭的次数
 * mysql_jdbc_demo_master_timeout/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内执行 SQL 发生的执行超时的次数
 * mysql_jdbc_demo_master_invalid_parameter/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内组装 SQL 发生的参数值设置不正确的次数
 * mysql_jdbc_demo_master_result_error/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内发生的 ResultSet 操作错误的次数
 * mysql_jdbc_demo_master_unexpected_error/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内执行 SQL 发生的预期外错误次数
 * mysql_jdbc_demo_master_sql_slow_execution/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内执行 SQL 发生的慢查次数

### SQL 执行数据项：
 * mysql_jdbc_demo_master_avg_exec_time/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 SQL 执行平均时间，单位：纳秒
 * mysql_jdbc_demo_master_max_exec_time/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 SQL 执行最大时间，单位：纳秒
 * mysql_jdbc_demo_master_tps/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内每秒平均 SQL 执行次数
 * mysql_jdbc_demo_master_peak_tps/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内每秒最大 SQL 执行次数
 
### SQL 行数数据项：
 * mysql_jdbc_demo_master_insert_rows_count/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 SQL 新增的数据行数
 * mysql_jdbc_demo_master_max_insert_rows_count/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内单条 SQL 新增的最大数据行数
 * mysql_jdbc_demo_master_delete_rows_count/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 SQL 删除的数据行数
 * mysql_jdbc_demo_master_max_delete_rows_count/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内单条 SQL 删除的最大数据行数
 * mysql_jdbc_demo_master_update_rows_count/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 SQL 更新的数据行数
 * mysql_jdbc_demo_master_max_update_rows_count/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内单条 SQL 更新的最大数据行数
 * mysql_jdbc_demo_master_select_rows_count/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 SQL 查询的数据行数
 * mysql_jdbc_demo_master_max_select_rows_count/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内单条 SQL 查询的最大数据行数

### SQL 执行 Socket 数据项：
 * mysql_jdbc_demo_master_socket_avg_read_bytes/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 Socket 平均每次读取的平均字节数
 * mysql_jdbc_demo_master_socket_read_bytes/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 Socket 读取的总字节数
 * mysql_jdbc_demo_master_socket_avg_written_bytes/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 Socket 平均每次写入的平均字节数
 * mysql_jdbc_demo_master_socket_written_bytes/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 Socket 写入的总字节数