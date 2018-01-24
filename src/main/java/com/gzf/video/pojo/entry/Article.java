package com.gzf.video.pojo.entry;

import org.bson.BsonTimestamp;

import java.util.List;

public class Article {

    /**
     * Article id.
     */
    private int articleId;

    /**
     * Article name.
     */
    private String articleName;

    /**
     * Article type.
     */
    private List<String> articleType;

    /**
     * Author id.
     */
    private int userId;

    /**
     * Author name.
     */
    private String userName;

    /**
     * Author url.
     */
    private String articleUrl;

    /**
     * Release time.
     */
    private BsonTimestamp releaseTime;

    /**
     * Hit times.
     */
    private int hit;


    public Article() {}

    public Article(final int articleId,
                   final String articleName,
                   final List<String> articleType,
                   final int userId,
                   final String userName,
                   final String articleUrl,
                   final BsonTimestamp releaseTime,
                   final int hit) {
        this.articleId = articleId;
        this.articleName = articleName;
        this.articleType = articleType;
        this.userId = userId;
        this.userName = userName;
        this.articleUrl = articleUrl;
        this.releaseTime = releaseTime;
        this.hit = hit;
    }


    public int getArticleId() {
        return articleId;
    }

    public void setArticleId(final int articleId) {
        this.articleId = articleId;
    }

    public String getArticleName() {
        return articleName;
    }

    public void setArticleName(final String articleName) {
        this.articleName = articleName;
    }

    public List<String> getArticleType() {
        return articleType;
    }

    public void setArticleType(final List<String> articleType) {
        this.articleType = articleType;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(final int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
    }

    public String getArticleUrl() {
        return articleUrl;
    }

    public void setArticleUrl(final String articleUrl) {
        this.articleUrl = articleUrl;
    }

    public BsonTimestamp getReleaseTime() {
        return releaseTime;
    }

    public void setReleaseTime(final BsonTimestamp releaseTime) {
        this.releaseTime = releaseTime;
    }

    public int getHit() {
        return hit;
    }

    public void setHit(final int hit) {
        this.hit = hit;
    }
}
