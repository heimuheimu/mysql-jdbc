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
import com.heimuheimu.mysql.jdbc.constant.BeanStatusEnum;
import com.heimuheimu.mysql.jdbc.facility.UnusableServiceNotifier;
import com.heimuheimu.mysql.jdbc.monitor.SocketMonitorFactory;
import com.heimuheimu.mysql.jdbc.net.BuildSocketException;
import com.heimuheimu.mysql.jdbc.net.SocketBuilder;
import com.heimuheimu.mysql.jdbc.net.SocketConfiguration;
import com.heimuheimu.mysql.jdbc.packet.MysqlPacket;
import com.heimuheimu.mysql.jdbc.packet.MysqlPacketReader;
import com.heimuheimu.naivemonitor.facility.MonitoredSocketOutputStream;
import com.heimuheimu.naivemonitor.monitor.SocketMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
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
     * @throws NullPointerException 如果 {@code configuration} 为 {@code null}，将会抛出此异常
     * @throws IllegalArgumentException 如果 Mysql 地址不符合规则，将会抛出此异常
     * @throws BuildSocketException 如果创建 {@link Socket} 过程中发生错误，将会抛出此异常
     */
    public MysqlChannel(ConnectionConfiguration configuration, UnusableServiceNotifier<MysqlChannel> unusableServiceNotifier)
            throws NullPointerException, IllegalArgumentException, BuildSocketException {
        this.connectionConfiguration = configuration;
        this.socket = SocketBuilder.create(configuration.getHost(), configuration.getSocketConfiguration());
        this.socketMonitor = SocketMonitorFactory.get(configuration.getHost());
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

    public List<MysqlPacket> send(Command command, long timeout) throws NullPointerException, SQLException {
        if (command == null) {
            String errorMessage = "Execute mysql command failed: `command should not be null`. Host: `" +
                    connectionConfiguration.getHost() + "`. Connection config: `" + connectionConfiguration + "`.";
            LOG.error(errorMessage);
            throw new NullPointerException(errorMessage);
        }
        if (state == BeanStatusEnum.NORMAL) {
            commandQueue.add(command);
        } else {
            String errorMessage = "Execute mysql command failed: `MysqlChannel is not initialized or has been closed`. State: `"
                    + state + "`. Host: `" + connectionConfiguration.getHost() + "`. Command: `" + command + "`. Connection config: `"
                    + connectionConfiguration + "`.";
            LOG.error(errorMessage);
            throw new SQLException(errorMessage);
        }
        return command.getResponsePacketList(timeout);
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
                    //启动 IO 线程
                    ioTask = new MysqlIOTask(config.getReceiveBufferSize());
                    ioTask.setName("mysql-io-" + socketAddress);
                    ioTask.start();
                    MYSQL_CONNECTION_LOG.info("MysqlChannel has benn initialized. Cost: `{}ms`. Host: `{}`. Local port: `{}`. Socket config: `{}`. Connection info: `{}`.",
                            (System.currentTimeMillis() - startTime), connectionConfiguration.getHost(), socket.getLocalPort(), config, connectionInfo);
                } else {
                    MYSQL_CONNECTION_LOG.error("Initialize MysqlChannel failed: `socket is not connected or has been closed`. Host: `{}`. Connection config: `{}`.",
                            connectionConfiguration.getHost(), connectionConfiguration);
                    close();
                }
            } catch (Exception e) {
                MYSQL_CONNECTION_LOG.error("Initialize MysqlChannel failed: `{}`. Host: `{}`. Connection config: `{}`.",
                        e.getMessage(), connectionConfiguration.getHost(), connectionConfiguration);
                LOG.error("Initialize MysqlChannel failed: `" + e.getMessage() + "`. Host: `" + connectionConfiguration.getHost()
                                + "`. Connection config: `" + connectionConfiguration + "`.", e);
                close();
            }
        }
    }

    @Override
    public synchronized void close() {
        if (state != BeanStatusEnum.CLOSED) {
            long startTime = System.currentTimeMillis();
            state = BeanStatusEnum.CLOSED;
            try {
                //关闭 Socket 连接
                socket.close();
                //停止 IO 线程
                ioTask.stopSignal = true;
                ioTask.interrupt();
                MYSQL_CONNECTION_LOG.info("MysqlChannel has been closed. Cost: `{}ms`. Host: `{}`. Connection config: `{}`.",
                        (System.currentTimeMillis() - startTime), connectionConfiguration.getHost(), connectionConfiguration);
            } catch (Exception e) {
                MYSQL_CONNECTION_LOG.error("Close MysqlChannel failed: `{}`. Host: `{}`. Connection config: `{}`.",
                        e.getMessage(), connectionConfiguration.getHost(), connectionConfiguration);
                LOG.error("Close MysqlChannel failed: `" + e.getMessage() + "`. Host: `" + connectionConfiguration.getHost()
                        + "`. Connection config: `" + connectionConfiguration + "`.", e);
            } finally {
                if (unusableServiceNotifier != null) {
                    unusableServiceNotifier.onClosed(this);
                }
                connectionInfo = null;
            }
        }
    }

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
                                        LOG.debug("Execute `PingCommand` success. Cost: `{}ms`. Host: `{}`. Connection info: `{}`.",
                                                System.currentTimeMillis() - pingCommandStartTime,
                                                connectionConfiguration.getHost(), connectionInfo);
                                    } else { //should not happen
                                        MYSQL_CONNECTION_LOG.info("MysqlChannel need to be closed: `execute PingCommand failed`. Host: `{}`. Connection info: `{}`.",
                                                connectionConfiguration.getHost(), connectionInfo);
                                        LOG.error("MysqlChannel need to be closed: `execute PingCommand failed`. Host: `{}`. Connection info: `{}`.",
                                                connectionConfiguration.getHost(), connectionInfo);
                                        MysqlChannel.this.close();
                                    }
                                } catch (Exception e) {
                                    MYSQL_CONNECTION_LOG.info("MysqlChannel need to be closed: `execute PingCommand failed`. Host: `{}`. Connection info: `{}`.",
                                            connectionConfiguration.getHost(), connectionInfo);
                                    LOG.error("MysqlChannel need to be closed: `execute PingCommand failed`. Host: `"
                                                    + connectionConfiguration.getHost() + "`. Connection info: `"
                                                    + connectionInfo + "`.", e);
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
                            MYSQL_CONNECTION_LOG.info("MysqlChannel need to be closed: `end of the input stream has been reached`. Host: `{}`. Connection info: `{}`.",
                                    connectionConfiguration.getHost(), connectionInfo);
                            close();
                        }
                    }
                } catch (InterruptedException ignored) { // do nothing

                } catch (Exception e) {
                    MYSQL_CONNECTION_LOG.info("MysqlChannel need to be closed: `{}`. Host: `{}`. Connection info: `{}`.",
                            e.getMessage(), connectionConfiguration.getHost(), connectionInfo);
                    LOG.error("MysqlChannel need to be closed: `" + e.getMessage() + "`. Host: `{}`. Connection info: `{}`.",
                            connectionConfiguration.getHost(), connectionInfo);
                    close();
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
}
