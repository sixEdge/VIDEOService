package com.gzf.video.controller;

import com.gzf.video.core.bean.inject.Autowire;
import com.gzf.video.core.controller.Controller;
import com.gzf.video.core.dispatcher.route.Route;
import com.gzf.video.core.http.HttpExchange;
import com.gzf.video.core.http.response.Response;
import com.gzf.video.service.UserRegisterService;
import com.gzf.video.core.http.request.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.gzf.video.core.http.request.HttpMethod.GET;
import static com.gzf.video.core.http.request.HttpMethod.POST;
import static com.gzf.video.pojo.component.CodeMessage.successMsg;
import static com.gzf.video.util.StringUtil.EMPTY_STRING;
import static com.gzf.video.util.StringUtil.anyNullOrEmpty;
import static io.netty.handler.codec.http.HttpHeaderValues.TEXT_PLAIN;
import static io.netty.handler.codec.http.HttpResponseStatus.*;

@Controller("/reg")
public class UserRegisterController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    // username, mail
    private static final String IDENTIFIER_PARAM    = "id";
    private static final String USER_NAME_PARAM     = "uname";
    private static final String MAIL_PARAM          = "mail";
    private static final String PASSWORD_PARAM      = "pwd";
    private static final String REMEMBER_ME         = "remb";

    // 0: user & name-login, 1: user & mail-login, 2: admin & mail-login
    private static final String LOGIN_MODE_PARAM    = "mode";


    @Autowire
    private UserRegisterService userRegisterService;


    @Route(method = POST, url = "/login")
    public Response login(HttpExchange ex) {
        // has login
        String preUserId;
        if ((preUserId = ex.getUserId()) != null) {
            return ex.okResponse(successMsg(preUserId));
        }

        Request req = ex.request();
        String identifier = req.getParameter(IDENTIFIER_PARAM);
        String password   = req.getParameter(PASSWORD_PARAM);
        String modeStr    = req.getParameter(LOGIN_MODE_PARAM);
        String rememberMe = req.getParameter(REMEMBER_ME);

        if (anyNullOrEmpty(identifier, password, modeStr) || identifier.length() > 64) {
            return ex.failedResponse(BAD_REQUEST);
        }

        userRegisterService.doLogin(ex, identifier, password, modeStr.charAt(0) == '0', true /* rememberMe.equals("true") */);

        return null;
    }


    @Route(method = POST, url = "/logout")
    public Response logout(HttpExchange ex) {
        userRegisterService.doLogout(ex.session());

        return ex.okResponse(successMsg(EMPTY_STRING));
    }


    @Route(method = POST, url = "/signUp")
    public Response signUp(HttpExchange ex) {
        Request req = ex.request();
        String username = req.getParameter(USER_NAME_PARAM);
        String mail     = req.getParameter(MAIL_PARAM);
        String password = req.getParameter(PASSWORD_PARAM);

        if (anyNullOrEmpty(username, mail) || password == null ||
                username.length() > 64 || password.length() < 8 || password.length() > 16) {
            return ex.failedResponse(BAD_REQUEST);
        }

        userRegisterService.doSignUp(ex, username, mail, password);

        return null;
    }


    @Route(method = GET, url = "/rsa")
    public Response rsaPublicKey(HttpExchange ex) {
        if (ex.getUserId() != null) {
            return ex.okResponse();
        }

        return ex.okResponse(userRegisterService.doGetRsaPublicKey(ex), TEXT_PLAIN);
    }
}
