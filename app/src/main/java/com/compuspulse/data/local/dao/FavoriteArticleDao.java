package com.compuspulse.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.compuspulse.data.local.entity.FavoriteArticleEntity;

import java.util.List;

@Dao
public interface FavoriteArticleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FavoriteArticleEntity entity);

    @Query("DELETE FROM favorite_articles WHERE username = :username AND articleId = :articleId")
    void deleteByUserAndArticleId(String username, int articleId);

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_articles WHERE username = :username AND articleId = :articleId)")
    boolean isFavorite(String username, int articleId);

    @Query("SELECT * FROM favorite_articles WHERE username = :username ORDER BY collectedAt DESC")
    List<FavoriteArticleEntity> getAllFavorites(String username);

    //ORODER BY 按照 时间戳设置查询结果的排序，DESC：逆序(最新的在最前面)
    @Query("SELECT * FROM favorite_articles WHERE username = :username ORDER BY collectedAt DESC")
    LiveData<List<FavoriteArticleEntity>> observeAllFavorites(String username);

    @Query("SELECT COUNT(*) FROM favorite_articles WHERE username = :username AND articleId = :articleId")
    LiveData<Integer> observeFavoriteCount(String username, int articleId);
}
