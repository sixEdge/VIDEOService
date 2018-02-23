package com.gzf.video.core.tool;

import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.gzf.video.core.session.storage.SessionStorage.SESSION_ID;
import static com.gzf.video.core.session.storage.SessionStorage.SESSION_ID_MAX_AGE;
import static com.gzf.video.core.session.storage.SessionStorage.SESSION_ID_PATH;

public abstract class CookieFunctions {

    // encode

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

    public static String cookieSessionId(final String sessionId) {
        Cookie cookieSessionId = new DefaultCookie(SESSION_ID, sessionId);
        cookieSessionId.setPath(SESSION_ID_PATH);
        cookieSessionId.setHttpOnly(true);
//        cookieSessionId.setSecure(true);  TODO add this sentence when use https
        cookieSessionId.setMaxAge(SESSION_ID_MAX_AGE);

        return encodeCookie(cookieSessionId);
    }


    // decode

    private static ServerCookieDecoder cookieDecoder = ServerCookieDecoder.STRICT;

    /**
     * @param cookiesString cookies in string
     * @return a set of cookies
     */
    @NotNull
    public static Set<Cookie> decodeCookies(@Nullable final String cookiesString) {
        if (cookiesString == null) {
            return Collections.emptySet();
        }

        return cookieDecoder.decode(cookiesString);
    }

    public static String getFromCookies(final Set<Cookie> cookies, final String key) {
        for (Cookie cookie : cookies) {
            if (key.equals(cookie.name())) {
                return cookie.value();
            }
        }

        return null;
    }
}
