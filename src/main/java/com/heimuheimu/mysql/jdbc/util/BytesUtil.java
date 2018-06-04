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

import java.util.Arrays;

/**
 * {@code BytesUtil} 提供字节转换的工具方法，例如将字节转换为整数。
 *
 * <p><strong>说明：</strong>{@code BytesUtil} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class BytesUtil {

    /**
     * 将字节数组中指定长度的字节转换为无符号整数后返回，字节数组的顺序应为 {@link java.nio.ByteOrder#LITTLE_ENDIAN}。
     *
     * @param src 字节数组，不允许为 {@code null}
     * @param offset 起始索引，允许的范围为：[0, {@code src.length})
     * @param length 读取的字节长度，允许的范围为: [1, 8]
     * @return 无符号整数
     * @throws NullPointerException 如果 {@code src} 为 {@code null}，将会抛出此异常
     * @throws ArrayIndexOutOfBoundsException 如果访问数组越界，将会抛出此异常
     * @throws IllegalArgumentException 如果 {@code offset} 或者 {@code length} 的值没有在允许的范围内，将会抛出此异常
     * @throws IllegalArgumentException 如果读取的字节长度为 8，且 {@code src[offset + 7]} 的值小于 0（long 类型溢出），将会抛出此异常
     */
    public static long toUnsignedInteger(byte[] src, int offset, int length) throws NullPointerException,
            ArrayIndexOutOfBoundsException, IllegalArgumentException {
        if (src == null) {
            throw new NullPointerException("Convert bytes to unsigned integer failed: `src could not be null`. `src`:`null`. `offset`:`"
                    + offset + "`. `length`:`" + length + "`.");
        }
        if (offset < 0 || offset >= src.length) {
            throw new IllegalArgumentException("Convert bytes to unsigned integer failed: `invalid offset`. `src`:`"
                    + Arrays.toString(src) + "`. `offset`:`" + offset + "`. `length`:`" + length + "`.");
        }
        if (length < 1 || length > 8) {
            throw new IllegalArgumentException("Convert bytes to unsigned integer failed: `invalid length`. `src`:`"
                    + Arrays.toString(src) + "`. `offset`:`" + offset + "`. `length`:`" + length + "`.");
        }
        if ((offset + length) > src.length) {
            throw new ArrayIndexOutOfBoundsException("Convert bytes to unsigned integer failed: `index is greater than or equal to the size of the array`. `src`:`"
                    + Arrays.toString(src) + "`. `offset`:`" + offset + "`. `length`:`" + length + "`.");
        }
        if (length == 8 && src[offset + 7] < 0) {
            throw new IllegalArgumentException("Convert bytes to unsigned integer failed: `the most significant byte is less than zero`. `src`:`"
                    + Arrays.toString(src) + "`. `offset`:`" + offset + "`. `length`:`" + length + "`.");
        }
        long value = 0;
        for (int i = 0; i < length; i++) {
            value |= ((long) src[offset + i] & 0xff) << (i * 8);
        }
        return value;
    }
}
