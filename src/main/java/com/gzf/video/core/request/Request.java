package com.gzf.video.core.request;

import com.gzf.video.core.server.handler.ActionHandler;
import com.gzf.video.core.session.Session;
import com.gzf.video.core.session.SessionStorage;
import com.gzf.video.util.StringUtil;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.gzf.video.core.session.SessionStorage.*;
import static com.gzf.video.core.session.SessionStorage.SESSION_ID_MAX_AGE;
import static com.gzf.video.util.ControllerFunctions.encodeCookies;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderNames.SET_COOKIE;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.COOKIE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public abstract class Request {

    private static final SessionStorage SESSION_STORAGE = SessionStorage.getINSTANCE();

    private ChannelHandlerContext ctx;

    private HttpHeaders headers;

    protected Map<String, String> parameters;

    private Set<Cookie> cookies;

    private Session session;

    private String sessionId;


    Request(final ChannelHandlerContext ctx,
            final HttpHeaders headers,
            @Nullable final Set<Cookie> cookies,
            @Nullable final Session session) {
        this.ctx = ctx;
        this.headers = headers;
        this.cookies = cookies;
        this.session = session;
    }

    public Request() {}


    public ChannelHandlerContext getContext() {
        return ctx;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    /**
     * Never be null.
     */
    public Set<Cookie> getCookies() {
        if (cookies == null) {
            String cs = headers.get(COOKIE);
            if (cs == null) {
                cookies = Collections.emptySet();
            } else {
                cookies = StringUtil.decodeCookies(cs);
            }
        }
        return cookies;
    }

    /**
     * Never be null.
     */
    public Session getSession() {
        if (session == null) {
            if (sessionId == null) {

                // the ActionHandler must bound to this ChannelHandlerContext
                ActionHandler actionHandler = (ActionHandler) ctx.handler();

                sessionId = actionHandler.getSessionId();

                if (sessionId == null) {
                    sessionId = UUID.randomUUID().toString();

                    actionHandler.setSessionId(sessionId);
                }
            }
            session = SESSION_STORAGE.getSession(sessionId);
        }
        return session;
    }



//    ------------------------------

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


    public ByteBuf newByteBuf(final int capacity) {
        return ctx.alloc().ioBuffer(capacity, capacity);
    }

    public ByteBuf newByteBuf(final byte[] bs) {
        return newByteBuf(bs.length).writeBytes(bs);
    }

    /**
     * With flush.
     */
    public ChannelFuture writeResponse(final FullHttpResponse resp) {
        ChannelFuture future;
        CharSequence connection = headers.get(CONNECTION);

        if (HttpHeaderValues.CLOSE.contentEqualsIgnoreCase(connection)) {
            future = ctx.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
        } else {
            resp.headers().add(CONNECTION, KEEP_ALIVE);
            future = ctx.writeAndFlush(resp);
        }

        return future;
    }

    /**
     * With flush.
     */
    public ChannelFuture writeResponse(final HttpResponseStatus status) {
        return writeResponse(new DefaultFullHttpResponse(HTTP_1_1, status));
    }

    /**
     * With flush.
     */
    public ChannelFuture writeResponse(final HttpResponseStatus status,
                                       final byte[] bs,
                                       final CharSequence contentType) {
        FullHttpResponse resp = new DefaultFullHttpResponse(HTTP_1_1, status, newByteBuf(bs));
        resp.headers().add(CONTENT_TYPE, contentType);
        resp.headers().add(CONTENT_LENGTH, bs.length);
        return writeResponse(resp);
    }


//    -------------------------------

    public abstract Map<String, String> parameters();



//    ------------------------------- request header

    public String getHeader(final CharSequence key) {
        return headers.get(key);
    }



//    ------------------------------- cookie

    public String getFromCookies(@NotNull final String key) {
        return StringUtil.getFromCookies(getCookies(), key);
    }



//    ------------------------------- session

    public Object getFromSession(final String key) {
        if (session == null) {
            getSession();
        }
        return session.get(key);
    }

    public Object addToSession(final String key, final Object val) {
        if (session == null) {
            getSession();
        }
        return session.put(key, val);
    }

    public String getUserId() {
        if (session == null) {
            getSession();
        }
        return session.getUserId();
    }

    public void setUserId(final String userId) {
        if (session == null) {
            getSession();
        }
        session.put(USER_ID, userId);
    }

    /**
     * Add session id and user id as cookies to response headers.<br />
     * Create an internal-session. <br />
     * Create session in the cache when {@code rememberMe} is true.
     *
     * @param headers http (response) headers
     * @param userId user id
     * @param rememberMe remember me
     */
    public void addIdentification(final HttpHeaders headers,
                                  final String userId,
                                  final boolean rememberMe) {
        Session session = getSession();
        session.setUserId(userId);

        Cookie cookieSessionId = new DefaultCookie(SESSION_ID, sessionId);
        cookieSessionId.setPath(SESSION_ID_PATH);
        cookieSessionId.setHttpOnly(true);

        Cookie cookieUserId = new DefaultCookie(USER_ID, "" + userId);
        cookieUserId.setPath(SESSION_ID_PATH);
        cookieUserId.setHttpOnly(true);

        if (rememberMe) {
            cookieSessionId.setMaxAge(SESSION_ID_MAX_AGE);
            cookieUserId.setMaxAge(SESSION_ID_MAX_AGE);
            SESSION_STORAGE.createLoginCache(sessionId, userId);
        }

        headers.add(SET_COOKIE, encodeCookies(cookieSessionId, cookieUserId));
    }
}
