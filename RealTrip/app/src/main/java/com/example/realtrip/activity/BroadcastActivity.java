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

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log??? ?????? ??????

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

    private boolean is_broadcast; // true : ?????????, false : ???????????? ??????

    Button broadcastButton; // ???????????? ??????

    Socket socket; // ??????
    Member login_member;
    ReceiveThread receiveThread;

    String temp_broadcast_title;

    /**
     * onCreate() ?????????
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast);
        Log.d(TAG,"onCreate()");
        is_broadcast = false;

        temp_broadcast_title = getIntent().getStringExtra("temp_broadcast_title");

        /*
         * ???????????? ????????? ?????? ?????? ????????? ??????
         */
        SharedPreferences sharedPreferences = getSharedPreferences("myAppData",MODE_PRIVATE);
        Gson gson = new Gson();
        login_member = gson.fromJson(sharedPreferences.getString("login_member","no_login"),Member.class);

        /*
            ?????? ?????? ??? ?????? ??????
         */
        receiveThread = new ReceiveThread();
        receiveThread.start();

        /*
            wowzaGoCoder ?????? ??? ?????????
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
                goCoderBroadcastConfig ?????? real_trip
             */
            goCoderBroadcastConfig.setHostAddress("d0c95d.entrypoint.cloud.wowza.com");
            goCoderBroadcastConfig.setPortNumber(1935);
            goCoderBroadcastConfig.setApplicationName("app-f84c");
            goCoderBroadcastConfig.setStreamName("a3926374");
            goCoderBroadcastConfig.setUsername("client46561");
            goCoderBroadcastConfig.setPassword("3215a976");
        }else if("1".equals(MYURL.which_streaming_str)){
            /*
                goCoderBroadcastConfig ?????? 1
             */
            goCoderBroadcastConfig.setHostAddress("39538f.entrypoint.cloud.wowza.com");
            goCoderBroadcastConfig.setPortNumber(1935);
            goCoderBroadcastConfig.setApplicationName("app-0cef");
            goCoderBroadcastConfig.setStreamName("f5a5af3d");
            goCoderBroadcastConfig.setUsername("client46561");
            goCoderBroadcastConfig.setPassword("06221be9");
        }else if("2".equals(MYURL.which_streaming_str)){
            /*
                goCoderBroadcastConfig ?????? 2
             */
            goCoderBroadcastConfig.setHostAddress("baa87c.entrypoint.cloud.wowza.com");
            goCoderBroadcastConfig.setPortNumber(1935);
            goCoderBroadcastConfig.setApplicationName("app-2655");
            goCoderBroadcastConfig.setStreamName("1e194fe3");
            goCoderBroadcastConfig.setUsername("client46561");
            goCoderBroadcastConfig.setPassword("be72838a");
        }else if("5".equals(MYURL.which_streaming_str)){
            /*
                goCoderBroadcastConfig ?????? 5
             */
            goCoderBroadcastConfig.setHostAddress("296a26.entrypoint.cloud.wowza.com");
            goCoderBroadcastConfig.setPortNumber(1935);
            goCoderBroadcastConfig.setApplicationName("app-fc3d");
            goCoderBroadcastConfig.setStreamName("0beeb12e");
            goCoderBroadcastConfig.setUsername("client46561");
            goCoderBroadcastConfig.setPassword("28abbcd3");
        }else if("6".equals(MYURL.which_streaming_str)){
            /*
                goCoderBroadcastConfig ?????? 6
             */
            goCoderBroadcastConfig.setHostAddress("d62361.entrypoint.cloud.wowza.com");
            goCoderBroadcastConfig.setPortNumber(1935);
            goCoderBroadcastConfig.setApplicationName("app-5d83");
            goCoderBroadcastConfig.setStreamName("db274678");
            goCoderBroadcastConfig.setUsername("client46561");
            goCoderBroadcastConfig.setPassword("83502b37");
        }else if("7".equals(MYURL.which_streaming_str)){
            /*
                goCoderBroadcastConfig ?????? 7
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
            ???????????? ??????
         */
        broadcastButton = findViewById(R.id.broadcast_btn);
        broadcastButton.setOnClickListener(this);

        /*
            ??????????????? <--> ??????????????? ????????? ??????
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

    } // onCreate() ?????????

    /**
     * onResume() ?????????
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

    } // onResume() ?????????

    /**
     * ??????????????? ?????? ?????? ????????? ?????? ??????
     */
    private long time= 0;
    @Override
    public void onBackPressed(){
        if(is_broadcast){ // ???????????????
            if(System.currentTimeMillis()-time>=2000){
                time=System.currentTimeMillis();
                Toast.makeText(getApplicationContext(),"?????? ????????? ?????? ??? ????????? ????????? ???????????????.",Toast.LENGTH_SHORT).show();
            }else if(System.currentTimeMillis()-time<2000){
                goCoderBroadcaster.endBroadcast(); // ?????? ???????????? ?????? ??????
                exitBroadcast(); // ?????? ??????
                finish();
            }
        }else{
            finish();
        }
    } // onBackPressed() ?????????

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
    } // onRequestPermissionResult() ?????????

    /**
     * ?????? ?????? ?????????
     */
    private static boolean hasPermissions(Context context, String[] permissions) {
        for(String permission : permissions)
            if (context.checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                return false;

        return true;
    } // hasPermissions() ?????????

    /**
     * ???????????? ??????
     */
    @Override
    public void onClick(View view) {
        if (!mPermissionsGranted){  // ????????? ??????
            Toast.makeText(getApplicationContext(),"???????????? ??????",Toast.LENGTH_SHORT).show();
            return;
        }
        if (goCoderBroadcaster.getStatus().isBroadcasting()) { // ???????????? ??????
            goCoderBroadcaster.endBroadcast(); // ?????? ??????
            exitBroadcast(); // ????????? ?????? ?????? ??????

        } else { // ??????????????? ?????? ??????
            goCoderBroadcaster.startBroadcast(goCoderBroadcastConfig, this); // ?????? ??????

        }
//        // return if the user hasn't granted the app the necessary permissions
//        if (!mPermissionsGranted) return;
//
//        // Ensure the minimum set of configuration settings have been specified necessary to
//        // initiate a broadcast streaming session
//        WOWZStreamingError configValidationError = goCoderBroadcastConfig.validateForBroadcast();
//
//        if (configValidationError != null) { // ????????? ??????
//            Toast.makeText(this, configValidationError.getErrorDescription(), Toast.LENGTH_LONG).show();
//        } else if (goCoderBroadcaster.getStatus().isBroadcasting()) {
//            goCoderBroadcaster.endBroadcast(); // ?????? ??????
//            exitBroadcast(); // ????????? ?????? ?????? ??????
//        } else {
//            goCoderBroadcaster.startBroadcast(goCoderBroadcastConfig, this); // ?????? ??????
//        }
    } // onClick() ?????????

    /**
     * ?????? ?????? ????????? ?????? ??????
     */
    @Override
    public void onWZStatus(final WOWZBroadcastStatus goCoderStatus) {
        // A successful status transition has been reported by the GoCoder SDK
        final StringBuffer statusMessage = new StringBuffer("Broadcast status: ");
        if(goCoderStatus.getState().equals(WOWZBroadcastStatus.BroadcastState.READY)){ // ?????? ?????????
            Log.d(TAG,"READY");
            statusMessage.append("Ready to begin broadcasting");
        }else if(goCoderStatus.getState().equals(WOWZBroadcastStatus.BroadcastState.BROADCASTING)){ // ????????? ????????? ???
            Log.d(TAG,"BROADCASTING");

            /*
               ?????? ?????? ??? ?????? ?????? ?????? ????????? ?????????
             */
            try{
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                pw.println("start??"+login_member.member_no+"\r\n");
                pw.flush();
            }catch (IOException e){
                e.printStackTrace();
            }
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    broadcastButton.setText("?????? ??????");
                    Toast.makeText(BroadcastActivity.this, "?????? ??????", Toast.LENGTH_LONG).show();
                }
            });
            statusMessage.append("Broadcast is active");
            is_broadcast=true;
            addBroadCastRoom(); // ?????? ??? ??????
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // ??????????????? ???????????? ?????? ?????? ?????? ??????????????????
        }else if(goCoderStatus.getState().equals(WOWZBroadcastStatus.BroadcastState.IDLE)){ // ?????? ??????
            Log.d(TAG,"IDLE");

            /*
                ????????? ?????? ?????? ?????????
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
                    broadcastButton.setText("?????? ??????");
                    Toast.makeText(BroadcastActivity.this, "?????? ??????", Toast.LENGTH_LONG).show();
                }
            });
            statusMessage.append("The broadcast is stopped"); // ?????? ?????? ?????? ?????????
            is_broadcast=false;
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // ??????????????? ????????? ????????? ????????????
        }

        // Display the status message using the U/I thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BroadcastActivity.this, statusMessage, Toast.LENGTH_LONG).show();
            }
        });
    } // onWZStatus() ?????????

    /**
     * ?????? ??? ????????? ???????????? ?????? ??????
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
    } // onWZError() ?????????

    /**
     * ???????????? ??????
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
    } // onWindowFocusChanged() ?????????

    /**
     * ????????? ?????? ??? ??????
     */
    private void addBroadCastRoom(){
        Log.d(TAG,"addBroadCastRoom() ??????");

        /*
         * ???????????? ????????? ?????? ?????? ????????? ??????
         */
        SharedPreferences sharedPreferences = getSharedPreferences("myAppData",MODE_PRIVATE);
        Gson gson = new Gson();
        final Member login_member = gson.fromJson(sharedPreferences.getString("login_member","no_login"),Member.class);

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                MYURL.URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) { // ????????? ??????
                        if("0".equals(response)){ // ????????? ?????? ??? ??????
                            Log.d(TAG,"????????????>> : ????????? ?????? ??? ?????? : "+response);
                        }else{ // ?????? ????????? ??????
                            Log.d(TAG,"????????????>> ????????? ?????? ??? ?????? ?????? : "+response);
                        }
                    }
                },
                new Response.ErrorListener() { // ?????? ??????
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG,"?????? ??????: "+error.toString());
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
            requestQueue??? ????????? requestQueue ??????
         */
        if(AppHelper.requestQueue == null){ // requestQueue ??? ?????? ??????
            AppHelper.requestQueue = Volley.newRequestQueue(getApplicationContext()); // requestQueue ??????
        }

        AppHelper.requestQueue.add(stringRequest); // ????????? requestQueue??? ??????

    } // addBroadCastRoom() ?????????

    /**
     * ????????? ?????? ?????? ?????????
     */
    private void exitBroadcast(){
        Log.d(TAG,"exitBroadcast() ??????");

        /*
         * ???????????? ????????? ?????? ?????? ????????? ??????
         */
        SharedPreferences sharedPreferences = getSharedPreferences("myAppData",MODE_PRIVATE);
        Gson gson = new Gson();
        final Member login_member = gson.fromJson(sharedPreferences.getString("login_member","no_login"),Member.class);

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                MYURL.URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) { // ????????? ??????
                        if("0".equals(response)){ // ????????? ?????? ??? ??????
                            Log.d(TAG,"????????????>> ???????????? ?????? : "+response);
                        }else{ // ?????? ????????? ??????
                            Log.d(TAG,"????????????>> ???????????? ?????? : "+response);
                        }
                    }
                },
                new Response.ErrorListener() { // ?????? ??????
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG,"?????? ??????: "+error.toString());
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
            requestQueue??? ????????? requestQueue ??????
         */
        if(AppHelper.requestQueue == null){ // requestQueue ??? ?????? ??????
            AppHelper.requestQueue = Volley.newRequestQueue(getApplicationContext()); // requestQueue ??????
        }

        AppHelper.requestQueue.add(stringRequest); // ????????? requestQueue??? ??????

    } // addBroadCastRoom() ?????????

    /**
     * ?????????????????? ????????? ?????? ?????????
     */
    class ReceiveThread extends Thread{

        boolean is_stop;

        private ReceiveThread(){
            is_stop=false;
        }

        @Override
        public void run() {
            super.run();
            Log.d(TAG, "????????? run()");
            String server_ip = "35.224.156.8";

            BufferedReader br = null;
            PrintWriter pw = null;

            try {

                socket = new Socket(server_ip, 5050); // ?????? ??????

                pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                br = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));



                /*
                 * ???????????? ?????? ?????? ???
                 */
                while(true){
                    String line = br.readLine(); // ????????? ??????
                    if(line==null){
                        Log.d(TAG,"????????? ?????????????????????");
                        break;
                    }else{ // ???????????? ??????????????? ?????? ?????? ??????
                        Log.d(TAG,"[??????] "+line);
                        if("finish".equals(line)){ // ???????????? ????????? ?????????
//                            showExitBroadcast(); // ???????????? ??????????????? ??????
                        }
                        Intent intent = new Intent("custom-event-name");
                        intent.putExtra("message",line);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

                        String[] response = line.split("???");
                        if("join_member".equals(response[0])){ // ?????? ??????
                            String join_member_no = response[1];
                            String join_member_nickname = response[2];
                            pw.println("join_member_nickname??"+join_member_no+"???"+join_member_nickname+"\r\n");
                            pw.flush();

                        }else if("join_member_nickname".equals(response[0])){ // ????????? ??????
                            final String join_member_nickname = response[1];
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(),join_member_nickname+"?????? ?????????????????????",Toast.LENGTH_LONG).show();
                                }
                            });
                        }else if("message".equals(response[0])){ // ????????? ??????
//                            String chat_room_name = response[1];
//                            String chat_member_no = response[2];
//                            String chat_content = response[3];
//                            String chat_time = response[4];
//                            if(Integer.parseInt(chat_member_no)!=login_member_no){ // ??????????????? ????????? ????????????
//                                getMemberInfo(Integer.parseInt(chat_member_no),chat_content, chat_room_name);
////                                sendNotification(chat_content); // ?????? ?????????
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
    } // RecieveThread ?????????

} // BroadcastActivity ?????????
