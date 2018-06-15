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
import com.heimuheimu.mysql.jdbc.constant.BeanStatusEnum;
import com.heimuheimu.mysql.jdbc.facility.UnusableServiceNotifier;
import com.heimuheimu.mysql.jdbc.monitor.SocketMonitorFactory;
import com.heimuheimu.mysql.jdbc.net.BuildSocketException;
import com.heimuheimu.mysql.jdbc.net.SocketBuilder;
import com.heimuheimu.mysql.jdbc.net.SocketConfiguration;
import com.heimuheimu.mysql.jdbc.packet.MysqlPacketReader;
import com.heimuheimu.naivemonitor.facility.MonitoredSocketOutputStream;
import com.heimuheimu.naivemonitor.monitor.SocketMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;

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

        private MysqlIOTask(Integer receiveBufferSize) throws IOException {
            this.outputStream = new MonitoredSocketOutputStream(socket.getOutputStream(), socketMonitor);

            receiveBufferSize = receiveBufferSize != null ? receiveBufferSize : 64 * 1024;
            this.reader = new MysqlPacketReader(socket.getInputStream(), receiveBufferSize, socketMonitor);

            HandshakeProcessor handshakeProcessor = new HandshakeProcessor(connectionConfiguration, outputStream, reader);
            connectionInfo = handshakeProcessor.doHandshake();
        }
    }
}
