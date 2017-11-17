package com.gzf.video.controller;

import com.alibaba.fastjson.JSONObject;
import com.gzf.video.core.annotation.Controller;
import com.gzf.video.core.annotation.action.Post;
import com.gzf.video.service.UserRegistService;
import com.gzf.video.util.ControllerFunctions;
import com.gzf.video.core.controller.action.RequestWrapper;
import com.gzf.video.pojo.component.CodeMessage;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;

@Controller("/reg")
public class UserController extends ControllerFunctions {
    private final Logger logger = LoggerFactory.getLogger(getClass());


    private static final String IDENTITY_PARAM = "id";
    private static final String PASSWORD_PARAM = "pwd";

    // 0 user & name, 1 user & mail, 2 admin & mail
    private static final String LOGIN_MODE_PARAM    = "mode";


    private UserRegistService userRegistService = new UserRegistService();

    @Post("/login")
    public FullHttpResponse userLogin(final RequestWrapper rw) {
        rw.release();
        if (rw.getUserId() != null) {
            byte[] json = JSONObject.toJSONBytes(CodeMessage.successState("您已登录"));
            return okResponse(rw.newByteBuf(json), APPLICATION_JSON);
        }

        Map<String, List<String>> params = rw.requestParams();

        String id;
        String password;
        int mode;
        try {
            id = params.get(IDENTITY_PARAM).get(0);
            password = params.get(PASSWORD_PARAM).get(0);
            mode = Integer.parseInt(params.get(LOGIN_MODE_PARAM).get(0), 10);
        } catch (Exception e) {
            rw.writeResponse(failedResponse(BAD_REQUEST));
            return null;
        }


        userRegistService.doLogin(id, password, mode == 0, rw.newPromise(String.class).addListener(f -> {
            String userId = (String) f.getNow();
            if (userId == null) {
                rw.writeResponse(okResponse(rw.newByteBuf(CodeMessage.successState("用户名或密码错误"))));
                return;
            }
            FullHttpResponse resp =
                    okResponse(rw.newByteBuf(CodeMessage.successState("登录成功")), APPLICATION_JSON);
            rw.addIdentification(resp.headers(), userId, true);
            rw.writeResponse(resp);
        }));

        return null;
    }

    @Post("/logout")
    public FullHttpResponse logout(final RequestWrapper rw) {
        // TODO logout()
        return null;
    }

    @Post("/signUp")
    public FullHttpResponse signUp(final RequestWrapper rw) {
        // TODO signUp()
        return null;
    }
}
