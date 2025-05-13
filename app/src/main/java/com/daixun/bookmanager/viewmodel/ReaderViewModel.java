package com.daixun.bookmanager.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.daixun.bookmanager.model.Reader;
import com.daixun.bookmanager.repository.ReaderRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ReaderViewModel extends AndroidViewModel {
    
    private final ReaderRepository repository;
    private final LiveData<List<Reader>> allReaders;
    
    public ReaderViewModel(@NonNull Application application) {
        super(application);
        repository = new ReaderRepository(application);
        allReaders = repository.getAllReaders();
    }
    
    public LiveData<List<Reader>> getAllReaders() {
        return allReaders;
    }
    
    public LiveData<List<Reader>> searchReaders(String keyword) {
        return repository.searchReaders(keyword);
    }
    
    public void insert(Reader reader) {
        repository.insert(reader);
    }
    
    public void update(Reader reader) {
        repository.update(reader);
    }
    
    public void delete(Reader reader) {
        repository.delete(reader);
    }
    
    public CompletableFuture<Reader> getReaderById(final int id) {
        return repository.getReaderById(id);
    }
    
    public CompletableFuture<Reader> getReaderByStudentId(final String studentId) {
        return repository.getReaderByStudentId(studentId);
    }
    
    public CompletableFuture<Integer> increaseBorrowCount(final int readerId) {
        return repository.increaseBorrowCount(readerId);
    }
    
    public void decreaseBorrowCount(final int readerId) {
        repository.decreaseBorrowCount(readerId);
    }
} 