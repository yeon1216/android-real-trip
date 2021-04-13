package com.example.realtrip.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.realtrip.AppHelper;
import com.example.realtrip.ChatDatabaseManager;
import com.example.realtrip.R;
import com.example.realtrip.asynctask.LoginCheck;
import com.example.realtrip.object.Member;
import com.example.realtrip.service.ChatService;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * LoginActivity class : 로그인 화면
 * - 회원가입 화면 이동
 * - 이메일로 회원정보 찾기 화면 이동
 */
public class LoginActivity extends AppCompatActivity {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    EditText email_et; // 아이디 입력 창
    EditText password_et; // 비밀번호 입력 창

    @Override
    protected void onCreate(Bundle savedInstanceState) { // onCreate() 메소드
        Log.d(TAG,"onCreate() 호출");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /**
         * requestQueue가 없으면 requestQueue 생성
         */
        if(AppHelper.requestQueue == null){ // requestQueue 가 없을 경우
            AppHelper.requestQueue = Volley.newRequestQueue(getApplicationContext()); // requestQueue 생성
        }

        /**
         * 여기에 테이블 생성
         */
//        ChatDatabaseManager.getInstance(getApplicationContext());

        /*
            자동로그인 체크
         */
        SharedPreferences sharedPreferences = getSharedPreferences("myAppData",MODE_PRIVATE);
        Gson gson = new Gson();
        String member_str = sharedPreferences.getString("login_member","no_login");
        Log.d(TAG,"member_str: "+member_str);
        if(!"no_login".equals(member_str)){ // 쉐어드에 로그인 되어있는 상태
            Member login_member = gson.fromJson(member_str,Member.class);
//            Log.d(TAG,"login_member.member_no: "+login_member.member_no);
            Log.d(TAG,"login_member.member_email: "+login_member.member_email);
//            Log.d(TAG,"login_member.nickname: "+login_member.member_nickname);
//            Log.d(TAG,"login_member.member_profile_img: "+login_member.member_profile_img);
            Log.d(TAG,"자동로그인 실행");
            Intent intent = new Intent(getApplicationContext(),HomeActivity.class);
            startActivity(intent);
            finishAffinity(); // 모든 액티비티 클리어
        }

        email_et = findViewById(R.id.email_et); // 아이디 입력 창
        if(getIntent().getStringExtra("email")!=null){ // intent에 email이 있다면
            // 이메일 입력창에 초기화 해주기
            email_et.setText(getIntent().getStringExtra("email"));
        }
        password_et = findViewById(R.id.password_et);  // 비밀번호 입력 창

        Button login_btn = findViewById(R.id.login_btn); // 로그인 버튼
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 로그인 버튼 클릭시 이벤트
                String inputId = email_et.getText().toString().trim(); // 입력한 아이디 (이메일)
                String password = password_et.getText().toString().trim(); // 입력한 비밀번호
                if(inputId.length()==0){ // 아이디를 입력하지 않은 경우
                    Toast.makeText(getApplicationContext(),"아이디를 입력해주세요",Toast.LENGTH_SHORT).show();
                }else if(password.length()==0){ // 비밀번호를 입력하지 않은 경우
                    Toast.makeText(getApplicationContext(),"비밀번호를 입력해주세요",Toast.LENGTH_SHORT).show();
                }else{ // 서버에 로그인 요청
                    LoginCheck loginCheck = new LoginCheck(getApplicationContext(), LoginActivity.this);
                    loginCheck.execute(inputId,password);
                }
            }
        });

        TextView join_tv = findViewById(R.id.join_tv); // 회원가입 텍스트뷰
        join_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 회원가입 텍스트뷰 클릭시 이벤트
                Intent intent = new Intent(getApplicationContext(),JoinActivity.class);
                startActivity(intent);
            }
        });

        TextView update_pass_tv = findViewById(R.id.update_pass_tv); // 비밀번호 찾기 텍스트뷰
        update_pass_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 비밀번호 찾기 텍스트뷰 클릭시 이벤트
                Intent intent = new Intent(getApplicationContext(),UpdatePassActivity.class);
                startActivity(intent);
            }
        });



    } // onCreate() 메소드

//    /**
//     * onDestroy() 메소드
//     */
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        Log.d(TAG,"onDestroy()     ChatService.serviceIntent : "+ChatService.serviceIntent);
//
//        /**
//         * ChatService를 백그라운드에서 실행시키기위해 ChatService를 종료시킴
//         */
//        if (ChatService.serviceIntent!=null) {
//            stopService(ChatService.serviceIntent);
//            ChatService.serviceIntent = null;
//        }
//    } // onDestroy() 메소드

} // LoginActivity 클래스
