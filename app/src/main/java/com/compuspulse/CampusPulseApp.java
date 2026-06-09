package com.compuspulse;

import android.app.Application;

import com.compuspulse.data.local.db.AppDatabase;

public class CampusPulseApp extends Application {

    private static CampusPulseApp instance;
    private AppDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        database = AppDatabase.getInstance(this);
    }

    public static CampusPulseApp getInstance() {
        return instance;
    }

    public AppDatabase getDatabase() {
        return database;
    }
}