package com.compuspulse.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compuspulse.data.remote.model.Article
import com.compuspulse.data.remote.model.Banner
import com.compuspulse.data.remote.model.PageResponse
import com.compuspulse.data.repository.HomeFeedRepository
import com.compuspulse.ui.home.HomeListAdapter
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val repository = HomeFeedRepository()

    private val bannerList = MutableLiveData<List<Banner>>(ArrayList())
    private val articleList = MutableLiveData<List<Article>>(ArrayList())
    private val refreshing = MutableLiveData(false)
    private val loadMoreStatus = MutableLiveData(HomeListAdapter.STATUS_IDLE)
    private val errorMessage = MutableLiveData<String>()

    private val currentArticles: MutableList<Article> = ArrayList()
    private var currentPage = 0
    private var noMoreData = false
    private var isLoadingMore = false

    fun getBannerList(): LiveData<List<Banner>> = bannerList

    fun getArticleList(): LiveData<List<Article>> = articleList

    fun getRefreshing(): LiveData<Boolean> = refreshing

    fun getLoadMoreStatus(): LiveData<Int> = loadMoreStatus

    fun getErrorMessage(): LiveData<String> = errorMessage

    fun loadHomeData() {
        refreshing.value = true
        loadBanners()
        currentPage = 0
        noMoreData = false
        loadArticlePage(true)
    }

    fun refreshArticles() {
        currentPage = 0
        noMoreData = false
        refreshing.value = true
        loadBanners()
        loadArticlePage(true)
    }

    fun loadMoreArticles() {
        //防止重复加载，防抖
        if (refreshing.value == true || isLoadingMore) {
            return
        }
        if (noMoreData) {
            loadMoreStatus.value = HomeListAdapter.STATUS_NO_MORE
            return
        }
        currentPage++
        loadMoreStatus.value = HomeListAdapter.STATUS_LOADING
        isLoadingMore = true
        loadArticlePage(false)
    }

    private fun loadBanners() {
        viewModelScope.launch {
            repository.fetchBannerList()
                .onSuccess { data ->
                    bannerList.value = data
                }
                .onFailure { throwable ->
                    errorMessage.value = throwable.message ?: "轮播图加载失败"
                }
        }
    }

    private fun loadArticlePage(clearBeforeAppend: Boolean) {
        viewModelScope.launch {
            repository.fetchHomeArticles(currentPage)
                .onSuccess { data ->
                    handleArticlePageSuccess(data, clearBeforeAppend)
                }
                .onFailure { throwable ->
                    handleArticlePageError(throwable.message ?: "首页文章加载失败", clearBeforeAppend)
                }
        }
    }

    private fun handleArticlePageSuccess(data: PageResponse<Article>, clearBeforeAppend: Boolean) {
        val pageData = data.getDatas()
        //是否要清除之前缓存的文章
        if (clearBeforeAppend) {
            currentArticles.clear()
        }
        currentArticles.addAll(pageData)
        articleList.value = ArrayList(currentArticles)
        noMoreData = data.isOver()
        loadMoreStatus.value =
            if (noMoreData) HomeListAdapter.STATUS_NO_MORE else HomeListAdapter.STATUS_IDLE
        refreshing.value = false
        isLoadingMore = false
    }

    private fun handleArticlePageError(message: String, clearBeforeAppend: Boolean) {
        //加载更多失败则页码回退一页
        if (!clearBeforeAppend && currentPage > 0) {
            currentPage--
        }
        errorMessage.value = message
        refreshing.value = false
        isLoadingMore = false
        loadMoreStatus.value =
            if (currentArticles.isEmpty()) HomeListAdapter.STATUS_IDLE else HomeListAdapter.STATUS_LOAD_FAIL
    }
}
