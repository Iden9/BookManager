package com.daixun.bookmanager.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.daixun.bookmanager.dao.BookDao;
import com.daixun.bookmanager.db.AppDatabase;
import com.daixun.bookmanager.model.Book;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class BookRepository {
    private final BookDao bookDao;
    private final LiveData<List<Book>> allBooks;
    private final LiveData<List<String>> allCategories;
    private final ExecutorService executorService;

    public BookRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        bookDao = db.bookDao();
        allBooks = bookDao.getAllBooks();
        allCategories = bookDao.getAllCategories();
        executorService = AppDatabase.databaseWriteExecutor;
    }

    public LiveData<List<Book>> getAllBooks() {
        return allBooks;
    }
    
    public LiveData<List<String>> getAllCategories() {
        return allCategories;
    }

    public LiveData<List<Book>> searchBooks(String keyword) {
        return bookDao.searchBooks(keyword);
    }

    public LiveData<List<Book>> getBooksByCategory(String category) {
        return bookDao.getBooksByCategory(category);
    }

    public void insert(Book book) {
        executorService.execute(() -> bookDao.insert(book));
    }

    public void update(Book book) {
        executorService.execute(() -> bookDao.update(book));
    }

    public void delete(Book book) {
        executorService.execute(() -> bookDao.delete(book));
    }

    public CompletableFuture<Book> getBookById(final int id) {
        return CompletableFuture.supplyAsync(() -> bookDao.getBookById(id), executorService);
    }

    public CompletableFuture<Book> getBookByIsbn(final String isbn) {
        return CompletableFuture.supplyAsync(() -> bookDao.getBookByIsbn(isbn), executorService);
    }

    public CompletableFuture<Integer> decreaseAvailableCount(final int bookId) {
        return CompletableFuture.supplyAsync(() -> bookDao.decreaseAvailableCount(bookId), executorService);
    }

    public void increaseAvailableCount(final int bookId) {
        executorService.execute(() -> bookDao.increaseAvailableCount(bookId));
    }
} 