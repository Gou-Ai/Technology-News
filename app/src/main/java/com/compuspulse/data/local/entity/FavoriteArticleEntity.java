package com.compuspulse.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(
        tableName = "favorite_articles",
        primaryKeys = {"username", "articleId"}
)
public class FavoriteArticleEntity {

    @NonNull
    private String username;

    private int articleId;
    private String title;
    private String summary;
    private String source;
    private String sourceUrl;
    private String publishTime;
    private String imageUrl;
    private int commentCount;
    private long collectedAt;

    public FavoriteArticleEntity(@NonNull String username, int articleId, String title, String summary,
                                 String source, String sourceUrl, String publishTime,
                                 String imageUrl, int commentCount, long collectedAt) {
        this.username = username;
        this.articleId = articleId;
        this.title = title;
        this.summary = summary;
        this.source = source;
        this.sourceUrl = sourceUrl;
        this.publishTime = publishTime;
        this.imageUrl = imageUrl;
        this.commentCount = commentCount;
        this.collectedAt = collectedAt;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    public void setUsername(@NonNull String username) {
        this.username = username;
    }

    public int getArticleId() {
        return articleId;
    }

    public void setArticleId(int articleId) {
        this.articleId = articleId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(String publishTime) {
        this.publishTime = publishTime;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public long getCollectedAt() {
        return collectedAt;
    }

    public void setCollectedAt(long collectedAt) {
        this.collectedAt = collectedAt;
    }
}