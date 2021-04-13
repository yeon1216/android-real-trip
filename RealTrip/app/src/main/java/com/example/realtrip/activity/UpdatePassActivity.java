package com.example.realtrip.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.realtrip.AppHelper;
import com.example.realtrip.MYURL;

import com.example.realtrip.R;
import com.example.realtrip.asynctask.CertificationNoCheck;
import com.example.realtrip.asynctask.EmailCheck;
import com.example.realtrip.asynctask.IsEmail;

import java.util.HashMap;
import java.util.Map;

/**
 * UpdatePassActivity class : 비밀번호 수정 화면
 * - 이메일, 비밀번호 입력
 * - 이메일 중복 검사 및 인증
 * - 비밀번호 조건 검사
 * - 비밀번호 수정 요청
 */
public class UpdatePassActivity extends AppCompatActivity {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    EditText email_et; // 이메일 입력창
    EditText certification_no_et; // 인증번호 입력창
    EditText password_et; // 비밀번호 입력창
    EditText password_check_et; // 비밀번호 확인 입력창

    String email; // 이메일을 저장해 놓는 전역변수

    @Override
    protected void onCreate(Bundle savedInstanceState) { // onCreate() 메소드
        Log.d(TAG,"onCreate() 메소드 호출");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_pass);

        email_et = findViewById(R.id.email_et); // 이메일 입력창
        certification_no_et = findViewById(R.id.certification_no_et); // 인증번호 입력창
        password_et = findViewById(R.id.password_et); // 비밀번호 입력창
        password_check_et = findViewById(R.id.password_check_et); // 비밀번호 확인 입력창

        /**
         * 이메일 인증
         */
        Button email_certification_btn = findViewById(R.id.email_certification_btn); // 이메일 인증 버튼
        email_certification_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 이메일 인증 버튼 클릭시 이벤트
                email = email_et.getText().toString().trim(); // 이메일
                IsEmail isEmail = new IsEmail(UpdatePassActivity.this); // 이메일이 있는지 체크하는 클래스 (asynctask)
                isEmail.execute(email); // asynctask 실행
//                isEmail(email); // 해당 메일이 있는지 검사 후 메일이 있다면 인증번호 생성 (서버)
            }
        });

        /**
         * 인증번호 체크
         */
        Button certification_no_check_btn = findViewById(R.id.certification_no_check_btn); // 인증번호 체크 버튼
        certification_no_check_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 인증번호 체크 버튼 클릭시 이벤트
            String certirication_no = certification_no_et.getText().toString().trim(); // 이메일 인증번호
            certificationNoCheck(email, certirication_no); // 인증번호 확인 요청 (서버)
            }
        });

        Button update_pass_btn = findViewById(R.id.update_pass_btn); // 비밀번호 수정 버튼
        update_pass_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) { // 비밀번호 수정 버튼 클릭시 이벤트
                String password = password_et.getText().toString().trim(); // 비밀번호
                String password_check = password_check_et.getText().toString().trim(); // 비밀번호 확인
                // 1. 비밀번호 조건 검사
                if(password.length()<2){
                    Toast.makeText(getApplicationContext(),"비밀번호를 3글자 이상으로 해주세요",Toast.LENGTH_SHORT).show();
                    return;
                }
                // 2. 비밀번호와 비밀번호 확인이 일치하는지 검사
                if(!password.equals(password_check)){
                    Toast.makeText(getApplicationContext(),"비밀번호와 비밀번호 확인이 일치하지 않습니다",Toast.LENGTH_SHORT).show();
                    return;
                }

                // 3. 비밀번호 수정 요청 (서버)
                updatePassRequest(email, password);
            }
        });


    } // onCreate() 메소드

    /**
     * 비밀번호 수정 요청 메소드
     * @param email 이메일
     * @param password 비밀번호
     */
    public void updatePassRequest(final String email, final String password){
        Log.d(TAG,"updatePassRequest() 호출");

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                MYURL.URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) { // 응답이 성공
                        Log.d(TAG,"응답 성공: "+response);
                        if("0".equals(response)){ // 비밀번호 수정 성공
                            Toast.makeText(getApplicationContext(),"비밀번호가 수정되었습니다",Toast.LENGTH_LONG).show();
                            /**
                             * 화면 전환
                             */
                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("email",email);
                            startActivity(intent);
                        }else{ // 비밀번호 수정 실패
                            Toast.makeText(getApplicationContext(),response,Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() { // 응답 실패
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG,"응답 에러: "+error.toString());
                        Toast.makeText(getApplicationContext(),error.toString(),Toast.LENGTH_SHORT).show();
                    }
                }

        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String,String>();
                params.put("mode","update_pass");
                params.put("email",email);
                params.put("password",password);
                return params;
            }
        };

        stringRequest.setShouldCache(false); // 이건 무엇인지 알아보기
        AppHelper.requestQueue.add(stringRequest); // 요청을 requestQueue에 담음
    }

    /**
     * 인증번호 확인 요청 메소드
     *  - 인증번호가 맞으면 비밀번호 수정 창 보이게 하기
     * @param email 이메일
     * @param certification_no 인증번호
     */
    public void certificationNoCheck(final String email,final String certification_no){
        Log.d(TAG,"certificationNoCheck() 호출");
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                MYURL.URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) { // 응답이 성공
                        Log.d(TAG,"응답 성공: "+response);
                        if("0".equals(response)){ // 올바른 인증번호 입력
                            Toast.makeText(getApplicationContext(),"인증번호가 확인되었습니다. 비밀번호를 수정해주세요.",Toast.LENGTH_LONG).show();

                            EditText email_et = findViewById(R.id.email_et); // 이메일 입력 창
                            email_et.setText(email);
                            email_et.setEnabled(false);
                            Button email_certification_btn = findViewById(R.id.email_certification_btn); // 이메일 인증 버튼
                            email_certification_btn.setVisibility(View.GONE);
                            LinearLayout certification_no_ll = findViewById(R.id.certification_no_ll); // 인증번호 입력 레이아웃
                            certification_no_ll.setVisibility(View.GONE);
                            LinearLayout password_ll = findViewById(R.id.password_ll); // 비밀번호 레이아웃
                            password_ll.setVisibility(View.VISIBLE);
                            LinearLayout password_check_ll = findViewById(R.id.password_check_ll); // 비밀번호 확인 레이아웃
                            password_check_ll.setVisibility(View.VISIBLE);
                            Button update_pass_btn = findViewById(R.id.update_pass_btn); // 비밀번호 수정 버튼
                            update_pass_btn.setVisibility(View.VISIBLE);
                        }else if("-1".equals(response)){ // 올바르지 않은 인증번호
                            Toast.makeText(getApplicationContext(),"올바른 인증번호를 입력해주세요",Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() { // 응답 실패
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG,"응답 에러: "+error.toString());
                        Toast.makeText(getApplicationContext(),error.toString(),Toast.LENGTH_SHORT).show();
                    }
                }

        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Log.d(TAG,"getParams() email: "+ email);
                Log.d(TAG,"getParams() certification_no: "+ certification_no);
                Map<String,String> params = new HashMap<String,String>();
                params.put("mode","certification_no_check");
                params.put("email",email);
                params.put("certification_no",certification_no);
                return params;
            }
        };

