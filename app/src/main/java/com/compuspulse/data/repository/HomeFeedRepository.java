package com.compuspulse.data.repository;

import com.compuspulse.data.remote.http.NetworkClient;
import com.compuspulse.data.remote.model.ApiResponse;
import com.compuspulse.data.remote.model.Article;
import com.compuspulse.data.remote.model.Banner;
import com.compuspulse.data.remote.model.PageResponse;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFeedRepository extends BaseRemoteRepository {

    private static final int DEFAULT_PAGE_SIZE = 20;
    //TypeToken捕获被擦除的泛型信息，帮助Gson反序列化的
    private static final Type BANNER_RESPONSE_TYPE =
            new TypeToken<ApiResponse<List<Banner>>>() { }.getType();
    private static final Type ARTICLE_PAGE_RESPONSE_TYPE =
            new TypeToken<ApiResponse<PageResponse<Article>>>() { }.getType();

    public void fetchBannerList(RepositoryCallback<List<Banner>> callback) {
        networkClient.get("banner/json", null, BANNER_RESPONSE_TYPE,
                new NetworkClient.ResponseCallback<List<Banner>>() {
                    @Override
                    public void onSuccess(ApiResponse<List<Banner>> response) {
                        if (response == null) {
                            callback.onError("轮播图加载失败");
                            return;
                        }

                        if (!response.isSuccess()) {
                            callback.onError(safeMessage(response.getErrorMsg(), "轮播图加载失败"));
                            return;
                        }
                        callback.onSuccess(response.getData() == null ? new ArrayList<>() : response.getData());
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError(message);
                    }
                });
    }

    public void fetchHomeArticles(int page, RepositoryCallback<PageResponse<Article>> callback) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page_size", String.valueOf(DEFAULT_PAGE_SIZE));
        networkClient.get("article/list/" + page + "/json", queryParams, ARTICLE_PAGE_RESPONSE_TYPE,
                new NetworkClient.ResponseCallback<PageResponse<Article>>() {
                    @Override
                    public void onSuccess(ApiResponse<PageResponse<Article>> response) {
                        dispatchArticlePage(response, callback, "首页文章加载失败");
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError(message);
                    }
                });
    }
}