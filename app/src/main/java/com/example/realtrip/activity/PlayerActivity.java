package com.example.realtrip.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.realtrip.MYURL;
import com.example.realtrip.R;
import com.example.realtrip.object.Member;
import com.example.realtrip.ui.MultiStateButton;
import com.google.gson.Gson;
import com.wowza.gocoder.sdk.api.WowzaGoCoder;
import com.wowza.gocoder.sdk.api.configuration.WOWZMediaConfig;
import com.wowza.gocoder.sdk.api.data.WOWZDataEvent;
import com.wowza.gocoder.sdk.api.data.WOWZDataMap;
import com.wowza.gocoder.sdk.api.errors.WOWZError;
import com.wowza.gocoder.sdk.api.logging.WOWZLog;
import com.wowza.gocoder.sdk.api.player.WOWZPlayerConfig;
import com.wowza.gocoder.sdk.api.player.WOWZPlayerView;
import com.wowza.gocoder.sdk.api.status.WOWZPlayerStatus;
import com.wowza.gocoder.sdk.api.status.WOWZPlayerStatusCallback;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class PlayerActivity extends AppCompatActivity  implements WOWZPlayerStatusCallback {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    // Stream player view
    private WOWZPlayerView mStreamPlayerView = null;
    private WOWZPlayerConfig mStreamPlayerConfig = null;

    // UI controls
    private ProgressDialog mBufferingDialog = null;
    private ProgressDialog mGoingDownDialog =null;

    MultiStateButton mBtnScale;

    WowzaGoCoder goCoder;

    WOWZPlayerStatusCallback wOWZPlayerStatusCallback;

    Socket socket; // 소켓
    Member login_member;
    ReceiveThread receiveThread;
    int temp_broadcast_member_no; // 현재 방송중인 멤버 번호


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG,"onNewIntent() 메소드");
        temp_broadcast_member_no = intent.getIntExtra("temp_broadcast_member_no",0);
        Log.d(TAG,"onNewIntent()     temp_broadcast_member_no: "+temp_broadcast_member_no);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player2);

        Log.d(TAG,"onCreate() 메소드");

        /*
         * 쉐어드에 저장된 멤버 정보 가지고 오기
         */
        SharedPreferences sharedPreferences = getSharedPreferences("myAppData",MODE_PRIVATE);
        Gson gson = new Gson();
        login_member = gson.fromJson(sharedPreferences.getString("login_member","no_login"),Member.class);

        temp_broadcast_member_no = getIntent().getIntExtra("temp_broadcast_member_no",0);
        Log.d(TAG,"temp_broadcast_member_no: "+temp_broadcast_member_no);

        /*
            소켓 연결 및 응답 대기
         */
        receiveThread = new ReceiveThread();
        receiveThread.start();

        goCoder = WowzaGoCoder.init(getApplicationContext(), "GOSK-1547-010C-23B6-30F7-7B49"); // Initialize the GoCoder SDK

        wOWZPlayerStatusCallback = this;

        if (goCoder == null) { // If initialization failed, retrieve the last error and display it
            WOWZError goCoderInitError = WowzaGoCoder.getLastError();
            Toast.makeText(this, "GoCoder SDK error: " + goCoderInitError.getErrorDescription(), Toast.LENGTH_LONG).show();
            return;
        }

        mStreamPlayerView = (WOWZPlayerView) findViewById(R.id.vwStreamPlayer); // 스트리밍 뷰어
        mBtnScale = findViewById(R.id.ic_scale); // 화면 크기 버튼

        mStreamPlayerConfig = new WOWZPlayerConfig();

        if("real_trip".equals(MYURL.which_streaming_str)){
            /*
                mStreamPlayerConfig 설정 real_trip
            */
            mStreamPlayerConfig.setIsPlayback(true);
            mStreamPlayerConfig.setHostAddress("d0c95d.entrypoint.cloud.wowza.com");
            mStreamPlayerConfig.setApplicationName("app-f84c");
            mStreamPlayerConfig.setStreamName("a3926374");
            mStreamPlayerConfig.setPortNumber(1935);
            mStreamPlayerConfig.setHLSEnabled(true);
            mStreamPlayerConfig.setHLSBackupURL("https://wowzaprod218-i.akamaihd.net/hls/live/1001954/9a8c72f5/playlist.m3u8");
        }else if("1".equals(MYURL.which_streaming_str)){
            /*
                mStreamPlayerConfig 설정 1
             */
            mStreamPlayerConfig.setIsPlayback(true);
            mStreamPlayerConfig.setHostAddress("39538f.entrypoint.cloud.wowza.com");
            mStreamPlayerConfig.setApplicationName("app-0cef");
            mStreamPlayerConfig.setStreamName("f5a5af3d");
            mStreamPlayerConfig.setPortNumber(1935);
            mStreamPlayerConfig.setHLSEnabled(true);
            mStreamPlayerConfig.setHLSBackupURL("https://wowzaprod218-i.akamaihd.net/hls/live/1001954/b4ea3057/playlist.m3u8");
        }else if("2".equals(MYURL.which_streaming_str)){
            /*
                mStreamPlayerConfig 설정 2
             */
            mStreamPlayerConfig.setIsPlayback(true);
            mStreamPlayerConfig.setHostAddress("baa87c.entrypoint.cloud.wowza.com");
            mStreamPlayerConfig.setApplicationName("app-2655");
            mStreamPlayerConfig.setStreamName("1e194fe3");
            mStreamPlayerConfig.setPortNumber(1935);
            mStreamPlayerConfig.setHLSEnabled(true);
            mStreamPlayerConfig.setHLSBackupURL("https://wowzaprod270-i.akamaihd.net/hls/live/1002629/a4b00e3f/playlist.m3u8");
        }else if("5".equals(MYURL.which_streaming_str)){
            /*
                mStreamPlayerConfig 설정 5
             */
            mStreamPlayerConfig.setIsPlayback(true);
            mStreamPlayerConfig.setHostAddress("296a26.entrypoint.cloud.wowza.com");
            mStreamPlayerConfig.setApplicationName("app-fc3d");
            mStreamPlayerConfig.setStreamName("0beeb12e");
            mStreamPlayerConfig.setPortNumber(1935);
            mStreamPlayerConfig.setHLSEnabled(true);
            mStreamPlayerConfig.setHLSBackupURL("https://wowzaprod270-i.akamaihd.net/hls/live/1002629/156fabe9/playlist.m3u8");
        }else if("6".equals(MYURL.which_streaming_str)){
            /*
                mStreamPlayerConfig 설정 6
             */
            mStreamPlayerConfig.setIsPlayback(true);
            mStreamPlayerConfig.setHostAddress("d62361.entrypoint.cloud.wowza.com");
            mStreamPlayerConfig.setApplicationName("app-5d83");
            mStreamPlayerConfig.setStreamName("db274678");
            mStreamPlayerConfig.setPortNumber(1935);
            mStreamPlayerConfig.setHLSEnabled(true);
            mStreamPlayerConfig.setHLSBackupURL("https://wowzaprod218-i.akamaihd.net/hls/live/1001954/11816786/playlist.m3u8");
        }else if("7".equals(MYURL.which_streaming_str)){
            /*
                mStreamPlayerConfig 설정 7
             */
            mStreamPlayerConfig.setIsPlayback(true);
            mStreamPlayerConfig.setHostAddress("b23026.entrypoint.cloud.wowza.com");
            mStreamPlayerConfig.setApplicationName("app-5529");
            mStreamPlayerConfig.setStreamName("7968af3f");
            mStreamPlayerConfig.setPortNumber(1935);
            mStreamPlayerConfig.setHLSEnabled(true);
            mStreamPlayerConfig.setHLSBackupURL("https://wowzaprod270-i.akamaihd.net/hls/live/1002629/1f458672/playlist.m3u8");
        }

        /*
            스트리밍 시작
         */
        mStreamPlayerView.play(mStreamPlayerConfig,wOWZPlayerStatusCallback);

        /*
            다이얼로그 초기화
         */
        initDialog();

        /*
            이건 머지?
            이벤트 받는 부분 같음
         */
        mStreamPlayerView.registerDataEventListener("onClientConnected", new WOWZDataEvent.EventListener() {
            @Override
            public WOWZDataMap onWZDataEvent(String eventName, WOWZDataMap eventParams) {
                WOWZLog.info(TAG, "onClientConnected data event received:\n" + eventParams.toString(true));
                Log.d(TAG, "onClientConnected data event received:\n" + eventParams.toString(true));

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                    }
                });

                // this demonstrates how to return a function result back to the original Wowza Streaming Engine
                // function call request
                WOWZDataMap functionResult = new WOWZDataMap();
                functionResult.put("greeting", "Hello New Client!");

                return functionResult;
            }
        });

        // testing player data event handler.
        mStreamPlayerView.registerDataEventListener("onWowzaData", new WOWZDataEvent.EventListener(){
            @Override
            public WOWZDataMap onWZDataEvent(String eventName, WOWZDataMap eventParams) {
                String meta = "";
                if(eventParams!=null)
                    meta = eventParams.toString();


                WOWZLog.debug("onWZDataEvent -> eventName "+eventName+" = "+meta);
                Log.d(TAG,"onWZDataEvent -> eventName "+eventName+" = "+meta);

                return null;
            }
        });



    } // onCeate() 메소드

    public void initDialog(){
        /*
            버퍼링 다이얼로그 설정
         */
        mBufferingDialog = new ProgressDialog(this);
        mBufferingDialog.setTitle("Real Trip"); // Retrieving Stream ...
        mBufferingDialog.setMessage("잠시만 기다려주세요 (buffering)"); // Buffering stream
        mBufferingDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "취소" // Cancel
                , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        /// test
                        cancelBuffering();
                    }
                });

        /*
            종료 다이얼로그 설정
         */
        mGoingDownDialog = new ProgressDialog(this);
        mGoingDownDialog.setTitle("Real Trip"); // Retrieving Stream ...
        mGoingDownDialog.setMessage("잠시만 기다려주세요 (going down)"); // Please wait while the decoder is shutting down.
    }


    /**
     * 방송 상태 콜백
     */
    @Override
    public synchronized void onWZStatus(WOWZPlayerStatus wowzPlayerStatus) {
//        Log.d(TAG,"onWZStatus() 메소드    wowzPlayerStatus.getState(): "+wowzPlayerStatus.getState());
        final WOWZPlayerStatus playerStatus = wowzPlayerStatus;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                switch (playerStatus.getState()) {
                    case BUFFERING: // 버퍼링
                        Log.d(TAG,"BUFFERING");

                        break;
                    case CONNECTING: // 연결
                        Log.d(TAG,"CONNECTING");
                        showStartingDialog();
                        break;
                    case STOPPING: // 정지
                        Log.d(TAG,"STOPPING");

                        break;
                    case PLAYING: // 재생
                        Log.d(TAG,"PLAYING");
                        hideBuffering();
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 스트리밍이 진행중인 동안 화면 계속 실행시켜놓기
                        break;
                    case IDLE: // 방송 종료
                        Log.d(TAG,"IDLE");

                        Toast.makeText(getApplicationContext(),"방송종료",Toast.LENGTH_SHORT).show();

                        break;
                }
            }
        });
    } // onWZStatus() 메소드

    /**
     * 방송중 에러상태 콜백
     */
    @Override
    public void onWZError(WOWZPlayerStatus wowzPlayerStatus) {
        Log.d(TAG,"onWZError : "+wowzPlayerStatus.getLastError());
    } // onWZError() 메소드

