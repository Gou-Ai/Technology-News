package com.compuspulse.data.remote.http

import com.compuspulse.CampusPulseApp
import com.compuspulse.data.remote.model.ApiResponse
import com.google.gson.Gson
import java.io.IOException
import java.lang.reflect.Type
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request

class NetworkClient private constructor() {

    private val cookieJar = PersistentCookieJar(CampusPulseApp.getInstance())
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .build()
    private val gson: Gson = Gson()

    suspend fun <T> get(
        path: String,
        queryParams: Map<String, String>?,
        responseType: Type
    ): ApiResponse<T> {
        val url = buildUrl(path, queryParams)
            ?: throw NetworkException("请求地址无效")

        val request = Request.Builder()
            .url(url)
            .get()
            .build()
        return execute(request, responseType)
    }

    suspend fun <T> postForm(
        path: String,
        queryParams: Map<String, String>?,
        formParams: Map<String, String>?,
        responseType: Type
    ): ApiResponse<T> {
        val url = buildUrl(path, queryParams)
            ?: throw NetworkException("请求地址无效")

        val formBuilder = FormBody.Builder()
        formParams?.forEach { (key, value) ->
            formBuilder.add(key, value)
        }

        val request = Request.Builder()
            .url(url)
            .post(formBuilder.build())
            .build()
        return execute(request, responseType)
    }

    private fun buildUrl(path: String, queryParams: Map<String, String>?): HttpUrl? {
        val baseUrl = BASE_URL.toHttpUrlOrNull() ?: return null
        val builder = baseUrl.newBuilder().addPathSegments(path)
        queryParams?.forEach { (key, value) ->
            builder.addQueryParameter(key, value)
        }
        return builder.build()
    }

    private suspend fun <T> execute(
        request: Request,
        responseType: Type
    ): ApiResponse<T> = withContext(Dispatchers.IO) {
        try {
            //.use能自动关闭Response
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw NetworkException("请求失败，请稍后重试")
                }

                val responseBody = response.body
                if (responseBody == null) {
                    throw NetworkException("服务器返回为空")
                }

                val rawBody = responseBody.string()
                try {
                    //将Json字符串解析成ApiResponse对象
                    val parsed: ApiResponse<T>? = gson.fromJson(rawBody, responseType)
                    parsed ?: throw NetworkException("服务器响应格式异常")
                } catch (exception: Exception) {
                    val detail = exception.message
                    val message = if (detail.isNullOrBlank()) {
                        "数据解析失败：服务器响应格式异常"
                    } else {
                        "数据解析失败：$detail"
                    }
                    throw NetworkException(message, exception)
                }
            }
        } catch (exception: IOException) {
            throw NetworkException(NETWORK_ERROR_MESSAGE, exception)
        }
    }

    fun clearCookies() {
        cookieJar.clear()
    }

    fun hasSessionCookies(): Boolean = BASE_URL.toHttpUrlOrNull()?.let(cookieJar::hasCookies) ?: false

    companion object {
        private const val BASE_URL = "https://wanandroid.com/"
        private const val NETWORK_ERROR_MESSAGE = "网络连接失败，请稍后重试"

        @Volatile
        private var instance: NetworkClient? = null

        @JvmStatic
        fun getInstance(): NetworkClient {
            return instance ?: synchronized(this) {
                instance ?: NetworkClient().also { instance = it }
            }
        }
    }
}

class NetworkException(message: String, cause: Throwable? = null) : Exception(message, cause)
