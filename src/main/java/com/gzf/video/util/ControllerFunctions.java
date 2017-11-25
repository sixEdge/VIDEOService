package com.gzf.video.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;

import java.util.Collection;
import java.util.List;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderNames.SET_COOKIE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public abstract class ControllerFunctions {





    // Json





    // FullResponse
    protected FullHttpResponse okResponse() {
        return new DefaultFullHttpResponse(HTTP_1_1, OK);
    }

    protected FullHttpResponse okResponse(final byte[] content) {
        return okResponse(Unpooled.wrappedBuffer(content));
    }

    protected FullHttpResponse okResponse(final byte[] content, final CharSequence contentType) {
        return okResponse(Unpooled.wrappedBuffer(content), contentType);
    }

    /**
     * <em>Note: The {@code content} must has not been read before.</em>
     *
     * @param content content
     * @return {@link FullHttpResponse}
     */
    protected FullHttpResponse okResponse(final ByteBuf content) {
        FullHttpResponse resp = new DefaultFullHttpResponse(HTTP_1_1, OK, content);
        resp.headers().add(CONTENT_LENGTH, content.writerIndex());
        return resp;
    }

    protected FullHttpResponse okResponse(final ByteBuf content, final CharSequence contentType) {
        FullHttpResponse resp = okResponse(content);
        resp.headers().add(CONTENT_TYPE, contentType);
        return resp;
    }

    protected FullHttpResponse failedResponse(final HttpResponseStatus status) {
        return new DefaultFullHttpResponse(HTTP_1_1, status);
    }

    protected FullHttpResponse failedResponse(final HttpResponseStatus status,
                                              final ByteBuf content,
                                              final CharSequence contentType) {
        FullHttpResponse resp = new DefaultFullHttpResponse(HTTP_1_1, status, content);
        resp.headers().add(CONTENT_TYPE, contentType);
        return resp;
    }





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

    public static HttpHeaders addCookie(final HttpHeaders headers, final Cookie c) {
        return headers.add(SET_COOKIE, c);
    }

    public static HttpHeaders addCookies(final HttpHeaders headers, final Collection<Cookie> cs) {
        return headers.add(SET_COOKIE, encodeCookies(cs));
    }
}
