package com.gzf.video.service;

import com.gzf.video.core.bean.Bean;
import com.gzf.video.core.bean.inject.Autowire;
import com.gzf.video.core.bean.inject.Component;
import com.gzf.video.core.dao.mongo.MongoCallback;
import com.gzf.video.core.http.HttpExchange;
import com.gzf.video.dao.collections._User;
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
    private _User _user;


    public void doGetUserInfo(HttpExchange ex, String identifier, char mode) {
        MongoCallback<Document> callback = result -> {
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

            _user._userInfo(userId, callback);
            break;
        case '1':
            _user._findUserInfoByUserName(identifier, callback);
            break;
        case '2':
            _user._findUserInfoByMail(identifier, callback);
            break;
        }
    }
}
