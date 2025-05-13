package com.daixun.bookmanager.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.daixun.bookmanager.dao.BorrowDao;
import com.daixun.bookmanager.db.AppDatabase;
import com.daixun.bookmanager.model.Borrow;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class BorrowRepository {
    private final BorrowDao borrowDao;
    private final LiveData<List<Borrow>> allBorrows;
    private final LiveData<List<Borrow>> activeBorrows;
    private final ExecutorService executorService;

    public BorrowRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        borrowDao = db.borrowDao();
        allBorrows = borrowDao.getAllBorrows();
        activeBorrows = borrowDao.getActiveBorrows();
        executorService = AppDatabase.databaseWriteExecutor;
    }

    public LiveData<List<Borrow>> getAllBorrows() {
        return allBorrows;
    }

    public LiveData<List<Borrow>> getActiveBorrows() {
        return activeBorrows;
    }

    public LiveData<List<Borrow>> getBorrowsByReaderId(final int readerId) {
        return borrowDao.getBorrowsByReaderId(readerId);
    }

    public LiveData<List<Borrow>> getBorrowsByBookId(final int bookId) {
        return borrowDao.getBorrowsByBookId(bookId);
    }

    public LiveData<List<Borrow>> getActiveBorrowsByReaderId(final int readerId) {
        return borrowDao.getActiveBorrowsByReaderId(readerId);
    }

    public LiveData<List<Borrow>> getOverdueBorrows(final long currentTime) {
        return borrowDao.getOverdueBorrows(currentTime);
    }

    public CompletableFuture<Long> insert(Borrow borrow) {
        return CompletableFuture.supplyAsync(() -> borrowDao.insert(borrow), executorService);
    }

    public void update(Borrow borrow) {
        executorService.execute(() -> borrowDao.update(borrow));
    }

    public void delete(Borrow borrow) {
        executorService.execute(() -> borrowDao.delete(borrow));
    }

    public CompletableFuture<Borrow> getBorrowById(final int id) {
        return CompletableFuture.supplyAsync(() -> borrowDao.getBorrowById(id), executorService);
    }

    public void markAsReturned(final int borrowId, final long returnDate) {
        executorService.execute(() -> borrowDao.markAsReturned(borrowId, returnDate));
    }
} 