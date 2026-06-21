package com.compuspulse.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.compuspulse.data.local.entity.FavoriteArticleEntity

@Dao
interface FavoriteArticleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteArticleEntity)

    @Query("DELETE FROM favorite_articles WHERE username = :username AND articleId = :articleId")
    suspend fun deleteByUserAndArticleId(username: String, articleId: Int)

    @Query("DELETE FROM favorite_articles WHERE username = :username")
    suspend fun clearAll(username: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_articles WHERE username = :username AND articleId = :articleId)")
    suspend fun isFavorite(username: String, articleId: Int): Boolean

    @Query("SELECT * FROM favorite_articles WHERE username = :username ORDER BY collectedAt DESC")
    suspend fun getAllFavorites(username: String): List<FavoriteArticleEntity>

    //ORODER BY 按照 时间戳设置查询结果的排序，DESC：逆序(最新的在最前面)
    @Query("SELECT * FROM favorite_articles WHERE username = :username ORDER BY collectedAt DESC")
    fun observeAllFavorites(username: String): LiveData<List<FavoriteArticleEntity>>

    @Query("SELECT COUNT(*) FROM favorite_articles WHERE username = :username AND articleId = :articleId")
    fun observeFavoriteCount(username: String, articleId: Int): LiveData<Int>
}
