package com.compuspulse.data.repository

import com.compuspulse.data.remote.http.NetworkClient
import com.compuspulse.data.remote.model.ApiResponse
import com.compuspulse.data.remote.model.Article
import com.compuspulse.data.remote.model.PageResponse

abstract class BaseRemoteRepository {

    protected val networkClient: NetworkClient = NetworkClient.getInstance()

    protected fun safeMessage(value: String?, fallback: String): String {
        return if (value.isNullOrBlank()) fallback else value
    }

    protected fun normalizeArticles(articles: List<Article>?) {
        articles?.forEach { article ->
            article.normalize()
        }
    }

    protected fun requireArticlePage(
        response: ApiResponse<PageResponse<Article>>?,
        defaultErrorMessage: String
    ): PageResponse<Article> {
        if (response == null) {
            throw RepositoryException(defaultErrorMessage)
        }

        if (!response.isSuccess() || response.getData() == null) {
            throw RepositoryException(safeMessage(response.getErrorMsg(), defaultErrorMessage))
        }

        val pageResponse = response.getData() ?: throw RepositoryException(defaultErrorMessage)
        normalizeArticles(pageResponse?.getDatas())
        return pageResponse
    }

    protected fun requireArticleList(
        response: ApiResponse<PageResponse<Article>>?,
        defaultErrorMessage: String
    ): List<Article> {
        if (response == null) {
            throw RepositoryException(defaultErrorMessage)
        }

        if (!response.isSuccess() || response.getData() == null) {
            throw RepositoryException(safeMessage(response.getErrorMsg(), defaultErrorMessage))
        }

        val articles = response.getData()?.getDatas()
        normalizeArticles(articles)
        return articles ?: ArrayList()
    }
}

class RepositoryException(message: String, cause: Throwable? = null) : Exception(message, cause)
