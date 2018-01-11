package com.gzf.video.util;

import com.mongodb.internal.HexUtils;

import java.io.File;

public class StringUtil {

    public static final char SEP = File.separatorChar;
    public static final String EMPTY_STRING = "";


    public static String concatWith(final String[] xs, final String insert) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < xs.length; i++) {
            if (i != 0) {
                s.append(insert);
            }
            s.append(xs[i]);
        }
        return s.toString();
    }


    public static boolean isNullOrEmpty(final String s) {
        return s == null || s.isEmpty();
    }

    public static boolean anyNullOrEmpty(final String... xs) {
        for (String s : xs) {
            if (isNullOrEmpty(s)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isNotNullOrEmpty(final String s) {
        return s != null && !s.isEmpty();
    }

    public static String notNullOrEmpty(final String s) {
        if (s == null) {
            throw new RuntimeException("null");
        } else if (s.isEmpty()) {
            throw new RuntimeException("empty string");
        }

        return s;
    }


    public static String hex(final byte[] data) {
        return HexUtils.toHex(data);
    }

    public static String hexMd5(final byte[] data) {
        return HexUtils.hexMD5(data);
    }
}
