package com.compuspulse.data.repository;

import com.compuspulse.CampusPulseApp;
import com.compuspulse.data.local.dao.UserDao;
import com.compuspulse.data.local.entity.UserEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthRepository {

    private final UserDao userDao;
    private final ExecutorService executorService;

    public AuthRepository() {
        userDao = CampusPulseApp.getInstance().getDatabase().userDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void register(String username, String password, AuthCallback callback) {
        executorService.execute(() -> {
            UserEntity existing = userDao.getUserByUsername(username);
            if (existing != null) {
                if (callback != null) {
                    callback.onResult(false, "账号已存在");
                }
                return;
            }

            UserEntity user = new UserEntity(
                    username,
                    password,
                    username,
                    System.currentTimeMillis()
            );
            userDao.insert(user);

            if (callback != null) {
                callback.onResult(true, "注册成功");
            }
        });
    }

    public void login(String username, String password, AuthCallback callback) {
        executorService.execute(() -> {
            UserEntity user = userDao.login(username, password);
            if (callback != null) {
                if (user != null) {
                    callback.onResult(true, "登录成功");
                } else {
                    callback.onResult(false, "账号或密码错误");
                }
            }
        });
    }

    public interface AuthCallback {
        void onResult(boolean success, String message);
    }
}