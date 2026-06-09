package com.compuspulse.data.remote.model;

import com.google.gson.annotations.SerializedName;

public class Banner {
    private int id;
    private String title;
    @SerializedName("imagePath")
    private String imageUrl;
    @SerializedName("url")
    private String targetUrl;

    public Banner() {
    }

    public Banner(int id, String title, String imageUrl, String targetUrl) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.targetUrl = targetUrl;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title == null ? "" : title;
    }

    public String getImageUrl() {
        return imageUrl == null ? "" : imageUrl;
    }

    public String getTargetUrl() {
        return targetUrl == null ? "" : targetUrl;
    }
}
