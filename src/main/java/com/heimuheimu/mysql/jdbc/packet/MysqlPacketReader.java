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

package com.heimuheimu.mysql.jdbc.packet;

import com.heimuheimu.mysql.jdbc.facility.parameter.ConstructorParameterChecker;
import com.heimuheimu.mysql.jdbc.facility.parameter.Parameters;
import com.heimuheimu.mysql.jdbc.util.BytesUtil;
import com.heimuheimu.naivemonitor.facility.MonitoredSocketInputStream;
import com.heimuheimu.naivemonitor.monitor.SocketMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * {@link MysqlPacket} 读取器，从指定的输入流中读取 RPC 数据。
 *
 * <p><strong>说明：</strong>{@code MysqlPacketReader} 类是非线程安全的，不允许多个线程使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class MysqlPacketReader {

    private final static Logger LOGGER = LoggerFactory.getLogger(MysqlPacketReader.class);

    /**
     * 用于读取 {@link MysqlPacket} 的输入流
     */
    private final BufferedInputStream inputStream;

    /**
     * 构造一个 {@link MysqlPacket} 读取器，从指定的输入流中读取 RPC 数据。
     *
     * @param inputStream 用于读取 {@link MysqlPacket} 的输入流，不允许为 {@code null}
     * @param bufferSize 批量读取的字节大小，不允许小于等于 0
     * @param socketMonitor Socket 读、写信息监控器，不允许为 {@code null}
     * @throws IllegalArgumentException 如果 {@code inputStream} 或者 {@code socketMonitor} 为 {@code null}，将会抛出此异常
     * @throws IllegalArgumentException 如果 {@code bufferSize} 小于等于 0，将会抛出此异常
     */
    public MysqlPacketReader(InputStream inputStream, int bufferSize, SocketMonitor socketMonitor) throws IllegalArgumentException {
        ConstructorParameterChecker checker = new ConstructorParameterChecker("MysqlPacketReader", LOGGER);
        checker.addParameter("inputStream", inputStream);
        checker.addParameter("bufferSize", bufferSize);
        checker.addParameter("socketMonitor", socketMonitor);

        checker.check("inputStream", "isNull", Parameters::isNull);
        checker.check("bufferSize", "isEqualOrLessThanZero", Parameters::isEqualOrLessThanZero);
        checker.check("socketMonitor", "isNull", Parameters::isNull);

        this.inputStream = new BufferedInputStream(new MonitoredSocketInputStream(inputStream, socketMonitor), bufferSize);
    }

    /**
     * 从输入流中读取 Mysql 数据包，如果输入流被关闭，则返回 {@code null}。
     *
     * @return Mysql 数据包，如果输入流被关闭，则返回 {@code null}
     * @throws IOException 如果读取 Mysql 数据包时发生 IO 错误，将抛出此异常
     */
    public MysqlPacket read() throws IOException {
        // 读取头部字节
        int headerPos = 0;
        byte[] header = new byte[4];
        while (headerPos < 4) {
            int readBytes = inputStream.read(header, headerPos, 4 - headerPos);
            if (readBytes >= 0) {
                headerPos += readBytes;
            } else { // 流已经关闭，返回 null
                return null;
            }
        }

        int payloadLength = (int) BytesUtil.decodeUnsignedInteger(header, 0, 3);
        int sequenceId = (int) BytesUtil.decodeUnsignedInteger(header, 3, 1);

        // 读取内容信息字节
        int payloadPos = 0;
        byte[] payload = new byte[payloadLength];
        while (payloadPos < payloadLength) {
            int readBytes = inputStream.read(payload, payloadPos, payloadLength - payloadPos);
            if (readBytes >= 0) {
                payloadPos += readBytes;
            } else { // 流已经关闭，返回 null
                return null;
            }
        }

        return new MysqlPacket(sequenceId, payload);
    }
}
