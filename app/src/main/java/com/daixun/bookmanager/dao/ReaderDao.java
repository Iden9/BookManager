package com.daixun.bookmanager.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.daixun.bookmanager.model.Reader;

import java.util.List;

@Dao
public interface ReaderDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Reader reader);
    
    @Update
    void update(Reader reader);
    
    @Delete
    void delete(Reader reader);
    
    @Query("SELECT * FROM readers WHERE id = :id LIMIT 1")
    Reader getReaderById(int id);
    
    @Query("SELECT * FROM readers WHERE studentId = :studentId LIMIT 1")
    Reader getReaderByStudentId(String studentId);
    
    @Query("SELECT * FROM readers")
    LiveData<List<Reader>> getAllReaders();
    
    @Query("SELECT * FROM readers WHERE name LIKE '%' || :keyword || '%' OR studentId LIKE '%' || :keyword || '%' OR department LIKE '%' || :keyword || '%'")
    LiveData<List<Reader>> searchReaders(String keyword);
    
    @Query("UPDATE readers SET currentBorrowCount = currentBorrowCount + 1 WHERE id = :readerId AND currentBorrowCount < maxBorrowCount")
    int increaseBorrowCount(int readerId);
    
    @Query("UPDATE readers SET currentBorrowCount = currentBorrowCount - 1 WHERE id = :readerId AND currentBorrowCount > 0")
    void decreaseBorrowCount(int readerId);
} 