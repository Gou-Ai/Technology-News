package com.compuspulse.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.compuspulse.data.remote.model.Article;
import com.compuspulse.data.remote.model.ProjectCategory;
import com.compuspulse.data.repository.CategoryRepository;
import com.compuspulse.data.repository.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;

public class CategoryViewModel extends ViewModel {

    private final CategoryRepository repository = new CategoryRepository();
    private final MutableLiveData<List<ProjectCategory>> categoryList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Article>> articleList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    public LiveData<List<ProjectCategory>> getCategoryList() {
        return categoryList;
    }

    public LiveData<List<Article>> getArticleList() {
        return articleList;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public void loadCategories() {
        loading.setValue(true);
        repository.fetchProjectCategories(new RepositoryCallback<List<ProjectCategory>>() {
            @Override
            public void onSuccess(List<ProjectCategory> data) {
                categoryList.setValue(data);
                loading.setValue(false);
                if (data != null && !data.isEmpty()) {
                    loadByCategory(data.get(0).getId());
                }
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                loading.setValue(false);
            }
        });
    }

    public void loadByCategory(int categoryId) {
        loading.setValue(true);
        repository.fetchArticlesByCategory(categoryId, new RepositoryCallback<List<Article>>() {
            @Override
            public void onSuccess(List<Article> data) {
                articleList.setValue(data == null ? new ArrayList<>() : data);
                loading.setValue(false);
            }

            @Override
            public void onError(String message) {
                articleList.setValue(new ArrayList<>());
                errorMessage.setValue(message);
                loading.setValue(false);
            }
        });
    }
}
