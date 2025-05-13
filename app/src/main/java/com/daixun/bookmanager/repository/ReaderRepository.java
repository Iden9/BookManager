package com.daixun.bookmanager.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.daixun.bookmanager.dao.ReaderDao;
import com.daixun.bookmanager.db.AppDatabase;
import com.daixun.bookmanager.model.Reader;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class ReaderRepository {
    private final ReaderDao readerDao;
    private final LiveData<List<Reader>> allReaders;
    private final ExecutorService executorService;

    public ReaderRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        readerDao = db.readerDao();
        allReaders = readerDao.getAllReaders();
        executorService = AppDatabase.databaseWriteExecutor;
    }

    public LiveData<List<Reader>> getAllReaders() {
        return allReaders;
    }

    public LiveData<List<Reader>> searchReaders(String keyword) {
        return readerDao.searchReaders(keyword);
    }

    public void insert(Reader reader) {
        executorService.execute(() -> readerDao.insert(reader));
    }

    public void update(Reader reader) {
        executorService.execute(() -> readerDao.update(reader));
    }

    public void delete(Reader reader) {
        executorService.execute(() -> readerDao.delete(reader));
    }

    public CompletableFuture<Reader> getReaderById(final int id) {
        return CompletableFuture.supplyAsync(() -> readerDao.getReaderById(id), executorService);
    }

    public CompletableFuture<Reader> getReaderByStudentId(final String studentId) {
        return CompletableFuture.supplyAsync(() -> readerDao.getReaderByStudentId(studentId), executorService);
    }

    public CompletableFuture<Integer> increaseBorrowCount(final int readerId) {
        return CompletableFuture.supplyAsync(() -> readerDao.increaseBorrowCount(readerId), executorService);
    }

    public void decreaseBorrowCount(final int readerId) {
        executorService.execute(() -> readerDao.decreaseBorrowCount(readerId));
    }
} 