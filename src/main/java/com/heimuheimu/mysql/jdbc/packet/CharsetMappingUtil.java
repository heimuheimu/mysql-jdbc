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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 提供工具方法用于 Mysql 字符集编码和 Java 字符集编码之间的映射，Mysql 字符集定义请参考：
 * <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_character_set.html">
 * Character Set
 * </a>
 *
 * <p><strong>说明：</strong>{@code CharsetMappingUtil} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class CharsetMappingUtil {

    private static final Logger LOG = LoggerFactory.getLogger(CharsetMappingUtil.class);

    /**
     * 通用编码存储单个字符需要的最大字节数 Map，Key 为通用编码名称，Value 为对应的最大字节数
     */
    private static final Map<String, Integer> MAX_BYTES_PER_CHAR_MAP;

    /**
     * 通用编码对应的 Java 字符集编码 Map，Key 为通用编码名称，Value 为对应的 Java 字符集编码
     */
    private static final Map<String, Charset> JAVA_CHARSET_MAP;

    /**
     * Mysql 字符集编码名称 Map，Key 为编码 ID，Value 为编码名称
     */
    private static final Map<Integer, String> CHARACTER_NAME_MAP;

    static {
        MAX_BYTES_PER_CHAR_MAP = new HashMap<>();
        MAX_BYTES_PER_CHAR_MAP.put("utf8mb4", 4);
        MAX_BYTES_PER_CHAR_MAP.put("utf16", 4);
        MAX_BYTES_PER_CHAR_MAP.put("utf16le", 4);
        MAX_BYTES_PER_CHAR_MAP.put("utf32", 4);
        MAX_BYTES_PER_CHAR_MAP.put("gb18030", 4);
        MAX_BYTES_PER_CHAR_MAP.put("ujis", 3);
        MAX_BYTES_PER_CHAR_MAP.put("utf8", 3);
        MAX_BYTES_PER_CHAR_MAP.put("eucjpms", 3);
        MAX_BYTES_PER_CHAR_MAP.put("big5", 2);
        MAX_BYTES_PER_CHAR_MAP.put("sjis", 2);
        MAX_BYTES_PER_CHAR_MAP.put("euckr", 2);
        MAX_BYTES_PER_CHAR_MAP.put("gb2312", 2);
        MAX_BYTES_PER_CHAR_MAP.put("gbk", 2);
        MAX_BYTES_PER_CHAR_MAP.put("ucs2", 2);
        MAX_BYTES_PER_CHAR_MAP.put("cp932", 2);

        JAVA_CHARSET_MAP = new HashMap<>();
        JAVA_CHARSET_MAP.put("big5", buildCharsetByName("Big5"));
        JAVA_CHARSET_MAP.put("gbk", buildCharsetByName("GBK"));
        JAVA_CHARSET_MAP.put("sjis", buildCharsetByName("SHIFT_JIS"));
        JAVA_CHARSET_MAP.put("cp932", buildCharsetByName("WINDOWS-31J"));
        JAVA_CHARSET_MAP.put("gb2312", buildCharsetByName("GB2312"));
        JAVA_CHARSET_MAP.put("ujis", buildCharsetByName("EUC_JP"));
        JAVA_CHARSET_MAP.put("eucjpms", buildCharsetByName("EUC_JP_Solaris"));
        JAVA_CHARSET_MAP.put("gb18030", buildCharsetByName("GB18030"));
        JAVA_CHARSET_MAP.put("euckr", buildCharsetByName("EUC-KR"));
        JAVA_CHARSET_MAP.put("latin1", StandardCharsets.ISO_8859_1);
        JAVA_CHARSET_MAP.put("swe7", buildCharsetByName("Cp1252"));
        JAVA_CHARSET_MAP.put("hp8", buildCharsetByName("Cp1252"));
        JAVA_CHARSET_MAP.put("dec8", buildCharsetByName("Cp1252"));
        JAVA_CHARSET_MAP.put("armscii8", buildCharsetByName("Cp1252"));
        JAVA_CHARSET_MAP.put("geostd8", buildCharsetByName("Cp1252"));
        JAVA_CHARSET_MAP.put("latin2", buildCharsetByName("ISO8859_2"));
        JAVA_CHARSET_MAP.put("greek", buildCharsetByName("ISO8859_7"));
        JAVA_CHARSET_MAP.put("latin7", buildCharsetByName("ISO-8859-13"));
        JAVA_CHARSET_MAP.put("hebrew", buildCharsetByName("ISO8859_8"));
        JAVA_CHARSET_MAP.put("latin5", buildCharsetByName("ISO8859_9"));
        JAVA_CHARSET_MAP.put("cp850", buildCharsetByName("Cp850"));
        JAVA_CHARSET_MAP.put("cp852", buildCharsetByName("Cp852"));
        JAVA_CHARSET_MAP.put("keybcs2", buildCharsetByName("Cp852"));
        JAVA_CHARSET_MAP.put("cp866", buildCharsetByName("Cp866"));
        JAVA_CHARSET_MAP.put("koi8r", buildCharsetByName("KOI8_R"));
        JAVA_CHARSET_MAP.put("koi8u", buildCharsetByName("KOI8_R"));
        JAVA_CHARSET_MAP.put("tis620", buildCharsetByName("TIS620"));
        JAVA_CHARSET_MAP.put("cp1250", buildCharsetByName("Cp1250"));
        JAVA_CHARSET_MAP.put("cp1251", buildCharsetByName("Cp1251"));
        JAVA_CHARSET_MAP.put("cp1256", buildCharsetByName("Cp1256"));
        JAVA_CHARSET_MAP.put("cp1257", buildCharsetByName("Cp1257"));
        JAVA_CHARSET_MAP.put("macroman", buildCharsetByName("MacRoman"));
        JAVA_CHARSET_MAP.put("macce", buildCharsetByName("MacCentralEurope"));
        JAVA_CHARSET_MAP.put("utf8", StandardCharsets.UTF_8);
        JAVA_CHARSET_MAP.put("utf8mb4", StandardCharsets.UTF_8);
        JAVA_CHARSET_MAP.put("ucs2", buildCharsetByName("UnicodeBig"));
        JAVA_CHARSET_MAP.put("binary", StandardCharsets.ISO_8859_1);
        JAVA_CHARSET_MAP.put("utf16", StandardCharsets.UTF_16);
        JAVA_CHARSET_MAP.put("utf16le", StandardCharsets.UTF_16LE);
        JAVA_CHARSET_MAP.put("utf32", Charset.forName("UTF-32"));

        CHARACTER_NAME_MAP = new HashMap<>();
        CHARACTER_NAME_MAP.put(1, "big5_chinese_ci");
        CHARACTER_NAME_MAP.put(2, "latin2_czech_cs");
        CHARACTER_NAME_MAP.put(3, "dec8_swedish_ci");
        CHARACTER_NAME_MAP.put(4, "cp850_general_ci");
        CHARACTER_NAME_MAP.put(5, "latin1_german1_ci");
        CHARACTER_NAME_MAP.put(6, "hp8_english_ci");
        CHARACTER_NAME_MAP.put(7, "koi8r_general_ci");
        CHARACTER_NAME_MAP.put(8, "latin1_swedish_ci");
        CHARACTER_NAME_MAP.put(9, "latin2_general_ci");
        CHARACTER_NAME_MAP.put(10, "swe7_swedish_ci");
        CHARACTER_NAME_MAP.put(11, "ascii_general_ci");
        CHARACTER_NAME_MAP.put(12, "ujis_japanese_ci");
        CHARACTER_NAME_MAP.put(13, "sjis_japanese_ci");
        CHARACTER_NAME_MAP.put(14, "cp1251_bulgarian_ci");
        CHARACTER_NAME_MAP.put(15, "latin1_danish_ci");
        CHARACTER_NAME_MAP.put(16, "hebrew_general_ci");
        CHARACTER_NAME_MAP.put(18, "tis620_thai_ci");
        CHARACTER_NAME_MAP.put(19, "euckr_korean_ci");
        CHARACTER_NAME_MAP.put(20, "latin7_estonian_cs");
        CHARACTER_NAME_MAP.put(21, "latin2_hungarian_ci");
        CHARACTER_NAME_MAP.put(22, "koi8u_general_ci");
        CHARACTER_NAME_MAP.put(23, "cp1251_ukrainian_ci");
        CHARACTER_NAME_MAP.put(24, "gb2312_chinese_ci");
        CHARACTER_NAME_MAP.put(25, "greek_general_ci");
        CHARACTER_NAME_MAP.put(26, "cp1250_general_ci");
        CHARACTER_NAME_MAP.put(27, "latin2_croatian_ci");
        CHARACTER_NAME_MAP.put(28, "gbk_chinese_ci");
        CHARACTER_NAME_MAP.put(29, "cp1257_lithuanian_ci");
        CHARACTER_NAME_MAP.put(30, "latin5_turkish_ci");
        CHARACTER_NAME_MAP.put(31, "latin1_german2_ci");
        CHARACTER_NAME_MAP.put(32, "armscii8_general_ci");
        CHARACTER_NAME_MAP.put(33, "utf8_general_ci");
        CHARACTER_NAME_MAP.put(34, "cp1250_czech_cs");
        CHARACTER_NAME_MAP.put(35, "ucs2_general_ci");
        CHARACTER_NAME_MAP.put(36, "cp866_general_ci");
        CHARACTER_NAME_MAP.put(37, "keybcs2_general_ci");
        CHARACTER_NAME_MAP.put(38, "macce_general_ci");
        CHARACTER_NAME_MAP.put(39, "macroman_general_ci");
        CHARACTER_NAME_MAP.put(40, "cp852_general_ci");
        CHARACTER_NAME_MAP.put(41, "latin7_general_ci");
        CHARACTER_NAME_MAP.put(42, "latin7_general_cs");
        CHARACTER_NAME_MAP.put(43, "macce_bin");
        CHARACTER_NAME_MAP.put(44, "cp1250_croatian_ci");
        CHARACTER_NAME_MAP.put(45, "utf8mb4_general_ci");
        CHARACTER_NAME_MAP.put(46, "utf8mb4_bin");
        CHARACTER_NAME_MAP.put(47, "latin1_bin");
        CHARACTER_NAME_MAP.put(48, "latin1_general_ci");
        CHARACTER_NAME_MAP.put(49, "latin1_general_cs");
        CHARACTER_NAME_MAP.put(50, "cp1251_bin");
        CHARACTER_NAME_MAP.put(51, "cp1251_general_ci");
        CHARACTER_NAME_MAP.put(52, "cp1251_general_cs");
        CHARACTER_NAME_MAP.put(53, "macroman_bin");
        CHARACTER_NAME_MAP.put(54, "utf16_general_ci");
        CHARACTER_NAME_MAP.put(55, "utf16_bin");
        CHARACTER_NAME_MAP.put(56, "utf16le_general_ci");
        CHARACTER_NAME_MAP.put(57, "cp1256_general_ci");
        CHARACTER_NAME_MAP.put(58, "cp1257_bin");
        CHARACTER_NAME_MAP.put(59, "cp1257_general_ci");
        CHARACTER_NAME_MAP.put(60, "utf32_general_ci");
        CHARACTER_NAME_MAP.put(61, "utf32_bin");
        CHARACTER_NAME_MAP.put(62, "utf16le_bin");
        CHARACTER_NAME_MAP.put(63, "binary");
        CHARACTER_NAME_MAP.put(64, "armscii8_bin");
        CHARACTER_NAME_MAP.put(65, "ascii_bin");
        CHARACTER_NAME_MAP.put(66, "cp1250_bin");
        CHARACTER_NAME_MAP.put(67, "cp1256_bin");
        CHARACTER_NAME_MAP.put(68, "cp866_bin");
        CHARACTER_NAME_MAP.put(69, "dec8_bin");
        CHARACTER_NAME_MAP.put(70, "greek_bin");
        CHARACTER_NAME_MAP.put(71, "hebrew_bin");
        CHARACTER_NAME_MAP.put(72, "hp8_bin");
        CHARACTER_NAME_MAP.put(73, "keybcs2_bin");
        CHARACTER_NAME_MAP.put(74, "koi8r_bin");
        CHARACTER_NAME_MAP.put(75, "koi8u_bin");
        CHARACTER_NAME_MAP.put(77, "latin2_bin");
        CHARACTER_NAME_MAP.put(78, "latin5_bin");
        CHARACTER_NAME_MAP.put(79, "latin7_bin");
        CHARACTER_NAME_MAP.put(80, "cp850_bin");
        CHARACTER_NAME_MAP.put(81, "cp852_bin");
        CHARACTER_NAME_MAP.put(82, "swe7_bin");
        CHARACTER_NAME_MAP.put(83, "utf8_bin");
        CHARACTER_NAME_MAP.put(84, "big5_bin");
        CHARACTER_NAME_MAP.put(85, "euckr_bin");
        CHARACTER_NAME_MAP.put(86, "gb2312_bin");
        CHARACTER_NAME_MAP.put(87, "gbk_bin");
        CHARACTER_NAME_MAP.put(88, "sjis_bin");
        CHARACTER_NAME_MAP.put(89, "tis620_bin");
        CHARACTER_NAME_MAP.put(90, "ucs2_bin");
        CHARACTER_NAME_MAP.put(91, "ujis_bin");
        CHARACTER_NAME_MAP.put(92, "geostd8_general_ci");
        CHARACTER_NAME_MAP.put(93, "geostd8_bin");
        CHARACTER_NAME_MAP.put(94, "latin1_spanish_ci");
        CHARACTER_NAME_MAP.put(95, "cp932_japanese_ci");
        CHARACTER_NAME_MAP.put(96, "cp932_bin");
        CHARACTER_NAME_MAP.put(97, "eucjpms_japanese_ci");
        CHARACTER_NAME_MAP.put(98, "eucjpms_bin");
        CHARACTER_NAME_MAP.put(99, "cp1250_polish_ci");
        CHARACTER_NAME_MAP.put(101, "utf16_unicode_ci");
        CHARACTER_NAME_MAP.put(102, "utf16_icelandic_ci");
        CHARACTER_NAME_MAP.put(103, "utf16_latvian_ci");
        CHARACTER_NAME_MAP.put(104, "utf16_romanian_ci");
        CHARACTER_NAME_MAP.put(105, "utf16_slovenian_ci");
        CHARACTER_NAME_MAP.put(106, "utf16_polish_ci");
        CHARACTER_NAME_MAP.put(107, "utf16_estonian_ci");
        CHARACTER_NAME_MAP.put(108, "utf16_spanish_ci");
        CHARACTER_NAME_MAP.put(109, "utf16_swedish_ci");
        CHARACTER_NAME_MAP.put(110, "utf16_turkish_ci");
        CHARACTER_NAME_MAP.put(111, "utf16_czech_ci");
        CHARACTER_NAME_MAP.put(112, "utf16_danish_ci");
        CHARACTER_NAME_MAP.put(113, "utf16_lithuanian_ci");
        CHARACTER_NAME_MAP.put(114, "utf16_slovak_ci");
        CHARACTER_NAME_MAP.put(115, "utf16_spanish2_ci");
        CHARACTER_NAME_MAP.put(116, "utf16_roman_ci");
        CHARACTER_NAME_MAP.put(117, "utf16_persian_ci");
        CHARACTER_NAME_MAP.put(118, "utf16_esperanto_ci");
        CHARACTER_NAME_MAP.put(119, "utf16_hungarian_ci");
        CHARACTER_NAME_MAP.put(120, "utf16_sinhala_ci");
        CHARACTER_NAME_MAP.put(121, "utf16_german2_ci");
        CHARACTER_NAME_MAP.put(122, "utf16_croatian_ci");
        CHARACTER_NAME_MAP.put(123, "utf16_unicode_520_ci");
        CHARACTER_NAME_MAP.put(124, "utf16_vietnamese_ci");
        CHARACTER_NAME_MAP.put(128, "ucs2_unicode_ci");
        CHARACTER_NAME_MAP.put(129, "ucs2_icelandic_ci");
        CHARACTER_NAME_MAP.put(130, "ucs2_latvian_ci");
        CHARACTER_NAME_MAP.put(131, "ucs2_romanian_ci");
        CHARACTER_NAME_MAP.put(132, "ucs2_slovenian_ci");
        CHARACTER_NAME_MAP.put(133, "ucs2_polish_ci");
        CHARACTER_NAME_MAP.put(134, "ucs2_estonian_ci");
        CHARACTER_NAME_MAP.put(135, "ucs2_spanish_ci");
        CHARACTER_NAME_MAP.put(136, "ucs2_swedish_ci");
        CHARACTER_NAME_MAP.put(137, "ucs2_turkish_ci");
        CHARACTER_NAME_MAP.put(138, "ucs2_czech_ci");
        CHARACTER_NAME_MAP.put(139, "ucs2_danish_ci");
        CHARACTER_NAME_MAP.put(140, "ucs2_lithuanian_ci");
        CHARACTER_NAME_MAP.put(141, "ucs2_slovak_ci");
        CHARACTER_NAME_MAP.put(142, "ucs2_spanish2_ci");
        CHARACTER_NAME_MAP.put(143, "ucs2_roman_ci");
        CHARACTER_NAME_MAP.put(144, "ucs2_persian_ci");
        CHARACTER_NAME_MAP.put(145, "ucs2_esperanto_ci");
        CHARACTER_NAME_MAP.put(146, "ucs2_hungarian_ci");
        CHARACTER_NAME_MAP.put(147, "ucs2_sinhala_ci");
        CHARACTER_NAME_MAP.put(148, "ucs2_german2_ci");
        CHARACTER_NAME_MAP.put(149, "ucs2_croatian_ci");
        CHARACTER_NAME_MAP.put(150, "ucs2_unicode_520_ci");
        CHARACTER_NAME_MAP.put(151, "ucs2_vietnamese_ci");
        CHARACTER_NAME_MAP.put(159, "ucs2_general_mysql500_ci");
        CHARACTER_NAME_MAP.put(160, "utf32_unicode_ci");
        CHARACTER_NAME_MAP.put(161, "utf32_icelandic_ci");
        CHARACTER_NAME_MAP.put(162, "utf32_latvian_ci");
        CHARACTER_NAME_MAP.put(163, "utf32_romanian_ci");
        CHARACTER_NAME_MAP.put(164, "utf32_slovenian_ci");
        CHARACTER_NAME_MAP.put(165, "utf32_polish_ci");
        CHARACTER_NAME_MAP.put(166, "utf32_estonian_ci");
        CHARACTER_NAME_MAP.put(167, "utf32_spanish_ci");
        CHARACTER_NAME_MAP.put(168, "utf32_swedish_ci");
        CHARACTER_NAME_MAP.put(169, "utf32_turkish_ci");
        CHARACTER_NAME_MAP.put(170, "utf32_czech_ci");
        CHARACTER_NAME_MAP.put(171, "utf32_danish_ci");
        CHARACTER_NAME_MAP.put(172, "utf32_lithuanian_ci");
        CHARACTER_NAME_MAP.put(173, "utf32_slovak_ci");
        CHARACTER_NAME_MAP.put(174, "utf32_spanish2_ci");
        CHARACTER_NAME_MAP.put(175, "utf32_roman_ci");
        CHARACTER_NAME_MAP.put(176, "utf32_persian_ci");
        CHARACTER_NAME_MAP.put(177, "utf32_esperanto_ci");
        CHARACTER_NAME_MAP.put(178, "utf32_hungarian_ci");
        CHARACTER_NAME_MAP.put(179, "utf32_sinhala_ci");
        CHARACTER_NAME_MAP.put(180, "utf32_german2_ci");
        CHARACTER_NAME_MAP.put(181, "utf32_croatian_ci");
        CHARACTER_NAME_MAP.put(182, "utf32_unicode_520_ci");
        CHARACTER_NAME_MAP.put(183, "utf32_vietnamese_ci");
        CHARACTER_NAME_MAP.put(192, "utf8_unicode_ci");
        CHARACTER_NAME_MAP.put(193, "utf8_icelandic_ci");
        CHARACTER_NAME_MAP.put(194, "utf8_latvian_ci");
        CHARACTER_NAME_MAP.put(195, "utf8_romanian_ci");
        CHARACTER_NAME_MAP.put(196, "utf8_slovenian_ci");
        CHARACTER_NAME_MAP.put(197, "utf8_polish_ci");
        CHARACTER_NAME_MAP.put(198, "utf8_estonian_ci");
        CHARACTER_NAME_MAP.put(199, "utf8_spanish_ci");
        CHARACTER_NAME_MAP.put(200, "utf8_swedish_ci");
        CHARACTER_NAME_MAP.put(201, "utf8_turkish_ci");
        CHARACTER_NAME_MAP.put(202, "utf8_czech_ci");
        CHARACTER_NAME_MAP.put(203, "utf8_danish_ci");
        CHARACTER_NAME_MAP.put(204, "utf8_lithuanian_ci");
        CHARACTER_NAME_MAP.put(205, "utf8_slovak_ci");
        CHARACTER_NAME_MAP.put(206, "utf8_spanish2_ci");
        CHARACTER_NAME_MAP.put(207, "utf8_roman_ci");
        CHARACTER_NAME_MAP.put(208, "utf8_persian_ci");
        CHARACTER_NAME_MAP.put(209, "utf8_esperanto_ci");
        CHARACTER_NAME_MAP.put(210, "utf8_hungarian_ci");
        CHARACTER_NAME_MAP.put(211, "utf8_sinhala_ci");
        CHARACTER_NAME_MAP.put(212, "utf8_german2_ci");
        CHARACTER_NAME_MAP.put(213, "utf8_croatian_ci");
        CHARACTER_NAME_MAP.put(214, "utf8_unicode_520_ci");
        CHARACTER_NAME_MAP.put(215, "utf8_vietnamese_ci");
        CHARACTER_NAME_MAP.put(223, "utf8_general_mysql500_ci");
        CHARACTER_NAME_MAP.put(224, "utf8mb4_unicode_ci");
        CHARACTER_NAME_MAP.put(225, "utf8mb4_icelandic_ci");
        CHARACTER_NAME_MAP.put(226, "utf8mb4_latvian_ci");
        CHARACTER_NAME_MAP.put(227, "utf8mb4_romanian_ci");
        CHARACTER_NAME_MAP.put(228, "utf8mb4_slovenian_ci");
        CHARACTER_NAME_MAP.put(229, "utf8mb4_polish_ci");
        CHARACTER_NAME_MAP.put(230, "utf8mb4_estonian_ci");
        CHARACTER_NAME_MAP.put(231, "utf8mb4_spanish_ci");
        CHARACTER_NAME_MAP.put(232, "utf8mb4_swedish_ci");
        CHARACTER_NAME_MAP.put(233, "utf8mb4_turkish_ci");
        CHARACTER_NAME_MAP.put(234, "utf8mb4_czech_ci");
        CHARACTER_NAME_MAP.put(235, "utf8mb4_danish_ci");
        CHARACTER_NAME_MAP.put(236, "utf8mb4_lithuanian_ci");
        CHARACTER_NAME_MAP.put(237, "utf8mb4_slovak_ci");
        CHARACTER_NAME_MAP.put(238, "utf8mb4_spanish2_ci");
        CHARACTER_NAME_MAP.put(239, "utf8mb4_roman_ci");
        CHARACTER_NAME_MAP.put(240, "utf8mb4_persian_ci");
        CHARACTER_NAME_MAP.put(241, "utf8mb4_esperanto_ci");
        CHARACTER_NAME_MAP.put(242, "utf8mb4_hungarian_ci");
        CHARACTER_NAME_MAP.put(243, "utf8mb4_sinhala_ci");
        CHARACTER_NAME_MAP.put(244, "utf8mb4_german2_ci");
        CHARACTER_NAME_MAP.put(245, "utf8mb4_croatian_ci");
        CHARACTER_NAME_MAP.put(246, "utf8mb4_unicode_520_ci");
        CHARACTER_NAME_MAP.put(247, "utf8mb4_vietnamese_ci");
        CHARACTER_NAME_MAP.put(248, "gb18030_chinese_ci");
        CHARACTER_NAME_MAP.put(249, "gb18030_bin");
        CHARACTER_NAME_MAP.put(250, "gb18030_unicode_520_ci");
    }

    /**
     * 判断该 Mysql 字符集编码是否区分大小写。
     *
     * @param mysqlCharacterId Mysql 字符集编码 ID
     * @return 是否区分大小写
     */
    public static boolean isCaseSensitive(int mysqlCharacterId) {
        String characterName = CHARACTER_NAME_MAP.get(mysqlCharacterId);
        return (characterName != null) && !characterName.endsWith("_ci");
    }

    /**
     * 获得该 Mysql 字符集编码存储单个字符需要的最大字节数。
     *
     * @param mysqlCharacterId Mysql 字符集编码 ID
     * @return 存储单个字符需要的最大字节数
     */
    public static int getMaxBytesPerChar(int mysqlCharacterId) {
        String charsetName = getCharsetName(mysqlCharacterId);
        Integer maxBytesPerChar = MAX_BYTES_PER_CHAR_MAP.get(charsetName);
        if (maxBytesPerChar != null) {
            return maxBytesPerChar;
        } else {
            return 1;
        }
    }

    /**
     * 根据 Mysql 字符集编码 ID 获得对应的 Java 字符集编码，该方法不会返回 {@code null}。
     *
     * @param mysqlCharacterId Mysql 字符集编码 ID
     * @return Java 字符集编码，不会为 {@code null}
     */
    public static Charset getJavaCharset(int mysqlCharacterId) {
        if (mysqlCharacterId == 45 || mysqlCharacterId == 46) { // just for performance
            return StandardCharsets.UTF_8;
        }
        String charsetName = getCharsetName(mysqlCharacterId);
        Charset javaCharset = JAVA_CHARSET_MAP.get(charsetName);
        return javaCharset != null ? javaCharset : StandardCharsets.UTF_8;
    }

    /**
     * 根据 Mysql 字符集编码 ID 获得对应的通用编码名称（不包含 Mysql 排序信息）。
     *
     * @param mysqlCharacterId Mysql 字符集编码 ID
     * @return 通用编码名称（不包含 Mysql 排序信息），不会为 {@code null}
     */
    private static String getCharsetName(int mysqlCharacterId) {
        String characterName = CHARACTER_NAME_MAP.get(mysqlCharacterId);
        if (characterName != null) {
            return characterName.split("_")[0];
        } else {
            return "";
        }
    }

    private static Charset buildCharsetByName(String charsetName) {
        try {
            return Charset.forName(charsetName);
        } catch (Exception e) {
            LOG.warn("Build Charset `{}` failed. Replaced by `UTF_8`.", charsetName);
            return StandardCharsets.UTF_8;
        }
    }
}
