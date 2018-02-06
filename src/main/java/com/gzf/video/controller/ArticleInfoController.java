package com.gzf.video.controller;

import com.gzf.video.core.bean.inject.Autowire;
import com.gzf.video.core.controller.Controller;
import com.gzf.video.core.dispatcher.route.Route;
import com.gzf.video.core.http.HttpExchange;
import com.gzf.video.core.http.request.Request;
import com.gzf.video.core.http.response.Response;
import com.gzf.video.service.ArticleService;
import org.apache.logging.log4j.core.util.datetime.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

import static com.gzf.video.core.http.request.HttpMethod.GET;
import static com.gzf.video.util.StringUtil.isNullOrEmpty;
import static com.gzf.video.util.StringUtil.stringToList;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;

@Controller("/article")
public class ArticleInfoController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String ARTICLE_ID_PARAM    = "aid";
    private static final String ARTICLE_NAME_PARAM  = "aname";
    private static final String AUTHOR_ID_PARAM     = "uid";
    private static final String AUTHOR_NAME_PARAM   = "uname";
    private static final String ARTICLE_TYPES_PARAM = "atypes";
    private static final String RELEASE_TIME_PARAM  = "rlstime";
    private static final String RELEASE_TIME_FROM_PARAM = "rlstimeFrom";
    private static final String RELEASE_TIME_END_PARAM  = "rlstimeEnd";

    private static final String DOMAIN_OFFSET   = "offset";
    private static final String DOMAIN_SIZE     = "size";


    private static final FastDateFormat DATE_FMT = FastDateFormat.getInstance("dd/MM/yy");


    @Autowire
    private ArticleService articleService;


    @Route
    public Response getArticleInfo(HttpExchange ex) {
        Request req = ex.request();
        int articleId;
        try {
            articleId = Integer.parseInt(req.getParameter(ARTICLE_ID_PARAM));
        } catch (NumberFormatException e) {
            logger.warn("ArticleInfoController#getArticleInfo", e);
            return ex.failedResponse(BAD_REQUEST);
        }

        articleService.doGetArticle(ex, articleId);

        return null;
    }


    /**
     * Filter articles by request parameters.<br />
     * 根据 {@code 文章名、作者 id 、作者名、文章类型、文章发布时间范围} 筛选文章。<br />
     * 从满足条件的文章中选取 [offset, offset + size] 区间的文章返回。
     */
    @Route(method = GET, url = "/filter")
    public Response findArticlesInfoList(HttpExchange ex) {
        String articleName;
        int authorId; String authorName;
        List<String> articleTypes;
        Date releaseTimeFrom, releaseTimeEnd;

        int offset; int size;

        // temp variable
        String authorIdStr;
        String articleTypesStr;
        String releaseTimeFromStr, releaseTimeEndStr;

        Request req = ex.request();
        articleName = req.getParameter(ARTICLE_NAME_PARAM);
        authorIdStr = req.getParameter(AUTHOR_ID_PARAM);
        authorName  = req.getParameter(AUTHOR_NAME_PARAM);
        articleTypesStr = req.getParameter(ARTICLE_TYPES_PARAM);
        releaseTimeFromStr = req.getParameter(RELEASE_TIME_FROM_PARAM);
        releaseTimeEndStr  = req.getParameter(RELEASE_TIME_END_PARAM);

        try {
            authorId     = isNullOrEmpty(authorIdStr) ? -1 : Integer.parseInt(authorIdStr);
            articleTypes = stringToList(articleTypesStr);
            releaseTimeFrom = DATE_FMT.parse(releaseTimeFromStr);
            releaseTimeEnd  = DATE_FMT.parse(releaseTimeEndStr);
            offset  = Integer.parseInt(req.getParameter(DOMAIN_OFFSET));
            size    = Integer.parseInt(req.getParameter(DOMAIN_SIZE));
        } catch (Exception e) {
            logger.warn("ArticleInfoController#findArticlesInfo", e);
            return ex.failedResponse(BAD_REQUEST);
        }

        articleService.doFindArticlesByFilter( ex
                                             , articleName
                                             , authorId
                                             , authorName
                                             , articleTypes
                                             , releaseTimeFrom
                                             , releaseTimeEnd
                                             , offset, size);

        return null;
    }
}
