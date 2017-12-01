package com.gzf.video.core.http.request;

import com.gzf.video.core.http.response.Response;
import com.gzf.video.core.session.Session;
import com.gzf.video.core.session.storage.SessionStorage;
import com.gzf.video.util.StringUtil;
import com.sun.istack.internal.Nullable;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;

import java.util.Map;
import java.util.Set;

import static com.gzf.video.core.session.storage.SessionStorage.*;
import static com.gzf.video.core.session.storage.SessionStorage.SESSION_ID_MAX_AGE;
import static com.gzf.video.util.ControllerFunctions.encodeCookie;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.SET_COOKIE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

/**
 * Must promise that there is only one thread use the instance at the same time.
 */
public abstract class Request {

    private static final SessionStorage SESSION_STORAGE = SessionStorage.getINSTANCE();


    private ChannelHandlerContext ctx;

    private HttpHeaders headers;

    protected Map<String, String> parameters;

    private Set<Cookie> cookies;

    private volatile Session session;

    private volatile boolean isNewSessionId;


    Request(final ChannelHandlerContext ctx,
            final HttpHeaders headers,
            final Set<Cookie> cookies,
            @Nullable final Session session) {
        this.ctx = ctx;
        this.headers = headers;
        this.cookies = cookies;
        this.session = session;
    }


    public ChannelHandlerContext getContext() {
        return ctx;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    /**
     * Never return null.
     */
    public Set<Cookie> cookies() {
        return cookies;
    }

    public String sessionId() {
        return session().getSessionId();
    }

    /**
     * Never return null.<br />
     * <em>NOTE: Shouldn't be called in async context, unless you know what you are doing.</em>
     */
    public Session session() {
        if (session == null) {
            session = SESSION_STORAGE.createSession();
            isNewSessionId = true;
        }

        return session;
    }

    public boolean isNewSessionId() {
        return isNewSessionId;
    }


    //    ------------------------------ base

    public Channel channel() {
        return ctx.channel();
    }

    public ByteBufAllocator alloc() {
        return ctx.alloc();
    }

    public abstract void release();

    public <V> Promise<V> newPromise(Class<V> clazz) {
        return new DefaultPromise<>(ctx.executor());
    }

    /**
     * Unreadable.
     */
    public ByteBuf newByteBuf(final int capacity) {
        return ctx.alloc().ioBuffer(capacity, capacity);
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
        CharSequence connection = headers.get(CONNECTION);

        if (isNewSessionId) {
            resp.headers().add(SET_COOKIE, encodeCookie(cookieSessionId(sessionId())));
        }

        if (HttpHeaderValues.CLOSE.contentEqualsIgnoreCase(connection)) {
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



//    ------------------------------- request parameter

    /**
     * Never be null.
     */
    public abstract Map<String, String> parameters();

    public String getParameter(final String key) {
        return parameters().get(key);
    }



//    ------------------------------- request header

    public String getHeader(final CharSequence key) {
        return headers.get(key);
    }



//    ------------------------------- cookie

    public String getCookie(final String key) {
        return StringUtil.getFromCookies(cookies(), key);
    }



//    ------------------------------- session

    public Object getFromSession(final String key) {
        return session().get(key);
    }

    public Object addToSession(final String key, final Object val) {
        return session().put(key, val);
    }

    public String getUserId() {
        return session().getUserId();
    }

    public void setUserId(final String userId) {
        session().put(USER_ID, userId);
    }

    /**
     * Add session id and user id as cookies to response headers.<br />
     * Create an internal-session. <br />
     * Create session in the cache when {@code rememberMe} is true.
     *
     * @param userId user id
     * @param rememberMe remember me
     */
    public void addIdentification(final String userId, final boolean rememberMe) {
        String sessionId = sessionId();
        setUserId(userId);

        if (rememberMe) {
            SESSION_STORAGE.createLoginCache(sessionId, userId);
        }
    }


    public static Cookie cookieSessionId(final String sessionId) {
        Cookie cookieSessionId = new DefaultCookie(SESSION_ID, sessionId);
        cookieSessionId.setPath(SESSION_ID_PATH);
        cookieSessionId.setHttpOnly(true);
        cookieSessionId.setMaxAge(SESSION_ID_MAX_AGE);

        return cookieSessionId;
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
