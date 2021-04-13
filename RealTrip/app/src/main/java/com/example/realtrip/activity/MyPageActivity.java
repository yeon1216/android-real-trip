package com.example.realtrip.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.realtrip.R;
import com.example.realtrip.object.Member;
import com.example.realtrip.service.ChatService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

/**
 * MyPageActivity 클래스
 * - 여행 후기 SNS
 * - 설계하자!!
 */
public class MyPageActivity extends AppCompatActivity {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    ImageView my_profile_img_iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);



        TextView update_profile_tv = findViewById(R.id.update_profile_tv); // 프로필 수정 텍스트뷰
        update_profile_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 프로필 수정 텍스트뷰 클릭시 이벤트
                Intent intent = new Intent(getApplicationContext(),UpdateProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });


        ImageView setting_iv = findViewById(R.id.setting_iv); // 설정 아이콘 이미지뷰
        setting_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 설정 아이콘 클릭시 이벤트
                Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });
    } // onCreate() 메소드

    @Override
    protected void onResume() { // onResume() 메소드
        super.onResume();
        Log.d(TAG,"onResume() 호출");
        /*
         * 쉐어드에 저장된 멤버 정보 가지고 오기
         */
        SharedPreferences sharedPreferences = getSharedPreferences("myAppData",MODE_PRIVATE);
        Gson gson = new Gson();
        Member login_member = gson.fromJson(sharedPreferences.getString("login_member","no_login"),Member.class);

        my_profile_img_iv = findViewById(R.id.my_profile_img_iv); // 프로필 이미지 이미지뷰
        /**
         * 프로필 사진 동그라미로 만드는 코드
         */
//        my_profile_img_iv.setBackground(new ShapeDrawable(new OvalShape()));
//        my_profile_img_iv.setClipToOutline(true);

        /**
         * 멤버 프로필 사진 적용
         */
        if("default".equals(login_member.member_profile_img)){ // 기본 프로필인 경우
            my_profile_img_iv.setImageResource(R.drawable.default_profile);
        }else{ // 프로필 사진이 있는 경우
            Glide.with(this)
                    .load("http://35.224.156.8/uploads/"+login_member.member_profile_img)
                    .thumbnail(0.1f)
                    .placeholder(R.drawable.loading)
                    .into(my_profile_img_iv);
        }

        TextView my_nickname = findViewById(R.id.my_nickname); // 닉네임 텍스트뷰
        my_nickname.setText(login_member.member_nickname);

        /**
         * 바텀 네비게이션
         */
        overridePendingTransition(0,0); // 애니메이션 효과 없애기 위한 코드
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view); // 바텀 네비게이션 바
        bottomNavigationView.getMenu().getItem(3).setChecked(true); // 마이페이지 메뉴 활성화
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) { // 바텀 네비게이션 메뉴 클릭시 이벤트
                Intent intent;
                switch (menuItem.getItemId()) {
                    case R.id.navigation_home: // 홈 메뉴 클릭 시
                        intent = new Intent(getApplicationContext(),HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
                        return true;
                    case R.id.navigation_real_trip: // 리얼 트립 메뉴 클릭 시
                        intent = new Intent(getApplicationContext(),RealTripActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
                        return true;
                    case R.id.navigation_chat: // 챗 메뉴 클릭 시
                        intent = new Intent(getApplicationContext(), ChatListActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
                        return true;
                    case R.id.navigation_mypage: // 마이페이지 메뉴 클릭 시
                        intent = new Intent(getApplicationContext(),MyPageActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        return true;
                }
                return false;
            }
        });
    } // onResume() 메소드

    @Override
    protected void onPause() { // onPause() 메소드
        super.onPause();
        overridePendingTransition(0,0); // 백키를 눌렀을 때 애니메이션 없애기
    } // onPause() 메소드

    @Override
    protected void onStop() { // onStop() 메소드
        super.onStop();
    } // onStop() 메소드

    /**
     * onDestroy() 메소드
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy()     ChatService.serviceIntent : "+ ChatService.serviceIntent);

        /*
         * ChatService를 백그라운드에서 실행시키기위해 ChatService를 종료시킴
         */
//        if (ChatService.serviceIntent!=null) {
//            stopService(ChatService.serviceIntent);
//            ChatService.serviceIntent = null;
//        }


    } // onDestroy() 메소드

} // MyPageActivity 클래스
