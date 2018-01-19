package com.gzf.video.controller;

import com.gzf.video.core.bean.inject.Autowire;
import com.gzf.video.core.controller.Controller;
import com.gzf.video.core.controller.action.method.Get;
import com.gzf.video.core.controller.action.method.Post;
import com.gzf.video.core.http.HttpExchange;
import com.gzf.video.core.http.response.Response;
import com.gzf.video.service.UserRegisterService;
import com.gzf.video.core.http.request.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.gzf.video.pojo.component.CodeMessage.successCode;
import static com.gzf.video.util.StringUtil.EMPTY_STRING;
import static com.gzf.video.util.StringUtil.anyNullOrEmpty;
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


    @Autowire
    private UserRegisterService userRegisterService;


    @Post("/login")
    public Response login(HttpExchange ex) {
        // has login
        String preUserId;
        if ((preUserId = ex.getUserId()) != null) {
            return ex.okResponse(successCode(preUserId));
        }

        Request req = ex.request();
        String identity = req.getParameter(IDENTITY_PARAM);
        String password = req.getParameter(PASSWORD_PARAM);
        String modeStr  = req.getParameter(LOGIN_MODE_PARAM);

        if (anyNullOrEmpty(identity, modeStr, password) || identity.length() > 64) {
            return ex.failedResponse(BAD_REQUEST);
        }

        userRegisterService.doLogin(ex, identity, password, modeStr.charAt(0) == '0', true);

        return null;
    }


    @Post("/logout")
    public Response logout(HttpExchange ex) {
        userRegisterService.doLogout(ex.session());

        return ex.okResponse(successCode(EMPTY_STRING));
    }


    @Post("/signUp")
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


    @Get("/rsa")
    public Response rsaPublicKey(HttpExchange ex) {
        if (ex.getUserId() != null) {
            return ex.okResponse();
        }

        byte[] publicKey = userRegisterService.doGetRsaPublicKey(ex);

        return ex.okResponse(publicKey, TEXT_PLAIN);
    }
}
