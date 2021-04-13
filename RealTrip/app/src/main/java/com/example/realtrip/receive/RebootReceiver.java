package com.example.realtrip.receive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.realtrip.service.ChatService;
import com.example.realtrip.service.RestartChatService;

/**
 * 핸드폰이 켜질 때 ChatService가 자동으로 실행되도록 하는 리시버 클래스
 */
public class RebootReceiver extends BroadcastReceiver {
    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,"onReceive() 메소드");
        /**
         * - 오레오버전 이상이면 RestartChatService >> ChatService
         * - 오레오버전 이하이면 바로 ChatService
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
