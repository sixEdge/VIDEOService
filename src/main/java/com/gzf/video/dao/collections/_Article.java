package com.gzf.video.dao.collections;

import com.gzf.video.core.bean.Bean;
import com.gzf.video.pojo.entity.Article;
import com.gzf.video.pojo.entity.ArticleInfo;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.FindIterable;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.bson.conversions.Bson;

import static com.gzf.video.dao.collections._Article.ArticleStruct.ARTICLE_ID;

/**
 * Article info.
 */
@Bean
public class _Article extends BaseCollection {

    private static final String COLLECTION = "article";
    public interface ArticleStruct {
        String ARTICLE_ID       =   "aId";      // int
        String ARTICLE_NAME     =   "aname";    // string
        String ARTICLE_TYPES    =   "atypes";   // [string]
        String AUTHOR_ID        =   "uId";      // int
        String AUTHOR_NAME      =   "uname";    // string
        String ARTICLE_URL      =   "aurl";     // string
        String RELEASE_TIME     =   "rlstime";  // date
        String HIT_TIMES        =   "hit";      // int
        String ARTICLE_CONTENT  =   "content";  // string
    }

    private static final MongoCollection<Document> articleColl = getCollection(COLLECTION);
    private static final MongoCollection<Article> articleColl1 = getCollection(COLLECTION, Article.class);
    private static final MongoCollection<ArticleInfo> articleInfoColl = getCollection(COLLECTION, ArticleInfo.class);


    public void _insertArticle(Article entity, SingleResultCallback<Void> callback) {
        articleColl1.insertOne(entity, callback);
    }


    public void _findArticle(Bson filter, SingleResultCallback<Article> callback) {
        articleColl1.find(filter).first(callback);
    }


    public void _deleteArticle(int articleId, SingleResultCallback<DeleteResult> callback) {
        Document document = new Document()
                .append(ARTICLE_ID, articleId);
        articleColl.deleteOne(document, callback);
    }


    /**
     * Find article info by unique aid.
     */
    public void _findArticleInfo(int articleId, SingleResultCallback<ArticleInfo> callback) {
        Document document = new Document()
                .append(ARTICLE_ID, articleId);
        articleInfoColl.find(document).first(callback);
    }


    public FindIterable<ArticleInfo> _findArticlesInfo(Bson filter) {
        return articleInfoColl.find(filter);
    }
}
