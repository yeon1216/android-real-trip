package com.example.realtrip.asynctask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.realtrip.AppHelper;
import com.example.realtrip.MYURL;
import com.example.realtrip.activity.HomeActivity;
import com.example.realtrip.service.ChatService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * LoginCheck 클래스
 * - 서버에 접근하여 로그인이 가능한지 체크
 */
public class LoginCheck extends AsyncTask<String, Void, String> {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    private Context context; // context가 필요한 경우를 위해서
    Activity activity;

    private Intent serviceIntent;

    /**
     * LoginCheck 클래스 생성자
     */
    public LoginCheck(Context context, Activity activity){
        this.context = context;
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        /**
         *
         */
        if(result.equals("-1")){ // 로그인 실패
            Toast.makeText(context,"로그인 실패",Toast.LENGTH_SHORT).show();
        }else{ // 로그인 성공
            int login_member_no = Integer.parseInt(result.trim());

            /**
             * 서비스 시작
             */
            if(ChatService.serviceIntent==null){ // 서비스가 실행 되어야하는 경우
                serviceIntent = new Intent(context, ChatService.class);
                serviceIntent.putExtra("startChatService","startChatService");
                serviceIntent.putExtra("login_member_no",String.valueOf(login_member_no));
                context.startService(serviceIntent);
            }else{ // 서비스가 이미 실행중인 경우
//                serviceIntent = ChatService.serviceIntent;
                /*
                 * 기존 서비스 정지
                 */
                Intent stop_service_intent = new Intent(context, ChatService.class);
                stop_service_intent.putExtra("stopChatService","stopChatService");
                context.startService(stop_service_intent);

                /*
                 * 서비스 재시작
                 */
                serviceIntent = new Intent(context, ChatService.class);
                serviceIntent.putExtra("startChatService","startChatService");
                serviceIntent.putExtra("login_member_no",String.valueOf(login_member_no));
                context.startService(serviceIntent);
            }



            /**
             * 로그인 멤버 번호를 이용하여 로그인한 멤버의 정보를 얻음
             */
            getLoginMemberInfo(login_member_no);
            Log.d(TAG,"login_member_no: "+login_member_no);

        }

    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    /**
     * doInBackground 메소드
     * - 서버에 로그인 요청
     * @param strings : 로그인 할 아이디와 비밀번호
     * @return : 서버에서 응답받은 값을 반환 (0:로그인 성공, 1:로그인 실패)
     */
    @Override
    protected String doInBackground(String... strings) {
        String id = strings[0]; // 로그인 할 id
        String pw = strings[1]; // 로그인 할 pw
        Log.d(TAG,"doInBackground  id(strings[0]): "+id);
        Log.d(TAG,"doInBackground  pw(strings[1]): "+pw);
//        String serverURL = "http://18.221.242.79/query.php"; // 서버 url 주소
        String serverURL = MYURL.URL; // 서버 url 주소
        String postParameters = "mode=login_action&id="+id+"&pw="+pw; // 요청시 보낼 값
        try{
            URL url = new URL(serverURL); // url 생성
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection(); // url을 연결한 HttpURLConnection 객체 생성

            /**
             * HttpURLConnection 설정
             */
            httpURLConnection.setReadTimeout(5000);
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.setRequestMethod("POST"); // post 통신 방식
            httpURLConnection.setDoInput(true); // 읽기모드 지정
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
            Log.d(TAG,"로그인 실패: Error "+e);
            return "1";
        }
    } // doInBackground() 메소드

    /**
     * getLoginMemberInfo() 메소드
     * - 로그인한 멤버 번호로 멤버의 정보를 가져와 sharedreference에 저장하기
     * @param login_member_no 로그인한 멤버 번호
     */
    public void getLoginMemberInfo(int login_member_no){
        Log.d(TAG,"getLoginMemberInfo() 호출");
        Log.d(TAG,"login_member_no: "+login_member_no);

        // Post 인자
        Map<String,String> params = new HashMap<String,String>();
        params.put("mode","find_login_member");
        params.put("login_member_no",Integer.toString(login_member_no));

        JSONObject jsonObject = new JSONObject(params); // Map을 json으로 만듬

//        String url = "http://18.221.242.79/query.php";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                MYURL.URL,
                jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) { // 응답 성공
                        Log.d(TAG,"응답 성공: "+response.toString());

                        /*
                         * 쉐어드에 로그인 멤버 정보 저장
                         */
                        SharedPreferences sharedPreferences = context.getSharedPreferences("myAppData",MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("login_member",response.toString()).commit();

                        Toast.makeText(context, "로그인 성공", Toast.LENGTH_SHORT).show();

                        /*
                         * ChatService를 백그라운드에서 실행시키기위해 ChatService를 종료시킴
                         */
                        if (ChatService.serviceIntent!=null) {
                            context.stopService(ChatService.serviceIntent);
                            ChatService.serviceIntent = null;
                        }



                        /*
                         * 화면 전환
                         */
                        Intent intent = new Intent(context.getApplicationContext(), HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                        activity.finishAffinity();  // 모든 액티비티 클리어

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) { // 응답 실패
                Log.d(TAG,"응답 실패: "+error.toString());
                Toast.makeText(context, "로그인 실패", Toast.LENGTH_SHORT).show();
            }
        }
        );

        AppHelper.requestQueue.add(jsonObjectRequest); // 요청 큐에 위 요청 추가

    }

} // LoginCheck 클래스 (AsyncTask)
