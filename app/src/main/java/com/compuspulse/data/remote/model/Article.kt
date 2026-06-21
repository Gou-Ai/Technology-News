package com.compuspulse.data.remote.model

import com.compuspulse.utils.HtmlUtils
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import kotlin.math.max

class Article() : Serializable {
    private var id: Int = 0
    private var title: String? = null
    private var summary: String? = null
    private var source: String? = null
    private var sourceUrl: String? = null
    private var publishTime: String? = null
    private var imageUrl: String? = null
    private var category: String? = null
    private var commentCount: Int = 0
    private var favorite: Boolean = false

    @SerializedName("originId")
    private var originId: Int = 0

    @SerializedName("desc")
    private var apiDesc: String? = null

    @SerializedName("author")
    private var author: String? = null

    @SerializedName("shareUser")
    private var shareUser: String? = null

    @SerializedName("link")
    private var link: String? = null

    @SerializedName("niceDate")
    private var niceDate: String? = null

    @SerializedName("niceShareDate")
    private var niceShareDate: String? = null

    @SerializedName("envelopePic")
    private var envelopePic: String? = null

    @SerializedName("chapterName")
    private var chapterName: String? = null

    @SerializedName("superChapterName")
    private var superChapterName: String? = null

    @SerializedName("zan")
    private var zan: Int = 0

    @SerializedName("collect")
    private var collect: Boolean = false

    constructor(
        id: Int,
        title: String?,
        summary: String?,
        source: String?,
        sourceUrl: String?,
        publishTime: String?,
        imageUrl: String?,
        category: String?,
        commentCount: Int,
        favorite: Boolean
    ) : this() {
        this.id = id
        this.title = title
        this.summary = summary
        this.source = source
        this.sourceUrl = sourceUrl
        this.publishTime = publishTime
        this.imageUrl = imageUrl
        this.category = category
        this.commentCount = commentCount
        this.favorite = favorite
    }

    fun normalize(): Article {
        //去除Html标签
        title = HtmlUtils.plainText(title)
        if (isBlank(title)) {
            title = "暂无标题"
        }

        if (isBlank(summary)) {
            val normalizedDesc = HtmlUtils.plainText(apiDesc)
            summary = if (!isBlank(normalizedDesc)) {
                normalizedDesc
            } else {
                val normalizedCategory = buildCategory()
                if (isBlank(normalizedCategory)) "暂无摘要" else "分类：$normalizedCategory"
            }
        } else {
            summary = HtmlUtils.plainText(summary)
        }

        if (isBlank(source)) {
            source = if (!isBlank(author)) author else if (!isBlank(shareUser)) shareUser else "WanAndroid"
        }

        if (isBlank(sourceUrl)) {
            sourceUrl = link
        }

        if (isBlank(publishTime)) {
            publishTime = if (!isBlank(niceDate)) niceDate else niceShareDate
        }

        if (isBlank(imageUrl)) {
            imageUrl = envelopePic
        }

        if (isBlank(category)) {
            category = buildCategory()
        }

        if (commentCount <= 0) {
            commentCount = max(zan, 0)
        }

        favorite = favorite || collect
        return this
    }

    private fun buildCategory(): String? {
        if (!isBlank(superChapterName) && !isBlank(chapterName)) {
            return "$superChapterName / $chapterName"
        }
        if (!isBlank(superChapterName)) {
            return superChapterName
        }
        return chapterName
    }

    private fun isBlank(value: String?): Boolean = value == null || value.trim().isEmpty()

    fun getId(): Int = id

    fun setId(id: Int) {
        this.id = id
    }

    fun getTitle(): String = title ?: ""

    fun setTitle(title: String?) {
        this.title = title
    }

    fun getSummary(): String = summary ?: ""

    fun setSummary(summary: String?) {
        this.summary = summary
    }

    fun getSource(): String = source ?: ""

    fun setSource(source: String?) {
        this.source = source
    }

    fun getSourceUrl(): String = sourceUrl ?: ""

    fun setSourceUrl(sourceUrl: String?) {
        this.sourceUrl = sourceUrl
    }

    fun getPublishTime(): String = publishTime ?: ""

    fun setPublishTime(publishTime: String?) {
        this.publishTime = publishTime
    }

    fun getImageUrl(): String = imageUrl ?: ""

    fun setImageUrl(imageUrl: String?) {
        this.imageUrl = imageUrl
    }

    fun getCategory(): String = category ?: ""

    fun setCategory(category: String?) {
        this.category = category
    }

    fun getCommentCount(): Int = commentCount

    fun setCommentCount(commentCount: Int) {
        this.commentCount = commentCount
    }

    fun isFavorite(): Boolean = favorite

    fun setFavorite(favorite: Boolean) {
        this.favorite = favorite
    }

    fun getOriginId(): Int = originId

    fun getServerArticleId(): Int = if (originId > 0) originId else id
}
