package com.daixun.bookmanager.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.daixun.bookmanager.R;
import com.daixun.bookmanager.model.Book;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private List<Book> books;
    private final OnBookClickListener listener;

    public BookAdapter(List<Book> books, OnBookClickListener listener) {
        this.books = books;
        this.listener = listener;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = books.get(position);
        holder.tvBookName.setText(book.getName());
        holder.tvAuthor.setText(book.getAuthor());
        holder.tvCategory.setText(book.getCategory());
        holder.tvCount.setText("可借: " + book.getAvailableCount() + "/" + book.getTotalCount());

        // 加载图书封面
        if (!TextUtils.isEmpty(book.getCoverUrl())) {
            Glide.with(holder.itemView.getContext())
                .load(book.getCoverUrl())
                .placeholder(R.drawable.ic_book_cover_placeholder)
                .error(R.drawable.ic_book_cover_placeholder)
                .into(holder.ivBookCover);
        } else {
            // 如果没有封面URL，显示默认图片
            holder.ivBookCover.setImageResource(R.drawable.ic_book_cover_placeholder);
        }

        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookClick(book);
            }
        });
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        final TextView tvBookName;
        final TextView tvAuthor;
        final TextView tvCategory;
        final TextView tvCount;
        final ImageView ivBookCover;

        BookViewHolder(View view) {
            super(view);
            tvBookName = view.findViewById(R.id.tvBookName);
            tvAuthor = view.findViewById(R.id.tvAuthor);
            tvCategory = view.findViewById(R.id.tvCategory);
            tvCount = view.findViewById(R.id.tvCount);
            ivBookCover = view.findViewById(R.id.ivBookCover);
        }
    }

    public interface OnBookClickListener {
        void onBookClick(Book book);
    }
} 