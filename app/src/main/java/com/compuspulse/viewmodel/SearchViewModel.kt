package com.compuspulse.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compuspulse.data.remote.model.Article
import com.compuspulse.data.repository.SearchRepository
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    private val repository = SearchRepository()
    private val resultList = MutableLiveData<List<Article>>(ArrayList())
    private val loading = MutableLiveData(false)
    private val errorMessage = MutableLiveData<String>()

    fun getResultList(): LiveData<List<Article>> = resultList

    fun getLoading(): LiveData<Boolean> = loading

    fun getErrorMessage(): LiveData<String> = errorMessage

    fun search(keyword: String) {
        loading.value = true
        viewModelScope.launch {
            repository.searchArticles(keyword)
                .onSuccess { data ->
                    resultList.value = data
                    loading.value = false
                }
                .onFailure { throwable ->
                    resultList.value = ArrayList()
                    errorMessage.value = throwable.message ?: "搜索失败"
                    loading.value = false
                }
        }
    }
}
