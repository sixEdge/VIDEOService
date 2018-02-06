package com.gzf.video.dao.collections;

import com.gzf.video.core.bean.Bean;
import com.gzf.video.pojo.entity.Article;
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
    }

    private static final MongoCollection<Document> articleCollection = getCollection(COLLECTION);
    private static final MongoCollection<Article> articleCollection1 = getCollection(COLLECTION, Article.class);


    public void _insertArticle(Article entity, SingleResultCallback<Void> callback) {
        articleCollection1.insertOne(entity, callback);
    }


    /**
     * Find by unique aid.
     */
    public void _findArticle(int articleId, SingleResultCallback<Document> callback) {
        Document document = new Document()
                .append(ARTICLE_ID, articleId);
        articleCollection.find(document).first(callback);
    }


    public FindIterable<Document> _findArticles(Bson filter) {
        return articleCollection.find(filter);
    }


    public FindIterable<Article> _findArticles1(Bson filter) {
        return articleCollection1.find(filter);
    }


    public void _deleteArticle(int articleId, SingleResultCallback<DeleteResult> callback) {
        Document document = new Document()
                .append(ARTICLE_ID, articleId);
        articleCollection.deleteOne(document, callback);
    }
}
