package com.gzf.video.service;

import com.gzf.video.core.bean.Bean;
import com.gzf.video.core.bean.inject.Autowire;
import com.gzf.video.core.http.HttpExchange;
import com.gzf.video.core.session.Session;
import com.gzf.video.core.session.storage.SessionStorage;
import com.gzf.video.dao.RSADAO;
import com.gzf.video.dao.collections._Login;
import com.gzf.video.util.StringUtil;
import com.mongodb.async.SingleResultCallback;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.PrivateKey;

import static com.gzf.video.core.session.storage.SessionStorage.RSA_PRIVATE_KEY;
import static com.gzf.video.dao.collections._Login.LoginStruct.USER_ID;
import static com.gzf.video.pojo.component.CodeMessage.failedCode;
import static com.gzf.video.pojo.component.CodeMessage.successCode;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

/**
 * Login & Logout & Sign up.
 */
@Bean
public class UserRegisterService {
    private static final Logger logger = LoggerFactory.getLogger(UserRegisterService.class);

    private static final SessionStorage SESSION_STORAGE = SessionStorage.getINSTANCE();

    @Autowire
    private RSADAO RSA_DAO;

    @Autowire
    private _Login _LOGIN;


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
                ex.writeResponse(OK, successCode(userId));
            } else {
                ex.writeResponse(OK, failedCode("用户名或密码错误"));
            }
        };

        // rsa decrypt & md5 encrypt
        String pwd = md5Password(ex, password);

        if (pwd == null) {
            ex.writeResponse(OK, failedCode("用户名或密码错误"));
            return;
        }

        if (useUsername) {
            _LOGIN._nameLogin(identity, pwd, callback);
        } else {
            _LOGIN._mailLogin(identity, pwd, callback);
        }
    }


    public void doLogout(Session session) {
        SESSION_STORAGE.destroyLoginCache(session.getSessionId());

        // TODO maybe better to use SessionStorage#destroySession()
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

        _LOGIN._signUp(username, mail, pwd, (result, t) -> {
            if (t != null) {
                ex.writeResponse(OK, successCode("注册成功"));
            } else {
                ex.writeResponse(OK, failedCode("注册失败"));
            }
        });
    }


    public RSADAO.RSAKeyPair doGetKeyPair() {
        return RSA_DAO.getKeyPair();
    }


    private String md5Password(final HttpExchange ex, final String cryptPwd) {
        // rsa decrypt
        String pwd = decryptPassword(ex, cryptPwd);

        if (pwd == null) {
            return null;
        }

        return StringUtil.hexMd5(pwd.getBytes());
    }

    private String decryptPassword(final HttpExchange ex, final String cryptPwd) {
        PrivateKey privateKey = (PrivateKey) ex.removeFromSession(RSA_PRIVATE_KEY);
        if (privateKey == null) {
            return null;
        }

        String pwd;
        try {
            pwd = RSA_DAO.decode(cryptPwd, privateKey);
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
}
