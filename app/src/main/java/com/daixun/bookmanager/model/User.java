package com.daixun.bookmanager.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String username;
    private String password;
    private boolean isAdmin; // true表示管理员，false表示学生用户
    private String avatarUrl; // 用户头像URL
    private String email; // 用户邮箱
    private String phone; // 用户电话
    
    // 构造函数
    public User(String username, String password, boolean isAdmin) {
        this.username = username;
        this.password = password;
        this.isAdmin = isAdmin;
        this.avatarUrl = ""; // 默认为空
        this.email = "";
        this.phone = "";
    }
    
    // 标记这个构造函数为忽略，这样Room就不会考虑它
    @Ignore
    public User(String username, String password, boolean isAdmin, String avatarUrl, String email, String phone) {
        this.username = username;
        this.password = password;
        this.isAdmin = isAdmin;
        this.avatarUrl = avatarUrl;
        this.email = email;
        this.phone = phone;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public boolean isAdmin() {
        return isAdmin;
    }
    
    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
} 