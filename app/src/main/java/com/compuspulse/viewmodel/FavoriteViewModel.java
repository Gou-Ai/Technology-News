package com.compuspulse.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.compuspulse.data.local.entity.FavoriteArticleEntity;
import com.compuspulse.data.repository.ArticleLocalRepository;

import java.util.List;

public class FavoriteViewModel extends ViewModel {

    private final ArticleLocalRepository repository = new ArticleLocalRepository();
    private final LiveData<List<FavoriteArticleEntity>> favoriteList = repository.observeFavorites();

    public LiveData<List<FavoriteArticleEntity>> getFavoriteList() {
        return favoriteList;
    }

    public void removeFavorite(int articleId) {
        repository.removeFavorite(articleId);
    }
}
