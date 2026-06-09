package com.compuspulse.data.remote.model;

import com.compuspulse.utils.HtmlUtils;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Article implements Serializable {
    private int id;
    private String title;
    private String summary;
    private String source;
    private String sourceUrl;
    private String publishTime;
    private String imageUrl;
    private String category;
    private int commentCount;
    private boolean favorite;

    @SerializedName("desc")
    private String apiDesc;
    @SerializedName("author")
    private String author;
    @SerializedName("shareUser")
    private String shareUser;
    @SerializedName("link")
    private String link;
    @SerializedName("niceDate")
    private String niceDate;
    @SerializedName("niceShareDate")
    private String niceShareDate;
    @SerializedName("envelopePic")
    private String envelopePic;
    @SerializedName("chapterName")
    private String chapterName;
    @SerializedName("superChapterName")
    private String superChapterName;
    @SerializedName("zan")
    private int zan;
    @SerializedName("collect")
    private boolean collect;

    public Article() {
    }

    public Article(int id, String title, String summary, String source,
                   String sourceUrl, String publishTime, String imageUrl,
                   String category, int commentCount, boolean favorite) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.source = source;
        this.sourceUrl = sourceUrl;
        this.publishTime = publishTime;
        this.imageUrl = imageUrl;
        this.category = category;
        this.commentCount = commentCount;
        this.favorite = favorite;
    }

    public Article normalize() {
        //去除Html标签
        title = HtmlUtils.plainText(title);
        if (isBlank(title)) {
            title = "暂无标题";
        }

        if (isBlank(summary)) {
            String normalizedDesc = HtmlUtils.plainText(apiDesc);
            if (!isBlank(normalizedDesc)) {
                summary = normalizedDesc;
            } else {
                String normalizedCategory = buildCategory();
                summary = isBlank(normalizedCategory) ? "暂无摘要" : "分类：" + normalizedCategory;
            }
        } else {
            summary = HtmlUtils.plainText(summary);
        }

        if (isBlank(source)) {
            source = !isBlank(author) ? author : (!isBlank(shareUser) ? shareUser : "WanAndroid");
        }

        if (isBlank(sourceUrl)) {
            sourceUrl = link;
        }

        if (isBlank(publishTime)) {
            publishTime = !isBlank(niceDate) ? niceDate : niceShareDate;
        }

        if (isBlank(imageUrl)) {
            imageUrl = envelopePic;
        }

        if (isBlank(category)) {
            category = buildCategory();
        }

        if (commentCount <= 0) {
            commentCount = Math.max(zan, 0);
        }

        favorite = favorite || collect;
        return this;
    }

    private String buildCategory() {
        if (!isBlank(superChapterName) && !isBlank(chapterName)) {
            return superChapterName + " / " + chapterName;
        }
        if (!isBlank(superChapterName)) {
            return superChapterName;
        }
        return chapterName;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title == null ? "" : title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary == null ? "" : summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getSource() {
        return source == null ? "" : source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceUrl() {
        return sourceUrl == null ? "" : sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getPublishTime() {
        return publishTime == null ? "" : publishTime;
    }

    public void setPublishTime(String publishTime) {
        this.publishTime = publishTime;
    }

    public String getImageUrl() {
        return imageUrl == null ? "" : imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategory() {
        return category == null ? "" : category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}
