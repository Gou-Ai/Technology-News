package com.compuspulse.data.remote.http

import android.content.Context
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class PersistentCookieJar(context: Context) : CookieJar {

    private val sharedPreferences = context.applicationContext
        .getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
    private val lock = Any()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) = synchronized(lock) {
        if (cookies.isEmpty()) {
            return
        }

        val validCookies = cookies
            .filter { cookie -> cookie.expiresAt > System.currentTimeMillis() }
            .map { cookie -> cookie.toString() }
            .toSet()

        if (validCookies.isEmpty()) {
            sharedPreferences.edit().remove(url.host).apply()
            return
        }

        sharedPreferences.edit()
            .putStringSet(url.host, validCookies)
            .apply()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> = synchronized(lock) {
        getValidCookies(url)
    }

    fun clear() = synchronized(lock) {
        sharedPreferences.edit().clear().apply()
    }

    fun hasCookies(url: HttpUrl): Boolean = synchronized(lock) {
        getValidCookies(url).isNotEmpty()
    }

    private fun getValidCookies(url: HttpUrl): List<Cookie> {
        val savedCookies = sharedPreferences.getStringSet(url.host, emptySet()).orEmpty()
        if (savedCookies.isEmpty()) {
            return emptyList()
        }

        val now = System.currentTimeMillis()
        val validCookies = savedCookies
            .mapNotNull { cookieValue -> Cookie.parse(url, cookieValue) }
            .filter { cookie -> cookie.expiresAt > now }
        val validCookieValues = validCookies.map { cookie -> cookie.toString() }.toSet()

        if (validCookieValues.size != savedCookies.size) {
            val editor = sharedPreferences.edit()
            if (validCookieValues.isEmpty()) {
                editor.remove(url.host)
            } else {
                editor.putStringSet(url.host, validCookieValues)
            }
            editor.apply()
        }

        return validCookies
    }

    companion object {
        private const val SP_NAME = "wanandroid_cookies"
    }
}
