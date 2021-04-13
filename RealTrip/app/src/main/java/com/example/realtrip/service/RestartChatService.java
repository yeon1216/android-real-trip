package com.example.realtrip.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.realtrip.R;
import com.example.realtrip.activity.MainActivity;
import com.example.realtrip.object.Member;
import com.google.gson.Gson;

/**
 * ChatService를 실행시키는 서비스
 */
public class RestartChatService extends Service {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    public RestartChatService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"onStartCommand() 메소드");

        /*
         * NotificationCompat.Builder 생성 및 설정
         */
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"default");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle(null);
        builder.setContentText(null);

        /*
         * 알림 클릭시 이벤트 생성
         */
        Intent notification_intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,notification_intent,0);
        builder.setContentIntent(pendingIntent);

        /*
         * 시스템에서 NotificationManager 받기
         */
        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        /*
         * 오레오버전 이상이면 notification에 채널 설정
         */
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            notificationManager.createNotificationChannel(new NotificationChannel("default","기본 채널", NotificationManager.IMPORTANCE_NONE));
        }

        /*
         * NotificationCompat.Builder 를 이용해 Notification 빌드
         * 여기서 만들어진 notification 은 startForeground() 를 위해서 만들어진 것이다
         */
        Notification notification = builder.build();
        startForeground(9,notification);

        /*
         * 쉐어드에 저장된 멤버 정보 가지고 오기
         */
        SharedPreferences sharedPreferences = getSharedPreferences("myAppData",MODE_PRIVATE);
        Gson gson = new Gson();
        Member login_member = gson.fromJson(sharedPreferences.getString("login_member","no_login"),Member.class);

        /*
         * 이 클래스의 원래 역할 (ChatService 실행) 수행
         */
        Intent in = new Intent(this,ChatService.class);
        in.putExtra("startChatService","startChatService");
        in.putExtra("login_member_no",String.valueOf(login_member.member_no));
        startService(in);

        stopForeground(true); // 알림 제거
        stopSelf(); // 서비스 종료

        return START_NOT_STICKY;
    } // onStartCommand() 메소드
} // RestartChatService 클래스스
