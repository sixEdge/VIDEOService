package com.gzf.video.core.server.handler;

import com.gzf.video.core.session.Session;
import com.gzf.video.core.session.storage.SessionStorage;
import com.sun.istack.internal.NotNull;

class UserInterceptor {

    private static final SessionStorage SESSION_MANAGER = SessionStorage.getINSTANCE();


    /**
     * Intercept non-login-user.
     *
     * @param cookieSessionId session id from request cookie
     * @return session associates with this user if the user has logged in, otherwise null
     */
    public static Session doIntercept(@NotNull final String cookieSessionId) {
        Session session = SESSION_MANAGER.getSession(cookieSessionId, false);
        return  session == null || session.getUserId() == null
                ? null
                : session;
    }
}
