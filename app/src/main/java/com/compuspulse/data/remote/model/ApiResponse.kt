package com.compuspulse.data.remote.model

class ApiResponse<T> {
    private var data: T? = null
    private var errorCode: Int = 0
    private var errorMsg: String? = null

    fun getData(): T? = data

    fun getErrorCode(): Int = errorCode

    fun getErrorMsg(): String? = errorMsg

    fun isSuccess(): Boolean = errorCode == 0
}
