# mysql-jdbc: 为高并发应用量身定制的 MySQL 数据库驱动，同时集成适用于生产环境的数据库连接池。

[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/heimuheimu/mysql-jdbc.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/heimuheimu/mysql-jdbc/context:java)

## 使用要求
* JDK 版本：1.8+ 
* 依赖类库：
  * [slf4j-log4j12 1.7.5+](https://mvnrepository.com/artifact/org.slf4j/slf4j-log4j12)
  * [naivemonitor 1.1+](https://github.com/heimuheimu/naivemonitor)
* MySQL 版本：5.5+

## 使用限制
* MySQL 用户认证插件应使用 [mysql_native_password](https://dev.mysql.com/doc/refman/8.0/en/native-pluggable-authentication.html) （MySQL 版本 >= 8.0.4 时，默认认证插件为 "caching_sha2_password"，需修改配置，例如执行以下 SQL：ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'password';）
* mysql-jdbc 不支持存储过程调用以及批量 SQL 执行相关 API 接口。

## 注意事项
### 1、字符集编码
MYSQL 允许在创建连接时指定 Character Set，mysql-jdbc 在创建连接时，默认使用的编码为 "utf8mb4_general_ci"（支持 EMOJI），
由于 MYSQL 会自动对字符集编码进行转换，通常情况下，无需改变此设置（即使表或字段的字符集为 "ascii_general_ci"、"gbk_chinese_ci" 等），
如果有特殊需求，可通过 "characterId" 参数进行设置，例如将编码设置为 "utf8_general_ci"：
```
jdbc:mysql://localhost:3306/demo?characterId=33
```
编码及编码 ID 映射关系可通过下面的 SQL 语句进行查询：
```
SELECT id, collation_name FROM information_schema.collations ORDER BY id;
```

MYSQL Character Set 更多信息请参考：[https://dev.mysql.com/doc/refman/8.0/en/charset.html](https://dev.mysql.com/doc/refman/8.0/en/charset.html)

### 2、affected rows
在执行 UPDATE 语句时，MYSQL 默认返回的 affected-rows 值为实际变更的行数，如果开启 "CLIENT_FOUND_ROWS" 特性，则返回的值为匹配的行数，
MYSQL 官方 JDBC 默认开启该特性，在 mysql-jdbc 中，可通过 "capabilitiesFlags" 参数进行开启，例如：
```
jdbc:mysql://localhost:3306/demo?capabilitiesFlags=2
```
affected-rows 更多信息请参考：[https://dev.mysql.com/doc/refman/8.0/en/mysql-affected-rows.html](https://dev.mysql.com/doc/refman/8.0/en/mysql-affected-rows.html)

capabilitiesFlags 的更多信息请参考：[CapabilitiesFlagsUtil](https://github.com/heimuheimu/mysql-jdbc/blob/master/src/main/java/com/heimuheimu/mysql/jdbc/packet/CapabilitiesFlagsUtil.java)

### 3、类型自动映射
在执行 java.sql.ResultSet#getObject(int columnIndex) 等类型自动映射方法时，为简化实现，mysql-jdbc 未完全按照[JDBC 规范](https://download.oracle.com/otndocs/jcp/jdbc-4_2-mrel2-spec/index.html)
中的附录 B.1 进行实现， 同时与 MYSQL 官方 JDBC 提供的映射关系也存在部分不一致，例如 tinyint(1) 在官方 JDBC 中映射为 Boolean, mysql-jdbc 映射为 Integer，
mysql-jdbc 映射关系如下：

MYSQL 字段类型 | Java 类型
------------ | -------------
TINYINT | Integer
SMALLINT | Integer
MEDIUMINT | Integer
INT | Integer
BIGINT | Long
DECIMAL | java.math.BigDecimal
FLOAT | Double
DOUBLE | Double
BIT | Boolean
DATE | java.sql.Date
DATETIME | java.sql.Timestamp
TIMESTAMP | java.sql.Timestamp
TIME | java.sql.Time
YEAR | java.sql.Date
CHAR | String
VARCHAR | String
BINARY | byte[]
VARBINARY | byte[]
BLOB | byte[]
MEDIUMBLOB | byte[]
LONGBLOB | byte[]
TINYTEXT | String
TEXT | String
MEDIUMTEXT | String
LONGTEXT | String
ENUM | String
SET | String


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
        <version>1.1-SNAPSHOT</version>
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

# SQL 执行 DEBUG 日志（当级别为 DEBUG 时，将会打印每一条执行的 SQL 以及执行结果，请勿在生产环境中开启）
log4j.logger.MYSQL_EXECUTION_DEBUG_LOG=WARN, MYSQL_EXECUTION_DEBUG_LOG
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

## [推荐使用] Prometheus 监控系统数据采集
#### 1. 实现 Prometheus 监控指标导出 Controller（注意：请勿将此 Controller 暴露给公网访问，需通过策略仅允许 Prometheus 服务器或者内网访问）
```java
@Controller
@RequestMapping("/internal/")
public class PrometheusMetricsController {
    
    private final PrometheusExporter exporter;
    
    @Autowired
    public PrometheusMetricsController(PrometheusExporter exporter) {
        this.exporter = exporter;
    }
    
    @RequestMapping("/metrics")
    public void metrics(HttpServletResponse response) throws IOException {
        PrometheusHttpWriter.write(exporter.export(), response);
    }
}
```

#### 2. 在 Spring 中配置 PrometheusExporter 实例
```xml
    <bean name="prometheusExporter" class="com.heimuheimu.naivemonitor.prometheus.PrometheusExporter">
        <constructor-arg index="0" >
            <list>
                <!-- 数据库监控信息采集器 -->
                <bean class="com.heimuheimu.mysql.jdbc.monitor.prometheus.DatabaseCompositePrometheusCollector">
                    <constructor-arg index="0">
                        <list>
                            <value>jdbc:mysql://localhost:3306/user, user_master</value> <!-- 用户信息主库，JDBC 地址需与数据库连接使用的地址一致 -->
                            <value>jdbc:mysql://localhost:3307/user, user_slave</value> <!-- 用户信息从库，JDBC 地址需与数据库连接使用的地址一致 -->
                        </list>
                    </constructor-arg>
                </bean>
            </list>
        </constructor-arg>
    </bean>
```

#### 3. 在 Prometheus 服务中配置对应的 Job，并添加以下规则：
```yaml
groups:
  # MySQL 报警规则配置
  - name: MysqlAlerts
    rules:
      # MySQL 执行错误报警配置（不包括慢查和主键或唯一索引冲突错误）
      - alert: 'MysqlExecutionError'
        expr: mysql_jdbc_exec_error_count{errorType!~"SlowExecution|DuplicateEntryForKey"} > 0
        annotations:
          summary: "MySQL 执行错误"
          description: "MySQL 执行错误，主机地址：[{{ $labels.instance }}]，项目名称：[{{ $labels.job }}]，数据库名称：[{{ $labels.database }}]，错误类型：[{{ $labels.errorType }}]"

      # MySQL 执行慢查报警配置
      - alert: 'MysqlSlowExecution'
        expr: mysql_jdbc_exec_error_count{errorType="SlowExecution"} > 3
        for: 2m
        annotations:
          summary: "MySQL 执行慢查"
          description: "MySQL 执行慢查，主机地址：[{{ $labels.instance }}]，项目名称：[{{ $labels.job }}]，数据库名称：[{{ $labels.database }}]，错误类型：[{{ $labels.errorType }}]"

      # MySQL 主键或唯一索引冲突错误报警配置
      - alert: 'MysqlDuplicateEntryForKey'
        expr: mysql_jdbc_exec_error_count{errorType="DuplicateEntryForKey"} > 3
        for: 2m
        annotations:
          summary: "MySQL 主键或唯一索引冲突错误"
          description: "MySQL 主键或唯一索引冲突错误，主机地址：[{{ $labels.instance }}]，项目名称：[{{ $labels.job }}]，数据库名称：[{{ $labels.database }}]，错误类型：[{{ $labels.errorType }}]"

      # MySQL 连接池发生连接泄漏报警配置
      - alert: 'MysqlConnectionLeaked'
        expr: mysql_jdbc_datasource_connection_leaked_count > 0
        annotations:
          summary: "MySQL 连接池发生连接泄漏"
          description: "MySQL 连接池发生连接泄漏，主机地址：[{{ $labels.instance }}]，项目名称：[{{ $labels.job }}]，数据库名称：[{{ $labels.database }}]"

      # MySQL 连接池获取连接失败报警配置
      - alert: 'MysqlGetConnectionFailed'
        expr: mysql_jdbc_datasource_get_connection_failed_count > 0
        annotations:
          summary: "MySQL 连接池获取连接失败"
          description: "MySQL 连接池获取连接失败，主机地址：[{{ $labels.instance }}]，项目名称：[{{ $labels.job }}]，数据库名称：[{{ $labels.database }}]"

      # MySQL 出现 SQLFeatureNotSupportedException 异常报警配置
      - alert: 'MysqlSqlFeatureNotSupported'
        expr: mysql_jdbc_sql_feature_not_supported > 0
        annotations:
          summary: "MySQL 出现 SQLFeatureNotSupportedException 异常"
          description: "MySQL 出现 SQLFeatureNotSupportedException 异常，主机地址：[{{ $labels.instance }}]，项目名称：[{{ $labels.job }}]"

  # MySQL 聚合记录配置
  - name: MysqlRecords
    rules:
      # 相邻两次采集周期内 SQL 语句执行次数，根据数据库名称进行聚合计算
      - record: database:mysql_jdbc_exec_count:sum
        expr: sum(mysql_jdbc_exec_count) by (database)

      # 相邻两次采集周期内 SQL 语句执行次数，根据项目名称和数据库名称进行聚合计算
      - record: job:mysql_jdbc_exec_count:sum
        expr: sum(mysql_jdbc_exec_count) by (job, database)

      # 相邻两次采集周期内每秒最大 SQL 语句执行次数，根据数据库名称进行聚合计算（该值为估算值，实际值一般小于该估算值）
      - record: database:mysql_jdbc_exec_peak_tps_count:sum
        expr: sum(mysql_jdbc_exec_peak_tps_count) by (database)

      # 相邻两次采集周期内每秒最大 SQL 语句执行次数，根据项目名称和数据库名称进行聚合计算（该值为估算值，实际值一般小于该估算值）
      - record: job:mysql_jdbc_exec_peak_tps_count:sum
        expr: sum(mysql_jdbc_exec_peak_tps_count) by (job, database)

      # 相邻两次采集周期内单条 SQL 语句最大执行时间，单位：毫秒，根据数据库名称进行聚合计算
      - record: database:mysql_jdbc_max_exec_time_millisecond:max
        expr: max(mysql_jdbc_max_exec_time_millisecond) by (database)

      # 相邻两次采集周期内单条 SQL 语句最大执行时间，单位：毫秒，根据项目名称和数据库名称进行聚合计算
      - record: job:mysql_jdbc_max_exec_time_millisecond:max
        expr: max(mysql_jdbc_max_exec_time_millisecond) by (job, database)

      # 相邻两次采集周期内每条 SQL 语句平均执行时间，单位：毫秒，根据数据库名称进行聚合计算
      - record: database:mysql_jdbc_avg_exec_time_millisecond:avg
        expr: avg(mysql_jdbc_avg_exec_time_millisecond) by (database)

      # 相邻两次采集周期内每条 SQL 语句平均执行时间，单位：毫秒，根据项目名称和数据库名称进行聚合计算
      - record: job:mysql_jdbc_avg_exec_time_millisecond:avg
        expr: avg(mysql_jdbc_avg_exec_time_millisecond) by (job, database)

      # 相邻两次采集周期内 SQL 语句执行失败次数（包含慢查），根据数据库名称进行聚合计算
      - record: database:mysql_jdbc_exec_error_count:sum
        expr: sum(mysql_jdbc_exec_error_count) by (database)

      # 相邻两次采集周期内 SQL 语句执行失败次数（包含慢查），根据项目名称和数据库名称进行聚合计算
      - record: job:mysql_jdbc_exec_error_count:sum
        expr: sum(mysql_jdbc_exec_error_count) by (job, database)

      # 相邻两次采集周期内 Socket 读取的字节总数，单位：MB，根据数据库名称进行聚合计算
      - record: database:mysql_jdbc_socket_read_megabytes:sum
        expr: sum(mysql_jdbc_socket_read_bytes) by (database) / 1024 / 1024

      # 相邻两次采集周期内 Socket 读取的字节总数，单位：MB，根据项目名称和数据库名称进行聚合计算
      - record: job:mysql_jdbc_socket_read_megabytes:sum
        expr: sum(mysql_jdbc_socket_read_bytes) by (job, database) / 1024 / 1024

      # 相邻两次采集周期内 Socket 写入的字节总数，单位：MB，根据数据库名称进行聚合计算
      - record: database:mysql_jdbc_socket_write_megabytes:sum
        expr: sum(mysql_jdbc_socket_write_bytes) by (database) / 1024 / 1024

      # 相邻两次采集周期内 Socket 写入的字节总数，单位：MB，根据项目名称和数据库名称进行聚合计算
      - record: job:mysql_jdbc_socket_write_megabytes:sum
        expr: sum(mysql_jdbc_socket_write_bytes) by (job, database) / 1024 / 1024
```

  完成以上工作后，在 Prometheus 系统中即可找到以下监控指标（仅展示用户信息主库相关指标）：
* 连接池信息指标：
  * mysql_jdbc_datasource_acquired_connection_count{database="user_master"}    &nbsp;&nbsp;&nbsp;&nbsp; 采集时刻连接池正在使用的连接数量
  * mysql_jdbc_datasource_max_acquired_connection_count{database="user_master"}    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内连接池使用的最大连接数量
  * mysql_jdbc_datasource_connection_leaked_count{database="user_master"}    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内连接池发生连接泄漏的次数
  * mysql_jdbc_datasource_get_connection_failed_count{database="user_master"}    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内连接池获取不到连接的次数
* SQL 执行信息指标：
  * mysql_jdbc_exec_count{database="user_master"}    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内 SQL 语句执行次数
  * mysql_jdbc_exec_peak_tps_count{database="user_master"}    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内每秒最大 SQL 语句执行次数
  * mysql_jdbc_avg_exec_time_millisecond{database="user_master"}    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内每条 SQL 语句平均执行时间，单位：毫秒
  * mysql_jdbc_max_exec_time_millisecond{database="user_master"}    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内单条 SQL 语句最大执行时间，单位：毫秒
* SQL 执行错误信息指标：
  * mysql_jdbc_exec_error_count{errorCode="-1",errorType="MysqlError",database="user_master"}    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内 SQL 语句执行出现 MYSQL 服务端执行异常的错误次数
  * mysql_jdbc_exec_error_count{errorCode="-2",errorType="IllegalState",database="user_master"}    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内 SQL 语句执行出现管道或命令已关闭的错误次数
  * mysql_jdbc_exec_error_count{errorCode="-3",errorType="Timeout",database="user_master"}    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内 SQL 语句执行出现执行超时的错误次数
  * mysql_jdbc_exec_error_count{errorCode="-4",errorType="InvalidParameter",database="user_master"}    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内 SQL 语句执行出现参数值设置不正确的错误次数
  * mysql_jdbc_exec_error_count{errorCode="-5",errorType="ResultSetError",database="user_master"}    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内查询结果集 ResultSet 操作异常的错误次数
  * mysql_jdbc_exec_error_count{errorCode="-6",errorType="UnexpectedError",database="user_master"}    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内 SQL 语句执行出现预期外异常的错误次数
  * mysql_jdbc_exec_error_count{errorCode="-7",errorType="SlowExecution",database="user_master"}    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内 SQL 语句执行出现执行过慢的错误次数
  * mysql_jdbc_exec_error_count{errorCode="-8",errorType="DuplicateEntryForKey",database="user_master"}    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内 SQL 语句执行出现主键或唯一索引冲突的错误次数
* 数据库 Socket 读、写信息指标：
  * mysql_jdbc_socket_read_count{database="user_master",remoteAddress="localhost:3306/user"}    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内 Socket 读取的次数
  * mysql_jdbc_socket_read_bytes{database="user_master",remoteAddress="localhost:3306/user"}    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内 Socket 读取的字节总数
  * mysql_jdbc_socket_max_read_bytes{database="user_master",remoteAddress="localhost:3306/user"}    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内单次 Socket 读取的最大字节数
  * mysql_jdbc_socket_write_count{database="user_master",remoteAddress="localhost:3306/user"}    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内 Socket 写入的次数
  * mysql_jdbc_socket_write_bytes{database="user_master",remoteAddress="localhost:3306/user"}    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内 Socket 写入的字节总数
  * mysql_jdbc_socket_max_write_bytes{database="user_master",remoteAddress="localhost:3306/user"}    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内单次 Socket 写入的最大字节数
* SELECT 语句统计信息指标：
  * mysql_jdbc_select_count{database="user_master"}    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内 SELECT 语句执行总次数
  * mysql_jdbc_select_rows_count{database="user_master"}    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内所有 SELECT 语句返回的记录总数
  * mysql_jdbc_max_select_rows_count{database="user_master"}    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内单条 SELECT 语句返回的最大记录数
* INSERT 语句统计信息指标：
  * mysql_jdbc_insert_count{database="user_master"}    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内所有 INSERT 语句执行总次数
  * mysql_jdbc_insert_rows_count{database="user_master"}    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内所有 INSERT 语句插入的记录总数
  * mysql_jdbc_max_insert_rows_count{database="user_master"}    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内单条 INSERT 语句插入的最大记录数
* UPDATE 语句统计信息指标：
  * mysql_jdbc_update_count{database="user_master"}    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内所有 UPDATE 语句执行总次数
  * mysql_jdbc_update_rows_count{database="user_master"}    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内所有 UPDATE 语句更新的记录总数
  * mysql_jdbc_max_update_rows_count{database="user_master"}    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内单条 UPDATE 语句更新的最大记录数
* DELETE 语句统计信息指标：
  * mysql_jdbc_delete_count{database="user_master"}    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内所有 DELETE 语句执行总次数
  * mysql_jdbc_delete_rows_count{database="user_master"}    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内所有 DELETE 语句删除的记录总数
  * mysql_jdbc_max_delete_rows_count{database="user_master"}    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内单条 DELETE 语句删除的最大记录数
  
  通过 util-grafana 项目可以为 mysql-jdbc 监控指标快速生成 Grafana 监控图表，项目地址：[https://github.com/heimuheimu/util-grafana](https://github.com/heimuheimu/util-grafana)

## Falcon 监控系统数据采集
#### 1. 在 Spring 中配置 Falcon 数据推送：
```xml
    <!-- 监控数据采集器列表 -->
    <util:list id="falconDataCollectorList">
        <!-- 用户信息主库数据采集器 -->
        <bean class="com.heimuheimu.mysql.jdbc.monitor.falcon.DatabaseDataCollector">
            <constructor-arg index="0" value="jdbc:mysql://localhost:3306/user" /> <!-- 用户信息主库 JDBC 地址，与数据库连接使用的地址一致 -->
            <constructor-arg index="1" value="user_master" /> <!-- 采集器名称，生成 Falcon 的 Metric 名称时使用，每个库唯一 -->
        </bean>
        <!-- 用户信息从库数据采集器 -->
        <bean class="com.heimuheimu.mysql.jdbc.monitor.falcon.DatabaseDataCollector">
            <constructor-arg index="0" value="jdbc:mysql://localhost:3306/user" /> <!-- 用户信息主库 JDBC 地址，与数据库连接使用的地址一致 -->
            <constructor-arg index="1" value="user_master" /> <!-- 采集器名称，生成 Falcon 的 Metric 名称时使用，每个库唯一 -->
        </bean>
    </util:list>
    
    <!-- Falcon 监控数据上报器 -->
    <bean id="falconReporter" class="com.heimuheimu.naivemonitor.falcon.FalconReporter" init-method="init" destroy-method="close">
        <constructor-arg index="0" value="http://127.0.0.1:1988/v1/push" /> <!-- Falcon 监控数据推送地址 -->
        <constructor-arg index="1" ref="falconDataCollectorList" />
    </bean>
```
  完成以上工作后，在 Falcon 系统中可以找到以下数据项（仅展示用户信息主库相关指标）：
* 连接池数据项：
 * mysql_jdbc_user_master_datasource_connection_leaked_count/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内连接池发生的连接泄漏次数
 * mysql_jdbc_user_master_datasource_get_connection_failed_count/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内连接池发生的获取连接失败次数
 * mysql_jdbc_user_master_datasource_acquired_connection_count/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 连接池当前被使用的连接数量
 * mysql_jdbc_user_master_datasource_max_acquired_connection_count/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内连接池被使用的最大连接数量（应根据此值设置连接池大小）
* SQL 执行错误数据项： 
 * mysql_jdbc_user_master_mysql_error/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内执行 SQL 发生的 MySQL 执行错误的次数
 * mysql_jdbc_user_master_illegal_state/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内执行 SQL 发生的连接已被关闭的次数
 * mysql_jdbc_user_master_timeout/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内执行 SQL 发生的执行超时的次数
 * mysql_jdbc_user_master_invalid_parameter/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内组装 SQL 发生的参数值设置不正确的次数
 * mysql_jdbc_user_master_result_error/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内发生的 ResultSet 操作错误的次数
 * mysql_jdbc_user_master_unexpected_error/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内执行 SQL 发生的预期外错误次数
 * mysql_jdbc_user_master_sql_slow_execution/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内执行 SQL 发生的慢查次数
* SQL 执行数据项：
 * mysql_jdbc_user_master_avg_exec_time/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 SQL 执行平均时间，单位：纳秒
 * mysql_jdbc_user_master_max_exec_time/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 SQL 执行最大时间，单位：纳秒
 * mysql_jdbc_user_master_tps/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内每秒平均 SQL 执行次数
 * mysql_jdbc_user_master_peak_tps/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内每秒最大 SQL 执行次数
* SQL 语句统计数据项：
 * mysql_jdbc_user_master_insert_rows_count/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 SQL 新增的数据行数
 * mysql_jdbc_user_master_max_insert_rows_count/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内单条 SQL 新增的最大数据行数
 * mysql_jdbc_user_master_delete_rows_count/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 SQL 删除的数据行数
 * mysql_jdbc_user_master_max_delete_rows_count/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内单条 SQL 删除的最大数据行数
 * mysql_jdbc_user_master_update_rows_count/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 SQL 更新的数据行数
 * mysql_jdbc_user_master_max_update_rows_count/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内单条 SQL 更新的最大数据行数
 * mysql_jdbc_user_master_select_rows_count/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 SQL 查询的数据行数
 * mysql_jdbc_user_master_max_select_rows_count/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内单条 SQL 查询的最大数据行数
* SQL 执行 Socket 数据项：
 * mysql_jdbc_user_master_socket_avg_read_bytes/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 Socket 平均每次读取的平均字节数
 * mysql_jdbc_user_master_socket_read_bytes/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 Socket 读取的总字节数
 * mysql_jdbc_user_master_socket_avg_written_bytes/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 Socket 平均每次写入的平均字节数
 * mysql_jdbc_user_master_socket_written_bytes/module=mysql_jdbc &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 Socket 写入的总字节数
 
## 版本发布记录
### V1.1-SNAPSHOT
### 新增特性：
 * 新增主键或唯一索引冲突错误监控和 SELECT、INSERT、UPDATE、DELETE 语句执行次数监控
 * 在与 MySQL 数据库建立连接时增加超时设置
 * 支持将监控数据推送至 Prometheus 监控系统

***

### V1.0
### 特性：
 * 自动 Kill 执行超时的数据库链接
 * 自动关闭泄漏的连接
 * 集成适用于生产环境的数据库连接池
 * 支持将监控数据推送至 Falcon 监控系统
  
## 更多信息
* [[推荐使用] Prometheus 监控系统](https://prometheus.io/docs/introduction/overview/)
* [Falcon 监控系统](https://book.open-falcon.org/zh/)