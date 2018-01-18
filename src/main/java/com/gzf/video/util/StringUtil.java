package com.gzf.video.util;

import com.mongodb.internal.HexUtils;
import org.bouncycastle.util.encoders.Base64;

import java.io.File;

public class StringUtil {

    public static final char SEP = File.separatorChar;
    public static final String EMPTY_STRING = "";


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


    public static byte[] base64Encode(final byte[] data) {
        return Base64.encode(data);
    }

    public static byte[] base64Decode(final byte[] data) {
        return Base64.decode(data);
    }

    public static byte[] base64Decode(final String data) {
        return Base64.decode(data);
    }

    public static String hex(final byte[] data) {
        return HexUtils.toHex(data);
    }

    public static String hexMd5(final byte[] data) {
        return HexUtils.hexMD5(data);
    }
}
