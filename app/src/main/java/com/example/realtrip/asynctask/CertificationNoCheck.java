package com.example.realtrip.asynctask;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.realtrip.MYURL;
import com.example.realtrip.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * CertificationNoCheck 클래스
 * - 인증번호 맞는지 체크
 */

public class CertificationNoCheck extends AsyncTask<String,Void,String> {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    Activity activity;
    Context context;
    String email;

    public CertificationNoCheck(Activity activity){
        this.activity = activity;
        this.context = activity.getApplicationContext();
    } // 생성자

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        if("서버 접근 안됨".equals(s)){ // 서버 접근 안됨
            Toast.makeText(context,"서버 접근 에러",Toast.LENGTH_SHORT).show();
        }else if("-1".equals(s.trim())){ // 올바른 인증번호가 아님
            Toast.makeText(context,"올바른 인증번호를 입력해주세요",Toast.LENGTH_SHORT).show();
        }else if("0".equals(s.trim())){ // 올바른 인증번호
            Toast.makeText(context,"인증번호가 확인되었습니다",Toast.LENGTH_SHORT).show();
            /**
             * 이메일 확인 후
             * -> 이메일, 인증번호 레이아웃 GONE
             * -> 닉네임, 비밀번호, 비밀번호 확인, 회원가입 버튼 VISIBLE
             */
//            LinearLayout email_ll = activity.findViewById(R.id.email_ll); // 이메일 입력 레이아웃
//            email_ll.setVisibility(View.GONE);
            EditText email_et = activity.findViewById(R.id.email_et); // 이메일 입력 창
            email_et.setText(email);
            email_et.setEnabled(false);
            Button email_certification_btn = activity.findViewById(R.id.email_certification_btn); // 이메일 인증 버튼
            email_certification_btn.setVisibility(View.GONE);
            LinearLayout certification_no_ll = activity.findViewById(R.id.certification_no_ll); // 인증번호 입력 레이아웃
            certification_no_ll.setVisibility(View.GONE);
            LinearLayout nickname_ll = activity.findViewById(R.id.nickname_ll); // 닉네임 레이아웃
            nickname_ll.setVisibility(View.VISIBLE);
            LinearLayout password_ll = activity.findViewById(R.id.password_ll); // 비밀번호 레이아웃
            password_ll.setVisibility(View.VISIBLE);
            LinearLayout password_check_ll = activity.findViewById(R.id.password_check_ll); // 비밀번호 확인 레이아웃
            password_check_ll.setVisibility(View.VISIBLE);
            Button join_btn = activity.findViewById(R.id.join_btn); // 회원가입 버튼
            join_btn.setVisibility(View.VISIBLE);
        }else{
            Toast.makeText(context,s,Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected String doInBackground(String... strings) {
        Log.d(TAG,"doInBackground() 호출");

        email = strings[0]; // 이메일
        String certification_no = strings[1]; // 인증번호
        String serverURL = MYURL.URL; // 서버 url 주소
        String postParameters = "mode=certification_no_check&email="+email+"&certification_no="+certification_no; // 요청시 보낼 값
        try{
            URL url = new URL(serverURL);
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection(); //url을 연결한 HttpURLConnection 객체 생성

            /**
             * HttpURLConnection 설정
             */
            httpURLConnection.setReadTimeout(5000);
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.setRequestMethod("POST"); // post 통신 방식
//            httpURLConnection.setDoInput(true); // 읽기모드 지정
//            httpURLConnection.setDoOutput(true); // 쓰기모드 지정
            httpURLConnection.connect();

            /**
             * OutputStream으로 요청 값 보내기?? (이건 더 확인해보기)
             */
            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(postParameters.getBytes("UTF-8"));
            outputStream.flush();
            outputStream.close();

            int responseStatusCode = httpURLConnection.getResponseCode(); // HttpURLConnection 객체로부터 응답 코드 받기
            Log.d(TAG,"response code : "+ responseStatusCode);

            InputStream inputStream;
            if(responseStatusCode == HttpURLConnection.HTTP_OK){ // responseStatusCode가 200인 경우
                inputStream = httpURLConnection.getInputStream(); // input 스트림 개방
            }else{ // responseStatusCode가 200이 아닌 경우
                inputStream = httpURLConnection.getErrorStream();
            }

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8")); // 문자열 세팅

            StringBuilder stringBuilder = new StringBuilder(); // 문자열을 담기 위한 객체 생성
            String line;

            while((line = bufferedReader.readLine())!=null){
                stringBuilder.append(line);
            }

            bufferedReader.close();

            String response_str = stringBuilder.toString().trim();
            Log.d(TAG,"응답: "+response_str);
            return response_str;

        }catch (Exception e){
            Log.d(TAG,"Error "+e);
            return "서버 접근 안됨";
        }
    } // doInBackground() 메소드
} // CertificationNoCheck 클래스
