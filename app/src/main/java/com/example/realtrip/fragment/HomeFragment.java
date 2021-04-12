package com.example.realtrip.fragment;

import android.content.ClipData;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.realtrip.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * HomeFragment 클래스
 * - 태그를 활용한 여행 SNS
 */
public class HomeFragment extends Fragment {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) { // onCreateView() 메소드
        View view = inflater.inflate(R.layout.fragment_mypage, container, false);
        BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.getMenu().getItem(0).setChecked(true);
        bottomNavigationView.getMenu().getItem(1).setChecked(false);
        bottomNavigationView.getMenu().getItem(2).setChecked(false);
        bottomNavigationView.getMenu().getItem(3).setChecked(false);
        return inflater.inflate(R.layout.fragment_home, container, false);
    } // onCreateView() 메소드
} // HomeFragment 클래스