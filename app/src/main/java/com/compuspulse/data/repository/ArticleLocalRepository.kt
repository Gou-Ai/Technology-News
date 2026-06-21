package com.compuspulse.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.compuspulse.CampusPulseApp
import com.compuspulse.data.local.dao.FavoriteArticleDao
import com.compuspulse.data.local.dao.HistoryArticleDao
import com.compuspulse.data.local.db.AppDatabase
import com.compuspulse.data.local.entity.FavoriteArticleEntity
import com.compuspulse.data.local.entity.HistoryArticleEntity
import com.compuspulse.data.remote.http.NetworkClient
import com.compuspulse.data.remote.model.ApiResponse
import com.compuspulse.data.remote.model.Article
import com.compuspulse.data.remote.model.PageResponse
import com.compuspulse.utils.SessionManager
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import java.util.Collections
import java.lang.reflect.Type
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class ArticleLocalRepository {

    private val networkClient: NetworkClient = NetworkClient.getInstance()
    private val favoriteArticleDao: FavoriteArticleDao = CampusPulseApp.getInstance()
        .database
        .favoriteArticleDao()
    private val historyArticleDao: HistoryArticleDao = CampusPulseApp.getInstance()
        .database
        .historyArticleDao()
    private val sessionManager: SessionManager = SessionManager(CampusPulseApp.getInstance())

    private fun getCurrentUsername(): String = sessionManager.getCurrentUsername()

    suspend fun saveHistory(article: Article) = withContext(Dispatchers.IO) {
        val username = getCurrentUsername()
        if (username.isBlank()) {
            return@withContext
        }

        val entity = HistoryArticleEntity(
            username,
            article.getId(),
            article.getTitle(),
            article.getSummary(),
            article.getSource(),
            article.getSourceUrl(),
            article.getPublishTime(),
            article.getImageUrl(),
            article.getCommentCount(),
            System.currentTimeMillis()
        )
        historyArticleDao.insert(entity)
        pruneHistory(username)
    }

    suspend fun addFavorite(article: Article): Result<Unit> = runCatching {
        val articleId = article.getServerArticleId()
        favoriteActionMutex(articleId).withLock {
            val response: ApiResponse<JsonElement> = networkClient.postForm(
                "lg/collect/$articleId/json",
                null,
                null,
                EMPTY_RESPONSE_TYPE
            )
            requireSuccess(response, "收藏失败")
            saveFavoriteLocal(article)
        }
    }

    private suspend fun saveFavoriteLocal(article: Article) = withContext(Dispatchers.IO) {
        val username = getCurrentUsername()
        if (username.isBlank()) {
            return@withContext
        }

        val articleId = article.getServerArticleId()
        val entity = FavoriteArticleEntity(
            username,
            articleId,
            article.getTitle(),
            article.getSummary(),
            article.getSource(),
            article.getSourceUrl(),
            article.getPublishTime(),
            article.getImageUrl(),
            article.getCommentCount(),
            System.currentTimeMillis()
        )
        favoriteArticleDao.insert(entity)
    }

    suspend fun removeFavorite(articleId: Int): Result<Unit> = runCatching {
        favoriteActionMutex(articleId).withLock {
            val response: ApiResponse<JsonElement> = networkClient.postForm(
                "lg/uncollect_originId/$articleId/json",
                null,
                null,
                EMPTY_RESPONSE_TYPE
            )
            requireSuccess(response, "取消收藏失败")
            removeFavoriteLocal(articleId)
        }
    }

    private suspend fun removeFavoriteLocal(articleId: Int) = withContext(Dispatchers.IO) {
        val username = getCurrentUsername()
        if (username.isBlank()) {
            return@withContext
        }

        favoriteArticleDao.deleteByUserAndArticleId(username, articleId)
    }

    suspend fun refreshFavoritesFromServer(): Result<Unit> = runCatching {
        val requestUsername = getCurrentUsername()
        if (requestUsername.isBlank()) {
            throw RepositoryException("请先登录")
        }

        val queryParams = hashMapOf("page_size" to FAVORITE_PAGE_SIZE.toString())
        val response: ApiResponse<PageResponse<Article>> = networkClient.get(
            "lg/collect/list/0/json",
            queryParams,
            FAVORITE_LIST_RESPONSE_TYPE
        )
        if (!response.isSuccess() || response.getData() == null) {
            throw RepositoryException(safeMessage(response.getErrorMsg(), "收藏列表加载失败"))
        }
        val serverFavorites = response.getData()?.getDatas().orEmpty()
        serverFavorites.forEach { article ->
            article.normalize()
            article.setFavorite(true)
        }
        replaceLocalFavorites(requestUsername, serverFavorites)
    }

    private suspend fun replaceLocalFavorites(username: String, articles: List<Article>) = withContext(Dispatchers.IO) {
        localDataMutex.withLock {
            if (username != getCurrentUsername()) {
                return@withLock
            }

            favoriteArticleDao.clearAll(username)
            articles.forEach { article ->
                val articleId = article.getServerArticleId()
                val entity = FavoriteArticleEntity(
                    username,
                    articleId,
                    article.getTitle(),
                    article.getSummary(),
                    article.getSource(),
                    article.getSourceUrl(),
                    article.getPublishTime(),
                    article.getImageUrl(),
                    article.getCommentCount(),
                    System.currentTimeMillis()
                )
                favoriteArticleDao.insert(entity)
            }
        }
    }

    fun observeFavoriteState(articleId: Int): LiveData<Boolean> {
        val username = getCurrentUsername()
        if (username.isBlank()) {
            return MutableLiveData(false)
        }
        //Transformations.map()转换LiveData的数据类型
        val result = MediatorLiveData<Boolean>()
        result.addSource(favoriteArticleDao.observeFavoriteCount(username, articleId)) { count: Int? ->
            result.value = count != null && count > 0
        }
        return result
    }

    fun observeFavorites(): LiveData<List<FavoriteArticleEntity>> {
        val username = getCurrentUsername()
        if (username.isBlank()) {
            return MutableLiveData(Collections.emptyList())
        }
        return favoriteArticleDao.observeAllFavorites(username)
    }

    fun observeHistory(): LiveData<List<HistoryArticleEntity>> {
        val username = getCurrentUsername()
        if (username.isBlank()) {
            return MutableLiveData(Collections.emptyList())
        }
        return historyArticleDao.observeAllHistory(username)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        val username = getCurrentUsername()
        if (username.isBlank()) {
            return@withContext
        }

        historyArticleDao.clearAll(username)
    }

    suspend fun clearUserLocalData(username: String) = withContext(Dispatchers.IO) {
        if (username.isBlank()) {
            return@withContext
        }

        localDataMutex.withLock {
            favoriteArticleDao.clearAll(username)
            historyArticleDao.clearAll(username)
        }
    }

    private suspend fun pruneHistory(username: String) {
        val cutoffTime = System.currentTimeMillis() - HISTORY_RETENTION_DAYS * MILLIS_PER_DAY
        historyArticleDao.deleteOlderThan(username, cutoffTime)
        historyArticleDao.trimToLatest(username, MAX_HISTORY_COUNT)
    }

    private fun requireSuccess(response: ApiResponse<JsonElement>?, defaultErrorMessage: String) {
        if (response == null || !response.isSuccess()) {
            throw RepositoryException(safeMessage(response?.getErrorMsg(), defaultErrorMessage))
        }
    }

    private fun safeMessage(value: String?, fallback: String): String {
        return if (value.isNullOrBlank()) fallback else value
    }

    private suspend fun favoriteActionMutex(articleId: Int): Mutex {
        return favoriteActionMutexesLock.withLock {
            favoriteActionMutexes.getOrPut(articleId) { Mutex() }
        }
    }

    companion object {
        private const val FAVORITE_PAGE_SIZE = 40
        private const val MAX_HISTORY_COUNT = 200
        private const val HISTORY_RETENTION_DAYS = 30L
        private const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L
        private val localDataMutex = Mutex()
        private val favoriteActionMutexesLock = Mutex()
        private val favoriteActionMutexes = HashMap<Int, Mutex>()
        private val EMPTY_RESPONSE_TYPE: Type = object : TypeToken<ApiResponse<JsonElement>>() {}.type
        private val FAVORITE_LIST_RESPONSE_TYPE: Type =
            object : TypeToken<ApiResponse<PageResponse<Article>>>() {}.type
    }
}
