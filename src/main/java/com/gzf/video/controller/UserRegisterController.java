package com.gzf.video.controller;

import com.gzf.video.core.controller.Controller;
import com.gzf.video.core.controller.action.method.Get;
import com.gzf.video.core.controller.action.method.Post;
import com.gzf.video.core.http.HttpExchange;
import com.gzf.video.core.http.response.Response;
import com.gzf.video.core.session.Session;
import com.gzf.video.service.RSASecurityService;
import com.gzf.video.service.UserRegisterService;
import com.gzf.video.core.http.request.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.PrivateKey;
import java.util.Optional;

import static com.gzf.video.core.session.storage.SessionStorage.RSA_PRIVATE_KEY;
import static com.gzf.video.core.session.storage.SessionStorage.SESSION_ID;
import static com.gzf.video.pojo.component.CodeMessage.failedCode;
import static com.gzf.video.pojo.component.CodeMessage.successCode;
import static com.gzf.video.service.RSASecurityService.RSAKeyPair;
import static com.gzf.video.util.StringUtil.EMPTY_STRING;
import static com.gzf.video.util.StringUtil.anyNullOrEmpty;
import static com.gzf.video.util.StringUtil.isNullOrEmpty;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;
import static io.netty.handler.codec.http.HttpHeaderValues.TEXT_PLAIN;
import static io.netty.handler.codec.http.HttpResponseStatus.*;

@Controller("/reg")
public class UserRegisterController {
    private final Logger logger = LoggerFactory.getLogger(getClass());


    private static final String IDENTITY_PARAM      = "id";
    private static final String USER_NAME_PARAM     = "uname";
    private static final String MAIL_PARAM          = "mail";
    private static final String PASSWORD_PARAM      = "pwd";

    // 0 user & name, 1 user & mail, 2 admin & mail
    private static final String LOGIN_MODE_PARAM    = "mode";


    private UserRegisterService userRegisterService = UserRegisterService.getINSTANCE();
    private RSASecurityService rsaSecurityService   = RSASecurityService.getINSTANCE();


    @Post("/login")
    public Response login(final HttpExchange ex) {
        Request req = ex.request();

        // has login
        String preUserId;
        if ((preUserId = req.getUserId()) != null) {
            return ex.okResponse(successCode(preUserId), APPLICATION_JSON);
        }

        String identity = req.getParameter(IDENTITY_PARAM);
        String password = req.getParameter(PASSWORD_PARAM);
        String modeStr  = req.getParameter(LOGIN_MODE_PARAM);

        if (anyNullOrEmpty(identity, modeStr, password) || identity.length() > 64) {
            return ex.failedResponse(BAD_REQUEST);
        }

        Session session = req.session();

        // get rsa private key
        PrivateKey privateKey = (PrivateKey) session.remove(RSA_PRIVATE_KEY);
        if (privateKey == null) {
            return ex.failedResponse(BAD_REQUEST);
        }

        Optional<String> opPwd;

        // rsa decrypt
        try {
            opPwd = rsaSecurityService.doDecode(password, privateKey);
        } catch (IOException e) {
            logger.error("RSA decode error.", e);
            return ex.failedResponse(INTERNAL_SERVER_ERROR);
        }

        if (!opPwd.isPresent()) {
            logger.warn("no RSA private key found, for client {}, user {}.",
                    ex.channel().remoteAddress(),
                    identity);
            return ex.failedResponse(BAD_REQUEST);
        }

        userRegisterService.doLogin(session, identity, opPwd.get(), modeStr.charAt(0) == '0', true,
                ex.newPromise(String.class).addListener(f -> {
                    String userId = (String) f.getNow();
                    if (userId == null) {
                        ex.writeResponse(OK, failedCode("用户名或密码错误"), APPLICATION_JSON);
                        return;
                    }

                    Response resp = ex.okResponse(successCode(userId), APPLICATION_JSON);
                    ex.writeResponse(resp);
                })
        );

        return null;
    }


    @Post("/logout")
    public Response logout(final HttpExchange ex) {
        String sessionId = ex.request().getParameter(SESSION_ID);

        if (isNullOrEmpty(sessionId) || sessionId.length() > 128) {
            return ex.failedResponse(BAD_REQUEST);
        }

        userRegisterService.doLogout(sessionId);

        return ex.okResponse(successCode(EMPTY_STRING), APPLICATION_JSON);
    }


    @Post("/signUp")
    public Response signUp(final HttpExchange ex) {
        Request req = ex.request();

        String username = req.getParameter(USER_NAME_PARAM);
        String mail     = req.getParameter(MAIL_PARAM);
        String password = req.getParameter(PASSWORD_PARAM);

        if (anyNullOrEmpty(username, mail) || password == null ||
                username.length() > 64 || password.length() < 8 || password.length() > 16) {
            return ex.failedResponse(BAD_REQUEST);
        }

        PrivateKey privateKey = (PrivateKey) req.session().remove(RSA_PRIVATE_KEY);
        if (privateKey == null) {
            return ex.failedResponse(BAD_REQUEST);
        }

        Optional<String> opPwd;

        try {
            opPwd = rsaSecurityService.doDecode(password, privateKey);
        } catch (IOException e) {
            logger.error("RSA decode error.", e);
            return ex.failedResponse(INTERNAL_SERVER_ERROR);
        }

        if (!opPwd.isPresent()) {
            logger.warn("RSA decode wrong, from {}.", ex.channel().remoteAddress());
            return ex.failedResponse(BAD_REQUEST);
        }

        userRegisterService.doSignUp(username, mail, opPwd.get(), ex.newPromise(Boolean.class).addListener(f -> {
            Boolean isSuccess = (Boolean) f.getNow();
            if (isSuccess) {
                ex.writeResponse(OK, successCode("注册成功"), APPLICATION_JSON);
            } else {
                ex.writeResponse(OK, failedCode("注册失败"), APPLICATION_JSON);
            }
        }));

        return null;
    }


    @Get("/rsa")
    public Response rsaPublicKey(final HttpExchange ex) {
        Request req = ex.request();

        if (req.getUserId() != null) {
            return ex.okResponse();
        }

        RSAKeyPair keyPair = rsaSecurityService.doGenerateKeyPair();

        req.addToSession(RSA_PRIVATE_KEY, keyPair.getPrivateKey());

        return ex.okResponse(keyPair.getPublicKeyStr().getBytes(), TEXT_PLAIN);
    }
}
