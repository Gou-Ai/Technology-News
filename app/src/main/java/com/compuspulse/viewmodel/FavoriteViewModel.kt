package com.compuspulse.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compuspulse.data.local.entity.FavoriteArticleEntity
import com.compuspulse.data.repository.ArticleLocalRepository
import kotlinx.coroutines.launch

class FavoriteViewModel : ViewModel() {

    private val repository = ArticleLocalRepository()
    private val favoriteList: LiveData<List<FavoriteArticleEntity>> = repository.observeFavorites()
    private val errorMessage = MutableLiveData<String>()
    private val pendingRemoveIds = mutableSetOf<Int>()

    fun getFavoriteList(): LiveData<List<FavoriteArticleEntity>> = favoriteList

    fun getErrorMessage(): LiveData<String> = errorMessage

    fun refreshFavorites() {
        viewModelScope.launch {
            repository.refreshFavoritesFromServer()
                .onFailure { throwable ->
                    errorMessage.value = throwable.message ?: "收藏列表加载失败"
                }
        }
    }

    fun removeFavorite(articleId: Int) {
        if (!pendingRemoveIds.add(articleId)) {
            return
        }
        viewModelScope.launch {
            try {
                repository.removeFavorite(articleId)
                    .onFailure { throwable ->
                        errorMessage.value = throwable.message ?: "取消收藏失败"
                    }
            } finally {
                pendingRemoveIds.remove(articleId)
            }
        }
    }
}
