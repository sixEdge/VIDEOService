package com.gzf.video.service;

import com.gzf.video.core.bean.Bean;
import com.gzf.video.core.bean.inject.Autowire;
import com.gzf.video.core.bean.inject.Component;
import com.gzf.video.core.http.HttpExchange;
import com.gzf.video.dao.collections._Article;
import com.gzf.video.pojo.entity.Article;
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
import static com.gzf.video.util.StringUtil.isNotNullOrEmpty;
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


    /**
     * Filter articles by arguments.
     *
     * @param ex {@link HttpExchange}
     * @param articleName   article name
     * @param authorId      author id
     * @param authorName    author name
     * @param articleTypes  article types
     * @param releaseTimeFrom   [releaseTimeFrom, releaseTimeEnd]
     * @param releaseTimeEnd    [releaseTimeFrom, releaseTimeEnd]
     * @param offset    offset
     * @param size      size
     */
    public void doFindArticlesByFilter(HttpExchange ex,
                                       final String articleName,
                                       final int authorId,
                                       final String authorName,
                                       final List<String> articleTypes,
                                       Date releaseTimeFrom, Date releaseTimeEnd,
                                       int offset, int size) {

        Document filter = buildFilter( articleName
                                     , authorId
                                     , authorName
                                     , articleTypes
                                     , releaseTimeFrom
                                     , releaseTimeEnd);

        // max-return-article-number is 100
        size = size > 100 ? 100 : size;

        List<Article> array = new ArrayList<>(size);

        SingleResultCallback<Void> callback = (result, t) -> {
            if (t == null) {
                ex.writeResponse(OK, successJson("" + array.size(), toJsonString(array)));
            } else {
                logger.error("findArticlesByFilter, filter: " + filter, t);
                ex.writeResponse(INTERNAL_SERVER_ERROR);
            }
        };

        article._findArticles1(filter)
                .skip(offset).limit(size)
                .forEach(array::add, callback);
    }


    private static Document buildFilter(String articleName,
                                        int authorId, String authorName,
                                        List<String> articleTypes,
                                        Date releaseFrom, Date releaseEnd) {
        Document filter = new Document();

        if (isNotNullOrEmpty(articleName)) {
            articleNameFilter(filter, articleName);
        }

        if (authorId > 0) {
            authorIdFilter(filter, authorId);
        }

        if (isNotNullOrEmpty(authorName)) {
            authorNameFilter(filter, authorName);
        }

        if (!articleTypes.isEmpty()) {
            articleTypeFilter(filter, articleTypes);
        }

        if (releaseFrom != null && releaseEnd != null) {
            releaseTimeDomainFilter(filter, releaseFrom, releaseEnd);
        }

        return filter;
    }

    private static void articleNameFilter(Document document, String fuzzyArticleName) {
        // case insensitive fuzzy search
        Pattern pattern = Pattern.compile("^.*" + fuzzyArticleName + ".*$", Pattern.CASE_INSENSITIVE);
        document.append(ARTICLE_NAME, pattern);
    }

    private static void authorIdFilter(Document document, int authorId) {
        document.append(AUTHOR_ID, authorId);
    }

    private static void authorNameFilter(Document document, String fuzzyAuthorName) {
        Pattern pattern = Pattern.compile("^.*" + fuzzyAuthorName + ".*$", Pattern.CASE_INSENSITIVE);
        document.append(AUTHOR_NAME, pattern);
    }

    private static void articleTypeFilter(Document document, List<String> articleTypes) {
        document.append(ARTICLE_TYPE, new Document("$all", articleTypes));
    }

    private static void releaseTimeDomainFilter(Document document, Date from, Date end) {
        document.append(RELEASE_TIME, new Document("$gt", from).append("$lt", end));
    }
}
