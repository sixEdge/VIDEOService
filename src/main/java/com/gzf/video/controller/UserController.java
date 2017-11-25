package com.gzf.video.controller;

import com.gzf.video.core.annotation.Controller;
import com.gzf.video.core.annotation.action.Get;
import com.gzf.video.core.annotation.action.Post;
import com.gzf.video.service.RSASecurityService;
import com.gzf.video.service.UserRegisterService;
import com.gzf.video.util.ControllerFunctions;
import com.gzf.video.core.request.Request;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.Map;

import static com.gzf.video.core.session.SessionStorage.RSA_PRIVATE_KEY;
import static com.gzf.video.core.session.SessionStorage.SESSION_ID;
import static com.gzf.video.pojo.component.CodeMessage.failedState;
import static com.gzf.video.pojo.component.CodeMessage.successState;
import static com.gzf.video.service.RSASecurityService.RSAKeyPair;
import static com.gzf.video.util.StringUtil.EMPTY_STRING;
import static com.gzf.video.util.StringUtil.anyNullOrEmpty;
import static com.gzf.video.util.StringUtil.isNullOrEmpty;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;
import static io.netty.handler.codec.http.HttpHeaderValues.TEXT_PLAIN;
import static io.netty.handler.codec.http.HttpResponseStatus.*;

@Controller("/reg")
public class UserController extends ControllerFunctions {
    private final Logger logger = LoggerFactory.getLogger(getClass());


    private static final String IDENTITY_PARAM      = "id";
    private static final String USER_NAME_PARAM     = "uname";
    private static final String MAIL_PARAM          = "mail";
    private static final String PASSWORD_PARAM      = "pwd";

    // 0 user & name, 1 user & mail, 2 admin & mail
    private static final String LOGIN_MODE_PARAM    = "mode";


    private UserRegisterService userRegisterService = UserRegisterService.getINSTANCE();
    private RSASecurityService rsaSecurityService = RSASecurityService.getINSTANCE();


    @Post("/login")
    public FullHttpResponse login(final Request req) {
        if (req.getUserId() != null) {
            return okResponse(req.newByteBuf(successState("您已登录")), APPLICATION_JSON);
        }

        String identity = req.getParameter(IDENTITY_PARAM);
        String password = req.getParameter(PASSWORD_PARAM);
        int mode;

        if (isNullOrEmpty(identity) || password == null ||
            identity.length() > 64 || password.length() < 8 || password.length() > 16) {
            return failedResponse(BAD_REQUEST);
        }

        try {
            mode = Integer.parseInt(req.getParameter(LOGIN_MODE_PARAM), 10);
        } catch (Exception e) {
            return failedResponse(BAD_REQUEST);
        }

        PrivateKey privateKey = (PrivateKey) req.getFromSession(RSA_PRIVATE_KEY);
        if (privateKey == null) {
            return failedResponse(BAD_REQUEST);
        }

        try {
            password = rsaSecurityService.doDecode(password, privateKey);
        } catch ( NoSuchPaddingException
                | NoSuchAlgorithmException
                | BadPaddingException
                | InvalidKeyException
                | IllegalBlockSizeException e) {
            logger.warn("RSA decode wrong.", e);
            return failedResponse(BAD_REQUEST);
        } catch (IOException e) {
            logger.error("RSA decode error.", e);
            return failedResponse(INTERNAL_SERVER_ERROR);
        }

        userRegisterService.doLogin(identity, password, mode == 0, req.newPromise(String.class).addListener(f -> {
            String userId = (String) f.getNow();
            if (userId == null) {
                req.writeResponse(OK, failedState("用户名或密码错误"), APPLICATION_JSON);
                return;
            }
            FullHttpResponse resp =
                    okResponse(req.newByteBuf(successState("登录成功")), APPLICATION_JSON);
            req.addIdentification(resp.headers(), userId, true);
            req.writeResponse(resp);
            req.release();
        }));

        return null;
    }

    @Post("/logout")
    public FullHttpResponse logout(final Request req) {
        Map<String, String> params = req.parameters();
        String sessionId = params.get(SESSION_ID);

        if (isNullOrEmpty(sessionId) || sessionId.length() > 128) {
            return failedResponse(BAD_REQUEST);
        }

        userRegisterService.doLogout(sessionId);
        req.release();

        return okResponse(req.newByteBuf(successState(EMPTY_STRING)));
    }

    @Post("/signUp")
    public FullHttpResponse signUp(final Request req) {
        Map<String, String> params = req.parameters();

        String username = params.get(USER_NAME_PARAM);
        String mail = params.get(MAIL_PARAM);
        String password = params.get(PASSWORD_PARAM);

        if (anyNullOrEmpty(username, mail) || password == null ||
                username.length() > 64 || password.length() < 8 || password.length() > 16) {
            return failedResponse(BAD_REQUEST);
        }

        userRegisterService.doSignUp(username, mail, password, req.newPromise(Boolean.class).addListener(f -> {
            Boolean isSuccess = (Boolean) f.getNow();
            if (isSuccess) {
                req.writeResponse(OK, successState("注册成功"), APPLICATION_JSON);
            } else {
                req.writeResponse(OK, failedState("注册失败"), APPLICATION_JSON);
            }
            req.release();
        }));

        return null;
    }

    @Get("/rsa")
    public FullHttpResponse rsaPublicKey(final Request req) {
        RSAKeyPair keyPair = rsaSecurityService.doGenerateKeyPair();

        req.addToSession(RSA_PRIVATE_KEY, keyPair.getPrivateKey());
        req.writeResponse(OK, keyPair.getPublicKeyStr().getBytes(), TEXT_PLAIN);

        return null;
    }
}
