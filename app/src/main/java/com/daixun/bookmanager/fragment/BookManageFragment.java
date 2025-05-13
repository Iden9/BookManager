package com.daixun.bookmanager.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.daixun.bookmanager.R;
import com.daixun.bookmanager.adapter.BookAdapter;
import com.daixun.bookmanager.model.Book;
import com.daixun.bookmanager.utils.SessionManager;
import com.daixun.bookmanager.viewmodel.BookViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookManageFragment extends Fragment implements BookAdapter.OnBookClickListener {

    private static final int PICK_IMAGE_REQUEST = 1;

    private BookViewModel bookViewModel;
    private BookAdapter adapter;
    private RecyclerView recyclerView;
    private EditText etSearch;
    private Button btnSearch;
    private TextView tvEmptyList;
    private FloatingActionButton fabAddBook;
    private SessionManager sessionManager;
    
    // 图书封面相关
    private ImageView ivBookCoverPreview;
    private Uri selectedImageUri = null;
    private String currentBookCoverUrl = null;
    private AlertDialog currentDialog = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_manage, container, false);
        
        // 初始化ViewModel和Session
        bookViewModel = new ViewModelProvider(requireActivity()).get(BookViewModel.class);
        sessionManager = SessionManager.getInstance(requireContext());
        
        // 初始化视图
        etSearch = view.findViewById(R.id.etSearch);
        btnSearch = view.findViewById(R.id.btnSearch);
        recyclerView = view.findViewById(R.id.recyclerView);
        tvEmptyList = view.findViewById(R.id.tvEmptyList);
        fabAddBook = view.findViewById(R.id.fabAddBook);
        
        // 设置RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), LinearLayout.VERTICAL));
        adapter = new BookAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);
        
        // 加载全部图书
        bookViewModel.getAllBooks().observe(getViewLifecycleOwner(), books -> {
            if (books.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                tvEmptyList.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                tvEmptyList.setVisibility(View.GONE);
                adapter.setBooks(books);
            }
        });
        
        // 搜索按钮点击事件
        btnSearch.setOnClickListener(v -> {
            String keyword = etSearch.getText().toString().trim();
            if (TextUtils.isEmpty(keyword)) {
                // 如果搜索框为空，显示全部图书
                bookViewModel.getAllBooks().observe(getViewLifecycleOwner(), books -> {
                    adapter.setBooks(books);
                });
            } else {
                // 否则按关键词搜索
                bookViewModel.searchBooks(keyword).observe(getViewLifecycleOwner(), books -> {
                    if (books.isEmpty()) {
                        Toast.makeText(requireContext(), "没有找到相关图书", Toast.LENGTH_SHORT).show();
                    }
                    adapter.setBooks(books);
                });
            }
        });
        
        // 添加图书按钮点击事件（仅管理员可见）
        if (sessionManager.isAdmin()) {
            fabAddBook.setVisibility(View.VISIBLE);
            fabAddBook.setOnClickListener(v -> showAddBookDialog());
        } else {
            fabAddBook.setVisibility(View.GONE);
        }
        
        return view;
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            
            // 更新预览图片
            if (ivBookCoverPreview != null && selectedImageUri != null) {
                Glide.with(this)
                    .load(selectedImageUri)
                    .placeholder(R.drawable.ic_book_cover_placeholder)
                    .into(ivBookCoverPreview);
                
                // 清空URL输入框，因为现在使用的是选择的图片
                if (currentDialog != null) {
                    EditText etCoverUrl = currentDialog.findViewById(R.id.etCoverUrl);
                    if (etCoverUrl != null) {
                        etCoverUrl.setText("");
                    }
                }
                
                Toast.makeText(requireContext(), "图片已选择", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    public void onBookClick(Book book) {
        // 检查是否有权限编辑
        if (sessionManager.isAdmin()) {
            showEditBookDialog(book);
        } else {
            showBookDetailsDialog(book);
        }
    }
    
    private void showAddBookDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_book, null);
        builder.setView(dialogView);
        
        // 初始化对话框视图
        EditText etISBN = dialogView.findViewById(R.id.etISBN);
        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etAuthor = dialogView.findViewById(R.id.etAuthor);
        EditText etPublisher = dialogView.findViewById(R.id.etPublisher);
        EditText etCategory = dialogView.findViewById(R.id.etCategory);
        EditText etPrice = dialogView.findViewById(R.id.etPrice);
        EditText etTotalCount = dialogView.findViewById(R.id.etTotalCount);
        EditText etLocation = dialogView.findViewById(R.id.etLocation);
        EditText etCoverUrl = dialogView.findViewById(R.id.etCoverUrl);
        
        // 初始化封面相关视图
        ivBookCoverPreview = dialogView.findViewById(R.id.ivBookCoverPreview);
        Button btnSelectCover = dialogView.findViewById(R.id.btnSelectCover);
        Button btnRemoveCover = dialogView.findViewById(R.id.btnRemoveCover);
        
        // 重置封面选择状态
        selectedImageUri = null;
        currentBookCoverUrl = null;
        
        // 设置封面选择按钮点击事件
        btnSelectCover.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "选择图书封面"), PICK_IMAGE_REQUEST);
        });
        
        // 设置移除封面按钮点击事件
        btnRemoveCover.setOnClickListener(v -> {
            selectedImageUri = null;
            currentBookCoverUrl = null;
            etCoverUrl.setText("");
            ivBookCoverPreview.setImageResource(R.drawable.ic_book_cover_placeholder);
            Toast.makeText(requireContext(), "已移除封面图片", Toast.LENGTH_SHORT).show();
        });
        
        builder.setTitle("添加图书")
                .setPositiveButton("保存", null)
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
        
        AlertDialog dialog = builder.create();
        currentDialog = dialog;
        dialog.show();
        
        // 重写点击事件，防止对话框自动关闭
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            // 获取输入值
            String isbn = etISBN.getText().toString().trim();
            String name = etName.getText().toString().trim();
            String author = etAuthor.getText().toString().trim();
            String publisher = etPublisher.getText().toString().trim();
            String category = etCategory.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();
            String totalCountStr = etTotalCount.getText().toString().trim();
            String location = etLocation.getText().toString().trim();
            String coverUrl = etCoverUrl.getText().toString().trim();
            
            // 验证输入
            if (TextUtils.isEmpty(isbn)) {
                etISBN.setError("请输入ISBN");
                return;
            }
            if (TextUtils.isEmpty(name)) {
                etName.setError("请输入书名");
                return;
            }
            if (TextUtils.isEmpty(author)) {
                etAuthor.setError("请输入作者");
                return;
            }
            if (TextUtils.isEmpty(publisher)) {
                etPublisher.setError("请输入出版社");
                return;
            }
            if (TextUtils.isEmpty(category)) {
                etCategory.setError("请输入分类");
                return;
            }
            if (TextUtils.isEmpty(priceStr)) {
                etPrice.setError("请输入价格");
                return;
            }
            if (TextUtils.isEmpty(totalCountStr)) {
                etTotalCount.setError("请输入数量");
                return;
            }
            if (TextUtils.isEmpty(location)) {
                etLocation.setError("请输入存放位置");
                return;
            }
            
            // 转换数据类型
            double price;
            int totalCount;
            try {
                price = Double.parseDouble(priceStr);
                totalCount = Integer.parseInt(totalCountStr);
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "请输入有效的数字", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 检查ISBN是否已存在
            bookViewModel.getBookByIsbn(isbn)
                    .thenAccept(existingBook -> {
                        if (existingBook != null) {
                            requireActivity().runOnUiThread(() -> {
                                etISBN.setError("ISBN已存在");
                            });
                        } else {
                            // 处理封面图片
                            String finalCoverUrl = coverUrl;
                            
                            if (selectedImageUri != null) {
                                // 保存选择的图片到应用内部存储
                                String savedImagePath = saveImageToInternalStorage(selectedImageUri, isbn);
                                if (!TextUtils.isEmpty(savedImagePath)) {
                                    finalCoverUrl = savedImagePath;
                                }
                            }
                            
                            // 创建新图书
                            Book newBook = new Book(isbn, name, author, publisher, 
                                    category, price, totalCount, totalCount, location, finalCoverUrl);
                            
                            bookViewModel.insert(newBook);
                            
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "添加图书成功", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            });
                        }
                    });
        });
    }
    
    private void showEditBookDialog(Book book) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_book, null);
        builder.setView(dialogView);
        
        // 初始化对话框视图并填充现有数据
        EditText etISBN = dialogView.findViewById(R.id.etISBN);
        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etAuthor = dialogView.findViewById(R.id.etAuthor);
        EditText etPublisher = dialogView.findViewById(R.id.etPublisher);
        EditText etCategory = dialogView.findViewById(R.id.etCategory);
        EditText etPrice = dialogView.findViewById(R.id.etPrice);
        EditText etTotalCount = dialogView.findViewById(R.id.etTotalCount);
        EditText etLocation = dialogView.findViewById(R.id.etLocation);
        EditText etCoverUrl = dialogView.findViewById(R.id.etCoverUrl);
        
        // 初始化封面相关视图
        ivBookCoverPreview = dialogView.findViewById(R.id.ivBookCoverPreview);
        Button btnSelectCover = dialogView.findViewById(R.id.btnSelectCover);
        Button btnRemoveCover = dialogView.findViewById(R.id.btnRemoveCover);
        
        // 填充现有数据
        etISBN.setText(book.getIsbn());
        etName.setText(book.getName());
        etAuthor.setText(book.getAuthor());
        etPublisher.setText(book.getPublisher());
        etCategory.setText(book.getCategory());
        etPrice.setText(String.valueOf(book.getPrice()));
        etTotalCount.setText(String.valueOf(book.getTotalCount()));
        etLocation.setText(book.getLocation());
        etCoverUrl.setText(book.getCoverUrl());
        
        // 显示当前封面
        currentBookCoverUrl = book.getCoverUrl();
        selectedImageUri = null;
        
        if (!TextUtils.isEmpty(currentBookCoverUrl)) {
            // 如果有封面URL，加载显示
            Glide.with(this)
                .load(currentBookCoverUrl)
                .placeholder(R.drawable.ic_book_cover_placeholder)
                .error(R.drawable.ic_book_cover_placeholder)
                .into(ivBookCoverPreview);
        } else {
            // 没有封面，显示占位图
            ivBookCoverPreview.setImageResource(R.drawable.ic_book_cover_placeholder);
        }
        
        // 设置封面选择按钮点击事件
        btnSelectCover.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "选择图书封面"), PICK_IMAGE_REQUEST);
        });
        
        // 设置移除封面按钮点击事件
        btnRemoveCover.setOnClickListener(v -> {
            selectedImageUri = null;
            currentBookCoverUrl = null;
            etCoverUrl.setText("");
            ivBookCoverPreview.setImageResource(R.drawable.ic_book_cover_placeholder);
            Toast.makeText(requireContext(), "已移除封面图片", Toast.LENGTH_SHORT).show();
        });
        
        // ISBN不允许修改
        etISBN.setEnabled(false);
        
        builder.setTitle("编辑图书")
                .setPositiveButton("保存", null)
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                .setNeutralButton("删除", (dialog, which) -> {
                    // 弹出确认删除对话框
                    new AlertDialog.Builder(requireContext())
                            .setTitle("确认删除")
                            .setMessage("确定要删除《" + book.getName() + "》吗？")
                            .setPositiveButton("确定", (dialog1, which1) -> {
                                bookViewModel.delete(book);
                                Toast.makeText(requireContext(), "图书已删除", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("取消", null)
                            .show();
                });
        
        AlertDialog dialog = builder.create();
        currentDialog = dialog;
        dialog.show();
        
        // 重写点击事件，防止对话框自动关闭
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            // 获取输入值
            String name = etName.getText().toString().trim();
            String author = etAuthor.getText().toString().trim();
            String publisher = etPublisher.getText().toString().trim();
            String category = etCategory.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();
            String totalCountStr = etTotalCount.getText().toString().trim();
            String location = etLocation.getText().toString().trim();
            String coverUrl = etCoverUrl.getText().toString().trim();
            
            // 验证输入
            if (TextUtils.isEmpty(name)) {
                etName.setError("请输入书名");
                return;
            }
            if (TextUtils.isEmpty(author)) {
                etAuthor.setError("请输入作者");
                return;
            }
            if (TextUtils.isEmpty(publisher)) {
                etPublisher.setError("请输入出版社");
                return;
            }
            if (TextUtils.isEmpty(category)) {
                etCategory.setError("请输入分类");
                return;
            }
            if (TextUtils.isEmpty(priceStr)) {
                etPrice.setError("请输入价格");
                return;
            }
            if (TextUtils.isEmpty(totalCountStr)) {
                etTotalCount.setError("请输入数量");
                return;
            }
            if (TextUtils.isEmpty(location)) {
                etLocation.setError("请输入存放位置");
                return;
            }
            
            // 转换数据类型
            double price;
            int totalCount;
            try {
                price = Double.parseDouble(priceStr);
                totalCount = Integer.parseInt(totalCountStr);
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "请输入有效的数字", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 计算可借数量变化
            int availableCountDiff = totalCount - book.getTotalCount();
            int newAvailableCount = book.getAvailableCount() + availableCountDiff;
            
            // 处理封面图片
            String finalCoverUrl;
            
            if (selectedImageUri != null) {
                // 保存选择的图片到应用内部存储
                String savedImagePath = saveImageToInternalStorage(selectedImageUri, book.getIsbn());
                if (!TextUtils.isEmpty(savedImagePath)) {
                    finalCoverUrl = savedImagePath;
                } else {
                    // 保存失败，使用输入的URL或原来的URL
                    finalCoverUrl = !TextUtils.isEmpty(coverUrl) ? coverUrl : currentBookCoverUrl;
                }
            } else if (!TextUtils.isEmpty(coverUrl)) {
                // 使用输入的URL
                finalCoverUrl = coverUrl;
            } else {
                // 没有新选择的图片，也没有输入URL，保持原URL不变或清空
                finalCoverUrl = currentBookCoverUrl != null ? currentBookCoverUrl : "";
            }
            
            // 更新图书信息
            book.setName(name);
            book.setAuthor(author);
            book.setPublisher(publisher);
            book.setCategory(category);
            book.setPrice(price);
            book.setTotalCount(totalCount);
            book.setAvailableCount(newAvailableCount < 0 ? 0 : newAvailableCount);
            book.setLocation(location);
            book.setCoverUrl(finalCoverUrl);
            
            bookViewModel.update(book);
            Toast.makeText(requireContext(), "图书信息更新成功", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
    }
    
    private void showBookDetailsDialog(Book book) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_book_details, null);
        builder.setView(dialogView);
        
        // 初始化视图并填充数据
        ImageView ivCover = dialogView.findViewById(R.id.ivBookCover);
        TextView tvBookName = dialogView.findViewById(R.id.tvBookName);
        TextView tvISBN = dialogView.findViewById(R.id.tvISBN);
        TextView tvAuthor = dialogView.findViewById(R.id.tvAuthor);
        TextView tvPublisher = dialogView.findViewById(R.id.tvPublisher);
        TextView tvCategory = dialogView.findViewById(R.id.tvCategory);
        TextView tvPrice = dialogView.findViewById(R.id.tvPrice);
        TextView tvCount = dialogView.findViewById(R.id.tvCount);
        TextView tvLocation = dialogView.findViewById(R.id.tvLocation);
        
        // 加载封面图片
        if (!TextUtils.isEmpty(book.getCoverUrl())) {
            Glide.with(this)
                .load(book.getCoverUrl())
                .placeholder(R.drawable.ic_book_cover_placeholder)
                .error(R.drawable.ic_book_cover_placeholder)
                .into(ivCover);
        } else {
            ivCover.setImageResource(R.drawable.ic_book_cover_placeholder);
        }
        
        tvBookName.setText(book.getName());
        tvISBN.setText("ISBN: " + book.getIsbn());
        tvAuthor.setText("作者: " + book.getAuthor());
        tvPublisher.setText("出版社: " + book.getPublisher());
        tvCategory.setText("分类: " + book.getCategory());
        tvPrice.setText("价格: ¥" + book.getPrice());
        tvCount.setText("库存: " + book.getAvailableCount() + "/" + book.getTotalCount());
        tvLocation.setText("位置: " + book.getLocation());
        
        builder.setTitle("图书详情")
                .setPositiveButton("确定", (dialog, which) -> dialog.dismiss());
        
        builder.create().show();
    }
    
    /**
     * 保存图片到应用内部存储
     * @param imageUri 图片URI
     * @param isbn 图书ISBN，用于生成唯一文件名
     * @return 保存后的文件路径
     */
    private String saveImageToInternalStorage(Uri imageUri, String isbn) {
        try {
            // 创建时间戳，确保文件名唯一
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "BOOK_" + isbn + "_" + timestamp + ".jpg";
            
            // 获取应用内部存储的书籍封面目录
            File directory = new File(requireContext().getFilesDir(), "book_covers");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            // 创建文件
            File imageFile = new File(directory, fileName);
            
            // 将URI指向的图片复制到内部存储
            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            
            inputStream.close();
            outputStream.close();
            
            // 返回文件路径
            return imageFile.getAbsolutePath();
            
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "保存图片失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return "";
        }
    }
}