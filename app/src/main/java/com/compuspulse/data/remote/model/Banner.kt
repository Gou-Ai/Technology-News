package com.compuspulse.data.remote.model

import com.google.gson.annotations.SerializedName

class Banner() {
    private var id: Int = 0
    private var title: String? = null

    @SerializedName("imagePath")
    private var imageUrl: String? = null

    @SerializedName("url")
    private var targetUrl: String? = null

    constructor(id: Int, title: String?, imageUrl: String?, targetUrl: String?) : this() {
        this.id = id
        this.title = title
        this.imageUrl = imageUrl
        this.targetUrl = targetUrl
    }

    fun getId(): Int = id

    fun getTitle(): String = title ?: ""

    fun getImageUrl(): String = imageUrl ?: ""

    fun getTargetUrl(): String = targetUrl ?: ""
}
