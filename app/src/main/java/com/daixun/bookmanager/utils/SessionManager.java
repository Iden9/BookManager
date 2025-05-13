package com.daixun.bookmanager.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.daixun.bookmanager.model.User;

public class SessionManager {
    private static final String PREF_NAME = "BookManagerSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_IS_ADMIN = "isAdmin";
    
    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;
    private final Context context;
    private static SessionManager instance;
    
    private SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }
    
    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context.getApplicationContext());
        }
        return instance;
    }
    
    public void createLoginSession(User user) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, user.getId());
        editor.putString(KEY_USERNAME, user.getUsername());
        editor.putBoolean(KEY_IS_ADMIN, user.isAdmin());
        editor.apply();
    }
    
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    public void logoutUser() {
        editor.clear();
        editor.apply();
    }
    
    public int getUserId() {
        return pref.getInt(KEY_USER_ID, -1);
    }
    
    public String getUsername() {
        return pref.getString(KEY_USERNAME, null);
    }
    
    public boolean isAdmin() {
        return pref.getBoolean(KEY_IS_ADMIN, false);
    }
}