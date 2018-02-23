package com.gzf.video.core.http;

import com.gzf.video.core.http.request.Request;
import com.gzf.video.core.session.Session;
import com.gzf.video.core.session.storage.SessionStorage;
import org.jetbrains.annotations.Nullable;

import static com.gzf.video.core.session.storage.SessionStorage.SESSION_ID;
import static com.gzf.video.core.tool.CookieFunctions.getFromCookies;

public abstract class SessionContext {

    private static final SessionStorage SESSION_STORAGE = SessionStorage.getINSTANCE();

    protected final Request request;

    protected volatile Session session;

    private volatile boolean isNewSessionId;


    SessionContext(final Request request,
                   @Nullable final Session session) {
        this.request = request;
        this.session = session;
    }


    public Session session() {
        if (session == null) {
            String sessionId = getFromCookies(request.cookies(), SESSION_ID);
            if (sessionId == null
                    || (session = SESSION_STORAGE.getSession(sessionId, false)) == null) {
                session = SESSION_STORAGE.createSession();
                isNewSessionId = true;
            }
        }

        return session;
    }


    boolean isNewSessionId() {
        return isNewSessionId;
    }

    public String sessionId() {
        return session().getSessionId();
    }

    public Object getFromSession(final String key) {
        return session().getAttribute(key);
    }

    public Object addToSession(final String key, final Object val) {
        return session().putAttribute(key, val);
    }

    public Object removeFromSession(final String key) {
        return session().removeAttribute(key);
    }

    public String getUserId() {
        return session().getUserId();
    }

    public void setUserId(final String userId) {
        session().setUserId(userId);
    }
}
