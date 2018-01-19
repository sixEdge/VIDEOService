package com.gzf.video.service;

import com.gzf.video.core.bean.Bean;
import com.gzf.video.core.bean.inject.Autowire;
import com.gzf.video.core.bean.inject.Component;
import com.gzf.video.core.http.HttpExchange;
import com.gzf.video.core.session.Session;
import com.gzf.video.core.session.storage.SessionStorage;
import com.gzf.video.dao.RsaDAO;
import com.gzf.video.dao.collections._Login;
import com.gzf.video.util.StringUtil;
import com.mongodb.async.SingleResultCallback;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
@Component
public class UserRegisterService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final SessionStorage SESSION_STORAGE = SessionStorage.getINSTANCE();

    @Autowire
    private RsaDAO RSA_DAO;

    @Autowire
    private _Login _LOGIN;


    public void doLogin(HttpExchange ex,
                        String identity,
                        String password,
                        boolean useUsername,
                        boolean rememberMe) {
        Session session = ex.session();
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


    public void doSignUp(HttpExchange ex,
                         String username,
                         String mail,
                         String password) {
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


    public byte[] doGetRsaPublicKey(HttpExchange ex) {
        RsaDAO.RSAKeyPair keyPair = RSA_DAO.getKeyPair();
        ex.addToSession(RSA_PRIVATE_KEY, keyPair.getPrivateKey());
        return keyPair.getPublicKeyBytes();
    }


    private String md5Password(final HttpExchange ex, final String cryptPwd) {
        // rsa decrypt
        byte[] pwd = decryptPassword(ex, cryptPwd);
        if (pwd == null) {
            return null;
        }

        // md5 encrypt
        return StringUtil.hexMd5(pwd);
    }

    private byte[] decryptPassword(final HttpExchange ex, final String cryptPwd) {
        PrivateKey privateKey = (PrivateKey) ex.removeFromSession(RSA_PRIVATE_KEY);
        if (privateKey == null) {
            logger.warn("no rsa private key found, client ip: {}.", ex.channel().remoteAddress());
            return null;
        }

        return RSA_DAO.decode(cryptPwd, privateKey);
    }

    private static void createIdentification(final Session session, final String userId, final boolean rememberMe) {
        session.setUserId(userId);
        if (rememberMe) {
            SESSION_STORAGE.createLoginCache(session.getSessionId(), userId);
        }
    }
}
