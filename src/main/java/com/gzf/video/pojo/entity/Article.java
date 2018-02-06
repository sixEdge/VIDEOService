package com.gzf.video.pojo.entity;

import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.Date;
import java.util.List;

import static com.gzf.video.dao.collections._Article.ArticleStruct.*;

public class Article {

    /**
     * Article id.
     */
    @BsonProperty(ARTICLE_ID)
    private int articleId;

    /**
     * Article name.
     */
    @BsonProperty(ARTICLE_NAME)
    private String articleName;

    /**
     * Article type.
     */
    @BsonProperty(ARTICLE_TYPES)
    private List<String> articleTypes;

    /**
     * Author id.
     */
    @BsonProperty(AUTHOR_ID)
    private int authorId;

    /**
     * Author name.
     */
    @BsonProperty(AUTHOR_NAME)
    private String authorName;

    /**
     * Author url.
     */
    @BsonProperty(ARTICLE_URL)
    private String articleUrl;

    /**
     * Release time.
     */
    @BsonProperty(RELEASE_TIME)
    private Date releaseTime;

    /**
     * Hit times.
     */
    @BsonProperty(HIT_TIMES)
    private int hit;


    public Article() {}

    public Article(final int articleId,
                   final String articleName,
                   final List<String> articleTypes,
                   final int authorId,
                   final String authorName,
                   final String articleUrl,
                   final Date releaseTime,
                   final int hit) {
        this.articleId = articleId;
        this.articleName = articleName;
        this.articleTypes = articleTypes;
        this.authorId = authorId;
        this.authorName = authorName;
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

    public List<String> getArticleTypes() {
        return articleTypes;
    }

    public void setArticleTypes(final List<String> articleTypes) {
        this.articleTypes = articleTypes;
    }

    public int getAuthorId() {
        return authorId;
    }

    public void setAuthorId(final int authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(final String authorName) {
        this.authorName = authorName;
    }

    public String getArticleUrl() {
        return articleUrl;
    }

    public void setArticleUrl(final String articleUrl) {
        this.articleUrl = articleUrl;
    }

    public Date getReleaseTime() {
        return releaseTime;
    }

    public void setReleaseTime(final Date releaseTime) {
        this.releaseTime = releaseTime;
    }

    public int getHit() {
        return hit;
    }

    public void setHit(final int hit) {
        this.hit = hit;
    }
}
