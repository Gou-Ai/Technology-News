package com.compuspulse.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String SP_NAME = "user_session";
    private static final String KEY_IS_LOGIN = "is_login";
    private static final String KEY_USERNAME = "username";

    private final SharedPreferences sharedPreferences;

    public SessionManager(Context context) {
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    public void saveLogin(String username) {
        sharedPreferences.edit()
                .putBoolean(KEY_IS_LOGIN, true)
                .putString(KEY_USERNAME, username)
                .apply();
    }

    public boolean isLogin() {
        return sharedPreferences.getBoolean(KEY_IS_LOGIN, false);
    }

    public String getCurrentUsername() {
        return sharedPreferences.getString(KEY_USERNAME, "");
    }

    public void logout() {
        sharedPreferences.edit().clear().apply();
    }
}