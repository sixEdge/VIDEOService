package com.gzf.video.core.http.request;

import com.gzf.video.core.session.Session;
import com.gzf.video.core.session.storage.SessionStorage;
import com.sun.istack.internal.Nullable;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.cookie.Cookie;

import java.util.Set;

import static com.gzf.video.core.session.storage.SessionStorage.SESSION_ID;
import static com.gzf.video.core.session.storage.SessionStorage.USER_ID;
import static com.gzf.video.util.CookieFunctions.decodeCookies;
import static com.gzf.video.util.CookieFunctions.getFromCookies;
import static io.netty.handler.codec.http.HttpHeaderNames.COOKIE;

public abstract class SessionRequest {


    private static final SessionStorage SESSION_STORAGE = SessionStorage.getINSTANCE();


    protected final HttpHeaders headers;

    private Set<Cookie> cookies;

    private volatile Session session;

    private volatile boolean isNewSessionId;


    SessionRequest(final HttpHeaders headers,
                   @Nullable final Set<Cookie> cookies,
                   @Nullable final Session session) {
        this.headers = headers;
        this.cookies = cookies;
        this.session = session;
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


    /**
     * Never return null.<br />
     */
    public Session session() {
        if (session == null) {
            String sessionId = getFromCookies(cookies(), SESSION_ID);
            if (sessionId == null
                    || (session = SESSION_STORAGE.getSession(sessionId, false)) == null) {
                session = SESSION_STORAGE.createSession();
                isNewSessionId = true;
            }
        }

        return session;
    }


    public String getHeader(final CharSequence key) {
        return headers.get(key);
    }

    public String getCookie(final String key) {
        return getFromCookies(cookies(), key);
    }

    public boolean isNewSessionId() {
        return isNewSessionId;
    }

    public String sessionId() {
        return session().getSessionId();
    }


    // session

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
}
