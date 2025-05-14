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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.daixun.bookmanager.R;
import com.daixun.bookmanager.model.User;
import com.daixun.bookmanager.utils.SessionManager;
import com.daixun.bookmanager.viewmodel.UserViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private UserViewModel userViewModel;
    private SessionManager sessionManager;
    private ImageView ivAvatar;
    private TextView tvUsername;
    private EditText etEmail;
    private EditText etPhone;
    private Button btnSave;
    private Button btnChangePassword;
    private Button btnSelectImage;
    private Uri selectedImageUri = null;
    private String currentAvatarUrl = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // 初始化ViewModel和SessionManager
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        sessionManager = SessionManager.getInstance(requireContext());

        // 初始化视图
        ivAvatar = view.findViewById(R.id.ivAvatar);
        tvUsername = view.findViewById(R.id.tvUsername);
        etEmail = view.findViewById(R.id.etEmail);
        etPhone = view.findViewById(R.id.etPhone);
        btnSave = view.findViewById(R.id.btnSave);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);
        btnSelectImage = view.findViewById(R.id.btnSelectImage);

        // 加载当前用户信息
        loadUserProfile();

        // 设置头像点击事件
        btnSelectImage.setOnClickListener(v -> openImagePicker());

        // 设置保存按钮点击事件
        btnSave.setOnClickListener(v -> saveUserProfile());

        // 设置修改密码按钮点击事件
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        return view;
    }

    private void loadUserProfile() {
        // 从SessionManager获取当前用户ID
        int userId = sessionManager.getUserId();
        if (userId != -1) {
            userViewModel.getUserById(userId).thenAccept(user -> {
                if (user != null) {
                    requireActivity().runOnUiThread(() -> {
                        tvUsername.setText(user.getUsername());
                        etEmail.setText(user.getEmail());
                        etPhone.setText(user.getPhone());
                        currentAvatarUrl = user.getAvatarUrl();

                        // 加载头像
                        if (!TextUtils.isEmpty(currentAvatarUrl)) {
                            Glide.with(requireContext())
                                    .load(currentAvatarUrl)
                                    .placeholder(R.drawable.ic_person)
                                    .error(R.drawable.ic_person)
                                    .into(ivAvatar);
                        }
                    });
                }
            });
        }
    }

    private void saveUserProfile() {
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        int userId = sessionManager.getUserId();

        if (userId != -1) {
            userViewModel.getUserById(userId).thenAccept(user -> {
                if (user != null) {
                    // 保存选择的头像
                    String avatarUrl = currentAvatarUrl;
                    if (selectedImageUri != null) {
                        avatarUrl = saveImageToInternalStorage(selectedImageUri);
                    }

                    // 更新用户信息
                    user.setEmail(email);
                    user.setPhone(phone);
                    if (avatarUrl != null) {
                        user.setAvatarUrl(avatarUrl);
                    }
                    userViewModel.update(user);

                    // 更新SessionManager中的信息
                    sessionManager.updateUserProfile(avatarUrl != null ? avatarUrl : "", email, phone);

                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "个人资料更新成功", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView);

        final EditText etCurrentPassword = dialogView.findViewById(R.id.etCurrentPassword);
        final EditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        final EditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);

        builder.setTitle("修改密码")
                .setPositiveButton("保存", null)
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        // 重写点击事件防止自动关闭
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String currentPassword = etCurrentPassword.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            // 验证输入
            if (TextUtils.isEmpty(currentPassword)) {
                etCurrentPassword.setError("请输入当前密码");
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

            // 验证当前密码
            String username = sessionManager.getUsername();
            userViewModel.login(username, currentPassword).thenAccept(user -> {
                if (user == null) {
                    requireActivity().runOnUiThread(() -> {
                        etCurrentPassword.setError("当前密码不正确");
                    });
                } else {
                    // 更新密码
                    user.setPassword(newPassword);
                    userViewModel.update(user);

                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "密码修改成功", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
                }
            });
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "选择头像"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();

            // 显示选择的图片
            Glide.with(this)
                    .load(selectedImageUri)
                    .into(ivAvatar);
        }
    }

    private String saveImageToInternalStorage(Uri imageUri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                return null;
            }

            // 创建文件名
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "AVATAR_" + sessionManager.getUsername() + "_" + timeStamp + ".jpg";
            File storageDir = new File(requireContext().getFilesDir(), "avatars");

            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }

            File imageFile = new File(storageDir, fileName);
            FileOutputStream outputStream = new FileOutputStream(imageFile);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.flush();
            outputStream.close();

            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
} 