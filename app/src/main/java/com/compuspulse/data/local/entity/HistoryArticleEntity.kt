package com.compuspulse.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "history_articles",
    primaryKeys = ["username", "articleId"]
)
data class HistoryArticleEntity(
    val username: String,
    var articleId: Int,
    var title: String?,
    var summary: String?,
    var source: String?,
    var sourceUrl: String?,
    var publishTime: String?,
    var imageUrl: String?,
    var commentCount: Int,
    var viewedAt: Long
)
