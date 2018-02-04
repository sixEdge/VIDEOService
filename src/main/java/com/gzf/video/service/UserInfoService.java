package com.gzf.video.service;

import com.gzf.video.core.bean.Bean;
import com.gzf.video.core.bean.inject.Autowire;
import com.gzf.video.core.bean.inject.Component;
import com.gzf.video.core.http.HttpExchange;
import com.gzf.video.dao.collections._User;
import com.mongodb.async.SingleResultCallback;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.gzf.video.pojo.component.CodeMessage.failedMsg;
import static com.gzf.video.pojo.component.CodeMessage.successJson;
import static com.gzf.video.util.StringUtil.EMPTY_STRING;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

@Bean
@Component
public class UserInfoService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowire
    private _User user;


    public void doGetUserInfo(HttpExchange ex, String identifier, char mode) {
        SingleResultCallback<Document> callback = (result, t) -> {
            if (result != null) {
                ex.writeResponse(OK, successJson(EMPTY_STRING, result.toJson()));
            } else {
                ex.writeResponse(OK, failedMsg("找不到该用户"));
            }
        };

        switch (mode) {
        case '0':
            int userId;
            try {
                userId = Integer.parseInt(identifier);
            } catch (NumberFormatException e) {
                logger.warn("UserInfoService#doGetUserInfo", e);
                ex.writeResponse(BAD_REQUEST);
                return;
            }

            user._userInfo(userId, callback);
            break;
        case '1':
            user._findUserInfoByUserName(identifier, callback);
            break;
        case '2':
            user._findUserInfoByMail(identifier, callback);
            break;
        }
    }
}
