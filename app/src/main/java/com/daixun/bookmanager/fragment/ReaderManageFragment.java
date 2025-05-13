package com.daixun.bookmanager.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.daixun.bookmanager.R;

public class ReaderManageFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 读者管理界面 - 骨架代码
        View view = inflater.inflate(R.layout.fragment_reader_manage, container, false);
        return view;
    }
} 