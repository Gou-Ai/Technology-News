package com.compuspulse

import android.app.Application
import com.compuspulse.data.local.db.AppDatabase

class CampusPulseApp : Application() {

    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getInstance(this)
    }

    companion object {
        private lateinit var instance: CampusPulseApp

        @JvmStatic
        fun getInstance(): CampusPulseApp = instance
    }
}
