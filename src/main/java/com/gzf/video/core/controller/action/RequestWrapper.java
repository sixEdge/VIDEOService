package com.gzf.video.core.controller.action;

import com.gzf.video.core.session.Session;
import com.gzf.video.core.session.SessionManager;
import com.gzf.video.util.StringUtil;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.gzf.video.core.session.SessionManager.*;
import static com.gzf.video.core.session.SessionManager.SESSION_ID_MAX_AGE;
import static com.gzf.video.util.ControllerFunctions.encodeCookies;
import static io.netty.handler.codec.http.HttpHeaderNames.SET_COOKIE;
import static io.netty.handler.codec.http.HttpHeaders.Names.COOKIE;

/**
 * Thread unsafe.
 */
public class RequestWrapper {

    private static final SessionManager SESSION_MANAGER = SessionManager.getINSTANCE();

    private ChannelHandlerContext ctx;

    private QueryStringDecoder queryStringDecoder;

    private HttpHeaders headers;

    private Set<Cookie> cookies;

    private Session session;

    private String sessionId;

    private ByteBuf content;

    // not come yet
    private Object object;


    public RequestWrapper(final ChannelHandlerContext ctx,
                          final QueryStringDecoder queryStringDecoder,
                          final FullHttpRequest req,
                          @Nullable final Set<Cookie> cookies,
                          @Nullable final Session session,
                          @NotNull final String sessionId) {
        this.ctx = ctx;
        this.queryStringDecoder = queryStringDecoder;
        this.headers = req.headers();
        this.cookies = cookies;
        this.session = session;
        this.sessionId = sessionId;
        this.content = req.content();
    }

    public ChannelHandlerContext getContext() {
        return ctx;
    }

    public QueryStringDecoder getQueryStringDecoder() {
        return queryStringDecoder;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public Set<Cookie> getCookies() {
        return cookies;
    }

    public Session getSession() {
        if (session == null) {
            assert sessionId != null;
            session = SESSION_MANAGER.getSession(sessionId);
        }
        return session;
    }

    public byte[] getContent() {
        return content.array();
    }



//    ------------------------------

    public Channel channel() {
        return ctx.channel();
    }

    public ByteBufAllocator alloc() {
        return ctx.alloc();
    }

    public boolean release() {
        return content.release();
    }

    public <V> Promise<V> newPromise(Class<V> clazz) {
        return new DefaultPromise<>(ctx.executor());
    }

    public ByteBuf newByteBuf(final int capacity) {
        return ctx.alloc().buffer(capacity, capacity);
    }

    public ByteBuf newByteBuf(final byte[] bs) {
        return newByteBuf(bs.length).writeBytes(bs);
    }

    /**
     * With flush.
     */
    public ChannelFuture writeResponse(final FullHttpResponse resp) {
        return ctx.writeAndFlush(resp);
    }


//    -------------------------------

    public Map<String, List<String>> requestParams() {
        return queryStringDecoder.parameters();
    }



//    ------------------------------- request header

    public String getHeader(final CharSequence key) {
        return headers.get(key);
    }



//    ------------------------------- cookie

    public String getFromCookies(@NotNull final String key) {
        if (cookies == null) {
            cookies = StringUtil.decodeCookies(getHeader(COOKIE));
        }
        return StringUtil.getFromCookies(cookies, key);
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
        Session session = SESSION_MANAGER.getSession(sessionId);
        session.setUserId(userId);

        Cookie cookieSessionId = new DefaultCookie(SESSION_ID, sessionId);
        cookieSessionId.setPath(SESSION_ID_PATH);
        cookieSessionId.setHttpOnly(true);

        Cookie cookieUserId = new DefaultCookie(USER_ID, userId);
        cookieUserId.setPath(SESSION_ID_PATH);
        cookieUserId.setHttpOnly(true);

        if (rememberMe) {
            cookieSessionId.setMaxAge(SESSION_ID_MAX_AGE);
            cookieUserId.setMaxAge(SESSION_ID_MAX_AGE);
            SESSION_MANAGER.createLoginCache(sessionId, userId);
        }

        headers.add(SET_COOKIE, encodeCookies(cookieSessionId, cookieUserId));
    }
}
