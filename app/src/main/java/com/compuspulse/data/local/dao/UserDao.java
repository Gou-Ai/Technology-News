package com.compuspulse.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.compuspulse.data.local.entity.UserEntity;

@Dao
public interface UserDao {

    //冲突时终止插入
    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(UserEntity user);

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    UserEntity getUserByUsername(String username);

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    UserEntity login(String username, String password);
}