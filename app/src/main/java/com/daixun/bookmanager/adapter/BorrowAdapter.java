package com.daixun.bookmanager.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.daixun.bookmanager.R;
import com.daixun.bookmanager.model.Book;
import com.daixun.bookmanager.model.Borrow;
import com.daixun.bookmanager.viewmodel.BookViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class BorrowAdapter extends RecyclerView.Adapter<BorrowAdapter.BorrowViewHolder> {

    private List<Borrow> borrows;
    private final OnBorrowClickListener listener;
    private BookViewModel bookViewModel;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public BorrowAdapter(List<Borrow> borrows, OnBorrowClickListener listener) {
        this.borrows = borrows;
        this.listener = listener;
    }

    public void setBorrows(List<Borrow> borrows) {
        this.borrows = borrows;
        notifyDataSetChanged();
    }

    public void setBookViewModel(BookViewModel bookViewModel) {
        this.bookViewModel = bookViewModel;
    }

    @NonNull
    @Override
    public BorrowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_borrow, parent, false);
        return new BorrowViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BorrowViewHolder holder, int position) {
        Borrow borrow = borrows.get(position);
        
        // 格式化日期
        String borrowDate = dateFormat.format(new Date(borrow.getBorrowDate()));
        String dueDate = dateFormat.format(new Date(borrow.getDueDate()));
        
        // 获取图书信息
        if (bookViewModel != null) {
            try {
                Book book = bookViewModel.getBookById(borrow.getBookId()).get();
                if (book != null) {
                    holder.tvBookName.setText(book.getName());
                    
                    // 设置点击事件
                    holder.itemView.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onBorrowClick(borrow, book);
                        }
                    });
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                holder.tvBookName.setText("未知图书");
            }
        } else {
            holder.tvBookName.setText("未知图书");
        }
        
        holder.tvBorrowDate.setText("借阅: " + borrowDate);
        holder.tvDueDate.setText("应还: " + dueDate);
        
        // 检查是否逾期
        long currentTime = System.currentTimeMillis();
        if (currentTime > borrow.getDueDate() && !borrow.isReturned()) {
            holder.tvStatus.setText("已逾期");
            holder.tvStatus.setTextColor(Color.RED);
        } else if (borrow.isReturned()) {
            holder.tvStatus.setText("已归还");
            holder.tvStatus.setTextColor(Color.GREEN);
        } else {
            holder.tvStatus.setText("借阅中");
            holder.tvStatus.setTextColor(Color.BLUE);
        }
    }

    @Override
    public int getItemCount() {
        return borrows != null ? borrows.size() : 0;
    }

    static class BorrowViewHolder extends RecyclerView.ViewHolder {
        final TextView tvBookName;
        final TextView tvBorrowDate;
        final TextView tvDueDate;
        final TextView tvStatus;

        BorrowViewHolder(View view) {
            super(view);
            tvBookName = view.findViewById(R.id.tvBookName);
            tvBorrowDate = view.findViewById(R.id.tvBorrowDate);
            tvDueDate = view.findViewById(R.id.tvDueDate);
            tvStatus = view.findViewById(R.id.tvStatus);
        }
    }

    public interface OnBorrowClickListener {
        void onBorrowClick(Borrow borrow, Book book);
    }
} 