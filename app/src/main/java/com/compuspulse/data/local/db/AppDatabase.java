package com.compuspulse.data.local.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.compuspulse.data.local.dao.FavoriteArticleDao;
import com.compuspulse.data.local.dao.HistoryArticleDao;
import com.compuspulse.data.local.dao.UserDao;
import com.compuspulse.data.local.entity.FavoriteArticleEntity;
import com.compuspulse.data.local.entity.HistoryArticleEntity;
import com.compuspulse.data.local.entity.UserEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
        entities = {
                UserEntity.class,
                FavoriteArticleEntity.class,
                HistoryArticleEntity.class
        },
        version = 2,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;
    private static final ExecutorService DATABASE_EXECUTOR = Executors.newFixedThreadPool(2);

    public abstract UserDao userDao();
    public abstract FavoriteArticleDao favoriteArticleDao();
    public abstract HistoryArticleDao historyArticleDao();

    public static ExecutorService getDatabaseExecutor() {
        return DATABASE_EXECUTOR;
    }

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "campus_pulse.db"
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
