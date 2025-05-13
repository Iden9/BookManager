package com.daixun.bookmanager.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.daixun.bookmanager.R;
import com.daixun.bookmanager.utils.SessionManager;
import com.daixun.bookmanager.viewmodel.BookViewModel;
import com.daixun.bookmanager.viewmodel.BorrowViewModel;
import com.daixun.bookmanager.viewmodel.ReaderViewModel;

public class HomeFragment extends Fragment {

    private BookViewModel bookViewModel;
    private ReaderViewModel readerViewModel;
    private BorrowViewModel borrowViewModel;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        sessionManager = SessionManager.getInstance(requireContext());
        
        bookViewModel = new ViewModelProvider(requireActivity()).get(BookViewModel.class);
        readerViewModel = new ViewModelProvider(requireActivity()).get(ReaderViewModel.class);
        borrowViewModel = new ViewModelProvider(requireActivity()).get(BorrowViewModel.class);
        
        TextView tvWelcome = view.findViewById(R.id.tvWelcome);
        TextView tvBookCount = view.findViewById(R.id.tvBookCount);
        TextView tvReaderCount = view.findViewById(R.id.tvReaderCount);
        TextView tvBorrowCount = view.findViewById(R.id.tvBorrowCount);
        
        // 设置欢迎信息
        tvWelcome.setText("欢迎您, " + sessionManager.getUsername() + 
                (sessionManager.isAdmin() ? " (管理员)" : " (学生用户)"));
        
        // 观察图书数量
        bookViewModel.getAllBooks().observe(getViewLifecycleOwner(), books -> {
            tvBookCount.setText("图书总数: " + books.size());
        });
        
        // 观察读者数量
        readerViewModel.getAllReaders().observe(getViewLifecycleOwner(), readers -> {
            tvReaderCount.setText("读者总数: " + readers.size());
        });
        
        // 观察借阅数量
        borrowViewModel.getActiveBorrows().observe(getViewLifecycleOwner(), borrows -> {
            tvBorrowCount.setText("当前借阅: " + borrows.size());
        });
        
        return view;
    }
}