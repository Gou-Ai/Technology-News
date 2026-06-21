package com.compuspulse.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.compuspulse.data.local.dao.FavoriteArticleDao
import com.compuspulse.data.local.dao.HistoryArticleDao
import com.compuspulse.data.local.entity.FavoriteArticleEntity
import com.compuspulse.data.local.entity.HistoryArticleEntity

@Database(
    entities = [
        FavoriteArticleEntity::class,
        HistoryArticleEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun favoriteArticleDao(): FavoriteArticleDao
    abstract fun historyArticleDao(): HistoryArticleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        @JvmStatic
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "campus_pulse.db"
                )
                    .addMigrations(MIGRATION_2_3)
                    .build()
                    .also { INSTANCE = it }
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS users")
            }
        }
    }
}
