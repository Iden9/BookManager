package com.daixun.bookmanager.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.daixun.bookmanager.R;
import com.daixun.bookmanager.model.User;
import com.daixun.bookmanager.utils.SessionManager;
import com.daixun.bookmanager.viewmodel.UserViewModel;

public class SettingsFragment extends Fragment {

    private UserViewModel userViewModel;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        
        sessionManager = SessionManager.getInstance(requireContext());
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        
        Button btnChangePassword = view.findViewById(R.id.btnChangePassword);
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        
        // 只有管理员可以创建用户
        Button btnCreateUser = view.findViewById(R.id.btnCreateUser);
        if (sessionManager.isAdmin()) {
            btnCreateUser.setVisibility(View.VISIBLE);
            btnCreateUser.setOnClickListener(v -> showCreateUserDialog());
        } else {
            btnCreateUser.setVisibility(View.GONE);
        }
        
        return view;
    }
    
    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView);
        
        final EditText etOldPassword = dialogView.findViewById(R.id.etCurrentPassword);
        final EditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        final EditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);
        
        builder.setTitle("修改密码")
                .setPositiveButton("确定", null)
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        // 重写点击事件，防止对话框自动关闭
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String oldPassword = etOldPassword.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();
            
            if (TextUtils.isEmpty(oldPassword)) {
                etOldPassword.setError("请输入旧密码");
                return;
            }
            
            if (TextUtils.isEmpty(newPassword)) {
                etNewPassword.setError("请输入新密码");
                return;
            }
            
            if (TextUtils.isEmpty(confirmPassword)) {
                etConfirmPassword.setError("请确认新密码");
                return;
            }
            
            if (!newPassword.equals(confirmPassword)) {
                etConfirmPassword.setError("两次输入的密码不一致");
                return;
            }
            
            // 验证旧密码
            String username = sessionManager.getUsername();
            userViewModel.login(username, oldPassword)
                    .thenAccept(user -> {
                        if (user != null) {
                            // 密码验证成功，更新密码
                            user.setPassword(newPassword);
                            userViewModel.update(user);
                            
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "密码修改成功", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            });
                        } else {
                            // 密码验证失败
                            requireActivity().runOnUiThread(() -> {
                                etOldPassword.setError("旧密码错误");
                            });
                        }
                    });
        });
    }
    
    private void showCreateUserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_user, null);
        builder.setView(dialogView);
        
        final EditText etUsername = dialogView.findViewById(R.id.etUsername);
        final EditText etPassword = dialogView.findViewById(R.id.etPassword);
        final Button rbAdmin = dialogView.findViewById(R.id.rbAdmin);
        final Button rbStudent = dialogView.findViewById(R.id.rbStudent);
        
        // 默认选择学生用户
        rbStudent.setSelected(true);
        
        builder.setTitle("创建用户")
                .setPositiveButton("确定", null)
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        // 单选按钮组逻辑
        rbAdmin.setOnClickListener(v -> {
            rbAdmin.setSelected(true);
            rbStudent.setSelected(false);
        });
        
        rbStudent.setOnClickListener(v -> {
            rbAdmin.setSelected(false);
            rbStudent.setSelected(true);
        });
        
        // 重写点击事件，防止对话框自动关闭
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            boolean isAdmin = rbAdmin.isSelected();
            
            if (TextUtils.isEmpty(username)) {
                etUsername.setError("请输入用户名");
                return;
            }
            
            if (TextUtils.isEmpty(password)) {
                etPassword.setError("请输入密码");
                return;
            }
            
            // 检查用户名是否已存在
            userViewModel.getUserByUsername(username)
                    .thenAccept(existingUser -> {
                        if (existingUser != null) {
                            requireActivity().runOnUiThread(() -> {
                                etUsername.setError("用户名已存在");
                            });
                        } else {
                            // 创建新用户
                            User newUser = new User(username, password, isAdmin);
                            userViewModel.insert(newUser);
                            
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "用户创建成功", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            });
                        }
                    });
        });
    }
} 