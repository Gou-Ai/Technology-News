package com.compuspulse.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.compuspulse.data.local.entity.HistoryArticleEntity

@Dao
interface HistoryArticleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: HistoryArticleEntity)

    @Query("SELECT * FROM history_articles WHERE username = :username ORDER BY viewedAt DESC")
    suspend fun getAllHistory(username: String): List<HistoryArticleEntity>

    @Query("SELECT * FROM history_articles WHERE username = :username ORDER BY viewedAt DESC")
    fun observeAllHistory(username: String): LiveData<List<HistoryArticleEntity>>

    @Query("DELETE FROM history_articles WHERE username = :username AND viewedAt < :cutoffTime")
    suspend fun deleteOlderThan(username: String, cutoffTime: Long)

    @Query(
        """
        DELETE FROM history_articles
        WHERE username = :username
        AND articleId NOT IN (
            SELECT articleId FROM history_articles
            WHERE username = :username
            ORDER BY viewedAt DESC
            LIMIT :maxCount
        )
        """
    )
    suspend fun trimToLatest(username: String, maxCount: Int)

    @Query("DELETE FROM history_articles WHERE username = :username")
    suspend fun clearAll(username: String)
}
