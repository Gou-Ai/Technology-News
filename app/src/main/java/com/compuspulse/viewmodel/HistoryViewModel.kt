package com.compuspulse.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compuspulse.data.local.entity.HistoryArticleEntity
import com.compuspulse.data.repository.ArticleLocalRepository
import kotlinx.coroutines.launch

class HistoryViewModel : ViewModel() {

    private val repository = ArticleLocalRepository()
    private val historyList: LiveData<List<HistoryArticleEntity>> = repository.observeHistory()

    fun getHistoryList(): LiveData<List<HistoryArticleEntity>> = historyList

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}
