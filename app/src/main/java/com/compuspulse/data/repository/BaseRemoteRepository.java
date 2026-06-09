package com.compuspulse.data.repository;

import com.compuspulse.data.remote.http.NetworkClient;
import com.compuspulse.data.remote.model.ApiResponse;
import com.compuspulse.data.remote.model.Article;
import com.compuspulse.data.remote.model.PageResponse;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseRemoteRepository {

    protected final NetworkClient networkClient = NetworkClient.getInstance();

    protected String safeMessage(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    protected void normalizeArticles(List<Article> articles) {
        if (articles == null) {
            return;
        }
        for (Article article : articles) {
            if (article != null) {
                article.normalize();
            }
        }
    }

    protected void dispatchArticlePage(ApiResponse<PageResponse<Article>> response,
                                       RepositoryCallback<PageResponse<Article>> callback,
                                       String defaultErrorMessage) {
        if (response == null) {
            callback.onError(defaultErrorMessage);
            return;
        }

        if (!response.isSuccess() || response.getData() == null) {
            callback.onError(safeMessage(response.getErrorMsg(), defaultErrorMessage));
            return;
        }

        PageResponse<Article> pageResponse = response.getData();
        normalizeArticles(pageResponse.getDatas());
        callback.onSuccess(pageResponse);
    }

    protected void dispatchArticleList(ApiResponse<PageResponse<Article>> response,
                                       RepositoryCallback<List<Article>> callback,
                                       String defaultErrorMessage) {
        if (response == null) {
            callback.onError(defaultErrorMessage);
            return;
        }

        if (!response.isSuccess() || response.getData() == null) {
            callback.onError(safeMessage(response.getErrorMsg(), defaultErrorMessage));
            return;
        }

        List<Article> articles = response.getData().getDatas();
        normalizeArticles(articles);
        callback.onSuccess(articles == null ? new ArrayList<>() : articles);
    }
}
