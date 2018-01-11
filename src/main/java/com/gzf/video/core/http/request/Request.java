package com.gzf.video.core.http.request;

import com.sun.istack.internal.Nullable;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;

import java.util.Map;
import java.util.Set;

import static com.gzf.video.util.CookieFunctions.decodeCookies;
import static com.gzf.video.util.CookieFunctions.getFromCookies;
import static io.netty.handler.codec.http.HttpHeaderNames.COOKIE;

/**
 * Be better to promise that whenever there is only one thread use the instance at the same time.
 */
public abstract class Request {

    final String uri;

    Map<String, String> parameters;

    private final HttpHeaders headers;

    private Set<Cookie> cookies;


    Request(final FullHttpRequest req,
            @Nullable final Set<Cookie> cookies) {
        this.uri = req.uri();
        this.headers = req.headers();
        this.cookies = cookies;
    }


    public HttpHeaders headers() {
        return headers;
    }

    public String getHeader(final CharSequence key) {
        return headers.get(key);
    }


    /**
     * Never return null.
     */
    public Set<Cookie> cookies() {
        if (cookies == null) {
            cookies = decodeCookies(headers.get(COOKIE));
        }
        return cookies;
    }

    public String getCookie(final String key) {
        return getFromCookies(cookies(), key);
    }


    /**
     * Never return null.
     */
    public abstract Map<String, String> parameters();

    public String getParameter(final String key) {
        return parameters().get(key);
    }
}
