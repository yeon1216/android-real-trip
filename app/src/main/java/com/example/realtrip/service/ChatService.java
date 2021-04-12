package com.example.realtrip.service;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.realtrip.AppHelper;
import com.example.realtrip.MYURL;
import com.example.realtrip.R;
import com.example.realtrip.activity.ChatActivity;
import com.example.realtrip.activity.HomeActivity;
import com.example.realtrip.object.Member;
import com.example.realtrip.receive.AlarmReceiver;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 채팅 서비스 클래스
 */
public class ChatService extends Service {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    int login_member_no; // 로그인중인 멤버 번호

    Socket socket; // 소켓

    ReceiveThread receiveThread; // 소켓으로부터 오는 메시지를 읽기위한 쓰레드

    boolean is_finish_chat_service; // 서비스를 종료하기 위한 변수

    public static Intent serviceIntent = null; // 서비스가 실행중인지 알기 위한 변수

    /**
     * onStartCommand() 메소드
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"onStartCommand() 호출");
        serviceIntent = intent;
        is_finish_chat_service=false;

        if(intent.getStringExtra("sendMessage") != null){ // 메시지 보내기
            String temp_chat = intent.getStringExtra("temp_chat");
            String chat_time = intent.getStringExtra("chat_time");
            String this_chat_room_name = intent.getStringExtra("this_chat_room_name");
            sendMessage(temp_chat,chat_time,this_chat_room_name);

        }else if(intent.getStringExtra("startChatService") != null){ // 서비스 시작 (로그인)
            login_member_no = Integer.parseInt(intent.getStringExtra("login_member_no")); // 로그인중인 멤버 번호 얻기
            receiveThread = new ReceiveThread();
            receiveThread.start(); // 소켓연결, 메시지 수신모드 실행

        }else if(intent.getStringExtra("stopChatService") != null){ // 챗 서비스 종료 (로그아웃)
            is_finish_chat_service = true;
            stopSelf(); // 서비스 종료
        }

        return Service.START_NOT_STICKY;
    } // onStartCommand() 메소드

    /**
     * onDestroy() 메소드
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG,"onDestroy()");

        if(!is_finish_chat_service){
            serviceIntent = null;
            setAlarmTimer();
            Thread.currentThread().interrupt();
        }

        disconnectWithSocket();
        if(receiveThread!=null){
            receiveThread.setIs_stop();
            receiveThread=null;
        }

    } // onDestroy() 메소드

    /**
     * onTaskRemoved() 메소드
     */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.d(TAG,"onTaskRemoved() 호출");
        if(!is_finish_chat_service){
            serviceIntent = null;
            setAlarmTimer();
            Thread.currentThread().interrupt();
        }

