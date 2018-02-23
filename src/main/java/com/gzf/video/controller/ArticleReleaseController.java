package com.gzf.video.controller;

import com.gzf.video.core.bean.inject.Autowire;
import com.gzf.video.core.controller.Controller;
import com.gzf.video.core.dispatcher.route.Route;
import com.gzf.video.core.http.HttpExchange;
import com.gzf.video.core.http.request.Request;
import com.gzf.video.core.http.response.Response;
import com.gzf.video.pojo.entity.Article;
import com.gzf.video.service.ArticleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import static com.gzf.video.core.http.request.HttpMethod.POST;
import static com.gzf.video.dao.collections._User.UserStruct.USERNAME;
import static com.gzf.video.util.StringUtil.stringToList;

@Controller("/user")
public class ArticleReleaseController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String ARTICLE_NAME_PARAM    = "aname";
    private static final String ARTICLE_TYPES_PARAM   = "atypes";
    private static final String ARTICLE_CONTENT_PARAM = "content";


    @Autowire
    private ArticleService articleService;


    @Route(method = POST, url = "/aRls")
    public Response releaseArticle(HttpExchange ex) {
        Article article = new Article();
        article.setAuthorId(Integer.parseInt(ex.getUserId()));
        article.setAuthorName((String) ex.getFromSession(USERNAME));

        Request req = ex.request();
        article.setArticleName(req.getParameter(ARTICLE_NAME_PARAM));
        article.setArticleTypes(stringToList(req.getParameter(ARTICLE_TYPES_PARAM)));
        article.setReleaseTime(new Date());
        article.setArticleContent(req.getParameter(ARTICLE_CONTENT_PARAM));

        articleService.doReleaseArticle(ex, article);

        return null;
    }
}
