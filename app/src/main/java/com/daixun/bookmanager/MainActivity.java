package com.daixun.bookmanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.daixun.bookmanager.fragment.BookBorrowFragment;
import com.daixun.bookmanager.fragment.BookManageFragment;
import com.daixun.bookmanager.fragment.BookSearchFragment;
import com.daixun.bookmanager.fragment.HomeFragment;
import com.daixun.bookmanager.fragment.ReaderManageFragment;
import com.daixun.bookmanager.fragment.SettingsFragment;
import com.daixun.bookmanager.utils.SessionManager;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private SessionManager sessionManager;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化会话管理器
        sessionManager = SessionManager.getInstance(getApplicationContext());
        
        // 检查用户是否已登录
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // 设置工具栏
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 设置抽屉布局
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // 设置抽屉切换开关
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // 更新导航抽屉头部用户信息
        updateNavHeader();
        
        // 根据用户权限设置菜单可见性
        updateMenuVisibility();

        // 默认加载首页
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }
    }

    private void updateNavHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView tvUsername = headerView.findViewById(R.id.tvNavUsername);
        TextView tvUserRole = headerView.findViewById(R.id.tvNavUserRole);
        
        tvUsername.setText(sessionManager.getUsername());
        tvUserRole.setText(sessionManager.isAdmin() ? "管理员" : "学生用户");
    }
    
    private void updateMenuVisibility() {
        boolean isAdmin = sessionManager.isAdmin();
        
        // 图书管理和读者管理只对管理员可见
        navigationView.getMenu().findItem(R.id.nav_book_manage).setVisible(isAdmin);
        navigationView.getMenu().findItem(R.id.nav_reader_manage).setVisible(isAdmin);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;
        
        int itemId = item.getItemId();
        if (itemId == R.id.nav_home) {
            selectedFragment = new HomeFragment();
        } else if (itemId == R.id.nav_book_manage) {
            if (sessionManager.isAdmin()) {
                selectedFragment = new BookManageFragment();
            } else {
                Toast.makeText(this, "权限不足", Toast.LENGTH_SHORT).show();
            }
        } else if (itemId == R.id.nav_book_borrow) {
            selectedFragment = new BookBorrowFragment();
        } else if (itemId == R.id.nav_reader_manage) {
            if (sessionManager.isAdmin()) {
                selectedFragment = new ReaderManageFragment();
            } else {
                Toast.makeText(this, "权限不足", Toast.LENGTH_SHORT).show();
            }
        } else if (itemId == R.id.nav_book_search) {
            selectedFragment = new BookSearchFragment();
        } else if (itemId == R.id.nav_settings) {
            selectedFragment = new SettingsFragment();
        } else if (itemId == R.id.nav_logout) {
            // 退出登录
            sessionManager.logoutUser();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return true;
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    selectedFragment).commit();
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}