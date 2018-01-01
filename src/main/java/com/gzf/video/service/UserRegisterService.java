package com.gzf.video.service;

import com.gzf.video.core.session.Session;
import com.gzf.video.core.session.storage.SessionStorage;
import com.gzf.video.dao._Login;
import com.gzf.video.util.StringUtil;
import com.mongodb.async.SingleResultCallback;
import io.netty.util.concurrent.Promise;
import org.bson.Document;

import static com.gzf.video.dao._Login.LoginStruct.USER_ID;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * Login & Logout & Sign up.
 */
public class UserRegisterService {

    private static final SessionStorage SESSION_STORAGE = SessionStorage.getINSTANCE();

    private static final _Login LOGIN_DAO = new _Login();


    public void doLogin(final Session session,
                        final String identity,
                        final String password,
                        final boolean useUsername,
                        final boolean rememberMe,
                        final Promise<String> promise) {
        SingleResultCallback<Document> callback = (result, t) -> {
            if (result != null) {
                String userId = "" + result.getInteger(USER_ID);
                createIdentification(session, userId, rememberMe);
                promise.setSuccess(userId);
            } else {
                promise.setSuccess(null);
            }
        };

        // encrypt
        String pwd = StringUtil.hexMd5(password.getBytes());

        if (useUsername) {
            LOGIN_DAO._nameLogin(identity, pwd, callback);
        } else {
            LOGIN_DAO._mailLogin(identity, pwd, callback);
        }
    }

    public void doLogout(final String sessionId) {
        SESSION_STORAGE.destroyLoginCache(sessionId);
        SESSION_STORAGE.getSession(sessionId).setUserId(null);
    }

    public void doSignUp(final String username,
                         final String mail,
                         final String password,
                         final Promise<Boolean> promise) {
        LOGIN_DAO._signUp(username, mail, password, (result, t) -> {
            if (t == null)
                promise.setSuccess(TRUE);
            else
                promise.setSuccess(FALSE);
        });
    }


    private static void createIdentification(final Session session, final String userId, final boolean rememberMe) {
        session.setUserId(userId);
        if (rememberMe) {
            SESSION_STORAGE.createLoginCache(session.getSessionId(), userId);
        }
    }


    private static final UserRegisterService INSTANCE = new UserRegisterService();

    public static UserRegisterService getINSTANCE() {
        return INSTANCE;
    }

    private UserRegisterService() {}
}
