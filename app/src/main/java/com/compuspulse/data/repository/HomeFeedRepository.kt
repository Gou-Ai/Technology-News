package com.compuspulse.data.repository

import com.compuspulse.data.remote.model.ApiResponse
import com.compuspulse.data.remote.model.Article
import com.compuspulse.data.remote.model.Banner
import com.compuspulse.data.remote.model.PageResponse
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class HomeFeedRepository : BaseRemoteRepository() {

    suspend fun fetchBannerList(): Result<List<Banner>> = runCatching {
        val response: ApiResponse<List<Banner>> = networkClient.get(
            "banner/json",
            null,
            BANNER_RESPONSE_TYPE
        )

        if (!response.isSuccess()) {
            throw RepositoryException(safeMessage(response.getErrorMsg(), "轮播图加载失败"))
        }
        response.getData() ?: ArrayList()
    }

    suspend fun fetchHomeArticles(page: Int): Result<PageResponse<Article>> = runCatching {
        val queryParams = HashMap<String, String>()
        queryParams["page_size"] = DEFAULT_PAGE_SIZE.toString()
        val response: ApiResponse<PageResponse<Article>> = networkClient.get(
            "article/list/$page/json",
            queryParams,
            ARTICLE_PAGE_RESPONSE_TYPE
        )
        requireArticlePage(response, "首页文章加载失败")
    }

    companion object {
        private const val DEFAULT_PAGE_SIZE = 20
        //TypeToken捕获被擦除的泛型信息，帮助Gson反序列化的
        private val BANNER_RESPONSE_TYPE: Type =
            object : TypeToken<ApiResponse<List<Banner>>>() {}.type
        private val ARTICLE_PAGE_RESPONSE_TYPE: Type =
            object : TypeToken<ApiResponse<PageResponse<Article>>>() {}.type
    }
}
