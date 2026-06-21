package com.compuspulse.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val sharedPreferences: SharedPreferences = context.applicationContext
        .getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)

    fun saveLogin(username: String) {
        sharedPreferences.edit()
            .putBoolean(KEY_IS_LOGIN, true)
            .putString(KEY_USERNAME, username)
            .apply()
    }

    fun isLogin(): Boolean = sharedPreferences.getBoolean(KEY_IS_LOGIN, false)

    fun getCurrentUsername(): String = sharedPreferences.getString(KEY_USERNAME, "") ?: ""

    fun logout() {
        sharedPreferences.edit().clear().apply()
    }

    companion object {
        private const val SP_NAME = "user_session"
        private const val KEY_IS_LOGIN = "is_login"
        private const val KEY_USERNAME = "username"
    }
}
