package com.daixun.bookmanager.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "borrows",
        foreignKeys = {
            @ForeignKey(entity = Book.class,
                    parentColumns = "id",
                    childColumns = "bookId",
                    onDelete = ForeignKey.CASCADE),
            @ForeignKey(entity = Reader.class,
                    parentColumns = "id",
                    childColumns = "readerId",
                    onDelete = ForeignKey.CASCADE)
        },
        indices = {
            @Index("bookId"),
            @Index("readerId")
        })
public class Borrow {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private int bookId;         // 关联的图书ID
    private int readerId;       // 关联的读者ID
    private long borrowDate;    // 借书日期，存储为时间戳
    private long dueDate;       // 应还日期，存储为时间戳
    private long returnDate;    // 实际归还日期，如果为0表示未归还
    private boolean isReturned; // 是否已归还
    private String remarks;     // 备注信息
    
    // 构造函数
    public Borrow(int bookId, int readerId, long borrowDate, long dueDate) {
        this.bookId = bookId;
        this.readerId = readerId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = 0;
        this.isReturned = false;
        this.remarks = "";
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getBookId() {
        return bookId;
    }
    
    public void setBookId(int bookId) {
        this.bookId = bookId;
    }
    
    public int getReaderId() {
        return readerId;
    }
    
    public void setReaderId(int readerId) {
        this.readerId = readerId;
    }
    
    public long getBorrowDate() {
        return borrowDate;
    }
    
    public void setBorrowDate(long borrowDate) {
        this.borrowDate = borrowDate;
    }
    
    public long getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(long dueDate) {
        this.dueDate = dueDate;
    }
    
    public long getReturnDate() {
        return returnDate;
    }
    
    public void setReturnDate(long returnDate) {
        this.returnDate = returnDate;
    }
    
    public boolean isReturned() {
        return isReturned;
    }
    
    public void setReturned(boolean returned) {
        isReturned = returned;
    }
    
    public String getRemarks() {
        return remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
} 