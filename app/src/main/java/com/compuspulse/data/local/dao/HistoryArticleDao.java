package com.compuspulse.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.compuspulse.data.local.entity.HistoryArticleEntity;

import java.util.List;

@Dao
public interface HistoryArticleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(HistoryArticleEntity entity);

    @Query("SELECT * FROM history_articles WHERE username = :username ORDER BY viewedAt DESC")
    List<HistoryArticleEntity> getAllHistory(String username);

    @Query("SELECT * FROM history_articles WHERE username = :username ORDER BY viewedAt DESC")
    LiveData<List<HistoryArticleEntity>> observeAllHistory(String username);

    @Query("DELETE FROM history_articles WHERE username = :username")
    void clearAll(String username);
}
