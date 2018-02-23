package com.gzf.video.pojo.entity;

import org.bson.codecs.pojo.annotations.BsonProperty;

import static com.gzf.video.dao.collections._Article.ArticleStruct.*;

public class Article extends ArticleInfo {

    // ... fields from ArticleInfo

    /**
     * Article content.
     */
    @BsonProperty(ARTICLE_CONTENT)
    private String articleContent;


    public Article() {}


    public String getArticleContent() {
        return articleContent;
    }

    public void setArticleContent(final String articleContent) {
        this.articleContent = articleContent;
    }
}
