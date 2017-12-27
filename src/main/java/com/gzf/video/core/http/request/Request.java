package com.gzf.video.core.http.request;

import com.gzf.video.core.http.response.Response;
import com.gzf.video.core.session.Session;
import com.sun.istack.internal.Nullable;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;

import java.util.Map;
import java.util.Set;

import static com.gzf.video.util.CookieFunctions.cookieSessionId;
import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

/**
 * Must promise that there is only one thread use the instance at the same time.
 */
public abstract class Request extends SessionRequest {

    private ChannelHandlerContext ctx;

    private final HttpMethod method;

    final String uri;

    Map<String, String> parameters;


    Request(final ChannelHandlerContext ctx,
            final FullHttpRequest req,
            @Nullable final Set<Cookie> cookies,
            @Nullable final Session session) {
        super(req.headers(), cookies, session);

        this.ctx = ctx;

        this.method  = req.method();
        this.uri     = req.uri();
    }


    public ChannelHandlerContext getContext() {
        return ctx;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }


    /**
     * Never be null.
     */
    public abstract Map<String, String> parameters();

    public String getParameter(final String key) {
        return parameters().get(key);
    }


    //    ------------------------------ base

    public Channel channel() {
        return ctx.channel();
    }

    public ByteBufAllocator alloc() {
        return ctx.alloc();
    }

    public <V> Promise<V> newPromise(Class<V> clazz) {
        return new DefaultPromise<>(ctx.executor());
    }

    /**
     * Unreadable.
     */
    public ByteBuf newByteBuf(final int capacity) {
        return alloc().ioBuffer(capacity, capacity);
    }

    public ByteBuf newByteBuf(final byte[] bs) {
        return newByteBuf(bs.length).writeBytes(bs);
    }


    //    ------------------------------ transform

    /**
     * With flush.
     */
    public ChannelFuture writeResponse(final Response resp) {
        ChannelFuture future;

        if (isNewSessionId()) {
            resp.headers().add(SET_COOKIE, cookieSessionId(sessionId()));
        }

        if (HttpHeaderValues.CLOSE.contentEqualsIgnoreCase(headers.get(CONNECTION))) {
            future = ctx.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
        } else {
            future = ctx.writeAndFlush(resp);
        }

        return future;
    }

    /**
     * With flush.
     */
    public ChannelFuture writeResponse(final HttpResponseStatus status) {
        return writeResponse(new Response(status));
    }

    /**
     * With flush.
     */
    public ChannelFuture writeResponse(final HttpResponseStatus status,
                                       final byte[] bs,
                                       final CharSequence contentType) {
        Response resp = new Response(status, newByteBuf(bs));
        resp.headers().add(CONTENT_TYPE, contentType);
        resp.headers().add(CONTENT_LENGTH, bs.length);
        return writeResponse(resp);
    }


    // Response

    public Response okResponse() {
        return new Response(OK);
    }

    public Response okResponse(final byte[] content, final CharSequence contentType) {
        return okResponse(newByteBuf(content), contentType);
    }

    /**
     * <em>Note: The {@code content} must has not been read before.</em>
     *
     * @param content content
     * @return {@link FullHttpResponse}
     */
    public Response okResponse(final ByteBuf content) {
        Response resp = new Response(OK, content);
        resp.headers().add(CONTENT_LENGTH, content.writerIndex());
        return resp;
    }

    public Response okResponse(final ByteBuf content, final CharSequence contentType) {
        Response resp = okResponse(content);
        resp.headers().add(CONTENT_TYPE, contentType);
        return resp;
    }


    public Response failedResponse(final HttpResponseStatus status) {
        return new Response(status);
    }

    public Response failedResponse(final HttpResponseStatus status,
                                   final ByteBuf content,
                                   final CharSequence contentType) {
        Response resp = new Response(status, content);
        resp.headers().add(CONTENT_TYPE, contentType);
        return resp;
    }
}
