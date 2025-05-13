package com.daixun.bookmanager.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "books")
public class Book {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String isbn;        // ISBN编号
    private String name;        // 书名
    private String author;      // 作者
    private String publisher;   // 出版社
    private String category;    // 分类
    private double price;       // 价格
    private int totalCount;     // 总数量
    private int availableCount; // 可借数量
    private String location;    // 存放位置
    private String coverUrl;    // 封面图片URL
    
    // 构造函数
    @Ignore
    public Book(String isbn, String name, String author, String publisher, 
                String category, double price, int totalCount, int availableCount, 
                String location) {
        this.isbn = isbn;
        this.name = name;
        this.author = author;
        this.publisher = publisher;
        this.category = category;
        this.price = price;
        this.totalCount = totalCount;
        this.availableCount = availableCount;
        this.location = location;
        this.coverUrl = ""; // 默认为空字符串
    }
    
    // 增加带封面URL的构造函数
    public Book(String isbn, String name, String author, String publisher, 
                String category, double price, int totalCount, int availableCount, 
                String location, String coverUrl) {
        this.isbn = isbn;
        this.name = name;
        this.author = author;
        this.publisher = publisher;
        this.category = category;
        this.price = price;
        this.totalCount = totalCount;
        this.availableCount = availableCount;
        this.location = location;
        this.coverUrl = coverUrl;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getIsbn() {
        return isbn;
    }
    
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public String getPublisher() {
        return publisher;
    }
    
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public double getPrice() {
        return price;
    }
    
    public void setPrice(double price) {
        this.price = price;
    }
    
    public int getTotalCount() {
        return totalCount;
    }
    
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
    
    public int getAvailableCount() {
        return availableCount;
    }
    
    public void setAvailableCount(int availableCount) {
        this.availableCount = availableCount;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getCoverUrl() {
        return coverUrl;
    }
    
    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }
} 