//        stringRequest.setShouldCache(false); // 이건 무엇인지 알아보기
        AppHelper.requestQueue.add(stringRequest); // 요청을 requestQueue에 담음
    }

    /**
     * isEmail()
     * 사용자가 입력한 이메일이 가입된 이메일이 맞는지 확인하고 가입된 이메일이면 인증번호를 생성하고 메일로 보내달라는 요청을 하는 메소드
     * @param email 사용자가 이메일 입력창에 입력한 이메일
     */
    public void isEmail(final String email){
        Log.d(TAG,"isEmail() 호출");
//        String url = "http://18.221.242.79/query.php";
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                MYURL.URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) { // 응답이 성공
                        Log.d(TAG,"응답 성공: "+response);
                        if("0".equals(response)){ // 이메일로 인증번호를 전송하였습니다. 인증번호를 입력해주세요
                            Toast.makeText(getApplicationContext(),"이메일로 인증번호를 전송하였습니다. 인증번호를 입력해주세요",Toast.LENGTH_LONG).show();
                            /**
                             * 인증번호 입력 창 활성화
                             */
                            LinearLayout certification_no_ll = findViewById(R.id.certification_no_ll); // 인증번호 확인 레이아웃
                            certification_no_ll.setVisibility(View.VISIBLE);
                        }else if("-1".equals(response)){ // 가입되지 않은 이메일
                            Toast.makeText(getApplicationContext(),"가입되지 않은 이메일입니다",Toast.LENGTH_LONG).show();
                        }else{ // 인증번호 실패 또는 기타 에러
                            Toast.makeText(getApplicationContext(),response,Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() { // 응답 실패
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG,"응답 에러: "+error.toString());
                        Toast.makeText(getApplicationContext(),error.toString(),Toast.LENGTH_SHORT).show();
                    }
                }

        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Log.d(TAG,"getParams() email: "+ email);
                Map<String,String> params = new HashMap<String,String>();
                params.put("mode","is_email");
                params.put("email",email);
                return params;
            }
        };

        stringRequest.setShouldCache(false); // 이건 무엇인지 알아보기
        AppHelper.requestQueue.add(stringRequest); // 요청을 requestQueue에 담음

    }

} // UpdatePassActivity 클래스
