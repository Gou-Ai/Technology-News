package com.compuspulse.data.repository

import com.compuspulse.data.remote.model.ApiResponse
import com.compuspulse.data.remote.model.Article
import com.compuspulse.data.remote.model.PageResponse
import com.compuspulse.data.remote.model.ProjectCategory
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class CategoryRepository : BaseRemoteRepository() {

    suspend fun fetchProjectCategories(): Result<List<ProjectCategory>> = runCatching {
        val response: ApiResponse<List<ProjectCategory>> = networkClient.get(
            "project/tree/json",
            null,
            CATEGORY_RESPONSE_TYPE
        )

        if (!response.isSuccess()) {
            throw RepositoryException(safeMessage(response.getErrorMsg(), "分类加载失败"))
        }

        val categories = ArrayList<ProjectCategory>()
        categories.add(ProjectCategory(LATEST_CATEGORY_ID, "最新文章"))
        response.getData()?.let { categories.addAll(it) }
        categories
    }

    suspend fun fetchArticlesByCategory(categoryId: Int): Result<List<Article>> = runCatching {
        val path = if (categoryId == LATEST_CATEGORY_ID) "article/list/0/json" else "project/list/1/json"
        val queryParams = HashMap<String, String>()
        queryParams["page_size"] = DEFAULT_PAGE_SIZE.toString()
        if (categoryId != LATEST_CATEGORY_ID) {
            queryParams["cid"] = categoryId.toString()
        }

        val response: ApiResponse<PageResponse<Article>> = networkClient.get(
            path,
            queryParams,
            ARTICLE_RESPONSE_TYPE
        )
        requireArticleList(response, "分类文章加载失败")
    }

    companion object {
        private const val DEFAULT_PAGE_SIZE = 20
        private const val LATEST_CATEGORY_ID = -1
        private val CATEGORY_RESPONSE_TYPE: Type =
            object : TypeToken<ApiResponse<List<ProjectCategory>>>() {}.type
        private val ARTICLE_RESPONSE_TYPE: Type =
            object : TypeToken<ApiResponse<PageResponse<Article>>>() {}.type
    }
}
