package com.example.realtrip.asynctask;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;
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
 * NicknameDuplicateCheck 클래스
 * - 서버에 닉네임 중복검사 요청
 */
public class NicknameDuplicateCheck extends AsyncTask<String,Void,String> {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    Activity activity;
    Context context;

    public NicknameDuplicateCheck(Context context,Activity activity){
        this.context = context;
        this.activity = activity;
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
        }else if("-1".equals(s.trim())){ // 이미 사용중인 닉에임
            Toast.makeText(context,"이미 사용중인 닉네임입니다",Toast.LENGTH_SHORT).show();
        }else if("0".equals(s.trim())){ // 사용 가능한 닉네임
            Toast.makeText(context,"사용 가능한 닉네임입니다",Toast.LENGTH_SHORT).show();
            EditText nickname_check_ok_et = activity.findViewById(R.id.nickname_check_ok_et);
            nickname_check_ok_et.setText("1");
        }

    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected String doInBackground(String... strings) {
        Log.d(TAG,"doInBackground() 호출");

        String nickname = strings[0]; // 회원가입 닉네임
//        String serverURL = "http://18.221.242.79/query.php"; // 서버 url 주소
        String serverURL = MYURL.URL; // 서버 url 주소
        String postParameters = "mode=nickname_duplicate_check&nickname="+nickname; // 요청시 보낼 값
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
    }
} // NicknameDuplicate 클래스
