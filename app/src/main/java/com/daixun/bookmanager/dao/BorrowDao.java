package com.daixun.bookmanager.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.daixun.bookmanager.model.Borrow;

import java.util.List;

@Dao
public interface BorrowDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Borrow borrow);
    
    @Update
    void update(Borrow borrow);
    
    @Delete
    void delete(Borrow borrow);
    
    @Query("SELECT * FROM borrows WHERE id = :id LIMIT 1")
    Borrow getBorrowById(int id);
    
    @Query("SELECT * FROM borrows")
    LiveData<List<Borrow>> getAllBorrows();
    
    @Query("SELECT * FROM borrows WHERE isReturned = 0")
    LiveData<List<Borrow>> getActiveBorrows();
    
    @Query("SELECT * FROM borrows WHERE readerId = :readerId")
    LiveData<List<Borrow>> getBorrowsByReaderId(int readerId);
    
    @Query("SELECT * FROM borrows WHERE bookId = :bookId")
    LiveData<List<Borrow>> getBorrowsByBookId(int bookId);
    
    @Query("SELECT * FROM borrows WHERE isReturned = 0 AND readerId = :readerId")
    LiveData<List<Borrow>> getActiveBorrowsByReaderId(int readerId);
    
    @Query("SELECT * FROM borrows WHERE dueDate < :currentTime AND isReturned = 0")
    LiveData<List<Borrow>> getOverdueBorrows(long currentTime);
    
    @Query("UPDATE borrows SET isReturned = 1, returnDate = :returnDate WHERE id = :borrowId")
    void markAsReturned(int borrowId, long returnDate);
} 