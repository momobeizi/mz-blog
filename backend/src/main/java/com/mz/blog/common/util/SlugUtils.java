package com.mz.blog.common.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;

import java.text.Normalizer;
import java.util.Locale;

public final class SlugUtils {

    private static final HanyuPinyinOutputFormat FORMAT = new HanyuPinyinOutputFormat();

    static {
        FORMAT.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        FORMAT.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        FORMAT.setVCharType(HanyuPinyinVCharType.WITH_V);
    }

    private SlugUtils() {
    }

    public static String slugify(String text) {
        if (text == null || text.isBlank()) {
            return "untitled";
        }
        StringBuilder value = new StringBuilder();
        for (char ch : text.trim().toCharArray()) {
            if (Character.isLetterOrDigit(ch) && ch < 128) {
                value.append(Character.toLowerCase(ch));
            } else if (Character.UnicodeScript.of(ch) == Character.UnicodeScript.HAN) {
                try {
                    String[] pinyin = PinyinHelper.toHanyuPinyinStringArray(ch, FORMAT);
                    if (pinyin != null && pinyin.length > 0) {
                        value.append(pinyin[0]);
                    }
                } catch (Exception ignored) {
                    value.append('-');
                }
            } else {
                value.append('-');
            }
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKD)
                .replaceAll("[^a-z0-9-]", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");
        if (normalized.length() > 80) {
            normalized = normalized.substring(0, 80).replaceAll("-$", "");
        }
        return normalized.isBlank() ? "untitled" : normalized.toLowerCase(Locale.ROOT);
    }
}
