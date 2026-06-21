package com.compuspulse.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compuspulse.data.remote.model.Article
import com.compuspulse.data.remote.model.ProjectCategory
import com.compuspulse.data.repository.CategoryRepository
import kotlinx.coroutines.launch

class CategoryViewModel : ViewModel() {

    private val repository = CategoryRepository()
    private val categoryList = MutableLiveData<List<ProjectCategory>>(ArrayList())
    private val articleList = MutableLiveData<List<Article>>(ArrayList())
    private val errorMessage = MutableLiveData<String>()
    private val loading = MutableLiveData(false)

    fun getCategoryList(): LiveData<List<ProjectCategory>> = categoryList

    fun getArticleList(): LiveData<List<Article>> = articleList

    fun getErrorMessage(): LiveData<String> = errorMessage

    fun getLoading(): LiveData<Boolean> = loading

    fun loadCategories() {
        loading.value = true
        viewModelScope.launch {
            repository.fetchProjectCategories()
                .onSuccess { data ->
                    categoryList.value = data
                    loading.value = false
                    if (data.isNotEmpty()) {
                        loadByCategory(data[0].getId())
                    }
                }
                .onFailure { throwable ->
                    errorMessage.value = throwable.message ?: "分类加载失败"
                    loading.value = false
                }
        }
    }

    fun loadByCategory(categoryId: Int) {
        loading.value = true
        viewModelScope.launch {
            repository.fetchArticlesByCategory(categoryId)
                .onSuccess { data ->
                    articleList.value = data
                    loading.value = false
                }
                .onFailure { throwable ->
                    articleList.value = ArrayList()
                    errorMessage.value = throwable.message ?: "分类文章加载失败"
                    loading.value = false
                }
        }
    }
}
