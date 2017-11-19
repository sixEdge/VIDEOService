package com.gzf.video.controller;

import com.alibaba.fastjson.JSONObject;
import com.gzf.video.core.annotation.Controller;
import com.gzf.video.core.annotation.action.Post;
import com.gzf.video.service.UserRegistService;
import com.gzf.video.util.ControllerFunctions;
import com.gzf.video.core.controller.action.RequestWrapper;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static com.gzf.video.core.session.SessionStorage.SESSION_ID;
import static com.gzf.video.pojo.component.CodeMessage.failedState;
import static com.gzf.video.pojo.component.CodeMessage.successState;
import static com.gzf.video.util.StringUtil.EMPTY_STRING;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;
import static io.netty.handler.codec.http.HttpResponseStatus.*;

@Controller("/reg")
public class UserController extends ControllerFunctions {
    private final Logger logger = LoggerFactory.getLogger(getClass());


    private static final String IDENTITY_PARAM = "id";
    private static final String USER_NAME_PARAM = "uname";
    private static final String MAIL_PARAM = "mail";
    private static final String PASSWORD_PARAM = "pwd";

    // 0 user & name, 1 user & mail, 2 admin & mail
    private static final String LOGIN_MODE_PARAM    = "mode";


    private UserRegistService userRegistService = new UserRegistService();

    @Post("/login")
    public FullHttpResponse userLogin(final RequestWrapper rw) {
        if (rw.getUserId() != null) {
            byte[] json = JSONObject.toJSONBytes(successState("您已登录"));
            return okResponse(rw.newByteBuf(json), APPLICATION_JSON);
        }

        Map<String, List<String>> params = rw.requestParams();

        String identity;
        String password;
        int mode;
        try {
            identity = params.get(IDENTITY_PARAM).get(0);
            password = params.get(PASSWORD_PARAM).get(0);
            mode = Integer.parseInt(params.get(LOGIN_MODE_PARAM).get(0), 10);
        } catch (Exception e) {
            rw.writeResponse(failedResponse(BAD_REQUEST));
            return null;
        }

        if (identity.isEmpty() || password.isEmpty() || identity.length() > 64 && password.length() > 16) {
            rw.writeResponse(failedResponse(BAD_REQUEST));
            return null;
        }


        userRegistService.doLogin(identity, password, mode == 0, rw.newPromise(String.class).addListener(f -> {
            String userId = (String) f.getNow();
            if (userId == null) {
                rw.writeResponse(OK, failedState("用户名或密码错误"), APPLICATION_JSON);
                return;
            }
            FullHttpResponse resp =
                    okResponse(rw.newByteBuf(successState("登录成功")), APPLICATION_JSON);
            rw.addIdentification(resp.headers(), userId, true);
            rw.writeResponse(resp);
            rw.release();
        }));

        return null;
    }

    @Post("/logout")
    public FullHttpResponse logout(final RequestWrapper rw) {
        Map<String, List<String>> params = rw.requestParams();

        String sessionId = params.get(SESSION_ID).get(0);
        if (sessionId.isEmpty() || sessionId.length() > 128) {
            rw.writeResponse(failedResponse(BAD_REQUEST));
            return null;
        }

        userRegistService.doLogout(sessionId);

        rw.release();
        return okResponse(rw.newByteBuf(successState(EMPTY_STRING)));
    }

    @Post("/signUp")
    public FullHttpResponse signUp(final RequestWrapper rw) {
        Map<String, List<String>> params = rw.requestParams();

        String username = params.get(USER_NAME_PARAM).get(0);
        String mail = params.get(MAIL_PARAM).get(0);
        String password = params.get(PASSWORD_PARAM).get(0);

        if (username.isEmpty() || username.length() > 64 || mail.isEmpty() || password.isEmpty()) {
            rw.writeResponse(failedResponse(BAD_REQUEST));
            return null;
        }

        userRegistService.doSignUp(username, mail, password, rw.newPromise(Boolean.class).addListener(f -> {
            Boolean isSuccess = (Boolean) f.getNow();
            if (isSuccess) {
                rw.writeResponse(OK, successState("注册成功"), APPLICATION_JSON);
            } else {
                rw.writeResponse(OK, failedState("注册失败"), APPLICATION_JSON);
            }
            rw.release();
        }));
        return null;
    }
}
