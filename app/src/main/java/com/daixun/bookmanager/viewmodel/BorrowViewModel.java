package com.daixun.bookmanager.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.daixun.bookmanager.model.Borrow;
import com.daixun.bookmanager.repository.BorrowRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BorrowViewModel extends AndroidViewModel {
    
    private final BorrowRepository repository;
    private final LiveData<List<Borrow>> allBorrows;
    private final LiveData<List<Borrow>> activeBorrows;
    
    public BorrowViewModel(@NonNull Application application) {
        super(application);
        repository = new BorrowRepository(application);
        allBorrows = repository.getAllBorrows();
        activeBorrows = repository.getActiveBorrows();
    }
    
    public LiveData<List<Borrow>> getAllBorrows() {
        return allBorrows;
    }
    
    public LiveData<List<Borrow>> getActiveBorrows() {
        return activeBorrows;
    }
    
    public LiveData<List<Borrow>> getBorrowsByReaderId(final int readerId) {
        return repository.getBorrowsByReaderId(readerId);
    }
    
    public LiveData<List<Borrow>> getBorrowsByBookId(final int bookId) {
        return repository.getBorrowsByBookId(bookId);
    }
    
    public LiveData<List<Borrow>> getActiveBorrowsByReaderId(final int readerId) {
        return repository.getActiveBorrowsByReaderId(readerId);
    }
    
    public LiveData<List<Borrow>> getOverdueBorrows(final long currentTime) {
        return repository.getOverdueBorrows(currentTime);
    }
    
    public CompletableFuture<Long> insert(Borrow borrow) {
        return repository.insert(borrow);
    }
    
    public void update(Borrow borrow) {
        repository.update(borrow);
    }
    
    public void delete(Borrow borrow) {
        repository.delete(borrow);
    }
    
    public CompletableFuture<Borrow> getBorrowById(final int id) {
        return repository.getBorrowById(id);
    }
    
    public void markAsReturned(final int borrowId, final long returnDate) {
        repository.markAsReturned(borrowId, returnDate);
    }
}