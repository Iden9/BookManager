package com.daixun.bookmanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.daixun.bookmanager.R;
import com.daixun.bookmanager.model.Reader;

import java.util.List;

public class ReaderAdapter extends RecyclerView.Adapter<ReaderAdapter.ReaderViewHolder> {

    private List<Reader> readers;
    private final OnReaderClickListener listener;

    public ReaderAdapter(List<Reader> readers, OnReaderClickListener listener) {
        this.readers = readers;
        this.listener = listener;
    }

    public void setReaders(List<Reader> readers) {
        this.readers = readers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReaderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reader, parent, false);
        return new ReaderViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ReaderViewHolder holder, int position) {
        Reader reader = readers.get(position);
        holder.tvReaderName.setText(reader.getName());
        holder.tvStudentId.setText("学号: " + reader.getStudentId());
        holder.tvDepartment.setText("院系: " + reader.getDepartment());
        holder.tvBorrowCount.setText("已借: " + reader.getCurrentBorrowCount() + "/" + reader.getMaxBorrowCount());
        holder.tvPhone.setText("电话: " + reader.getPhoneNumber());

        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReaderClick(reader);
            }
        });
    }

    @Override
    public int getItemCount() {
        return readers != null ? readers.size() : 0;
    }

    static class ReaderViewHolder extends RecyclerView.ViewHolder {
        final TextView tvReaderName;
        final TextView tvStudentId;
        final TextView tvDepartment;
        final TextView tvBorrowCount;
        final TextView tvPhone;

        ReaderViewHolder(View view) {
            super(view);
            tvReaderName = view.findViewById(R.id.tvReaderName);
            tvStudentId = view.findViewById(R.id.tvStudentId);
            tvDepartment = view.findViewById(R.id.tvDepartment);
            tvBorrowCount = view.findViewById(R.id.tvBorrowCount);
            tvPhone = view.findViewById(R.id.tvPhone);
        }
    }

    public interface OnReaderClickListener {
        void onReaderClick(Reader reader);
    }
} 