        disconnectWithSocket();
        if(receiveThread!=null){
            receiveThread.setIs_stop();
            receiveThread=null;
        }
    } // onTaskRemoved() 메소드

    /**
     * onBind() 메소드
     */
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.d(TAG,"onBind() 호출");
        throw new UnsupportedOperationException("Not yet implemented");
    } // onBind() 메소드

    /**
     * 소켓과 연결 해제하는 메소드
     */
    void disconnectWithSocket(){
        /*
         * 소켓과 연결 종료
         */
        new Thread(){
            public void run(){
                try {
                    PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
                    String request = "quit¡\r\n";
                    pw.println(request);
                }catch (IOException e) {
                    e.printStackTrace();
                }catch (NullPointerException e){
                    e.printStackTrace();
                    Log.d(TAG,"java.lang.NullPointerException: Attempt to invoke virtual method 'java.io.OutputStream java.net.Socket.getOutputStream()' on a null object reference at com.example.realtrip.service.ChatService$1.run");
                }
            }
        }.start();

    } // disconnectWithSocket() 메소드

    /**
     * 메시지 전송 메소드
     */
    private void sendMessage(final String temp_chat, final String chat_time, final String this_chat_room_name) {
        new Thread(){
            public void run(){
                try {
                    PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
                    String request = "message¡"+this_chat_room_name+"ㅣ"+login_member_no+"ㅣ"+temp_chat+"ㅣ"+chat_time+"\r\n";
                    Log.d(TAG,"request: "+request);
                    pw.println(request);
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    } // sendMessage() 메소드

    /**
     * 서비스 종료시(onDestroy 에서) 알람을 설정하는 메소드
     */
    protected void setAlarmTimer() {
        Log.d(TAG,"setAlarmTimer() 메소드");

        /*
         * Calendar 에서 1초 뒤 시간 저장
         */
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
//        c.add(Calendar.SECOND, 1);
        c.add(Calendar.MILLISECOND,10);

        /*
         * 인텐트 설정
         */
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0,intent,0);

        /*
         * 설정한 시간과 인텐트로 알림 설정
         */
        AlarmManager mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE); // 알림 서비스 시스템에서 알림 메니저 얻기
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), sender); // 알림 메니저에 1초뒤에 알림 보내달라고 설정 (이 알림으로 ChatService가 다시 실행됨)

    } // setAlarmTimer() 메소드

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

                socket = new Socket(server_ip, 5000); // 서버 생성

                pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                br = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

                /*
                 * 채팅방 참여 및 참여 멤버 번호 서버에 보내기
                 */
                pw.println("join¡"+login_member_no+"\r\n");
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
                        Intent intent = new Intent("custom-event-name");
                        intent.putExtra("message",line);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

                        String[] response = line.split("ㅣ");
                        if("message".equals(response[0])){ // 메시지 수신
                            String chat_room_name = response[1];
                            String chat_member_no = response[2];
                            String chat_content = response[3];
                            String chat_time = response[4];
                            if(Integer.parseInt(chat_member_no)!=login_member_no){ // 로그인중인 멤버가 아니라면
                                getMemberInfo(Integer.parseInt(chat_member_no),chat_content, chat_room_name);
//                                sendNotification(chat_content); // 알림 보내기
                            }
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

    /*
        알림을 설정하는 메소드
     */
    private void sendNotification(String messageBody, String member_str, String chat_room_name) {
        Log.d(TAG,"sendNotification() 메소드");

        Gson gson = new Gson();
        Member member = gson.fromJson(member_str,Member.class);

        /*
             알림 클릭시 해당 채팅방으로 이동
         */
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("chat_member",member_str);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);

        String channelId = "fcm_default_channel";//getString(R.string.default_notification_channel_id);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher)//drawable.splash)
                        .setContentTitle(member.member_nickname)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        /*
            오레오버전부터 notification 채널이 필요해짐
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // 오레오버전 이상이면
            NotificationChannel channel = new NotificationChannel(channelId,"Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(member.member_no /* ID of notification */, notificationBuilder.build());

    } // sendNotification() 메소드

    /**
     * getMemberInfo() 메소드
     */
    public void getMemberInfo(int member_no, final String chat_content, final String chat_room_name){
        Log.d(TAG,"getMemberInfo() 호출");

        // Post 인자
        Map<String,String> params = new HashMap<String,String>();
        params.put("mode","find_member");
        params.put("member_no",Integer.toString(member_no));

        JSONObject jsonObject = new JSONObject(params); // Map을 json으로 만듬

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, MYURL.URL,jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) { // 응답 성공
                        Log.d(TAG,"응답 성공: "+response.toString());

                        sendNotification(chat_content,response.toString(), chat_room_name);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) { // 응답 실패
                Log.d(TAG,"응답 실패: "+error.toString());

            }
        }
        );

        /*
            requestQueue가 없으면 requestQueue 생성
         */
        if(AppHelper.requestQueue == null){ // requestQueue 가 없을 경우
            AppHelper.requestQueue = Volley.newRequestQueue(getApplicationContext()); // requestQueue 생성
        }

        AppHelper.requestQueue.add(jsonObjectRequest); // 요청 큐에 위 요청 추가

    } // getMemberInfo() 메소드

} // ChatService 클래스