//

    /**
     * 화면 크기 조절 버튼 클릭시 이벤트
     */
    public void onToggleScaleMode(View v) {
        int newScaleMode = mStreamPlayerView.getScaleMode() == WOWZMediaConfig.RESIZE_TO_ASPECT ? WOWZMediaConfig.FILL_VIEW : WOWZMediaConfig.RESIZE_TO_ASPECT;
        mBtnScale.setState(newScaleMode == WOWZMediaConfig.FILL_VIEW);
        mStreamPlayerView.setScaleMode(newScaleMode);
    }

//    /**
//     * 스트리밍 재생 버튼 클릭시 이벤트
//     */
//    public void onTogglePlayStream(View v) {
//        if (mStreamPlayerView.getCurrentStatus().isPlaying()) { // 재생중이면
//            mStreamPlayerView.stop(); // 정지
//        } else if (mStreamPlayerView.isReadyToPlay()) { // 정지중이면
//            this.playStream(); // 재생
//        }
//    }

//    /**
//     * 스트리밍 재생
//     */
//    public void playStream(){
//        Log.d(TAG,"playStream() 메소드");
//        if(!this.isNetworkAvailable()){
//            displayErrorDialog("No internet connection, please try again later.");
//            return;
//        }
//        showBuffering();
//        mStreamPlayerView.setMaxSecondsWithNoPackets(4);
//        mStreamPlayerView.play(mStreamPlayerConfig, this);
//    }

