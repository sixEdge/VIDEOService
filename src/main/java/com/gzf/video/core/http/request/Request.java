package com.gzf.video.core.http.request;

import com.gzf.video.core.session.Session;
import com.sun.istack.internal.Nullable;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;

import java.util.Map;
import java.util.Set;

/**
 * Be better to promise that whenever there is only one thread use the instance at the same time.
 */
public abstract class Request extends SessionRequest {

    final String uri;

    Map<String, String> parameters;

    Request(final FullHttpRequest req,
            @Nullable final Set<Cookie> cookies,
            @Nullable final Session session) {
        super(req.headers(), cookies, session);

        this.uri = req.uri();
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    /**
     * Never return null.
     */
    public abstract Map<String, String> parameters();

    public String getParameter(final String key) {
        return parameters().get(key);
    }
}
