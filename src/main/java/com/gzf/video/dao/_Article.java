package com.gzf.video.dao;

import com.gzf.video.core.dao.MongoProvider;
import com.gzf.video.pojo.entry.Article;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.FindIterable;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.bson.conversions.Bson;

import static com.gzf.video.dao._Article.ArticleStruct.ARTICLE_ID;

/**
 * Article.
 */
public class _Article {

    private static final MongoProvider MONGO_PROVIDER = MongoProvider.getINSTANCE();


    public static final String ARTICLE_COLLECTION = "article";
    public interface ArticleStruct {
        String ARTICLE_ID       =   "aId";      // int
        String ARTICLE_NAME     =   "aname";    // string
        String ARTICLE_TYPE     =   "atype";    // array
        String USER_ID          =   "uId";      // int
        String USERNAME         =   "uname";    // string
        String ARTICLE_URL      =   "aurl";     // string
        String RELEASE_TIME     =   "rlstime";  // timestamp
    }


    private final MongoCollection<Article> articleCollection =
            MONGO_PROVIDER.getCollection(ARTICLE_COLLECTION, Article.class);


    public void _insertArticle(final Article entity, final SingleResultCallback<Void> callback) {
        articleCollection.insertOne(entity, callback);
    }


    public void _findArticle(final int articleId, final SingleResultCallback<Article> callback) {
        Document document = new Document()
                .append(ARTICLE_ID, articleId);
        articleCollection.find(document).first(callback);
    }


    public FindIterable<Article> _findArticles(final Bson filter) {
        return articleCollection.find(filter, Article.class);
    }


    public void _deleteArticle(final int articleId, final SingleResultCallback<DeleteResult> callback) {
        Document document = new Document()
                .append(ARTICLE_ID, articleId);
        articleCollection.deleteOne(document, callback);
    }
}
