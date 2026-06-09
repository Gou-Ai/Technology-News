package com.compuspulse.data.remote.http;

import android.os.Handler;
import android.os.Looper;

import com.compuspulse.data.remote.model.ApiResponse;
import com.google.gson.Gson;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class NetworkClient {

    private static final String BASE_URL = "https://wanandroid.com/";
    private static final String NETWORK_ERROR_MESSAGE = "网络连接失败，请稍后重试";
    private static volatile NetworkClient instance;

    private final OkHttpClient okHttpClient;
    private final Gson gson;
    private final Handler mainHandler;

    private NetworkClient() {
        okHttpClient = new OkHttpClient.Builder().build();
        gson = new Gson();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static NetworkClient getInstance() {
        if (instance == null) {
            synchronized (NetworkClient.class) {
                if (instance == null) {
                    instance = new NetworkClient();
                }
            }
        }
        return instance;
    }

    public <T> void get(String path,
                        Map<String, String> queryParams,
                        Type responseType,
                        ResponseCallback<T> callback) {
        HttpUrl url = buildUrl(path, queryParams);
        if (url == null) {
            dispatchError(callback, "请求地址无效");
            return;
        }

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        execute(request, responseType, callback);
    }

    public <T> void postForm(String path,
                             Map<String, String> queryParams,
                             Map<String, String> formParams,
                             Type responseType,
                             ResponseCallback<T> callback) {
        HttpUrl url = buildUrl(path, queryParams);
        if (url == null) {
            dispatchError(callback, "请求地址无效");
            return;
        }

        FormBody.Builder formBuilder = new FormBody.Builder();
        if (formParams != null) {
            for (Map.Entry<String, String> entry : formParams.entrySet()) {
                formBuilder.add(entry.getKey(), entry.getValue());
            }
        }

        Request request = new Request.Builder()
                .url(url)
                .post(formBuilder.build())
                .build();
        execute(request, responseType, callback);
    }

    private HttpUrl buildUrl(String path, Map<String, String> queryParams) {
        HttpUrl baseUrl = HttpUrl.parse(BASE_URL);
        if (baseUrl == null) {
            return null;
        }

        HttpUrl.Builder builder = baseUrl.newBuilder().addPathSegments(path);
        if (queryParams != null) {
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                builder.addQueryParameter(entry.getKey(), entry.getValue());
            }
        }
        return builder.build();
    }

    private <T> void execute(Request request,
                             Type responseType,
                             ResponseCallback<T> callback) {
        okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                dispatchError(callback, NETWORK_ERROR_MESSAGE);
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    closeQuietly(response.body());
                    dispatchError(callback, "请求失败，请稍后重试");
                    return;
                }

                ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    dispatchError(callback, "服务器返回为空");
                    return;
                }

                try {
                    //将Json字符串解析成ApiResponse对象
                    ApiResponse<T> apiResponse = gson.fromJson(responseBody.string(), responseType);
                    mainHandler.post(() -> callback.onSuccess(apiResponse));
                } catch (Exception exception) {
                    dispatchError(callback, "数据解析失败，请稍后重试");
                } finally {
                    closeQuietly(responseBody);
                }
            }
        });
    }

    private void dispatchError(ResponseCallback<?> callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }

    private void closeQuietly(ResponseBody responseBody) {
        if (responseBody != null) {
            responseBody.close();
        }
    }

    public interface ResponseCallback<T> {
        void onSuccess(ApiResponse<T> response);
        void onError(String message);
    }
}
