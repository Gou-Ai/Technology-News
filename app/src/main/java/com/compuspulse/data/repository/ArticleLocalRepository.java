package com.compuspulse.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.compuspulse.CampusPulseApp;
import com.compuspulse.data.local.dao.FavoriteArticleDao;
import com.compuspulse.data.local.dao.HistoryArticleDao;
import com.compuspulse.data.local.db.AppDatabase;
import com.compuspulse.data.local.entity.FavoriteArticleEntity;
import com.compuspulse.data.local.entity.HistoryArticleEntity;
import com.compuspulse.data.remote.model.Article;
import com.compuspulse.utils.SessionManager;

import java.util.Collections;
import java.util.List;

public class ArticleLocalRepository {

    private final FavoriteArticleDao favoriteArticleDao;
    private final HistoryArticleDao historyArticleDao;
    private final SessionManager sessionManager;

    public ArticleLocalRepository() {
        favoriteArticleDao = CampusPulseApp.getInstance()
                .getDatabase()
                .favoriteArticleDao();
        historyArticleDao = CampusPulseApp.getInstance()
                .getDatabase()
                .historyArticleDao();
        sessionManager = new SessionManager(CampusPulseApp.getInstance());
    }

    private String getCurrentUsername() {
        return sessionManager.getCurrentUsername();
    }

    public void saveHistory(Article article) {
        String username = getCurrentUsername();
        if (username == null || username.trim().isEmpty()) {
            return;
        }

        AppDatabase.getDatabaseExecutor().execute(() -> {
            HistoryArticleEntity entity = new HistoryArticleEntity(
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
            );
            historyArticleDao.insert(entity);
        });
    }

    public void addFavorite(Article article) {
        String username = getCurrentUsername();
        if (username == null || username.trim().isEmpty()) {
            return;
        }

        AppDatabase.getDatabaseExecutor().execute(() -> {
            FavoriteArticleEntity entity = new FavoriteArticleEntity(
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
            );
            favoriteArticleDao.insert(entity);
        });
    }

    public void removeFavorite(int articleId) {
        String username = getCurrentUsername();
        if (username == null || username.trim().isEmpty()) {
            return;
        }

        AppDatabase.getDatabaseExecutor().execute(() ->
                favoriteArticleDao.deleteByUserAndArticleId(username, articleId));
    }

    public LiveData<Boolean> observeFavoriteState(int articleId) {
        String username = getCurrentUsername();
        if (username == null || username.trim().isEmpty()) {
            MutableLiveData<Boolean> emptyState = new MutableLiveData<>(false);
            return emptyState;
        }
        //Transformations.map()转换LiveData的数据类型
        return Transformations.map(
                favoriteArticleDao.observeFavoriteCount(username, articleId),
                count -> count != null && count > 0
        );
    }

    public LiveData<List<FavoriteArticleEntity>> observeFavorites() {
        String username = getCurrentUsername();
        if (username == null || username.trim().isEmpty()) {
            MutableLiveData<List<FavoriteArticleEntity>> emptyList = new MutableLiveData<>(Collections.emptyList());
            return emptyList;
        }
        return favoriteArticleDao.observeAllFavorites(username);
    }

    public LiveData<List<HistoryArticleEntity>> observeHistory() {
        String username = getCurrentUsername();
        if (username == null || username.trim().isEmpty()) {
            MutableLiveData<List<HistoryArticleEntity>> emptyList = new MutableLiveData<>(Collections.emptyList());
            return emptyList;
        }
        return historyArticleDao.observeAllHistory(username);
    }

    public void clearHistory() {
        String username = getCurrentUsername();
        if (username == null || username.trim().isEmpty()) {
            return;
        }

        AppDatabase.getDatabaseExecutor().execute(() -> historyArticleDao.clearAll(username));
    }
}
