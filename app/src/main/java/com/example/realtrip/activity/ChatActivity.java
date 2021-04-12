package com.example.realtrip.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.realtrip.AppHelper;
import com.example.realtrip.ChatDatabaseManager;
import com.example.realtrip.MYURL;
import com.example.realtrip.R;
import com.example.realtrip.adapter.ChatAdapter;
import com.example.realtrip.adapter.ReviewAdapter;
import com.example.realtrip.item.ReviewItem;
import com.example.realtrip.object.Chat;
import com.example.realtrip.object.Member;
import com.example.realtrip.service.ChatService;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * ChatActivity 클래스
 *
 */
public class ChatActivity extends AppCompatActivity {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    EditText chat_et;
    Button send_btn;

    Member login_member;
    Member chat_member;
    String this_chat_room_name;

    ArrayList<Chat> chats;

    private Socket socket;
    RecyclerView chat_recyclerview;
    ChatAdapter chatAdapter;

    Handler handler;

    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "custom-event-name" is broadcasted.
    private BroadcastReceiver mMessageReceiver; // 서비스에서 보내주는 메시지를 읽는 리씨버

    NotiRemoveThread notiRemoveThread; // 푸시알림 제거해주는 쓰레드



    /**
     * onCreate() 메소드
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chat_et = findViewById(R.id.chat_et); // 채팅 입력 창
        Button send_btn = findViewById(R.id.send_btn);
        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String temp_chat = chat_et.getText().toString();
                if("".equals(temp_chat.trim())){ // 대화를 입력하지 않고 전송버튼을 누른경우
                    Toast.makeText(getApplicationContext(),"대화를 입력해주세요",Toast.LENGTH_SHORT).show();
                    return;
                }
                String chat_time = timeToString(Calendar.getInstance());
                chat_et.setText("");
                chat_et.requestFocus();
                sendMessage(temp_chat, chat_time); // 소켓으로 메시지 전송

                chatAdapter.addItem(new Chat(this_chat_room_name,login_member.member_no,temp_chat,chat_time,login_member.member_nickname,login_member.member_profile_img)); // 어댑터에 채팅 아이템 추가
                chat_recyclerview.scrollToPosition(chatAdapter.getItemCount()-1); // 채팅이 온 경우 새로운 채팅 바로 보여주기
            }
        });

        /*
            채팅 멤버 정보 가지고 오기
         */
        SharedPreferences sharedPreferences = getSharedPreferences("myAppData",MODE_PRIVATE);
        Gson gson = new Gson();
        login_member = gson.fromJson(sharedPreferences.getString("login_member","no_login"),Member.class);
        chat_member = gson.fromJson(getIntent().getStringExtra("chat_member"),Member.class);

        /*
            채팅방 이름 설정
         */
        if(login_member.member_no>chat_member.member_no){
            this_chat_room_name = chat_member.member_no+","+login_member.member_no;
        }else{
            this_chat_room_name = login_member.member_no+","+chat_member.member_no;
        }

        /*
            채팅 상대 ui 표현
         */
        TextView chat_member_nickname = findViewById(R.id.chat_member_nickname);
        chat_member_nickname.setText(chat_member.member_nickname);
        ImageView chat_member_profile_img = findViewById(R.id.chat_member_profile_img);
        if("default".equals(chat_member.member_profile_img)){ // 기본 프로필인 경우
            chat_member_profile_img.setImageResource(R.drawable.default_profile);
        }else{ // 프로필 사진이 있는 경우
            Glide.with(this)
                    .load("http://35.224.156.8/uploads/"+chat_member.member_profile_img)
                    .thumbnail(0.1f)
                    .placeholder(R.drawable.loading)
                    .into(chat_member_profile_img);
        }

        /*
            디비에서 채팅 가지고와서 chats 에 넣어주기
         */
//        chats = ChatDatabaseManager.getInstance(getApplicationContext()).chatgetAllThisChatRoom(login_member,chat_member,this_chat_room_name);
//        chat_recyclerview = findViewById(R.id.chat_recyclerview);
//        chat_recyclerview.setLayoutManager(new LinearLayoutManager(this));
//        chatAdapter = new ChatAdapter(chats,ChatActivity.this);
//        chat_recyclerview.setAdapter(chatAdapter);

        getChatThisChatRoomFromServer(login_member,chat_member,this_chat_room_name); // 서버에서 채팅 가지고오기

