package com.gzf.video.service;

import com.gzf.video.core.bean.Bean;
import com.gzf.video.core.bean.inject.Autowire;
import com.gzf.video.core.bean.inject.Component;
import com.gzf.video.core.http.HttpExchange;
import com.gzf.video.dao.collections._Article;
import com.gzf.video.pojo.entry.Article;
import com.mongodb.async.SingleResultCallback;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import static com.gzf.video.dao.collections._Article.ArticleStruct.*;
import static com.gzf.video.pojo.component.CodeMessage.*;
import static com.gzf.video.util.StringUtil.EMPTY_STRING;
import static com.gzf.video.util.StringUtil.toJsonString;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

@Bean
@Component
public class ArticleService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowire
    private _Article article;


    public void doGetArticle(HttpExchange ex, int articleId) {
        SingleResultCallback<Document> callback = (result, t) -> {
            if (result != null) {
                ex.writeResponse(OK, successJson(EMPTY_STRING, result.toJson()));
            } else {
                ex.writeResponse(OK, failedMsg("未找到文章"));
            }
        };

        article._findArticle(articleId, callback);
    }


    public void doFindArticlesByFilter(HttpExchange ex, Document filter, int offset, int size) {
        List<Article> array = new ArrayList<>(size);

        SingleResultCallback<Void> callback = (result, t) -> {
            if (t == null) {
                ex.writeResponse(OK, successJson(EMPTY_STRING, toJsonString(array)));
            } else {
                logger.error("findArticlesByFilter, filter: " + filter, t);
                ex.writeResponse(INTERNAL_SERVER_ERROR);
            }
        };

        article._findArticles1(filter).skip(offset).limit(size)
//              .maxTime(5L, TimeUnit.SECONDS)
                .forEach(array::add, callback);
    }

    public static Document articleNameFilter(Document document, String fuzzyArticleName) {
        // case insensitive fuzzy search
        Pattern pattern = Pattern.compile("^.*" + fuzzyArticleName + ".*$", Pattern.CASE_INSENSITIVE);
        return document.append(ARTICLE_NAME, pattern);
    }

    public static Document authorIdFilter(Document document, int authorId) {
        return document.append(AUTHOR_ID, authorId);
    }

    public static Document authorNameFilter(Document document, String fuzzyAuthorName) {
        // case insensitive fuzzy search
        Pattern pattern = Pattern.compile("^.*" + fuzzyAuthorName + ".*$", Pattern.CASE_INSENSITIVE);
        return document.append(AUTHOR_NAME, pattern);
    }

    public static Document articleTypeFilter(Document document, List<String> articleTypes) {
        return document.append(ARTICLE_TYPE, new Document("$all", articleTypes));
    }

    public static Document releaseTimeDomainFilter(Document document, Date from, Date end) {
        return document.append(RELEASE_TIME, new Document("$gt", from).append("$lt", end));
    }
}
