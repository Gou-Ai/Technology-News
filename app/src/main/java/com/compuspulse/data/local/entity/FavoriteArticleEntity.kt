package com.compuspulse.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "favorite_articles",
    primaryKeys = ["username", "articleId"]
)
data class FavoriteArticleEntity(
    val username: String,
    var articleId: Int,
    var title: String?,
    var summary: String?,
    var source: String?,
    var sourceUrl: String?,
    var publishTime: String?,
    var imageUrl: String?,
    var commentCount: Int,
    var collectedAt: Long
)
