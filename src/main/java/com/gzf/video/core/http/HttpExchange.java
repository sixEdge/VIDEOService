package com.gzf.video.core.http;

import com.gzf.video.core.http.request.Request;
import com.gzf.video.core.http.response.Response;
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
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

public class HttpExchange {

    private ChannelHandlerContext context;

    private Request request;

    public HttpExchange(final ChannelHandlerContext context, final Request request) {
        this.context = context;
        this.request = request;
    }

    public ChannelHandlerContext context() {
        return context;
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

    public Request request() {
        return request;
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
     * @return {@link Response}
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


    //    ------------------------------ transform

    /**
     * With flush.
     */
    public ChannelFuture writeResponse(final Response resp) {
        ChannelFuture future;

        if (request.isNewSessionId()) {
            resp.headers().add(SET_COOKIE, cookieSessionId(request.sessionId()));
        }

        if (HttpHeaderValues.CLOSE.contentEqualsIgnoreCase(request.getHeader(CONNECTION))) {
            future = context.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
        } else {
            future = context.writeAndFlush(resp);
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
}
