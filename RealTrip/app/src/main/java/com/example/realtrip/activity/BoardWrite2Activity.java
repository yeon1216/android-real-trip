package com.example.realtrip.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.realtrip.AppHelper;
import com.example.realtrip.R;
import com.example.realtrip.MYURL;
import com.example.realtrip.object.Member;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

/**
 * BoardWrite2Activity 클래스
 * - 글 내용과 태그를 등록
 */
public class BoardWrite2Activity extends AppCompatActivity {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    EditText board_content_et; // 글 내용 작성 창

    @Override
    protected void onCreate(Bundle savedInstanceState) { // onCreate() 메소드
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_write2);

        board_content_et = findViewById(R.id.board_content_et); // 글 내용 작성 창
        board_content_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) { // 입력하기 전
//                Log.d(TAG,"beforeTextChanged() charSequence: "+charSequence+",start: "+start+", count: "+count+", after: "+after);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int count, int after) { // 변화가 있는 경우
//                Log.d(TAG,"onTextChanged() charSequence: "+charSequence+",start: "+start+", count: "+count+", after: "+after);

            }

            @Override
            public void afterTextChanged(Editable editable) { // 입력이 끝났을 때
//                Log.d(TAG,"afterTextChanged()");
            }
        });

        Button write_btn = findViewById(R.id.write_btn); // 글 작성 버튼
        write_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 글 작성 버튼 클릭시 이벤트
                // 1. 작성한 글 정보와 작성자 정보 가지고 오기
                String board_content = board_content_et.getText().toString();
                SharedPreferences sharedPreferences = getSharedPreferences("myAppData",MODE_PRIVATE);
                int login_member_no = (new Gson()).fromJson(sharedPreferences.getString("login_member","no_login"),Member.class).member_no; // 현재 로그인 한 멤버의 멤버번호

                // 2. 디비에 저장
                writeBoardRequest(board_content,login_member_no);


                // 3. 화면 전환
            }
        });

    } // onCreate() 메소드

    /**
     * writeBoardRequest() 글작성 요청 메소드
     * @param board_content 글 내용
     * @param login_member_no 로그인한 멤버 번호
     */
    void writeBoardRequest(final String board_content,final int login_member_no){
        Log.d(TAG,"writeBoardRequest() 메소드 호출");
        Log.d(TAG,"board_content: "+board_content+", login_member_no: "+login_member_no);

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                MYURL.URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) { // 응답이 성공
                        Log.d(TAG,"응답 성공: "+response);
                        if("0".equals(response)){ // 게시글 작성 성공
                            Toast.makeText(getApplicationContext(),"게시글이 작성되었습니다",Toast.LENGTH_LONG).show();
                            /**
                             * 화면 전환
                             */
                            Intent intent = new Intent(getApplicationContext(),HomeActivity.class);
                            intent.putExtra("write_board","write_board");
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }else{ // 게시글 작성 실패 또는 기타 에러
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
                params.put("mode","write_board");
                params.put("board_content",board_content);
                params.put("board_write_member_no",Integer.toString(login_member_no));
                return params;
            }
        };

        stringRequest.setShouldCache(false); // 이건 무엇인지 알아보기
        AppHelper.requestQueue.add(stringRequest); // 요청을 requestQueue에 담음

    } // writeBoardRequest() 메소드

} // BoardWrite2Activity 클래스