//    /**
//     * 네트워크가 사용가능한지 판단하는 메소드
//     */
//    private boolean isNetworkAvailable() {
//        Log.d(TAG,"isNetworkAvailable() 메소드");
//        ConnectivityManager connectivityManager
//                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        if (connectivityManager==null)
//            return false;
//        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
//        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
//    }

    /*
        #####################################################################################################################
        다이얼로그 관련 메소드 시작
        #####################################################################################################################
     */

    /**
     * 방송종료 다이얼로그
     */
    private void showExitBroadcast(){
        AlertDialog.Builder builder = new AlertDialog.Builder(PlayerActivity.this);
        builder.setTitle("Real Trip");
        builder.setMessage("방송이 종료되었습니다.");
        builder.setPositiveButton("나가기",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish(); // activity 종료
                    }
                });
        builder.show(); // 다이얼로그 보이게 하기
    }

    /**
     * 버퍼링 메소드
     */
    private void showBuffering() {
        Log.d(TAG,"showBuffering() 메소드");
        try {
            if (mBufferingDialog == null) return;

            if(mBufferingDialog.isShowing()){
//                mBufferingDialog.setMessage(getResources().getString(R.string.msg_please_wait));
                mBufferingDialog.setMessage("잠시만 기다려주세요");
                return;
            }

            final Handler mainThreadHandler = new Handler(getBaseContext().getMainLooper());
            mBufferingDialog.setCancelable(false);
            mBufferingDialog.show();
            mBufferingDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);
            (new Thread(){
                public void run(){

                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mBufferingDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(true);
                        }
                    });
                }
            }).start();
        }
        catch(Exception ex) {
            WOWZLog.warn(TAG, "showBuffering:" + ex);
        }
    } // showBuffering() 메소드

    private void showStartingDialog(){
        Log.d(TAG,"showStartingDialog() 메소드");
        try {
            if (mBufferingDialog == null) return;
//            hideBuffering();
//            mBufferingDialog.setMessage(getResources().getString(R.string.msg_connecting)); // Please wait while connect ...
            mBufferingDialog.setMessage("연결중입니다. 잠시만 기다려주세요."); // Please wait while connect ...
            if(!mBufferingDialog.isShowing()) {
                mBufferingDialog.setCancelable(false);
                mBufferingDialog.show();
            }
        }
        catch(Exception ex){
            WOWZLog.warn(TAG, "showTearingdownDialog:" + ex);
        }
    }

    private void showTearingdownDialog(){
        Log.d(TAG,"showTearingdownDialog() 메소드");
        try {
            if (mGoingDownDialog == null) return;
            hideBuffering();
            if(!mGoingDownDialog.isShowing()) {
                mGoingDownDialog.setCancelable(false);
                mGoingDownDialog.show();
            }
        }
        catch(Exception ex){
            WOWZLog.warn(TAG, "showTearingdownDialog:" + ex);
        }
    }

    private void hideBuffering() {
        Log.d(TAG,"hideBuffering() 메소드");
        if (mBufferingDialog!=null && mBufferingDialog.isShowing())
            mBufferingDialog.dismiss();
    }

    private void cancelBuffering() {
        Log.d(TAG,"cancelBuffering() 메소드");
        showTearingdownDialog();
        hideTearingdownDialog();
        mStreamPlayerView.stop();
    }

    private void hideTearingdownDialog(){
        Log.d(TAG,"hideTearingdownDialog() 메소드");
        try {
            if (mGoingDownDialog == null) return;
            hideBuffering();
            mGoingDownDialog.dismiss();
        }
        catch(Exception ex){
            WOWZLog.warn(TAG, "hideTearingdownDialog exception:" + ex);
        }
    }

    /**
     * Display an alert dialog containing an error message.
     *
     * @param errorMessage The error message text
     */
    protected void displayErrorDialog(String errorMessage) {
        Log.d(TAG,"displayErrorDialog() 메소드");
        // Log the error message
        try {
            WOWZLog.error(TAG, "ERROR: " + errorMessage);

            // Display an alert dialog containing the error message
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.myDialog));
            //AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
            builder.setMessage(errorMessage)
                    .setTitle("에러 발생");
            builder.setPositiveButton("닫기", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });

            builder.create().show();
        }
        catch(Exception ex){}
    }

    /**
     * Display an alert dialog containing the error message for
     * an error returned from the GoCoder SDK.
     *
     * @param goCoderSDKError An error returned from the GoCoder SDK.
     */
    protected void displayErrorDialog(WOWZError goCoderSDKError) {
        Log.d(TAG,"displayErrorDialog() 메소드");
        displayErrorDialog(goCoderSDKError.getErrorDescription());
    }

    /*
        #####################################################################################################################
        다이얼로그 관련 메소드 끝
        #####################################################################################################################
     */


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
                 * 방송 참여 및 참여 멤버 번호 서버에 보내기
                 */
