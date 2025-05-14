package com.daixun.bookmanager.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.daixun.bookmanager.adapter.ReaderAdapter;
import com.daixun.bookmanager.model.Reader;
import com.daixun.bookmanager.utils.SessionManager;
import com.daixun.bookmanager.viewmodel.ReaderViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ReaderManageFragment extends Fragment implements ReaderAdapter.OnReaderClickListener {

    private ReaderViewModel readerViewModel;
    private ReaderAdapter adapter;
    private RecyclerView recyclerView;
    private EditText etSearch;
    private Button btnSearch;
    private TextView tvEmptyList;
    private FloatingActionButton fabAddReader;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reader_manage, container, false);
        
        // 初始化ViewModel和Session
        readerViewModel = new ViewModelProvider(requireActivity()).get(ReaderViewModel.class);
        sessionManager = SessionManager.getInstance(requireContext());
        
        // 初始化视图
        etSearch = view.findViewById(R.id.etSearch);
        btnSearch = view.findViewById(R.id.btnSearch);
        recyclerView = view.findViewById(R.id.recyclerView);
        tvEmptyList = view.findViewById(R.id.tvEmptyList);
        fabAddReader = view.findViewById(R.id.fabAddReader);
        
        // 设置RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        adapter = new ReaderAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);
        
        // 加载全部读者
        readerViewModel.getAllReaders().observe(getViewLifecycleOwner(), readers -> {
            if (readers.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                tvEmptyList.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                tvEmptyList.setVisibility(View.GONE);
                adapter.setReaders(readers);
            }
        });
        
        // 搜索按钮点击事件
        btnSearch.setOnClickListener(v -> {
            String keyword = etSearch.getText().toString().trim();
            if (TextUtils.isEmpty(keyword)) {
                // 如果搜索框为空，显示全部读者
                readerViewModel.getAllReaders().observe(getViewLifecycleOwner(), readers -> {
                    adapter.setReaders(readers);
                });
            } else {
                // 否则按关键词搜索
                readerViewModel.searchReaders(keyword).observe(getViewLifecycleOwner(), readers -> {
                    if (readers.isEmpty()) {
                        Toast.makeText(requireContext(), "没有找到相关读者", Toast.LENGTH_SHORT).show();
                    }
                    adapter.setReaders(readers);
                });
            }
        });
        
        // 添加读者按钮点击事件（仅管理员可见）
        if (sessionManager.isAdmin()) {
            fabAddReader.setVisibility(View.VISIBLE);
            fabAddReader.setOnClickListener(v -> showAddReaderDialog());
        } else {
            fabAddReader.setVisibility(View.GONE);
        }
        
        return view;
    }
    
    @Override
    public void onReaderClick(Reader reader) {
        // 检查是否有权限编辑
        if (sessionManager.isAdmin()) {
            showEditReaderDialog(reader);
        } else {
            showReaderDetailsDialog(reader);
        }
    }
    
    private void showAddReaderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_reader, null);
        builder.setView(dialogView);
        
        // 初始化对话框视图
        EditText etStudentId = dialogView.findViewById(R.id.etStudentId);
        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etDepartment = dialogView.findViewById(R.id.etDepartment);
        EditText etPhone = dialogView.findViewById(R.id.etPhone);
        EditText etEmail = dialogView.findViewById(R.id.etEmail);
        EditText etMaxBorrowCount = dialogView.findViewById(R.id.etMaxBorrowCount);
        
        // 设置默认最大借阅数量
        etMaxBorrowCount.setText("5");
        
        builder.setTitle("添加读者")
                .setPositiveButton("保存", null)
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        // 重写点击事件，防止对话框自动关闭
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            // 获取输入值
            String studentId = etStudentId.getText().toString().trim();
            String name = etName.getText().toString().trim();
            String department = etDepartment.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String maxBorrowCountStr = etMaxBorrowCount.getText().toString().trim();
            
            // 验证输入
            if (TextUtils.isEmpty(studentId)) {
                etStudentId.setError("请输入学号");
                return;
            }
            if (TextUtils.isEmpty(name)) {
                etName.setError("请输入姓名");
                return;
            }
            if (TextUtils.isEmpty(department)) {
                etDepartment.setError("请输入院系");
                return;
            }
            if (TextUtils.isEmpty(maxBorrowCountStr)) {
                etMaxBorrowCount.setError("请输入最大借阅数量");
                return;
            }
            
            // 转换数据类型
            int maxBorrowCount;
            try {
                maxBorrowCount = Integer.parseInt(maxBorrowCountStr);
                if (maxBorrowCount <= 0) {
                    etMaxBorrowCount.setError("最大借阅数量必须大于0");
                    return;
                }
            } catch (NumberFormatException e) {
                etMaxBorrowCount.setError("请输入有效的数字");
                return;
            }
            
            // 检查学号是否已存在
            readerViewModel.getReaderByStudentId(studentId)
                    .thenAccept(existingReader -> {
                        if (existingReader != null) {
                            requireActivity().runOnUiThread(() -> {
                                etStudentId.setError("学号已存在");
                            });
                        } else {
                            // 创建新读者
                            Reader newReader = new Reader(name, studentId, department, phone, email, maxBorrowCount);
                            readerViewModel.insert(newReader);
                            
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "添加读者成功", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            });
                        }
                    });
        });
    }
    
    private void showEditReaderDialog(Reader reader) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_reader, null);
        builder.setView(dialogView);
        
        // 初始化对话框视图并填充现有数据
        EditText etStudentId = dialogView.findViewById(R.id.etStudentId);
        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etDepartment = dialogView.findViewById(R.id.etDepartment);
        EditText etPhone = dialogView.findViewById(R.id.etPhone);
        EditText etEmail = dialogView.findViewById(R.id.etEmail);
        EditText etMaxBorrowCount = dialogView.findViewById(R.id.etMaxBorrowCount);
        
        // 填充现有数据
        etStudentId.setText(reader.getStudentId());
        etName.setText(reader.getName());
        etDepartment.setText(reader.getDepartment());
        etPhone.setText(reader.getPhoneNumber());
        etEmail.setText(reader.getEmail());
        etMaxBorrowCount.setText(String.valueOf(reader.getMaxBorrowCount()));
        
        // 学号不允许修改
        etStudentId.setEnabled(false);
        
        builder.setTitle("编辑读者")
                .setPositiveButton("保存", null)
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                .setNeutralButton("删除", (dialog, which) -> {
                    // 弹出确认删除对话框
                    if (reader.getCurrentBorrowCount() > 0) {
                        Toast.makeText(requireContext(), "该读者还有未归还的图书，不能删除", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    new AlertDialog.Builder(requireContext())
                            .setTitle("确认删除")
                            .setMessage("确定要删除读者 " + reader.getName() + " 吗？")
                            .setPositiveButton("确定", (dialog1, which1) -> {
                                readerViewModel.delete(reader);
                                Toast.makeText(requireContext(), "读者已删除", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("取消", null)
                            .show();
                });
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        // 重写点击事件，防止对话框自动关闭
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            // 获取输入值
            String name = etName.getText().toString().trim();
            String department = etDepartment.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String maxBorrowCountStr = etMaxBorrowCount.getText().toString().trim();
            
            // 验证输入
            if (TextUtils.isEmpty(name)) {
                etName.setError("请输入姓名");
                return;
            }
            if (TextUtils.isEmpty(department)) {
                etDepartment.setError("请输入院系");
                return;
            }
            if (TextUtils.isEmpty(maxBorrowCountStr)) {
                etMaxBorrowCount.setError("请输入最大借阅数量");
                return;
            }
            
            // 转换数据类型
            int maxBorrowCount;
            try {
                maxBorrowCount = Integer.parseInt(maxBorrowCountStr);
                if (maxBorrowCount <= 0) {
                    etMaxBorrowCount.setError("最大借阅数量必须大于0");
                    return;
                }
                
                // 检查最大借阅数量是否小于当前借阅数量
                if (maxBorrowCount < reader.getCurrentBorrowCount()) {
                    etMaxBorrowCount.setError("最大借阅数量不能小于当前借阅数量 " + reader.getCurrentBorrowCount());
                    return;
                }
            } catch (NumberFormatException e) {
                etMaxBorrowCount.setError("请输入有效的数字");
                return;
            }
            
            // 更新读者信息
            reader.setName(name);
            reader.setDepartment(department);
            reader.setPhoneNumber(phone);
            reader.setEmail(email);
            reader.setMaxBorrowCount(maxBorrowCount);
            
            readerViewModel.update(reader);
            Toast.makeText(requireContext(), "读者信息更新成功", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
    }
    
    private void showReaderDetailsDialog(Reader reader) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_reader_details, null);
        builder.setView(dialogView);
        
        // 初始化视图并填充数据
        TextView tvReaderName = dialogView.findViewById(R.id.tvReaderName);
        TextView tvStudentId = dialogView.findViewById(R.id.tvStudentId);
        TextView tvDepartment = dialogView.findViewById(R.id.tvDepartment);
        TextView tvPhone = dialogView.findViewById(R.id.tvPhone);
        TextView tvEmail = dialogView.findViewById(R.id.tvEmail);
        TextView tvBorrowCount = dialogView.findViewById(R.id.tvBorrowCount);
        TextView tvMaxBorrowCount = dialogView.findViewById(R.id.tvMaxBorrowCount);
        
        tvReaderName.setText(reader.getName());
        tvStudentId.setText("学号: " + reader.getStudentId());
        tvDepartment.setText("院系: " + reader.getDepartment());
        tvPhone.setText("电话: " + reader.getPhoneNumber());
        tvEmail.setText("邮箱: " + reader.getEmail());
        tvBorrowCount.setText("当前借阅数量: " + reader.getCurrentBorrowCount());
        tvMaxBorrowCount.setText("最大借阅数量: " + reader.getMaxBorrowCount());
        
        builder.setTitle("读者详情")
                .setPositiveButton("确定", (dialog, which) -> dialog.dismiss());
        
        builder.create().show();
    }
} 