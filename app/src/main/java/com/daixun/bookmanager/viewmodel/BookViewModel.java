package com.daixun.bookmanager.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.daixun.bookmanager.model.Book;
import com.daixun.bookmanager.repository.BookRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BookViewModel extends AndroidViewModel {
    
    private final BookRepository repository;
    private final LiveData<List<Book>> allBooks;
    private final LiveData<List<String>> allCategories;
    
    public BookViewModel(@NonNull Application application) {
        super(application);
        repository = new BookRepository(application);
        allBooks = repository.getAllBooks();
        allCategories = repository.getAllCategories();
    }
    
    public LiveData<List<Book>> getAllBooks() {
        return allBooks;
    }
    
    public LiveData<List<String>> getAllCategories() {
        return allCategories;
    }
    
    public LiveData<List<Book>> searchBooks(String keyword) {
        return repository.searchBooks(keyword);
    }
    
    public LiveData<List<Book>> getBooksByCategory(String category) {
        return repository.getBooksByCategory(category);
    }
    
    public void insert(Book book) {
        repository.insert(book);
    }
    
    public void update(Book book) {
        repository.update(book);
    }
    
    public void delete(Book book) {
        repository.delete(book);
    }
    
    public CompletableFuture<Book> getBookById(final int id) {
        return repository.getBookById(id);
    }
    
    public CompletableFuture<Book> getBookByIsbn(final String isbn) {
        return repository.getBookByIsbn(isbn);
    }
    
    public CompletableFuture<Integer> decreaseAvailableCount(final int bookId) {
        return repository.decreaseAvailableCount(bookId);
    }
    
    public void increaseAvailableCount(final int bookId) {
        repository.increaseAvailableCount(bookId);
    }
} 