package com.compuspulse.data.repository;

import com.compuspulse.data.remote.http.NetworkClient;
import com.compuspulse.data.remote.model.ApiResponse;
import com.compuspulse.data.remote.model.Article;
import com.compuspulse.data.remote.model.PageResponse;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchRepository extends BaseRemoteRepository {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final Type SEARCH_RESPONSE_TYPE =
            new TypeToken<ApiResponse<PageResponse<Article>>>() { }.getType();

    public void searchArticles(String keyword, RepositoryCallback<List<Article>> callback) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page_size", String.valueOf(DEFAULT_PAGE_SIZE));

        Map<String, String> formParams = new HashMap<>();
        formParams.put("k", keyword);

        networkClient.postForm("article/query/0/json", queryParams, formParams, SEARCH_RESPONSE_TYPE,
                new NetworkClient.ResponseCallback<PageResponse<Article>>() {
                    @Override
                    public void onSuccess(ApiResponse<PageResponse<Article>> response) {
                        dispatchArticleList(response, callback, "搜索失败");
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError(message);
                    }
                });
    }
}
