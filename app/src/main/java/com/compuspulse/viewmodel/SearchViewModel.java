package com.compuspulse.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.compuspulse.data.remote.model.Article;
import com.compuspulse.data.repository.RepositoryCallback;
import com.compuspulse.data.repository.SearchRepository;

import java.util.ArrayList;
import java.util.List;

public class SearchViewModel extends ViewModel {

    private final SearchRepository repository = new SearchRepository();
    private final MutableLiveData<List<Article>> resultList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LiveData<List<Article>> getResultList() {
        return resultList;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void search(String keyword) {
        loading.setValue(true);
        repository.searchArticles(keyword, new RepositoryCallback<List<Article>>() {
            @Override
            public void onSuccess(List<Article> data) {
                resultList.setValue(data == null ? new ArrayList<>() : data);
                loading.setValue(false);
            }

            @Override
            public void onError(String message) {
                resultList.setValue(new ArrayList<>());
                errorMessage.setValue(message);
                loading.setValue(false);
            }
        });
    }
}
