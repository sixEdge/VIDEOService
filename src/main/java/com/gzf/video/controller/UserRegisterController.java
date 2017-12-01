package com.gzf.video.controller;

import com.gzf.video.core.annotation.Controller;
import com.gzf.video.core.annotation.action.Get;
import com.gzf.video.core.annotation.action.Post;
import com.gzf.video.core.http.response.Response;
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
    public Response login(final Request req) {

        // has login
        String preUserId;
        if ((preUserId = req.getUserId()) != null) {
            return req.okResponse(req.newByteBuf(successCode(preUserId)), APPLICATION_JSON);
        }

        String identity = req.getParameter(IDENTITY_PARAM);
        String password = req.getParameter(PASSWORD_PARAM);
        String modeStr  = req.getParameter(LOGIN_MODE_PARAM);

        if (anyNullOrEmpty(identity, modeStr, password) || identity.length() > 64) {
            return req.failedResponse(BAD_REQUEST);
        }


        // get rsa private key
        PrivateKey privateKey = (PrivateKey) req.session().remove(RSA_PRIVATE_KEY);
        if (privateKey == null) {
            return req.failedResponse(BAD_REQUEST);
        }

        Optional<String> opPwd;

        // rsa decode
        try {
            opPwd = rsaSecurityService.doDecode(password, privateKey);
        } catch (IOException e) {
            logger.error("RSA decode error.", e);
            return req.failedResponse(INTERNAL_SERVER_ERROR);
        }

        if (!opPwd.isPresent()) {
            logger.warn("no RSA private key found, for client {}, user {}.",
                    req.channel().remoteAddress(),
                    identity);
            return req.failedResponse(BAD_REQUEST);
        }

        userRegisterService.doLogin(identity, opPwd.get(), modeStr.charAt(0) == '0', req.newPromise(String.class).addListener(f -> {
            String userId = (String) f.getNow();
            if (userId == null) {
                req.writeResponse(OK, failedCode("用户名或密码错误"), APPLICATION_JSON);
                return;
            }

            Response resp = req.okResponse(successCode(userId), APPLICATION_JSON);
            req.addIdentification(userId, true);
            req.writeResponse(resp);
            req.release();
        }));

        return null;
    }


    @Post("/logout")
    public Response logout(final Request req) {
        String sessionId = req.getParameter(SESSION_ID);

        if (isNullOrEmpty(sessionId) || sessionId.length() > 128) {
            return req.failedResponse(BAD_REQUEST);
        }

        userRegisterService.doLogout(sessionId);
        req.release();

        return req.okResponse(successCode(EMPTY_STRING), APPLICATION_JSON);
    }


    @Post("/signUp")
    public Response signUp(final Request req) {
        String username = req.getParameter(USER_NAME_PARAM);
        String mail     = req.getParameter(MAIL_PARAM);
        String password = req.getParameter(PASSWORD_PARAM);

        if (anyNullOrEmpty(username, mail) || password == null ||
                username.length() > 64 || password.length() < 8 || password.length() > 16) {
            return req.failedResponse(BAD_REQUEST);
        }

        PrivateKey privateKey = (PrivateKey) req.session().remove(RSA_PRIVATE_KEY);
        if (privateKey == null) {
            return req.failedResponse(BAD_REQUEST);
        }

        Optional<String> opPwd;

        try {
            opPwd = rsaSecurityService.doDecode(password, privateKey);
        } catch (IOException e) {
            logger.error("RSA decode error.", e);
            return req.failedResponse(INTERNAL_SERVER_ERROR);
        }

        if (!opPwd.isPresent()) {
            logger.warn("RSA decode wrong, from {}.", req.channel().remoteAddress());
            return req.failedResponse(BAD_REQUEST);
        }

        userRegisterService.doSignUp(username, mail, opPwd.get(), req.newPromise(Boolean.class).addListener(f -> {
            Boolean isSuccess = (Boolean) f.getNow();
            if (isSuccess) {
                req.writeResponse(OK, successCode("注册成功"), APPLICATION_JSON);
            } else {
                req.writeResponse(OK, failedCode("注册失败"), APPLICATION_JSON);
            }
            req.release();
        }));

        return null;
    }


    @Get("/rsa")
    public Response rsaPublicKey(final Request req) {
        RSAKeyPair keyPair = rsaSecurityService.doGenerateKeyPair();

        req.addToSession(RSA_PRIVATE_KEY, keyPair.getPrivateKey());
        req.writeResponse(OK, keyPair.getPublicKeyStr().getBytes(), TEXT_PLAIN);

        return null;
    }
}
