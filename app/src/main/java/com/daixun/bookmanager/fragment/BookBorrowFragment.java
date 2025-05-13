package com.daixun.bookmanager.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daixun.bookmanager.R;
import com.daixun.bookmanager.adapter.BookAdapter;
import com.daixun.bookmanager.adapter.BorrowAdapter;
import com.daixun.bookmanager.model.Book;
import com.daixun.bookmanager.model.Borrow;
import com.daixun.bookmanager.model.Reader;
import com.daixun.bookmanager.utils.SessionManager;
import com.daixun.bookmanager.viewmodel.BookViewModel;
import com.daixun.bookmanager.viewmodel.BorrowViewModel;
import com.daixun.bookmanager.viewmodel.ReaderViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BookBorrowFragment extends Fragment implements BookAdapter.OnBookClickListener, BorrowAdapter.OnBorrowClickListener {

    private BookViewModel bookViewModel;
    private BorrowViewModel borrowViewModel;
    private ReaderViewModel readerViewModel;
    private SessionManager sessionManager;
    
    private TabHost tabHost;
    private RecyclerView rvBooks;
    private RecyclerView rvBorrows;
    private EditText etSearch;
    private Button btnSearch;
    private TextView tvNoBooks;
    private TextView tvNoBorrows;
    
    private BookAdapter bookAdapter;
    private BorrowAdapter borrowAdapter;
    
    private Reader currentReader;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_borrow, container, false);
        
        // 初始化ViewModel和SessionManager
        bookViewModel = new ViewModelProvider(requireActivity()).get(BookViewModel.class);
        borrowViewModel = new ViewModelProvider(requireActivity()).get(BorrowViewModel.class);
        readerViewModel = new ViewModelProvider(requireActivity()).get(ReaderViewModel.class);
        sessionManager = SessionManager.getInstance(requireContext());
        
        // 初始化视图
        setupTabHost(view);
        setupBooksTab(view);
        setupBorrowsTab(view);
        
        // 加载当前读者信息
        loadCurrentReader();
        
        return view;
    }
    
    private void setupTabHost(View view) {
        tabHost = view.findViewById(R.id.tabHost);
        tabHost.setup();
        
        // 添加"可借图书"选项卡
        TabHost.TabSpec booksTab = tabHost.newTabSpec("books");
        booksTab.setIndicator("可借图书");
        booksTab.setContent(R.id.tabBooks);
        tabHost.addTab(booksTab);
        
        // 添加"我的借阅"选项卡
        TabHost.TabSpec borrowsTab = tabHost.newTabSpec("borrows");
        borrowsTab.setIndicator("我的借阅");
        borrowsTab.setContent(R.id.tabBorrows);
        tabHost.addTab(borrowsTab);
    }
    
    private void setupBooksTab(View view) {
        rvBooks = view.findViewById(R.id.rvBooks);
        etSearch = view.findViewById(R.id.etSearchBook);
        btnSearch = view.findViewById(R.id.btnSearchBook);
        tvNoBooks = view.findViewById(R.id.tvNoBooks);
        
        // 配置图书列表
        rvBooks.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvBooks.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        bookAdapter = new BookAdapter(new ArrayList<>(), this);
        rvBooks.setAdapter(bookAdapter);
        
        // 加载可借图书
        bookViewModel.getAllBooks().observe(getViewLifecycleOwner(), books -> {
            List<Book> availableBooks = new ArrayList<>();
            for (Book book : books) {
                if (book.getAvailableCount() > 0) {
                    availableBooks.add(book);
                }
            }
            
            if (availableBooks.isEmpty()) {
                rvBooks.setVisibility(View.GONE);
                tvNoBooks.setVisibility(View.VISIBLE);
            } else {
                rvBooks.setVisibility(View.VISIBLE);
                tvNoBooks.setVisibility(View.GONE);
                bookAdapter.setBooks(availableBooks);
            }
        });
        
        // 搜索按钮点击事件
        btnSearch.setOnClickListener(v -> {
            String keyword = etSearch.getText().toString().trim();
            if (TextUtils.isEmpty(keyword)) {
                // 显示所有可借图书
                bookViewModel.getAllBooks().observe(getViewLifecycleOwner(), books -> {
                    List<Book> availableBooks = new ArrayList<>();
                    for (Book book : books) {
                        if (book.getAvailableCount() > 0) {
                            availableBooks.add(book);
                        }
                    }
                    bookAdapter.setBooks(availableBooks);
                });
            } else {
                // 按关键词搜索
                bookViewModel.searchBooks(keyword).observe(getViewLifecycleOwner(), books -> {
                    List<Book> availableBooks = new ArrayList<>();
                    for (Book book : books) {
                        if (book.getAvailableCount() > 0) {
                            availableBooks.add(book);
                        }
                    }
                    
                    if (availableBooks.isEmpty()) {
                        Toast.makeText(requireContext(), "没有找到可借的相关图书", Toast.LENGTH_SHORT).show();
                    }
                    bookAdapter.setBooks(availableBooks);
                });
            }
        });
    }
    
    private void setupBorrowsTab(View view) {
        rvBorrows = view.findViewById(R.id.rvBorrows);
        tvNoBorrows = view.findViewById(R.id.tvNoBorrows);
        
        // 配置借阅列表
        rvBorrows.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvBorrows.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        borrowAdapter = new BorrowAdapter(new ArrayList<>(), this);
        borrowAdapter.setBookViewModel(bookViewModel);
        rvBorrows.setAdapter(borrowAdapter);
    }
    
    private void loadCurrentReader() {
        // 根据当前登录用户名查找对应的读者
        String username = sessionManager.getUsername();
        readerViewModel.getAllReaders().observe(getViewLifecycleOwner(), readers -> {
            for (Reader reader : readers) {
                if (reader.getStudentId().equals(username)) {
                    currentReader = reader;
                    loadBorrowRecords(reader.getId());
                    return;
                }
            }
            
            // 如果找不到对应的读者记录，创建一个新的记录
            if (currentReader == null) {
                showCreateReaderDialog(username);
            }
        });
    }
    
    private void loadBorrowRecords(int readerId) {
        borrowViewModel.getActiveBorrowsByReaderId(readerId).observe(getViewLifecycleOwner(), borrows -> {
            if (borrows.isEmpty()) {
                rvBorrows.setVisibility(View.GONE);
                tvNoBorrows.setVisibility(View.VISIBLE);
            } else {
                rvBorrows.setVisibility(View.VISIBLE);
                tvNoBorrows.setVisibility(View.GONE);
                borrowAdapter.setBorrows(borrows);
            }
        });
    }
    
    private void showCreateReaderDialog(String username) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_reader, null);
        builder.setView(dialogView);
        
        final EditText etName = dialogView.findViewById(R.id.etReaderName);
        final EditText etPhone = dialogView.findViewById(R.id.etReaderPhone);
        final EditText etAddress = dialogView.findViewById(R.id.etReaderAddress);
        
        builder.setTitle("完善读者信息")
                .setMessage("首次使用借阅功能，请完善您的个人信息")
                .setPositiveButton("保存", null)
                .setCancelable(false);
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        // 重写点击事件，防止对话框自动关闭
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String department = "未指定"; // 使用默认值
            String phone = etPhone.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            
            if (TextUtils.isEmpty(name)) {
                etName.setError("请输入姓名");
                return;
            }
            
            // 创建新读者
            Reader newReader = new Reader(name, username, department, phone, address, 5);
            readerViewModel.insert(newReader);
            
            Toast.makeText(requireContext(), "读者信息已保存", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            
            // 延迟加载，确保读者信息已经保存到数据库
            new android.os.Handler().postDelayed(() -> loadCurrentReader(), 500);
        });
    }
    
    @Override
    public void onBookClick(Book book) {
        // 显示图书详情和借阅选项
        showBorrowDialog(book);
    }
    
    @Override
    public void onBorrowClick(Borrow borrow, Book book) {
        // 显示借阅详情和归还选项
        showReturnDialog(borrow, book);
    }
    
    private void showBorrowDialog(Book book) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_borrow_book, null);
        builder.setView(dialogView);
        
        // 初始化视图
        TextView tvBookName = dialogView.findViewById(R.id.tvBookName);
        TextView tvISBN = dialogView.findViewById(R.id.tvISBN);
        TextView tvAuthor = dialogView.findViewById(R.id.tvAuthor);
        TextView tvPublisher = dialogView.findViewById(R.id.tvPublisher);
        TextView tvAvailableCount = dialogView.findViewById(R.id.tvAvailableCount);
        EditText etBorrowDays = dialogView.findViewById(R.id.etBorrowDays);
        
        // 填充数据
        tvBookName.setText(book.getName());
        tvISBN.setText("ISBN: " + book.getIsbn());
        tvAuthor.setText("作者: " + book.getAuthor());
        tvPublisher.setText("出版社: " + book.getPublisher());
        tvAvailableCount.setText("可借数量: " + book.getAvailableCount());
        
        builder.setTitle("借阅图书")
                .setPositiveButton("借阅", null)
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        // 重写点击事件，防止对话框自动关闭
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            // 检查是否有读者信息
            if (currentReader == null) {
                Toast.makeText(requireContext(), "读者信息不存在，请先完善读者信息", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                showCreateReaderDialog(sessionManager.getUsername());
                return;
            }
            
            // 检查是否超过最大借阅数
            if (currentReader.getCurrentBorrowCount() >= currentReader.getMaxBorrowCount()) {
                Toast.makeText(requireContext(), "已达到最大借阅数量", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                return;
            }
            
            // 获取借阅天数
            String borrowDaysStr = etBorrowDays.getText().toString().trim();
            if (TextUtils.isEmpty(borrowDaysStr)) {
                etBorrowDays.setError("请输入借阅天数");
                return;
            }
            
            int borrowDays;
            try {
                borrowDays = Integer.parseInt(borrowDaysStr);
                if (borrowDays <= 0 || borrowDays > 30) {
                    etBorrowDays.setError("借阅天数应在1-30天之间");
                    return;
                }
            } catch (NumberFormatException e) {
                etBorrowDays.setError("请输入有效的数字");
                return;
            }
            
            // 执行借阅流程
            borrowBook(book, currentReader, borrowDays);
            dialog.dismiss();
        });
    }
    
    private void borrowBook(Book book, Reader reader, int borrowDays) {
        // 1. 减少图书可借数量
        CompletableFuture<Integer> decreaseBookFuture = bookViewModel.decreaseAvailableCount(book.getId());
        
        // 2. 增加读者当前借阅数
        CompletableFuture<Integer> increaseReaderFuture = readerViewModel.increaseBorrowCount(reader.getId());
        
        // 3. 创建借阅记录
        CompletableFuture.allOf(decreaseBookFuture, increaseReaderFuture)
                .thenAcceptAsync(v -> {
                    if (decreaseBookFuture.join() > 0 && increaseReaderFuture.join() > 0) {
                        // 成功减少图书数量和增加读者借阅数
                        
                        // 计算借阅日期和应还日期
                        long currentTime = System.currentTimeMillis();
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(currentTime);
                        calendar.add(Calendar.DAY_OF_YEAR, borrowDays);
                        long dueDate = calendar.getTimeInMillis();
                        
                        // 创建新的借阅记录
                        Borrow borrow = new Borrow(book.getId(), reader.getId(), currentTime, dueDate);
                        borrowViewModel.insert(borrow)
                                .thenAccept(id -> {
                                    requireActivity().runOnUiThread(() -> {
                                        Toast.makeText(requireContext(), "借阅成功", Toast.LENGTH_SHORT).show();
                                    });
                                });
                    } else {
                        // 减少图书数量或增加读者借阅数失败
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "借阅失败，请重试", Toast.LENGTH_SHORT).show();
                        });
                    }
                }, java.util.concurrent.Executors.newSingleThreadExecutor());
    }
    
    private void showReturnDialog(Borrow borrow, Book book) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_return_book, null);
        builder.setView(dialogView);
        
        // 初始化视图
        TextView tvBookName = dialogView.findViewById(R.id.tvBookName);
        TextView tvBorrowDate = dialogView.findViewById(R.id.tvBorrowDate);
        TextView tvDueDate = dialogView.findViewById(R.id.tvDueDate);
        TextView tvStatus = dialogView.findViewById(R.id.tvStatus);
        
        // 填充数据
        tvBookName.setText(book.getName());
        tvBorrowDate.setText("借阅日期: " + new Date(borrow.getBorrowDate()).toString());
        tvDueDate.setText("应还日期: " + new Date(borrow.getDueDate()).toString());
        
        // 检查是否逾期
        long currentTime = System.currentTimeMillis();
        boolean isOverdue = currentTime > borrow.getDueDate();
        if (isOverdue) {
            tvStatus.setText("状态: 已逾期");
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            tvStatus.setText("状态: 正常");
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }
        
        builder.setTitle("归还图书")
                .setPositiveButton("归还", (dialog, which) -> returnBook(borrow, book))
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
        
        builder.create().show();
    }
    
    private void returnBook(Borrow borrow, Book book) {
        // 1. 更新借阅记录为已归还
        borrowViewModel.markAsReturned(borrow.getId(), System.currentTimeMillis());
        
        // 2. 增加图书可借数量
        bookViewModel.increaseAvailableCount(book.getId());
        
        // 3. 减少读者当前借阅数
        readerViewModel.decreaseBorrowCount(currentReader.getId());
        
        Toast.makeText(requireContext(), "归还成功", Toast.LENGTH_SHORT).show();
    }
}