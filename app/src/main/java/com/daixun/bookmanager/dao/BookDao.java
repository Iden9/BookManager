package com.daixun.bookmanager.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.daixun.bookmanager.model.Book;

import java.util.List;

@Dao
public interface BookDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Book book);
    
    @Update
    void update(Book book);
    
    @Delete
    void delete(Book book);
    
    @Query("SELECT * FROM books WHERE id = :id LIMIT 1")
    Book getBookById(int id);
    
    @Query("SELECT * FROM books WHERE isbn = :isbn LIMIT 1")
    Book getBookByIsbn(String isbn);
    
    @Query("SELECT * FROM books")
    LiveData<List<Book>> getAllBooks();
    
    @Query("SELECT * FROM books WHERE name LIKE '%' || :keyword || '%' OR author LIKE '%' || :keyword || '%' OR publisher LIKE '%' || :keyword || '%' OR category LIKE '%' || :keyword || '%'")
    LiveData<List<Book>> searchBooks(String keyword);
    
    @Query("SELECT * FROM books WHERE category = :category")
    LiveData<List<Book>> getBooksByCategory(String category);
    
    @Query("SELECT DISTINCT category FROM books")
    LiveData<List<String>> getAllCategories();
    
    @Query("UPDATE books SET availableCount = availableCount - 1 WHERE id = :bookId AND availableCount > 0")
    int decreaseAvailableCount(int bookId);
    
    @Query("UPDATE books SET availableCount = availableCount + 1 WHERE id = :bookId")
    void increaseAvailableCount(int bookId);
} 