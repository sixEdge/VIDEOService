package com.gzf.video.core.http.request;

import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

import static com.gzf.video.core.tool.CookieFunctions.decodeCookies;
import static com.gzf.video.core.tool.CookieFunctions.getFromCookies;
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


    @NotNull
    public Set<Cookie> cookies() {
        if (cookies == null) {
            cookies = decodeCookies(headers.get(COOKIE));
        }
        return cookies;
    }

    public String getCookie(final String key) {
        return getFromCookies(cookies(), key);
    }


    public abstract Map<String, String> parameters();

    public String getParameter(final String name) {
        return parameters().get(name);
    }
}
