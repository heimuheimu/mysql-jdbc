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

package com.heimuheimu.mysql.jdbc.channel;

import com.heimuheimu.mysql.jdbc.ConnectionConfiguration;
import com.heimuheimu.mysql.jdbc.ConnectionInfo;
import com.heimuheimu.mysql.jdbc.command.Command;
import com.heimuheimu.mysql.jdbc.command.PingCommand;
import com.heimuheimu.mysql.jdbc.command.SQLCommand;
import com.heimuheimu.mysql.jdbc.constant.BeanStatusEnum;
import com.heimuheimu.mysql.jdbc.facility.UnusableServiceNotifier;
import com.heimuheimu.mysql.jdbc.facility.parameter.ConstructorParameterChecker;
import com.heimuheimu.mysql.jdbc.facility.parameter.Parameters;
import com.heimuheimu.mysql.jdbc.monitor.SocketMonitorFactory;
import com.heimuheimu.mysql.jdbc.net.BuildSocketException;
import com.heimuheimu.mysql.jdbc.net.SocketBuilder;
import com.heimuheimu.mysql.jdbc.net.SocketConfiguration;
import com.heimuheimu.mysql.jdbc.packet.MysqlPacket;
import com.heimuheimu.mysql.jdbc.packet.MysqlPacketReader;
import com.heimuheimu.mysql.jdbc.packet.generic.ErrorPacket;
import com.heimuheimu.mysql.jdbc.util.LogBuildUtil;
import com.heimuheimu.naivemonitor.facility.MonitoredSocketOutputStream;
import com.heimuheimu.naivemonitor.monitor.SocketMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLTimeoutException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Mysql Client/Server 协议与 Mysql 服务进行数据交互的管道。协议定义请参考文档：
 * <p>
 * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/PAGE_PROTOCOL.html">
 *     Client/Server Protocol
 * </a>
 * </p>
 *
 * <p><strong>说明：</strong>{@code MysqlChannel} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class MysqlChannel implements Closeable {

    private static final Logger MYSQL_CONNECTION_LOG = LoggerFactory.getLogger("MYSQL_CONNECTION_LOG");
    
    private static final Logger LOG = LoggerFactory.getLogger(MysqlChannel.class);

    /**
     * 建立 Mysql 数据库连接使用的配置信息
     */
    private final ConnectionConfiguration connectionConfiguration;

    /**
     * 与 Mysql 服务器建立的 Socket 连接
     */
    private final Socket socket;

    /**
     * 当前数据交互管道使用的 Socket 信息监控器
     */
    private final SocketMonitor socketMonitor;

    /**
     * {@code MysqlChannel} 不可用通知器，允许为 {@code null}
     */
    private final UnusableServiceNotifier<MysqlChannel> unusableServiceNotifier;

    /**
     * 当前实例所处状态
     */
    private volatile BeanStatusEnum state = BeanStatusEnum.UNINITIALIZED;

    /**
     * Mysql 数据库连接信息
     */
    private volatile ConnectionInfo connectionInfo = null;

    /**
     * IO 线程
     */
    private MysqlIOTask ioTask = null;

    /**
     * 等待发送的 Mysql 命令队列
     */
    private final LinkedBlockingQueue<Command> commandQueue = new LinkedBlockingQueue<>();

    /**
     * 构造一个与 Mysql 服务进行数据交互的管道。
     *
     * @param configuration 建立 Mysql 数据库连接使用的配置信息，不允许 {@code null}
     * @param unusableServiceNotifier {@code MysqlChannel} 不可用通知器，允许为 {@code null}
     * @throws IllegalArgumentException 如果 {@code configuration} 为 {@code null}，将会抛出此异常
     * @throws IllegalArgumentException 如果 Mysql 地址不符合规则，将会抛出此异常
     * @throws BuildSocketException 如果创建 {@link Socket} 过程中发生错误，将会抛出此异常
     */
    public MysqlChannel(ConnectionConfiguration configuration, UnusableServiceNotifier<MysqlChannel> unusableServiceNotifier)
            throws IllegalArgumentException, BuildSocketException {
        ConstructorParameterChecker checker = new ConstructorParameterChecker("MysqlChannel", LOG);
        checker.addParameter("connectionConfiguration", configuration);
        checker.addParameter("unusableServiceNotifier", unusableServiceNotifier);

        checker.check("connectionConfiguration", "isNull", Parameters::isNull);

        this.connectionConfiguration = configuration;
        this.socket = SocketBuilder.create(configuration.getHost(), configuration.getSocketConfiguration());
        this.socketMonitor = SocketMonitorFactory.get(configuration.getHost(), configuration.getDatabaseName());
        this.unusableServiceNotifier = unusableServiceNotifier;
    }

    /**
     * 获得建立 Mysql 数据库连接使用的配置信息，该方法不会返回 {@code null}。
     *
     * @return Mysql 数据库连接使用的配置信息
     */
    public ConnectionConfiguration getConnectionConfiguration() {
        return connectionConfiguration;
    }

    /**
     * 获得 Mysql 数据库连接信息，如果连接未建立或已关闭，将会返回 {@code null}。
     *
     * @return Mysql 数据库连接信息
     */
    public ConnectionInfo getConnectionInfo() {
        return connectionInfo;
    }

    /**
     * 判断当前与 Mysql 服务进行数据交互的管道是否可用。
     *
     * @return 管道是否可用
     */
    public boolean isAvailable() {
        return state == BeanStatusEnum.NORMAL;
    }

    /**
     * 发送一个 Mysql 命令，并返回响应数据列表，如果等待响应数据超时，当前 {@code MysqlChannel} 将会被直接关闭。
     *
     * @param command Mysql 命令
     * @param timeout 超时时间，单位：毫秒
     * @return 该命令对应的响应数据列表，不会返回 {@code null}
     * @throws NullPointerException 如果 {@code command} 为 {@code null}，将会抛出此异常
     * @throws IllegalStateException 当前 {@code MysqlChannel} 未初始化或已被关闭，将抛出此异常
     * @throws IllegalStateException 等待响应数据过程中，命令被关闭或中断，将抛出此异常
     * @throws SQLTimeoutException 如果等待响应数据超时，将抛出此异常
     */
    public List<MysqlPacket> send(Command command, long timeout) throws NullPointerException, IllegalStateException, SQLTimeoutException {
        if (command == null) {
            Map<String, Object> extendParameterMap = new HashMap<>();
            extendParameterMap.put("timeout", timeout);
            throw new NullPointerException("Execute mysql command failed: `command could not be null`." + buildLogForParameters(extendParameterMap));
        }
        if (state == BeanStatusEnum.NORMAL) {
            commandQueue.add(command);
        } else {
            Map<String, Object> extendParameterMap = new HashMap<>();
            extendParameterMap.put("timeout", timeout);
            extendParameterMap.put("command", command);
            throw new IllegalStateException("Execute mysql command failed: `MysqlChannel is not initialized or has been closed`. `state`:`"
                    + state + "`." + buildLogForParameters(extendParameterMap));
        }
        try {
            return command.getResponsePacketList(timeout);
        } catch (SQLTimeoutException e) {
            Map<String, Object> extendParameterMap = new HashMap<>();
            extendParameterMap.put("timeout", timeout);
            extendParameterMap.put("command", command);
            String parametersLog = buildLogForParameters(extendParameterMap);
            MYSQL_CONNECTION_LOG.error("MysqlChannel need to be closed: `execute command timeout`.{}", parametersLog);
            LOG.error("Execute mysql command failed: `wait response packet timeout, MysqlChannel need to be closed`." + parametersLog, e);
            close(true);
            throw e;
        }
    }

    /**
     * 执行 {@code MysqlChannel} 初始化操作。
     */
    public synchronized void init() {
        if (state == BeanStatusEnum.UNINITIALIZED) {
            try {
                if (socket.isConnected() && !socket.isClosed()) {
                    state = BeanStatusEnum.NORMAL;
                    long startTime = System.currentTimeMillis();
                    SocketConfiguration config = SocketBuilder.getConfig(socket);
                    String socketAddress = connectionConfiguration.getHost() + "/" + socket.getLocalPort();
                    // 启动 IO 线程
                    ioTask = new MysqlIOTask(config.getReceiveBufferSize());
                    ioTask.setName("mysql-io-" + socketAddress);
                    ioTask.start();

                    Map<String, Object> extendParameterMap = new HashMap<>();
                    extendParameterMap.put("localSocketPort", socket.getLocalPort());
                    extendParameterMap.put("socketConfig", config);
                    MYSQL_CONNECTION_LOG.info("MysqlChannel has benn initialized. `cost`:`{}ms`.{}",
                            (System.currentTimeMillis() - startTime), buildLogForParameters(extendParameterMap));
                } else {
                    MYSQL_CONNECTION_LOG.error("Initialize MysqlChannel failed: `socket is not connected or has been closed`.{}",
                            buildLogForParameters(null));
                    close();
                }
            } catch (Exception e) {
                String parametersLog = buildLogForParameters(null);
                MYSQL_CONNECTION_LOG.error("Initialize MysqlChannel failed: `{}`.{}", e.getMessage(), parametersLog);
                LOG.error("Initialize MysqlChannel failed: `" + e.getMessage() + "`." + parametersLog, e);
                close();
            }
        }
    }

    @Override
    public synchronized void close() {
        close(false);
    }

    private synchronized void close(boolean sendKillCommand) {
        if (state != BeanStatusEnum.CLOSED) {
            long startTime = System.currentTimeMillis();
            state = BeanStatusEnum.CLOSED;
            if (sendKillCommand) {
                MysqlChannelKillTask killTask = new MysqlChannelKillTask(connectionConfiguration, connectionInfo);
                killTask.setName("mysql-channel-kill-task");
                killTask.start();
            }
            try {
                //关闭 Socket 连接
                socket.close();
                //停止 IO 线程
                if (ioTask != null) {
                    ioTask.stopSignal = true;
                    ioTask.interrupt();
                }
                MYSQL_CONNECTION_LOG.info("MysqlChannel has been closed. `cost`:`{}ms`.{}",
                        (System.currentTimeMillis() - startTime), buildLogForParameters(null));
            } catch (Exception e) {
                String parametersLog = buildLogForParameters(null);
                MYSQL_CONNECTION_LOG.error("Close MysqlChannel failed: `{}`.{}", e.getMessage(), parametersLog);
                LOG.error("Close MysqlChannel failed: `" + e.getMessage() + "`." + parametersLog, e);
            } finally {
                if (unusableServiceNotifier != null) {
                    unusableServiceNotifier.onClosed(this);
                }
                connectionInfo = null;
            }
        }
    }

    @Override
    public String toString() {
        return "MysqlChannel{" +
                "connectionConfiguration=" + connectionConfiguration +
                ", socket=" + socket +
                ", unusableServiceNotifier=" + unusableServiceNotifier +
                ", state=" + state +
                ", connectionInfo=" + connectionInfo +
                '}';
    }

    /**
     * 返回当前 {@code MysqlChannel} 相关参数信息，用于日志打印。
     *
     * @return 当前 {@code MysqlChannel} 相关参数信息
     */
    private String buildLogForParameters(Map<String, Object> extendParameterMap) {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("host", connectionConfiguration == null ? "" : connectionConfiguration.getHost());
        parameterMap.put("connectionId", connectionInfo == null ? "" : connectionInfo.getConnectionId());
        if (extendParameterMap != null && !extendParameterMap.isEmpty()) {
            parameterMap.putAll(extendParameterMap);
        }
        parameterMap.put("connectionConfiguration", connectionConfiguration);
        parameterMap.put("connectionInfo", connectionInfo);
        return LogBuildUtil.build(parameterMap);
    }

    /**
     * Mysql IO 线程
     */
    private class MysqlIOTask extends Thread {

        private final MonitoredSocketOutputStream outputStream;

        private final MysqlPacketReader reader;

        private volatile boolean stopSignal = false;

        /**
         * 等待响应数据包的 Mysql 命令队列
         */
        private final LinkedList<Command> waitingQueue = new LinkedList<>();

        private MysqlIOTask(Integer receiveBufferSize) throws IOException {
            this.outputStream = new MonitoredSocketOutputStream(socket.getOutputStream(), socketMonitor);

            receiveBufferSize = receiveBufferSize != null ? receiveBufferSize : 64 * 1024;
            this.reader = new MysqlPacketReader(socket.getInputStream(), receiveBufferSize, socketMonitor);

            HandshakeProcessor handshakeProcessor = new HandshakeProcessor(connectionConfiguration, outputStream, reader);
            connectionInfo = handshakeProcessor.doHandshake();
        }

        @Override
        public void run() {
            int pingPeriod = connectionConfiguration.getPingPeriod();
            Command command;
            while (!stopSignal) {
                try {
                    if (pingPeriod <= 0) {
                        command = commandQueue.take();
                    } else {
                        command = commandQueue.poll(pingPeriod, TimeUnit.SECONDS);
                        if (command == null) { // 如果心跳检测时间内没有请求，创建一个 Ping 命令进行发送
                            long pingCommandStartTime = System.currentTimeMillis();
                            PingCommand pingCommand = new PingCommand();
                            Thread pingCheckThread = new Thread(() -> { // 启动一个异步线程检查心跳是否有正常返回
                                try {
                                    if ( pingCommand.isSuccess(5000) ) {
                                        LOG.debug("Execute `PingCommand` success. `cost`:`{}ms`.{}",
                                                System.currentTimeMillis() - pingCommandStartTime,
                                                buildLogForParameters(null));
                                    } else { // should not happen
                                        String parametersLog = buildLogForParameters(null);
                                        MYSQL_CONNECTION_LOG.info("MysqlChannel need to be closed: `execute PingCommand failed`.{}", parametersLog);
                                        LOG.error("MysqlChannel need to be closed: `execute PingCommand failed`.{}", parametersLog);
                                        MysqlChannel.this.close();
                                    }
                                } catch (Exception e) {
                                    String parametersLog = buildLogForParameters(null);
                                    MYSQL_CONNECTION_LOG.info("MysqlChannel need to be closed: `execute PingCommand failed`.{}", parametersLog);
                                    LOG.error("MysqlChannel need to be closed: `execute PingCommand failed`." + parametersLog, e);
                                    MysqlChannel.this.close();
                                }
                            });
                            String socketAddress = connectionConfiguration.getHost() + "/" + socket.getLocalPort();
                            pingCheckThread.setName("mysql-ping-check-" + socketAddress);
                            pingCheckThread.start();
                            command = pingCommand;
                        }
                    }

                    byte[] requestPacket = command.getRequestByteArray();
                    outputStream.write(requestPacket);
                    outputStream.flush();

                    if (command.hasResponsePacket()) {
                        waitingQueue.add(command);
                    }

                    // 如果该连接某个命令一直等待不到返回，可能会一直阻塞
                    while (waitingQueue.size() > 0) {
                        command = waitingQueue.peek();
                        MysqlPacket responsePacket = reader.read();
                        if (responsePacket != null) {
                            command.receiveResponsePacket(responsePacket);
                            if (!command.hasResponsePacket()) {
                                waitingQueue.poll();
                            }
                        } else {
                            MYSQL_CONNECTION_LOG.info("MysqlChannel need to be closed: `end of the input stream has been reached`.{}",
                                    buildLogForParameters(null));
                            close();
                            break;
                        }
                    }
                } catch (InterruptedException ignored) { // do nothing

                } catch (Exception e) {
                    if (state != BeanStatusEnum.CLOSED) {
                        String parametersLog = buildLogForParameters(null);
                        MYSQL_CONNECTION_LOG.error("MysqlChannel need to be closed: `{}`.{}", e.getMessage(), parametersLog);
                        LOG.error("MysqlChannel need to be closed: `" + e.getMessage() + "`." + parametersLog, e);
                        close();
                    }
                }
            }
            while ((command = waitingQueue.poll()) != null) {
                command.close();
            }
            while ((command = commandQueue.poll()) != null) {
                command.close();
            }
        }
    }

    private static class MysqlChannelKillTask extends Thread {

        private final ConnectionConfiguration connectionConfiguration;

        private final ConnectionInfo connectionInfo;

        private MysqlChannelKillTask(ConnectionConfiguration connectionConfiguration, ConnectionInfo connectionInfo) {
            this.connectionConfiguration = connectionConfiguration;
            this.connectionInfo = connectionInfo;
        }

        @Override
        public void run() {
            if (connectionInfo != null) {
                long startTime = System.currentTimeMillis();
                ConnectionConfiguration temporaryConnectionConfiguration = new ConnectionConfiguration(
                        connectionConfiguration.getHost(), connectionConfiguration.getDatabaseName(),
                        connectionConfiguration.getUsername(), connectionConfiguration.getPassword()
                );
                try (MysqlChannel channel = new MysqlChannel(temporaryConnectionConfiguration, null)) {
                    channel.init();
                    SQLCommand killCommand = new SQLCommand("KILL " + connectionInfo.getConnectionId(), channel.getConnectionInfo());
                    channel.send(killCommand, 5000);
                    ErrorPacket errorPacket = killCommand.getErrorPacket();
                    if (errorPacket != null) {
                        MYSQL_CONNECTION_LOG.error("Kill connection failed: `{}`. `cost`:`{}ms` `connectionId`:`{}`. `host`:`{}`. `databaseName`:`{}`.",
                                errorPacket.getErrorMessage(), System.currentTimeMillis() - startTime,
                                connectionInfo.getConnectionId(), temporaryConnectionConfiguration.getHost(),
                                temporaryConnectionConfiguration.getDatabaseName());
                        LOG.error("Kill connection failed: `{}`. `cost`:`{}ms` `connectionId`:`{}`. `host`:`{}`. `databaseName`:`{}`.",
                                errorPacket.getErrorMessage(), System.currentTimeMillis() - startTime,
                                connectionInfo.getConnectionId(), temporaryConnectionConfiguration.getHost(),
                                temporaryConnectionConfiguration.getDatabaseName());
                    } else {
                        MYSQL_CONNECTION_LOG.info("Kill connection success. `cost`:`{}ms`. `connectionId`:`{}`. `host`:`{}`. `databaseName`:`{}`.",
                                System.currentTimeMillis() - startTime, connectionInfo.getConnectionId(),
                                temporaryConnectionConfiguration.getHost(), temporaryConnectionConfiguration.getDatabaseName());
                    }
                } catch (Exception e) {
                    String errorMessage = "Kill connection failed: `unexpected error`. `cost`:`" + (System.currentTimeMillis() - startTime)
                            + "ms` `connectionId`:`" + connectionInfo.getConnectionId() + "`. `host`:`" + temporaryConnectionConfiguration.getHost()
                            + "`. `databaseName`:`" + temporaryConnectionConfiguration.getDatabaseName() + "`.";
                    MYSQL_CONNECTION_LOG.error(errorMessage);
                    LOG.error(errorMessage, e);
                }
            } else {
                LOG.error("Kill connection failed: `null connection info`."); // should not happen, just for bug detection
            }
        }
    }
}