        /*
            핸들러
         */
        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                String line = msg.getData().getString("response");
                String[] response = line.split("ㅣ");
                if("message".equals(response[0])){ // 메시지 수신
                    String chat_room_name = response[1];
                    String chat_member_no = response[2];
                    String chat_content = response[3];
                    String chat_time = response[4];

                    if(chat_room_name.equals(this_chat_room_name)){ // 현재 채팅방에 온 메시지인경우
                        Log.d(TAG,"[채팅방] "+line);
//                        ChatDatabaseManager chatDatabaseManager = ChatDatabaseManager.getInstance(getApplicationContext());
//                        chatDatabaseManager.chatRoomInsert(chat_room_name,chat_content,chat_time); // 채팅방이 없는 경우 채팅방 생성
//                        chatDatabaseManager.chatInsert(chat_room_name,Integer.parseInt(chat_member_no),chat_content,chat_time); // 채팅 INSERT
                        if(Integer.parseInt(chat_member_no)==chat_member.member_no){
                            chatAdapter.addItem(new Chat(chat_room_name,Integer.parseInt(chat_member_no),chat_content,chat_time,chat_member.member_nickname,chat_member.member_profile_img));
                            chat_recyclerview.scrollToPosition(chatAdapter.getItemCount()-1);
                        }else if(Integer.parseInt(chat_member_no)==login_member.member_no){
                            /*
                                로그인 한 멤버의 클라이언트에서만 서버에 채팅 업로드
                             */
                            chatInsertToServer(chat_room_name,Integer.parseInt(chat_member_no),chat_content,chat_time);
                        }
                    }
                }
            }
        }; // Handler

        /*
            리씨버 설정
         */
        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO Auto-generated method stub
                // Get extra data included in the Intent
                String message = intent.getStringExtra("message");
                Log.d(TAG, "Got message: " + message);

                /*
                 * 핸들러에 메시지 보내기
                 */
                Bundle data = new Bundle();
                data.putString("response",message);
                Message msg = new Message();
                msg.setData(data);
                handler.sendMessage(msg);
            }
        };

    } // onCreate() 메소드

    /**
     * onResume() 메소드
     */
    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("custom-event-name")); // 리씨버 등록

        /*
            푸시알림 제거 쓰레드 시작
         */
        notiRemoveThread = new NotiRemoveThread();
        notiRemoveThread.start();

    } // onResume() 메소드

    /**
     * onPause() 메소드
     */
    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver); // 리씨버 해지

        /*
            푸시알림 제거 쓰레드 시작
         */
        notiRemoveThread.threadStop();
        notiRemoveThread.interrupt();
        notiRemoveThread = null;
    } // onPause() 메소드

    /**
     * onDestroy() 메소드
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
    } // onDestroy() 메소드

    /**
     * 서버에서 채팅 가지오고는 메소드
     */
    public void getChatThisChatRoomFromServer(Member login_member, Member chat_member, String this_chat_room_name){
        Log.d(TAG,"getChatThisChatRoomFromServer() 호출");

        chats = new ArrayList<>();

        // 요청 인자
        Map<String,String> params = new HashMap<String,String>();
        params.put("mode","get_chat_this_chat_room");
        params.put("this_chat_room_name",this_chat_room_name);
        params.put("login_member_no",String.valueOf(login_member.member_no));
        params.put("login_member_nickname",login_member.member_nickname);
        params.put("login_member_profile_img",login_member.member_profile_img);
        params.put("chat_member_nickname",chat_member.member_nickname);
        params.put("chat_member_profile_img",chat_member.member_profile_img);

        JSONObject jsonObject = new JSONObject(params); // Map을 json으로 만듬

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(jsonObject);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST, MYURL.URL, jsonArray,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response_arr) { // 응답 성공
                        Log.d(TAG,"응답 성공: "+response_arr.toString());

                        try{
                            for(int i=0; i<response_arr.length();i++){
                                JSONObject response = response_arr.getJSONObject(i);
                                Gson gson = new Gson();
                                chats.add(gson.fromJson(response.toString(), Chat.class)); // 채팅 리스트에 해당 채팅 추가
                            }
                        }catch (JSONException e){
                            Log.d(TAG,"JSONException: "+e.toString());
                        }

                        /*
                         * 채팅 recyclerview 관련 코드
                         */
                        chat_recyclerview = findViewById(R.id.chat_recyclerview);
                        chat_recyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        sort(chats); // 리뷰를 최근순으로 정렬
                        chatAdapter = new ChatAdapter(chats,ChatActivity.this);
                        chat_recyclerview.setAdapter(chatAdapter);
                        chat_recyclerview.scrollToPosition(chatAdapter.getItemCount()-1);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) { // 응답 실패
                Log.d(TAG,"응답 실패: "+error.toString());
            }
        });

        /*
            requestQueue가 없으면 requestQueue 생성
         */
        if(AppHelper.requestQueue == null){ // requestQueue 가 없을 경우
            AppHelper.requestQueue = Volley.newRequestQueue(getApplicationContext()); // requestQueue 생성
        }

        AppHelper.requestQueue.add(jsonArrayRequest); // 요청 큐에 위 요청 추가
    } // getChatThisChatRoomFromServer() 메소드

    /**
     * sort() 메소드
     * 채팅을 최근 순으로 정렬해주는 메소드
     */
    ArrayList<Chat> sort(ArrayList<Chat> chats){
        for (int i = 0; i < chats.size()-1; i++) {
            for (int j = 1; j < chats.size(); j++) {
                if(chats.get(j-1).chat_no > chats.get(j).chat_no){
                    Chat temp_chat = chats.get(j-1);
                    chats.set(j-1,chats.get(j));
                    chats.set(j,temp_chat);
                }
            }
        }
        return chats;
    } // 정렬 메소드

    /**
     * 메시지 전송 메소드
     */
    private void sendMessage(String temp_chat, String chat_time) {

        /**
         * 서비스 시작
         */
        Intent send_message_intent = new Intent(getApplicationContext(), ChatService.class);
        send_message_intent.putExtra("sendMessage","sendMessage");
        send_message_intent.putExtra("temp_chat",temp_chat);
        send_message_intent.putExtra("chat_time",chat_time);
        send_message_intent.putExtra("this_chat_room_name",this_chat_room_name);
        startService(send_message_intent);

    } // sendMessage() 메소드

    /**
     * Calendar를 년월일시분초로 반환 메소드
     */
    public String timeToString(Calendar time) {
        String timeToString = (time.get(Calendar.YEAR)) + "." + (time.get(Calendar.MONTH) + 1) + "."
                + (time.get(Calendar.DAY_OF_MONTH)) + " " + (time.get(Calendar.HOUR_OF_DAY)) + "시"
                + (time.get(Calendar.MINUTE))+"분"+(time.get(Calendar.SECOND))+"초";
        return timeToString.substring(2);
    } // timeToString() 메소드

    /**
     * 서버에 채팅을 업로드 ( + 만약 채팅방이 없을 경우 채팅방 생성)
     */
    private void chatInsertToServer(final String chat_room_name, final int chat_member_no, final String chat_content, final String chat_time){
        Log.d(TAG,"chatInsertToServer() 호출");

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                MYURL.URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) { // 응답이 성공
                        if("0".equals(response)){ // 채팅 업로드 성공
                            Log.d(TAG,"응답성공>> 채팅 업로드 성공: "+response);

                        }else{ // 채팅 업로드 실패
                            Log.d(TAG,"응답성공>> 채팅 업로드 실패: "+response);
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
                params.put("mode","chat_insert");
                params.put("chat_room_name",chat_room_name);
                params.put("chat_member_no",String.valueOf(chat_member_no));
                params.put("chat_content",chat_content);
                params.put("chat_time",chat_time);
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
    } // chatInsertToServer() 메소드

    /**
     * 노티를 제거해주는 쓰레드
     */
    class NotiRemoveThread extends Thread{
        String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그
        private boolean stop; // 쓰레드를 멈추기 위한 변수

        /**
         * 생성자
         */
        NotiRemoveThread(){
            this.stop = false;
        } // 쓰레드 생성자

        @Override
        public void run(){
            Log.d(TAG,"푸시알림 제거 쓰레드 실행");
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            while(true){
                if(this.stop==true){
                    break;
                }
                notificationManager.cancel(chat_member.member_no);
            }
        } // run() 메소드

        public void threadStop(){
            this.stop = true;
        }

    } // NotiRemoveThread 클래스

} // ChatAcitivity 클래스