//                String request = "message¡"+this_chat_room_name+"ㅣ"+login_member_no+"ㅣ"+temp_chat+"ㅣ"+chat_time+"\r\n";
                Log.d(TAG, "[소켓에 보냄] join¡"+temp_broadcast_member_no+"ㅣ"+login_member.member_no+"ㅣ"+login_member.member_nickname+"\r\n");
                pw.println("join¡"+temp_broadcast_member_no+"ㅣ"+login_member.member_no+"ㅣ"+login_member.member_nickname+"\r\n");
                pw.flush();

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
                            try { Thread.sleep(12000); } catch (InterruptedException e) { }
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    showExitBroadcast(); // 방송종료 다이얼로그 생성
                                }
                            });
                        }
                        Intent intent = new Intent("custom-event-name");
                        intent.putExtra("message",line);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

                        String[] response = line.split("ㅣ");
                        if("join_member_nickname".equals(response[0])){ // 멤버 참여
                            final String join_member_nickname = response[1];
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    if(!login_member.member_nickname.equals(join_member_nickname)){
                                        Toast.makeText(getApplicationContext(),join_member_nickname+"님이 참여하였습니다",Toast.LENGTH_LONG).show();
                                    }
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

    /**
     * 메시지 전송 메소드
     */
    private void sendMessage(final String temp_chat) {
        new Thread(){
            public void run(){
                try {
                    PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
                    String request = "message¡"+temp_chat+"\r\n";
                    Log.d(TAG,"request: "+request);
                    pw.println(request);
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    } // sendMessage() 메소드

} // PlayerActivity 클래스
