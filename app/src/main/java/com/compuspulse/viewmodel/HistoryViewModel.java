package com.compuspulse.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.compuspulse.data.local.entity.HistoryArticleEntity;
import com.compuspulse.data.repository.ArticleLocalRepository;

import java.util.List;

public class HistoryViewModel extends ViewModel {

    private final ArticleLocalRepository repository = new ArticleLocalRepository();
    private final LiveData<List<HistoryArticleEntity>> historyList = repository.observeHistory();

    public LiveData<List<HistoryArticleEntity>> getHistoryList() {
        return historyList;
    }

    public void clearHistory() {
        repository.clearHistory();
    }
}
