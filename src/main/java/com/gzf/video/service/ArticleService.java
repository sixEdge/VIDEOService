package com.gzf.video.service;

import com.gzf.video.core.bean.Bean;
import com.gzf.video.core.bean.inject.Autowire;
import com.gzf.video.core.bean.inject.Component;
import com.gzf.video.core.http.HttpExchange;
import com.gzf.video.dao.collections._Article;
import com.mongodb.async.SingleResultCallback;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.gzf.video.pojo.component.CodeMessage.*;
import static com.gzf.video.util.StringUtil.EMPTY_STRING;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

@Bean
@Component
public class ArticleService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowire
    private _Article article;


    public void doFindArticle(HttpExchange ex, int articleId) {
        SingleResultCallback<Document> callback = (result, t) -> {
            if (result != null) {
                ex.writeResponse(OK, successJson(EMPTY_STRING, result.toJson()));
            } else {
                ex.writeResponse(OK, failedMsg("未找到文章"));
            }
        };

        article._findArticle(articleId, callback);
    }
}
