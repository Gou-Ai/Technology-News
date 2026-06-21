package com.compuspulse.ui.home

import com.compuspulse.data.remote.model.Article
import com.compuspulse.data.remote.model.Banner

class HomeListItem private constructor(
    val type: Int,
    val banners: List<Banner>,
    val article: Article?,
    val footerStatus: Int
) {

    val stableId: Long
        get() {
            if (type == TYPE_BANNER) {
                return Long.MIN_VALUE
            }
            if (type == TYPE_FOOTER) {
                return Long.MAX_VALUE
            }
            return article?.getId()?.toLong() ?: RecyclerIds.EMPTY_ARTICLE_ID
        }

    fun isContentSame(other: HomeListItem?): Boolean {
        if (other == null || type != other.type) {
            return false
        }
        if (type == TYPE_BANNER) {
            return sameBannerList(banners, other.banners)
        }
        if (type == TYPE_FOOTER) {
            return footerStatus == other.footerStatus
        }
        if (article == null || other.article == null) {
            return article === other.article
        }
        return article.getId() == other.article.getId() &&
            article.getTitle() == other.article.getTitle() &&
            article.getSummary() == other.article.getSummary() &&
            article.getSource() == other.article.getSource() &&
            article.getSourceUrl() == other.article.getSourceUrl() &&
            article.getPublishTime() == other.article.getPublishTime() &&
            article.getImageUrl() == other.article.getImageUrl() &&
            article.getCategory() == other.article.getCategory() &&
            article.getCommentCount() == other.article.getCommentCount() &&
            article.isFavorite() == other.article.isFavorite()
    }

    private fun sameBannerList(oldList: List<Banner>?, newList: List<Banner>?): Boolean {
        if (oldList === newList) {
            return true
        }
        if (oldList == null || newList == null || oldList.size != newList.size) {
            return false
        }
        for (index in oldList.indices) {
            val oldBanner = oldList[index]
            val newBanner = newList[index]
            if (oldBanner.getId() != newBanner.getId() ||
                oldBanner.getTitle() != newBanner.getTitle() ||
                oldBanner.getImageUrl() != newBanner.getImageUrl() ||
                oldBanner.getTargetUrl() != newBanner.getTargetUrl()
            ) {
                return false
            }
        }
        return true
    }

    private object RecyclerIds {
        const val EMPTY_ARTICLE_ID = -2L
    }

    companion object {
        const val TYPE_BANNER = 0
        const val TYPE_ARTICLE = 1
        const val TYPE_FOOTER = 2

        @JvmStatic
        fun banner(banners: List<Banner>?): HomeListItem {
            return HomeListItem(TYPE_BANNER, banners ?: emptyList(), null, HomeListAdapter.STATUS_IDLE)
        }

        @JvmStatic
        fun article(article: Article): HomeListItem {
            return HomeListItem(TYPE_ARTICLE, emptyList(), article, HomeListAdapter.STATUS_IDLE)
        }

        @JvmStatic
        fun footer(footerStatus: Int): HomeListItem {
            return HomeListItem(TYPE_FOOTER, emptyList(), null, footerStatus)
        }
    }
}
