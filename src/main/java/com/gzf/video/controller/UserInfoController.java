package com.gzf.video.controller;

import com.gzf.video.core.bean.inject.Autowire;
import com.gzf.video.core.controller.Controller;
import com.gzf.video.core.dispatcher.route.Route;
import com.gzf.video.core.http.HttpExchange;
import com.gzf.video.core.http.request.Request;
import com.gzf.video.core.http.response.Response;
import com.gzf.video.service.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.gzf.video.util.StringUtil.anyNullOrEmpty;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;

@Controller("/user")
public class UserInfoController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    // userId, username, mail
    private static final String IDENTIFIER_PARAM    = "id";

    // the identifier will be: 0: userId, 1: username, 2: mail
    private static final String SEARCH_MODE_PARAM   = "mode";


    @Autowire
    private UserInfoService userInfoService;


    @Route
    public Response getUserInfo(HttpExchange ex) {
        Request req = ex.request();
        String identifier  = req.getParameter(IDENTIFIER_PARAM);
        String modeStr     = req.getParameter(SEARCH_MODE_PARAM);

        if (anyNullOrEmpty(identifier, modeStr) || identifier.length() > 64) {
            return ex.failedResponse(BAD_REQUEST);
        }

        userInfoService.doGetUserInfo(ex, identifier, modeStr.charAt(0));

        return null;
    }
}
