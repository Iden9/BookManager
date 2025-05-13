package com.daixun.bookmanager.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "readers")
public class Reader {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String name;           // 读者姓名
    private String studentId;      // 学号
    private String department;     // 院系
    private String phoneNumber;    // 联系电话
    private String email;          // 邮箱
    private int maxBorrowCount;    // 最大借书数量
    private int currentBorrowCount;// 当前借书数量
    
    // 构造函数
    public Reader(String name, String studentId, String department, 
                  String phoneNumber, String email, int maxBorrowCount) {
        this.name = name;
        this.studentId = studentId;
        this.department = department;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.maxBorrowCount = maxBorrowCount;
        this.currentBorrowCount = 0; // 初始为0本
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getStudentId() {
        return studentId;
    }
    
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public int getMaxBorrowCount() {
        return maxBorrowCount;
    }
    
    public void setMaxBorrowCount(int maxBorrowCount) {
        this.maxBorrowCount = maxBorrowCount;
    }
    
    public int getCurrentBorrowCount() {
        return currentBorrowCount;
    }
    
    public void setCurrentBorrowCount(int currentBorrowCount) {
        this.currentBorrowCount = currentBorrowCount;
    }
} 