package com.example.realtrip.activity;



import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
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
import com.example.realtrip.object.Member;
import com.google.gson.Gson;
import com.wowza.gocoder.sdk.api.WowzaGoCoder;
import com.wowza.gocoder.sdk.api.broadcast.WOWZBroadcast;
import com.wowza.gocoder.sdk.api.broadcast.WOWZBroadcastConfig;
import com.wowza.gocoder.sdk.api.configuration.WOWZMediaConfig;
import com.wowza.gocoder.sdk.api.devices.WOWZAudioDevice;
import com.wowza.gocoder.sdk.api.devices.WOWZCamera;
import com.wowza.gocoder.sdk.api.devices.WOWZCameraView;
import com.wowza.gocoder.sdk.api.errors.WOWZError;
import com.wowza.gocoder.sdk.api.errors.WOWZStreamingError;
import com.wowza.gocoder.sdk.api.status.WOWZBroadcastStatus;
import com.wowza.gocoder.sdk.api.status.WOWZBroadcastStatusCallback;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class BroadcastActivity extends AppCompatActivity implements WOWZBroadcastStatusCallback, View.OnClickListener {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    private WowzaGoCoder goCoder; // The top-level GoCoder API interface

    private WOWZCameraView goCoderCameraView; // The GoCoder SDK camera view

    private WOWZAudioDevice goCoderAudioDevice; // The GoCoder SDK audio device

    private WOWZBroadcast goCoderBroadcaster; // The GoCoder SDK broadcaster

    private WOWZBroadcastConfig goCoderBroadcastConfig; // The broadcast configuration settings

    // Properties needed for Android 6+ permissions handling
    private static final int PERMISSIONS_REQUEST_CODE = 0x1;
    private boolean mPermissionsGranted = true;
    private String[] mRequiredPermissions = new String[] {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };

    private boolean is_broadcast; // true : 방송중, false : 방송중이 아님

    Button broadcastButton; // 방송하기 버튼

    Socket socket; // 소켓
    Member login_member;
    ReceiveThread receiveThread;

    String temp_broadcast_title;

    /**
     * onCreate() 메소드
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast);
        Log.d(TAG,"onCreate()");
        is_broadcast = false;

        temp_broadcast_title = getIntent().getStringExtra("temp_broadcast_title");

        /*
         * 쉐어드에 저장된 멤버 정보 가지고 오기
         */
        SharedPreferences sharedPreferences = getSharedPreferences("myAppData",MODE_PRIVATE);
        Gson gson = new Gson();
        login_member = gson.fromJson(sharedPreferences.getString("login_member","no_login"),Member.class);

        /*
            소켓 연결 및 응답 대기
         */
        receiveThread = new ReceiveThread();
        receiveThread.start();

        /*
            wowzaGoCoder 생성 및 초기화
         */
        goCoder = WowzaGoCoder.init(getApplicationContext(), "GOSK-1547-010C-23B6-30F7-7B49"); // Initialize the GoCoder SDK

        if (goCoder == null) { // If initialization failed, retrieve the last error and display it
            WOWZError goCoderInitError = WowzaGoCoder.getLastError();
            Toast.makeText(this, "GoCoder SDK error: " + goCoderInitError.getErrorDescription(), Toast.LENGTH_LONG).show();
            return;
        }

        goCoderCameraView = (WOWZCameraView) findViewById(R.id.camera_preview); // Associate the WOWZCameraView defined in the U/I layout with the corresponding class member

        goCoderAudioDevice = new WOWZAudioDevice(); // Create an audio device instance for capturing and broadcasting audio

        goCoderBroadcaster = new WOWZBroadcast(); // Create a broadcaster instance

        goCoderBroadcastConfig = new WOWZBroadcastConfig(WOWZMediaConfig.FRAME_SIZE_1920x1080); // Create a configuration instance for the broadcaster
//        goCoderBroadcastConfig = new WOWZBroadcastConfig(WOWZMediaConfig.FRAME_SIZE_1280x720); // Create a configuration instance for the broadcaster
//        goCoderBroadcastConfig = new WOWZBroadcastConfig(WOWZMediaConfig.FRAME_SIZE_640x480); // Create a configuration instance for the broadcaster



        if("real_trip".equals(MYURL.which_streaming_str)){
            /*
                goCoderBroadcastConfig 설정 real_trip
             */
            goCoderBroadcastConfig.setHostAddress("d0c95d.entrypoint.cloud.wowza.com");
            goCoderBroadcastConfig.setPortNumber(1935);
            goCoderBroadcastConfig.setApplicationName("app-f84c");
            goCoderBroadcastConfig.setStreamName("a3926374");
            goCoderBroadcastConfig.setUsername("client46561");
            goCoderBroadcastConfig.setPassword("3215a976");
        }else if("1".equals(MYURL.which_streaming_str)){
            /*
                goCoderBroadcastConfig 설정 1
             */
            goCoderBroadcastConfig.setHostAddress("39538f.entrypoint.cloud.wowza.com");
            goCoderBroadcastConfig.setPortNumber(1935);
            goCoderBroadcastConfig.setApplicationName("app-0cef");
            goCoderBroadcastConfig.setStreamName("f5a5af3d");
            goCoderBroadcastConfig.setUsername("client46561");
            goCoderBroadcastConfig.setPassword("06221be9");
        }else if("2".equals(MYURL.which_streaming_str)){
            /*
                goCoderBroadcastConfig 설정 2
             */
            goCoderBroadcastConfig.setHostAddress("baa87c.entrypoint.cloud.wowza.com");
            goCoderBroadcastConfig.setPortNumber(1935);
            goCoderBroadcastConfig.setApplicationName("app-2655");
            goCoderBroadcastConfig.setStreamName("1e194fe3");
            goCoderBroadcastConfig.setUsername("client46561");
            goCoderBroadcastConfig.setPassword("be72838a");
        }else if("5".equals(MYURL.which_streaming_str)){
            /*
                goCoderBroadcastConfig 설정 5
             */
            goCoderBroadcastConfig.setHostAddress("296a26.entrypoint.cloud.wowza.com");
            goCoderBroadcastConfig.setPortNumber(1935);
            goCoderBroadcastConfig.setApplicationName("app-fc3d");
            goCoderBroadcastConfig.setStreamName("0beeb12e");
            goCoderBroadcastConfig.setUsername("client46561");
            goCoderBroadcastConfig.setPassword("28abbcd3");
        }else if("6".equals(MYURL.which_streaming_str)){
            /*
                goCoderBroadcastConfig 설정 6
             */
            goCoderBroadcastConfig.setHostAddress("d62361.entrypoint.cloud.wowza.com");
            goCoderBroadcastConfig.setPortNumber(1935);
            goCoderBroadcastConfig.setApplicationName("app-5d83");
            goCoderBroadcastConfig.setStreamName("db274678");
            goCoderBroadcastConfig.setUsername("client46561");
            goCoderBroadcastConfig.setPassword("83502b37");
        }else if("7".equals(MYURL.which_streaming_str)){
            /*
                goCoderBroadcastConfig 설정 7
             */
            goCoderBroadcastConfig.setHostAddress("b23026.entrypoint.cloud.wowza.com");
            goCoderBroadcastConfig.setPortNumber(1935);
            goCoderBroadcastConfig.setApplicationName("app-5529");
            goCoderBroadcastConfig.setStreamName("7968af3f");
            goCoderBroadcastConfig.setUsername("client46561");
            goCoderBroadcastConfig.setPassword("385ad9bc");
        }










        goCoderBroadcastConfig.setVideoBroadcaster(goCoderCameraView); // Designate the camera preview as the video broadcaster


        goCoderBroadcastConfig.setAudioBroadcaster(goCoderAudioDevice); // Designate the audio device as the audio broadcaster

        /*
            방송하기 버튼
         */
        broadcastButton = findViewById(R.id.broadcast_btn);
        broadcastButton.setOnClickListener(this);

        /*
            후면카메라 <--> 전면카메라 바꾸는 코드
         */
        Button switch_camera_btn = findViewById(R.id.switch_camera_btn);
        switch_camera_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (goCoderCameraView == null) return;

                WOWZCamera newCamera = goCoderCameraView.switchCamera();
                if (newCamera != null) {
                    if (newCamera.hasCapability(WOWZCamera.FOCUS_MODE_CONTINUOUS))
                        newCamera.setFocusMode(WOWZCamera.FOCUS_MODE_CONTINUOUS);
                }
            }
        });

    } // onCreate() 메소드

    /**
     * onResume() 메소드
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume()");
        // If running on Android 6 (Marshmallow) or above, check to see if the necessary permissions
        // have been granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mPermissionsGranted = hasPermissions(this, mRequiredPermissions);
            if (!mPermissionsGranted)
                ActivityCompat.requestPermissions(this, mRequiredPermissions, PERMISSIONS_REQUEST_CODE);
        } else
            mPermissionsGranted = true;

        // Start the camera preview display
        if (mPermissionsGranted && goCoderCameraView != null) {
            if (goCoderCameraView.isPreviewPaused())
                goCoderCameraView.onResume();
            else
                goCoderCameraView.startPreview();
        }

    } // onResume() 메소드

    /**
     * 뒤로가기를 연속 두번 누르면 방송 종료
     */
    private long time= 0;
    @Override
    public void onBackPressed(){
        if(is_broadcast){ // 방송중이면
            if(System.currentTimeMillis()-time>=2000){
                time=System.currentTimeMillis();
                Toast.makeText(getApplicationContext(),"뒤로 버튼을 한번 더 누르면 방송이 종료됩니다.",Toast.LENGTH_SHORT).show();
            }else if(System.currentTimeMillis()-time<2000){
                goCoderBroadcaster.endBroadcast(); // 현재 방송중인 방송 종료
                exitBroadcast(); // 방송 종료
                finish();
            }
        }else{
            finish();
        }
    } // onBackPressed() 메소드

    //
    // Callback invoked in response to a call to ActivityCompat.requestPermissions() to interpret
    // the results of the permissions request
    //
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        mPermissionsGranted = true;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                // Check the result of each permission granted
                for(int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        mPermissionsGranted = false;
                    }
                }
            }
        }
    } // onRequestPermissionResult() 메소드

    /**
     * 권한 체크 메소드
     */
    private static boolean hasPermissions(Context context, String[] permissions) {
        for(String permission : permissions)
            if (context.checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                return false;

        return true;
    } // hasPermissions() 메소드

    /**
     * 방송하기 버튼
     */
    @Override
    public void onClick(View view) {
        if (!mPermissionsGranted){  // 권한이 없음
            Toast.makeText(getApplicationContext(),"권한허용 안됨",Toast.LENGTH_SHORT).show();
            return;
        }
        if (goCoderBroadcaster.getStatus().isBroadcasting()) { // 방송중인 상황
            goCoderBroadcaster.endBroadcast(); // 방송 종료
            exitBroadcast(); // 서버에 방송 종료 알림

        } else { // 방송중이지 않은 상황
            goCoderBroadcaster.startBroadcast(goCoderBroadcastConfig, this); // 방송 시작

        }
//        // return if the user hasn't granted the app the necessary permissions
//        if (!mPermissionsGranted) return;
//
//        // Ensure the minimum set of configuration settings have been specified necessary to
//        // initiate a broadcast streaming session
//        WOWZStreamingError configValidationError = goCoderBroadcastConfig.validateForBroadcast();
//
//        if (configValidationError != null) { // 에러가 있음
//            Toast.makeText(this, configValidationError.getErrorDescription(), Toast.LENGTH_LONG).show();
//        } else if (goCoderBroadcaster.getStatus().isBroadcasting()) {
//            goCoderBroadcaster.endBroadcast(); // 방송 종료
//            exitBroadcast(); // 서버에 방송 종료 알림
//        } else {
//            goCoderBroadcaster.startBroadcast(goCoderBroadcastConfig, this); // 방송 시작
//        }
    } // onClick() 메소드

    /**
     * 방송 상태 변경시 상태 콜백
     */
    @Override
    public void onWZStatus(final WOWZBroadcastStatus goCoderStatus) {
        // A successful status transition has been reported by the GoCoder SDK
        final StringBuffer statusMessage = new StringBuffer("Broadcast status: ");
        if(goCoderStatus.getState().equals(WOWZBroadcastStatus.BroadcastState.READY)){ // 방송 준비중
            Log.d(TAG,"READY");
            statusMessage.append("Ready to begin broadcasting");
        }else if(goCoderStatus.getState().equals(WOWZBroadcastStatus.BroadcastState.BROADCASTING)){ // 방송이 활성화 됨
            Log.d(TAG,"BROADCASTING");

            /*
               방송 참여 및 참여 멤버 번호 서버에 보내기
             */
            try{
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                pw.println("start¡"+login_member.member_no+"\r\n");
                pw.flush();
            }catch (IOException e){
                e.printStackTrace();
            }
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    broadcastButton.setText("방송 종료");
                    Toast.makeText(BroadcastActivity.this, "방송 시작", Toast.LENGTH_LONG).show();
                }
            });
            statusMessage.append("Broadcast is active");
            is_broadcast=true;
            addBroadCastRoom(); // 방송 방 추가
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 스트리밍이 진행중인 동안 화면 계속 실행시켜놓기
        }else if(goCoderStatus.getState().equals(WOWZBroadcastStatus.BroadcastState.IDLE)){ // 방송 종료
            Log.d(TAG,"IDLE");

            /*
                소켓에 방송 종료 알리기
             */
            try{
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                pw.println("finish\r\n");
                pw.flush();
            }catch (IOException e){
                e.printStackTrace();
            }
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    broadcastButton.setText("방송 시작");
                    Toast.makeText(BroadcastActivity.this, "방송 종료", Toast.LENGTH_LONG).show();
                }
            });
            statusMessage.append("The broadcast is stopped"); // 방송 종료 상태 알리기
            is_broadcast=false;
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 스트리밍이 끝나면 플래그 제거하기
        }

        // Display the status message using the U/I thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BroadcastActivity.this, statusMessage, Toast.LENGTH_LONG).show();
            }
        });
    } // onWZStatus() 메소드

    /**
     * 방송 중 에러가 발생하면 상태 콜백
     */
    @Override
    public void onWZError(final WOWZBroadcastStatus goCoderStatus) {
        // If an error is reported by the GoCoder SDK, display a message
        // containing the error details using the U/I thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BroadcastActivity.this, "Streaming error: " + goCoderStatus.getLastError().getErrorDescription(), Toast.LENGTH_LONG).show();
                Log.d(TAG,"Streaming error: "+goCoderStatus.getLastError().getErrorDescription());
            }
        });
    } // onWZError() 메소드

    /**
     * 전체화면 모드
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        if (rootView != null)
            rootView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    } // onWindowFocusChanged() 메소드

    /**
     * 서버에 방송 방 추가
     */
    private void addBroadCastRoom(){
        Log.d(TAG,"addBroadCastRoom() 호출");

        /*
         * 쉐어드에 저장된 멤버 정보 가지고 오기
         */
        SharedPreferences sharedPreferences = getSharedPreferences("myAppData",MODE_PRIVATE);
        Gson gson = new Gson();
        final Member login_member = gson.fromJson(sharedPreferences.getString("login_member","no_login"),Member.class);

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                MYURL.URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) { // 응답이 성공
                        if("0".equals(response)){ // 서버에 방송 방 추가
                            Log.d(TAG,"응답성공>> : 서버에 방송 방 추가 : "+response);
                        }else{ // 채팅 업로드 실패
                            Log.d(TAG,"응답성공>> 서버에 방송 방 추가 실패 : "+response);
                        }
                    }
                },
                new Response.ErrorListener() { // 응답 실패
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG,"응답 에러: "+error.toString());
                    }
                }

        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String,String>();
                params.put("mode","add_broadcast_room");
                params.put("broadcast_room_title",temp_broadcast_title);
                params.put("broadcast_member_no",String.valueOf(login_member.member_no));
                params.put("broadcast_member_nickname",login_member.member_nickname);
                params.put("broadcast_member_profile_img",login_member.member_profile_img);
                return params;
            }
        };

        /*
            requestQueue가 없으면 requestQueue 생성
         */
        if(AppHelper.requestQueue == null){ // requestQueue 가 없을 경우
            AppHelper.requestQueue = Volley.newRequestQueue(getApplicationContext()); // requestQueue 생성
        }

        AppHelper.requestQueue.add(stringRequest); // 요청을 requestQueue에 담음

    } // addBroadCastRoom() 메소드

    /**
     * 서버에 방송 종료 알리기
     */
    private void exitBroadcast(){
        Log.d(TAG,"exitBroadcast() 호출");

        /*
         * 쉐어드에 저장된 멤버 정보 가지고 오기
         */
        SharedPreferences sharedPreferences = getSharedPreferences("myAppData",MODE_PRIVATE);
        Gson gson = new Gson();
        final Member login_member = gson.fromJson(sharedPreferences.getString("login_member","no_login"),Member.class);

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                MYURL.URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) { // 응답이 성공
                        if("0".equals(response)){ // 서버에 방송 방 추가
                            Log.d(TAG,"응답성공>> 방송종료 성공 : "+response);
                        }else{ // 채팅 업로드 실패
                            Log.d(TAG,"응답성공>> 방송종료 실패 : "+response);
                        }
                    }
                },
                new Response.ErrorListener() { // 응답 실패
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG,"응답 에러: "+error.toString());
                    }
                }

        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String,String>();
                params.put("mode","exit_broadcast");
                params.put("broadcast_member_no",String.valueOf(login_member.member_no));
                return params;
            }
        };

        /*
            requestQueue가 없으면 requestQueue 생성
         */
        if(AppHelper.requestQueue == null){ // requestQueue 가 없을 경우
            AppHelper.requestQueue = Volley.newRequestQueue(getApplicationContext()); // requestQueue 생성
        }

        AppHelper.requestQueue.add(stringRequest); // 요청을 requestQueue에 담음

    } // addBroadCastRoom() 메소드

    /**
     * 소켓서버에서 메시지 받는 쓰레드
     */
    class ReceiveThread extends Thread{

        boolean is_stop;

        private ReceiveThread(){
            is_stop=false;
        }

        @Override
        public void run() {
            super.run();
            Log.d(TAG, "쓰레드 run()");
            String server_ip = "35.224.156.8";

            BufferedReader br = null;
            PrintWriter pw = null;

            try {

                socket = new Socket(server_ip, 5050); // 서버 생성

                pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                br = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));



                /*
                 * 소켓서버 응답 받는 곳
                 */
                while(true){
                    String line = br.readLine(); // 여기서 대기
                    if(line==null){
                        Log.d(TAG,"서버가 종료되었습니다");
                        break;
                    }else{ // 서버에서 정상적으로 응답 받은 경우
                        Log.d(TAG,"[서버] "+line);
                        if("finish".equals(line)){ // 방송종료 응답을 받으면
//                            showExitBroadcast(); // 방송종료 다이얼로그 생성
                        }
                        Intent intent = new Intent("custom-event-name");
                        intent.putExtra("message",line);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

                        String[] response = line.split("ㅣ");
                        if("join_member".equals(response[0])){ // 멤버 참여
                            String join_member_no = response[1];
                            String join_member_nickname = response[2];
                            pw.println("join_member_nickname¡"+join_member_no+"ㅣ"+join_member_nickname+"\r\n");
                            pw.flush();

                        }else if("join_member_nickname".equals(response[0])){ // 메시지 수신
                            final String join_member_nickname = response[1];
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(),join_member_nickname+"님이 참여하였습니다",Toast.LENGTH_LONG).show();
                                }
                            });
                        }else if("message".equals(response[0])){ // 메시지 수신
//                            String chat_room_name = response[1];
//                            String chat_member_no = response[2];
//                            String chat_content = response[3];
//                            String chat_time = response[4];
//                            if(Integer.parseInt(chat_member_no)!=login_member_no){ // 로그인중인 멤버가 아니라면
//                                getMemberInfo(Integer.parseInt(chat_member_no),chat_content, chat_room_name);
////                                sendNotification(chat_content); // 알림 보내기
//                            }
                        }

                    }
                }

            }catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG,"IOException : "+e.toString());
            }catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG,"Exception : "+e.toString());
            }finally {
                try {
                    if(pw != null) {
                        pw.close();
                    }
                    if(br != null) {
                        br.close();
                    }
                    if(socket != null) {
                        socket.close();
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

        public void setIs_stop(){
            Log.d(TAG, "setIs_stop()");
            this.is_stop=true;
        }
    } // RecieveThread 클래스

} // BroadcastActivity 클래스
