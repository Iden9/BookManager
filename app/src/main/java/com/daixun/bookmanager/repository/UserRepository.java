package com.daixun.bookmanager.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.daixun.bookmanager.dao.UserDao;
import com.daixun.bookmanager.db.AppDatabase;
import com.daixun.bookmanager.model.User;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class UserRepository {
    private final UserDao userDao;
    private final LiveData<List<User>> allUsers;
    private final ExecutorService executorService;

    public UserRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        userDao = db.userDao();
        allUsers = userDao.getAllUsers();
        executorService = AppDatabase.databaseWriteExecutor;
    }

    public LiveData<List<User>> getAllUsers() {
        return allUsers;
    }

    public void insert(User user) {
        executorService.execute(() -> userDao.insert(user));
    }

    public void update(User user) {
        executorService.execute(() -> userDao.update(user));
    }

    public void delete(User user) {
        executorService.execute(() -> userDao.delete(user));
    }

    public CompletableFuture<User> getUserById(final int id) {
        return CompletableFuture.supplyAsync(() -> userDao.getUserById(id), executorService);
    }

    public CompletableFuture<User> getUserByUsername(final String username) {
        return CompletableFuture.supplyAsync(() -> userDao.getUserByUsername(username), executorService);
    }

    public CompletableFuture<User> login(final String username, final String password) {
        return CompletableFuture.supplyAsync(() -> userDao.login(username, password), executorService);
    }

    public CompletableFuture<Integer> getUserCount() {
        return CompletableFuture.supplyAsync(userDao::getUserCount, executorService);
    }
}