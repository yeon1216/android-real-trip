package com.example.realtrip.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.realtrip.R;
import com.example.realtrip.activity.LoginActivity;
import com.example.realtrip.object.Member;
import com.example.realtrip.service.ChatService;
import com.google.gson.Gson;

/**
 * SettingActivity 클래스
 * - 로그아웃, 비밀번호 수정 기능
 * - 기타 여러가지 앱 설정 할때 사용 가능
 */
public class SettingActivity extends AppCompatActivity {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    @Override
    protected void onCreate(Bundle savedInstanceState) { // onCreate() 메소드
        Log.d(TAG,"onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        TextView logout_tv = findViewById(R.id.logout_tv); // 로그아웃 텍스트 뷰
        logout_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 로그아웃 텍스트 뷰 클릭시 이벤트
                /*
                 * 쉐어드에서 로그인멤버 제거
                 */
                SharedPreferences sharedPreferences = getSharedPreferences("myAppData",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                Gson gson = new Gson();
                Member login_member = gson.fromJson(sharedPreferences.getString("login_member","no_login"), Member.class);
                editor.remove("login_member").commit();

                Toast.makeText(getApplicationContext(),"로그아웃 되었습니다",Toast.LENGTH_SHORT).show();

                /**
                 * 채팅 서비스 정지
                 */
                Intent stop_service_intent = new Intent(getApplicationContext(), ChatService.class);
                stop_service_intent.putExtra("stopChatService","stopChatService");
                startService(stop_service_intent);

                /**
                 * 로그인 화면으로 이동
                 */
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.putExtra("email",login_member.member_email);
                startActivity(intent);
                finishAffinity(); // 모든 액티비티 클리어
            }
        });

    } // onCreate() 메소드

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy()");
    }
} // Setting 클래스
