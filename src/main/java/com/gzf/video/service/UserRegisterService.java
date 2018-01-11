package com.gzf.video.service;

import com.gzf.video.core.http.HttpExchange;
import com.gzf.video.core.session.Session;
import com.gzf.video.core.session.storage.SessionStorage;
import com.gzf.video.dao._Login;
import com.gzf.video.util.StringUtil;
import com.mongodb.async.SingleResultCallback;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.PrivateKey;

import static com.gzf.video.core.session.storage.SessionStorage.RSA_PRIVATE_KEY;
import static com.gzf.video.dao._Login.LoginStruct.USER_ID;
import static com.gzf.video.pojo.component.CodeMessage.failedCode;
import static com.gzf.video.pojo.component.CodeMessage.successCode;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

/**
 * Login & Logout & Sign up.
 */
public class UserRegisterService {
    private static final Logger logger = LoggerFactory.getLogger(UserRegisterService.class);

    private static final SessionStorage SESSION_STORAGE = SessionStorage.getINSTANCE();

    private static final RSASecurityService rsaSecurityService = RSASecurityService.getINSTANCE();

    private static final _Login LOGIN_DAO = new _Login();


    public void doLogin(Session session,
                        String identity,
                        String password,
                        boolean useUsername,
                        boolean rememberMe,
                        HttpExchange ex) {
        SingleResultCallback<Document> callback = (result, t) -> {
            if (result != null) {
                String userId = "" + result.getInteger(USER_ID);
                createIdentification(session, userId, rememberMe);
                ex.writeResponse(ex.okResponse(successCode(userId)));
            } else {
                ex.writeResponse(OK, failedCode("用户名或密码错误"));
            }
        };

        // rsa decrypt & md5 encrypt
        String pwd = md5Password(ex, password);

        if (pwd == null) {
            ex.writeResponse(OK, failedCode("注册失败"));
            return;
        }

        if (useUsername) {
            LOGIN_DAO._nameLogin(identity, pwd, callback);
        } else {
            LOGIN_DAO._mailLogin(identity, pwd, callback);
        }
    }


    public void doLogout(Session session) {
        SESSION_STORAGE.destroyLoginCache(session.getSessionId());

        // TODO Maybe better to use SESSION_STORAGE#destroySession()
        session.setUserId(null);
        session.clear();
    }


    public void doSignUp(String username,
                         String mail,
                         String password,
                         HttpExchange ex) {
        // rsa decrypt & md5 encrypt
        String pwd = md5Password(ex, password);

        if (pwd == null) {
            ex.writeResponse(OK, failedCode("注册失败"));
            return;
        }

        LOGIN_DAO._signUp(username, mail, pwd, (result, t) -> {
            if (t != null) {
                ex.writeResponse(OK, successCode("注册成功"));
            } else {
                ex.writeResponse(OK, failedCode("注册失败"));
            }
        });
    }


    private static String md5Password(final HttpExchange ex, final String cryptPwd) {
        // rsa decrypt
        String pwd = decryptPassword(ex, cryptPwd);

        if (pwd == null) {
            return null;
        }

        return StringUtil.hexMd5(pwd.getBytes());
    }

    private static String decryptPassword(final HttpExchange ex, final String cryptPwd) {
        PrivateKey privateKey = (PrivateKey) ex.removeFromSession(RSA_PRIVATE_KEY);
        if (privateKey == null) {
            return null;
        }

        String pwd;
        try {
            pwd = rsaSecurityService.doDecode(cryptPwd, privateKey);
        } catch (IOException e) {
            logger.error("RSA decode error.", e);
            return null;
        }

        return pwd;
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
