package com.gzf.video.util;

import com.mongodb.internal.HexUtils;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;

import java.io.File;
import java.util.Collections;
import java.util.Set;

public class StringUtil {

    public static final char SEP = File.separatorChar;
    public static final String EMPTY_STRING = "";
    public static final String CLASS_PATH = StringUtil.class.getResource("/").getPath();
    public static final String LOCAL_HOST_IPV4 = "127.0.0.1";
    public static final String LOCAL_HOST_IPV6 = "0:0:0:0:0:0:0:1";





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





    private static ServerCookieDecoder cookieDecoder = ServerCookieDecoder.STRICT;

    /**
     * @param cookiesString cookies in string
     * @return a set of cookies, never be null
     */
    public static Set<Cookie> decodeCookies(@Nullable final String cookiesString) {
        if (cookiesString == null) {
            return Collections.emptySet();
        }

        return cookieDecoder.decode(cookiesString);
    }

    public static String getFromCookies(@NotNull final Set<Cookie> cookies, @NotNull final String key) {
        for (Cookie cookie : cookies) {
            if (key.equals(cookie.name())) {
                return cookie.value();
            }
        }

        return null;
    }
}
