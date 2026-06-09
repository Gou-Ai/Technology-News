package com.compuspulse.data.repository;

import com.compuspulse.data.remote.http.NetworkClient;
import com.compuspulse.data.remote.model.ApiResponse;
import com.compuspulse.data.remote.model.Article;
import com.compuspulse.data.remote.model.PageResponse;
import com.compuspulse.data.remote.model.ProjectCategory;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryRepository extends BaseRemoteRepository {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int LATEST_CATEGORY_ID = -1;
    private static final Type CATEGORY_RESPONSE_TYPE =
            new TypeToken<ApiResponse<List<ProjectCategory>>>() { }.getType();
    private static final Type ARTICLE_RESPONSE_TYPE =
            new TypeToken<ApiResponse<PageResponse<Article>>>() { }.getType();

    public void fetchProjectCategories(RepositoryCallback<List<ProjectCategory>> callback) {
        networkClient.get("project/tree/json", null, CATEGORY_RESPONSE_TYPE,
                new NetworkClient.ResponseCallback<List<ProjectCategory>>() {
                    @Override
                    public void onSuccess(ApiResponse<List<ProjectCategory>> response) {
                        if (response == null) {
                            callback.onError("分类加载失败");
                            return;
                        }

                        if (!response.isSuccess()) {
                            callback.onError(safeMessage(response.getErrorMsg(), "分类加载失败"));
                            return;
                        }

                        List<ProjectCategory> categories = new ArrayList<>();
                        categories.add(new ProjectCategory(LATEST_CATEGORY_ID, "最新文章"));
                        if (response.getData() != null) {
                            categories.addAll(response.getData());
                        }
                        callback.onSuccess(categories);
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError(message);
                    }
                });
    }

    public void fetchArticlesByCategory(int categoryId, RepositoryCallback<List<Article>> callback) {
        String path = categoryId == LATEST_CATEGORY_ID ? "article/list/0/json" : "project/list/1/json";
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page_size", String.valueOf(DEFAULT_PAGE_SIZE));
        if (categoryId != LATEST_CATEGORY_ID) {
            queryParams.put("cid", String.valueOf(categoryId));
        }

        networkClient.get(path, queryParams, ARTICLE_RESPONSE_TYPE,
                new NetworkClient.ResponseCallback<PageResponse<Article>>() {
                    @Override
                    public void onSuccess(ApiResponse<PageResponse<Article>> response) {
                        dispatchArticleList(response, callback, "分类文章加载失败");
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError(message);
                    }
                });
    }
}
