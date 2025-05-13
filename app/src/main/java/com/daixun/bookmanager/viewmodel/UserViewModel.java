package com.daixun.bookmanager.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.daixun.bookmanager.model.User;
import com.daixun.bookmanager.repository.UserRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class UserViewModel extends AndroidViewModel {
    
    private final UserRepository repository;
    private final LiveData<List<User>> allUsers;
    
    public UserViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
        allUsers = repository.getAllUsers();
    }
    
    public LiveData<List<User>> getAllUsers() {
        return allUsers;
    }
    
    public void insert(User user) {
        repository.insert(user);
    }
    
    public void update(User user) {
        repository.update(user);
    }
    
    public void delete(User user) {
        repository.delete(user);
    }
    
    public CompletableFuture<User> getUserByUsername(final String username) {
        return repository.getUserByUsername(username);
    }
    
    public CompletableFuture<User> login(final String username, final String password) {
        return repository.login(username, password);
    }
    
    public CompletableFuture<Integer> getUserCount() {
        return repository.getUserCount();
    }
} 