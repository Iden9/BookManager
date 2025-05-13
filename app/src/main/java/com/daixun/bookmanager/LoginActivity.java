package com.daixun.bookmanager;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.daixun.bookmanager.model.User;
import com.daixun.bookmanager.repository.UserRepository;
import com.daixun.bookmanager.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {
    
    private TextInputEditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private UserRepository userRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        // 初始化视图
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        
        // 初始化仓库和会话管理器
        userRepository = new UserRepository(getApplication());
        sessionManager = SessionManager.getInstance(getApplicationContext());
        
        // 如果已经登录，直接进入主界面
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
        
        // 登录按钮点击事件
        btnLogin.setOnClickListener(view -> {
            String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
            
            if (TextUtils.isEmpty(username)) {
                etUsername.setError("请输入用户名");
                return;
            }
            
            if (TextUtils.isEmpty(password)) {
                etPassword.setError("请输入密码");
                return;
            }
            
            // 尝试登录
            userRepository.login(username, password)
                    .thenAccept(user -> {
                        if (user != null) {
                            // 登录成功
                            sessionManager.createLoginSession(user);
                            runOnUiThread(() -> {
                                Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            });
                        } else {
                            // 登录失败
                            runOnUiThread(() -> Toast.makeText(LoginActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show());
                        }
                    })
                    .exceptionally(e -> {
                        runOnUiThread(() -> Toast.makeText(LoginActivity.this, "登录失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        return null;
                    });
        });
        
        // 注册点击事件
        tvRegister.setOnClickListener(view -> {
            // TODO: 跳转到注册页面
            Toast.makeText(LoginActivity.this, "注册功能待实现", Toast.LENGTH_SHORT).show();
        });
    }
}