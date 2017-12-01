package com.gzf.video.util;

import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;

import java.util.Collection;
import java.util.List;

public abstract class ControllerFunctions {

    // Json


    // Cookie
    private static ServerCookieEncoder cookieEncoder = ServerCookieEncoder.STRICT;

    public static String encodeCookie(final Cookie c) {
        return cookieEncoder.encode(c);
    }

    public static String encodeCookie(final String key, final String val) {
        return cookieEncoder.encode(key, val);
    }

    public static List<String> encodeCookies(final Cookie... cs) {
        return cookieEncoder.encode(cs);
    }

    public static List<String> encodeCookies(final Iterable<? extends Cookie> cs) {
        return cookieEncoder.encode(cs);
    }

    public static List<String> encodeCookies(final Collection<? extends Cookie> cs) {
        return cookieEncoder.encode(cs);
    }
}
