package com.gzf.video.core.http;

import com.gzf.video.core.http.request.Request;
import com.gzf.video.core.http.response.Response;
import com.gzf.video.core.session.Session;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;

import static com.gzf.video.util.CookieFunctions.cookieSessionId;
import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

public class HttpExchange extends SessionContext {

    private ChannelHandlerContext context;

    public HttpExchange(final ChannelHandlerContext context,
                        final Request request,
                        final Session session) {
        super(request, session);
        this.context = context;
    }

    public ChannelHandlerContext context() {
        return context;
    }

    public Request request() {
        return request;
    }

    public Channel channel() {
        return context.channel();
    }

    public ByteBufAllocator alloc() {
        return context.alloc();
    }

    public <V> Promise<V> newPromise(Class<V> clazz) {
        return new DefaultPromise<>(context.executor());
    }

    /**
     * Unreadable io buffer.
     */
    public ByteBuf ioByteBuf(final int capacity) {
        return alloc().ioBuffer(capacity, capacity);
    }

    public ByteBuf ioByteBuf(final byte[] bs) {
        return ioByteBuf(bs.length).writeBytes(bs);
    }

    public Response okResponse() {
        return new Response(OK);
    }

    /**
     * Construct ok response with content-type {@code application/json}.
     */
    public Response okResponse(final byte[] content) {
        return okResponse(ioByteBuf(content));
    }

    public Response okResponse(final byte[] content, final CharSequence contentType) {
        return okResponse(ioByteBuf(content), contentType);
    }

    /**
     * Construct ok response with content-type {@code application/json}.
     * <br />
     * <em>Note: The {@code content} must has not been read before.</em>
     *
     * @param content content
     * @return {@link Response}
     */
    public Response okResponse(final ByteBuf content) {
        return okResponse(content, APPLICATION_JSON);
    }

    public Response okResponse(final ByteBuf content, final CharSequence contentType) {
        Response resp = okResponse(content);
        resp.headers().add(CONTENT_TYPE, contentType);
        resp.headers().add(CONTENT_LENGTH, content.writerIndex());
        return resp;
    }


    public Response failedResponse(final HttpResponseStatus status) {
        return new Response(status);
    }

    /**
     * Construct failed response with content-type {@code application-json}.
     */
    public Response failedResponse(final HttpResponseStatus status, final ByteBuf content) {
        return failedResponse(status, content, APPLICATION_JSON);
    }

    public Response failedResponse(final HttpResponseStatus status,
                                   final ByteBuf content,
                                   final CharSequence contentType) {
        Response resp = new Response(status, content);
        resp.headers().add(CONTENT_TYPE, contentType);
        resp.headers().add(CONTENT_LENGTH, content.writerIndex());
        return resp;
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

        if (HttpHeaderValues.CLOSE.contentEqualsIgnoreCase(request.getHeader(CONNECTION))) {
            future = context.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
        } else {
            future = context.writeAndFlush(resp);
        }

        return future;
    }

    public ChannelFuture writeResponse(final HttpResponseStatus status) {
        return writeResponse(new Response(status));
    }

    /**
     * Write response with content-type {@code application/json}.
     */
    public ChannelFuture writeResponse(final HttpResponseStatus status, final byte[] bs) {
        return writeResponse(status, bs, APPLICATION_JSON);
    }

    public ChannelFuture writeResponse(final HttpResponseStatus status,
                                       final byte[] bs,
                                       final CharSequence contentType) {
        Response resp = new Response(status, ioByteBuf(bs));
        resp.headers().add(CONTENT_TYPE, contentType);
        resp.headers().add(CONTENT_LENGTH, bs.length);
        return writeResponse(resp);
    }
}
