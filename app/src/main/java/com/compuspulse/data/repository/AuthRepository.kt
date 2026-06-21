package com.compuspulse.data.repository

import com.compuspulse.data.remote.http.NetworkClient
import com.compuspulse.data.remote.model.ApiResponse
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class AuthRepository {

    private val networkClient: NetworkClient = NetworkClient.getInstance()

    suspend fun register(username: String, password: String): Result<Unit> = runCatching {
        networkClient.clearCookies()
        val formParams = hashMapOf(
            "username" to username,
            "password" to password,
            "repassword" to password
        )

        val response: ApiResponse<JsonElement> = networkClient.postForm(
            "user/register",
            null,
            formParams,
            AUTH_RESPONSE_TYPE
        )
        requireAuthSuccess(response)
        requireSessionCookies()
    }

    suspend fun login(username: String, password: String): Result<Unit> = runCatching {
        networkClient.clearCookies()
        val formParams = hashMapOf(
            "username" to username,
            "password" to password
        )

        val response: ApiResponse<JsonElement> = networkClient.postForm(
            "user/login",
            null,
            formParams,
            AUTH_RESPONSE_TYPE
        )
        requireAuthSuccess(response)
        requireSessionCookies()
    }

    suspend fun logout(): Result<Unit> = runCatching {
        try {
            val response: ApiResponse<JsonElement> = networkClient.get(
                "user/logout/json",
                null,
                AUTH_RESPONSE_TYPE
            )
            requireAuthSuccess(response)
        } finally {
            networkClient.clearCookies()
        }
    }

    private fun requireAuthSuccess(response: ApiResponse<JsonElement>?) {
        if (response == null || !response.isSuccess()) {
            val message = response?.getErrorMsg()
            throw RepositoryException(if (message.isNullOrBlank()) "服务器返回错误" else message)
        }
    }

    private fun requireSessionCookies() {
        if (!networkClient.hasSessionCookies()) {
            throw RepositoryException("登录状态保存失败，请重新登录")
        }
    }

    companion object {
        private val AUTH_RESPONSE_TYPE: Type = object : TypeToken<ApiResponse<JsonElement>>() {}.type
    }
}
