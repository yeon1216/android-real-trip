package com.example.realtrip.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.realtrip.R;
import com.example.realtrip.activity.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import static android.content.Context.MODE_PRIVATE;

public class MyPageFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_mypage, container, false);

        BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.getMenu().getItem(0).setChecked(false);
        bottomNavigationView.getMenu().getItem(1).setChecked(false);
        bottomNavigationView.getMenu().getItem(2).setChecked(false);
        bottomNavigationView.getMenu().getItem(3).setChecked(true);

        Button logout_btn = view.findViewById(R.id.logout_btn); // 로그아웃 버튼
        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 로그아웃 버튼 클릭시 이벤트
                /**
                 * 쉐어드에서 로그인멤버번호를 0으로 바꾸기
                 */
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("myAppData",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("login_member_no",0).commit();

                /**
                 * 로그인 화면으로 이동
                 */
                Intent intent = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                getActivity().finishAffinity(); // 모든 액티비티 클리어
            }
        });
        return view;
    }
}