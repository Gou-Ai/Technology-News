package com.compuspulse.data.repository

import com.compuspulse.data.remote.model.ApiResponse
import com.compuspulse.data.remote.model.Article
import com.compuspulse.data.remote.model.PageResponse
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class SearchRepository : BaseRemoteRepository() {

    suspend fun searchArticles(keyword: String): Result<List<Article>> = runCatching {
        val queryParams = HashMap<String, String>()
        queryParams["page_size"] = DEFAULT_PAGE_SIZE.toString()

        val formParams = HashMap<String, String>()
        formParams["k"] = keyword

        val response: ApiResponse<PageResponse<Article>> = networkClient.postForm(
            "article/query/0/json",
            queryParams,
            formParams,
            SEARCH_RESPONSE_TYPE
        )
        requireArticleList(response, "搜索失败")
    }

    companion object {
        private const val DEFAULT_PAGE_SIZE = 20
        private val SEARCH_RESPONSE_TYPE: Type =
            object : TypeToken<ApiResponse<PageResponse<Article>>>() {}.type
    }
}
