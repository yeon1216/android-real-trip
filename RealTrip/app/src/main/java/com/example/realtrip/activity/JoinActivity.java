package com.example.realtrip.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.realtrip.R;
import com.example.realtrip.asynctask.CertificationNoCheck;
import com.example.realtrip.asynctask.EmailCheck;
import com.example.realtrip.asynctask.Join;
import com.example.realtrip.asynctask.NicknameDuplicateCheck;

/**
 * JoinActivity class : 회원가입 화면
 * - 이메일, 닉네임, 비밀번호 입력
 * - 이메일 중복 검사 및 인증
 * - 닉네임 중복 검사
 * - 비밀번호 조건 검사
 */
public class JoinActivity extends AppCompatActivity {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    EditText email_et; // 이메일 입력창
    EditText certification_no_et; // 인증번호 입력창
    EditText nickname_et; // 닉네임 입력창
    EditText password_et; // 비밀번호 입력창
    EditText password_check_et; // 비밀번호 확인 입력창
    EditText email_certification_ok_et; // 이메일 인증 수행 했는지 여부 (0: 수행 안했거나 실패, 1: 수행 완료)
    EditText nickname_check_ok_et; // 닉네임 검사를 수행 했는지 여부 (0: 수행 안했거나 실패, 1: 수행 완료)

    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) { // onCreate() 메소드
        Log.d(TAG,"onCreate() 메소드 호출");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        email_et = findViewById(R.id.email_et); // 이메일 입력창
        certification_no_et = findViewById(R.id.certification_no_et); // 인증번호 입력창
        nickname_et = findViewById(R.id.nickname_et); // 닉네임 입력창
        password_et = findViewById(R.id.password_et); // 비밀번호 입력창
        password_check_et = findViewById(R.id.password_check_et); // 비밀번호 확인 입력창
        email_certification_ok_et = findViewById(R.id.email_certification_ok_et); // 이메일 인증 수행 했는지 여부 (0: 수행 안했거나 실패, 1: 수행 완료)
        nickname_check_ok_et = findViewById(R.id.nickname_check_ok_et); // 닉네임 검사를 수행 했는지 여부 (0: 수행 안했거나 실패, 1: 수행 완료)

        /**
         * 닉네임을 수정하면 nickname_check_ok_et를 0으로 바꾸어줌
         */
        nickname_et.addTextChangedListener(new TextWatcher() { // 이메일 입력창 TextWatcher
           @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) { // 입력하기 전
//                Log.d(TAG,"beforeTextChanged() charSequence: "+charSequence+",start: "+start+", count: "+count+", after: "+after);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int count, int after) { // 변화가 있는 경우
//                Log.d(TAG,"onTextChanged() charSequence: "+charSequence+",start: "+start+", count: "+count+", after: "+after);
                nickname_check_ok_et.setText("0"); // nickname_check_ok_et를 0으로 바꾸어줌
            }

            @Override
            public void afterTextChanged(Editable editable) { // 입력이 끝났을 때
//                Log.d(TAG,"afterTextChanged()");
            }
        });

        Button email_certification_btn = findViewById(R.id.email_certification_btn); // 이메일 인증 버튼
        email_certification_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 이메일 인증 버튼 클릭시 이벤트
                email = email_et.getText().toString().trim(); // 이메일
                // 1. 이메일 조건 검사
                // 2. 이메일 중복 검사 (서버)
                // 3. 이상 없다면 email_certification 테이블에 인증번호 생성 (서버)
                EmailCheck emailCheck = new EmailCheck(JoinActivity.this);
                emailCheck.execute(email);
            }
        });

        Button certification_no_check_btn = findViewById(R.id.certification_no_check_btn); // 인증번호 체크 버튼
        certification_no_check_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 인증번호 체크 버튼 클릭시 이벤트
                String certirication_no = certification_no_et.getText().toString().trim(); // 이메일 인증번호
                // 1. 이메일 인증번호 검사 (서버)
                // 2. 이메일 인증 완료시 email_certification_ok_et 1로 수정
                CertificationNoCheck certificationNoCheck = new CertificationNoCheck(JoinActivity.this);
                certificationNoCheck.execute(email,certirication_no);
            }
        });

        final Button nickname_check_btn = findViewById(R.id.nickname_check_btn); // 닉네임 검사 버튼
        nickname_check_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 닉네임 검사 버튼 클릭시 이벤트
                String nickname = nickname_et.getText().toString().trim(); // 닉네임

                // 1. 닉네임 조건 검사
                if(nickname.length()<2){
                    Toast.makeText(getApplicationContext(),"닉네임을 3글자 이상으로 해주세요",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(nickname.length()>8){
                    Toast.makeText(getApplicationContext(),"닉네임을 8글자 이하로 해주세요",Toast.LENGTH_SHORT).show();
                    return;
                }

                // 2. 닉네임 중복 검사 (서버)
                NicknameDuplicateCheck nicknameDuplicateCheck = new NicknameDuplicateCheck(getApplicationContext(),JoinActivity.this);
                nicknameDuplicateCheck.execute(nickname);

                // 3. 닉네임 검사 완료시 nickname_check_ok_et 1로 수정

            }
        });

        Button join_btn = findViewById(R.id.join_btn); // 회원가입 버튼
        join_btn.setOnClickListener(new View.OnClickListener() { // 회원가입 버튼 클릭 이벤트
            @Override
            public void onClick(View view) {
                Log.d(TAG,"회원가입 버튼 클릭");
//                String email = email_et.getText().toString().trim(); // 이메일
//                String certirication_no = certification_no_et.getText().toString().trim(); // 이메일 인증번호
                String nickname = nickname_et.getText().toString().trim(); // 닉네임
                String password = password_et.getText().toString().trim(); // 비밀번호
                String password_check = password_check_et.getText().toString().trim(); // 비밀번호 확인

                // 1. 닉네임 중복검사를 수행했는지 체크
                if("0".equals(nickname_check_ok_et.getText().toString().trim())){ // 닉네임 중복검사가 필요한 경우
                    Toast.makeText(getApplicationContext(),"닉네임 중복검사를 해주세요",Toast.LENGTH_SHORT).show();
                    return;
                }

                // 2. 비밀번호 조건 검사
                if(password.length()<2){
                    Toast.makeText(getApplicationContext(),"비밀번호를 3글자 이상으로 해주세요",Toast.LENGTH_SHORT).show();
                    return;
                }

                // 3. 비밀번호와 비밀번호가 일치하는지 검사
                if(!password.equals(password_check)){
                    Toast.makeText(getApplicationContext(),"비밀번호와 비밀번호 확인이 일치하지 않습니다",Toast.LENGTH_SHORT).show();
                    return;
                }

                // 4. 회원가입 수행 (서버)
                Join join = new Join(getApplicationContext());
                join.execute(email, password, nickname);

            }
        });

    } // onCreate() 메소드

} // JoiActivity 클래스
