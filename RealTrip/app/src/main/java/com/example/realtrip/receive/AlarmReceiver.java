package com.example.realtrip.receive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.realtrip.service.ChatService;
import com.example.realtrip.service.RestartChatService;

/**
 * ChatService를 백그라운드에서 실행시키기위해 중간역할을 하는 receiver 클래스
 */
public class AlarmReceiver extends BroadcastReceiver {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,"onReceive() 메소드");

        /*
            - 오레오버전 이상이면 RestartChatService >> ChatService
            - 오레오버전 이하이면 바로 ChatService
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent in = new Intent(context, RestartChatService.class);
            context.startForegroundService(in);
        } else {
            Intent in = new Intent(context, ChatService.class);
            context.startService(in);
        }
    }

}
