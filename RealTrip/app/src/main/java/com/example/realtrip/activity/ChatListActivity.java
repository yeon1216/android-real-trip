package com.example.realtrip.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.realtrip.AppHelper;
import com.example.realtrip.MYURL;
import com.example.realtrip.R;
import com.example.realtrip.adapter.ChatAdapter;
import com.example.realtrip.adapter.ChatRoomAdapter;
import com.example.realtrip.object.Chat;
import com.example.realtrip.object.ChatRoom;
import com.example.realtrip.object.Member;
import com.example.realtrip.service.ChatService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * ChatListActivity 클래스
 * - 채팅 리스트를 볼 수 있다
 */
public class ChatListActivity extends AppCompatActivity {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    Member login_member;

    EditText search_et;

    ImageView search_member_profile_img_iv;
    TextView search_member_nickname_tv;

    ArrayList<ChatRoom> chatRooms; // 채팅방 리스트
    RecyclerView chat_list_recyclerview;
    ChatRoomAdapter chatRoomAdapter;

    private BroadcastReceiver mMessageReceiver; // 서비스에서 보내주는 메시지를 읽는 리씨버

    Handler handler; // 채팅방 ui를 수정하는 핸들러

    /**
     * onCreate() 메소드
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) { // onCreate() 메소드
        Log.d(TAG,"onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        /*
         * floating 버튼
         */
        FloatingActionButton floating_action_btn = findViewById(R.id.floating_action_btn); // 챗봇화면으로 이동하는 플로팅 버튼
        floating_action_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 플로팅 버튼 클릭시 이벤트
                startActivity(new Intent(getApplicationContext(), ChatBotActivity.class));
            }
        });

        /*
            로그인 멤버 정보 쉐어드에서 가지고오기
         */
        SharedPreferences sharedPreferences = getSharedPreferences("myAppData",MODE_PRIVATE);
        Gson gson = new Gson();
        login_member = gson.fromJson(sharedPreferences.getString("login_member","no_login"),Member.class);

        search_member_profile_img_iv = findViewById(R.id.search_member_profile_img_iv); // 검색한 멤버 프로필 이미지 이미지뷰
        search_member_nickname_tv = findViewById(R.id.search_member_nickname_tv); // 검색한 멤버 닉네임 텍스트뷰

        search_et = findViewById(R.id.search_et); // 대화할 상대를 검색하는 검색창
        search_et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) { // 키보드에 있는 검색 버튼 클릭시 이벤트
                if(actionId == EditorInfo.IME_ACTION_SEARCH){
                    String search_member = search_et.getText().toString(); // 입력한 검색어 받아오기

                    /*
                        검색했을 때 키보드 숨기는 코드
                     */
                    InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(search_et.getWindowToken(),0);

                    /*
                        검색한 멤버정보 가지고오기
                     */
                    getMemberInfo(search_member);
                }
                return false;
            }
        });

        ImageView search_iv = findViewById(R.id.search_iv); // 검색 이미지뷰
        search_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 검색 이미지뷰 클릭시 이벤트
                if(findViewById(R.id.search_ll).getVisibility()==View.VISIBLE){
                    search_et.setText("");
                    findViewById(R.id.search_ll).setVisibility(View.GONE);
                    findViewById(R.id.search_result_ll).setVisibility(View.GONE);
                }else{
                    findViewById(R.id.search_ll).setVisibility(View.VISIBLE);
                }
            }
        });

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

                chatRoomAdapter.getMessageFromSocketServer(message);

                /*
                    핸들러에 메시지 보내기
                 */
                Bundle data = new Bundle();
                data.putString("response",message);
                Message msg = new Message();
                msg.setData(data);
                handler.sendMessage(msg);
            }
        };

        /*
            채팅방 ui 수정하는 핸들러
         */
        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
//                Toast.makeText(getApplicationContext(),msg.getData().getString("response"),Toast.LENGTH_SHORT).show();

                String line = msg.getData().getString("response");
                String[] response = line.split("ㅣ");
                if("message".equals(response[0])){ // 메시지 수신
                    String chat_room_name = response[1];
                    String chat_member_no = response[2];
                    String chat_content = response[3];
                    String chat_time = response[4];
                }

            }
        };

    } // onCreate() 메소드

    /**
     * onResume() 메소드
     */
    @Override
    protected void onResume() { // onResume() 메소드
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("custom-event-name")); // 리씨버 등록

        getChatRoomFromServer(login_member); // 서버에서 채팅방 가지고오기

        overridePendingTransition(0,0); // 화면전환 애니메이션 없애는 코드

        /*
            바텀 네비게이션
         */
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.getMenu().getItem(2).setChecked(true); // 채팅 메뉴 활성화
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) { // 바텀 네비게이션 메뉴 클릭시 이벤트
                Intent intent;
                switch (menuItem.getItemId()) {
                    case R.id.navigation_home: // 홈 메뉴 클릭 시
                        intent = new Intent(getApplicationContext(),HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
                        return true;
                    case R.id.navigation_real_trip: // 리얼 트립 메뉴 클릭 시
                        intent = new Intent(getApplicationContext(),RealTripActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
                        return true;
                    case R.id.navigation_chat: // 챗 메뉴 클릭 시
                        intent = new Intent(getApplicationContext(), ChatListActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        return true;
                    case R.id.navigation_mypage: // 마이페이지 메뉴 클릭 시
                        intent = new Intent(getApplicationContext(),MyPageActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
                        return true;
                }
                return false;
            }
        });
    } // onResume() 메소드

    /**
     * onPause() 메소드
     */
    @Override
    protected void onPause() { // onPause() 메소드
        super.onPause();
        overridePendingTransition(0,0); // 화면전환 애니메이션 없애는 코드
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver); // 리씨버 해지
    } // onPause() 메소드

    /**
     * onDestroy() 메소드
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy()     ChatService.serviceIntent : "+ ChatService.serviceIntent);

        /*
         * ChatService를 백그라운드에서 실행시키기위해 ChatService를 종료시킴
         */
//        if (ChatService.serviceIntent!=null) {
//            stopService(ChatService.serviceIntent);
//            ChatService.serviceIntent = null;
//        }


    } // onDestroy() 메소드


    /**
     * 서버에서 채팅방 리스트 가지고오는 메소드
     */
    public void getChatRoomFromServer(Member login_member){
        Log.d(TAG,"getChatThisChatRoomFromServer() 호출");

        chatRooms = new ArrayList<>();

        // 요청 인자
        Map<String,String> params = new HashMap<String,String>();
        params.put("mode","get_chat_room");
        params.put("login_member_no",String.valueOf(login_member.member_no));

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
                                chatRooms.add(gson.fromJson(response.toString(), ChatRoom.class));
                            }
                        }catch (JSONException e){
                            Log.d(TAG,"JSONException: "+e.toString());
                        }

                        /*
                            채팅방 recyclerview 관련 코드
                         */
                        chat_list_recyclerview = findViewById(R.id.chat_list_recyclerview);
                        chat_list_recyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        sort(chatRooms); // 리뷰를 최근순으로 정렬
                        chatRoomAdapter = new ChatRoomAdapter(chatRooms,ChatListActivity.this);
                        chat_list_recyclerview.setAdapter(chatRoomAdapter);
                        chat_list_recyclerview.scrollToPosition(0);

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
    } // getChatRoomFromServer() 메소드

    /**
     * sort() 메소드
     * 채팅방을 시간순으로 정렬해주는 메소드
     */
    ArrayList<ChatRoom> sort(ArrayList<ChatRoom> chatRooms){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy.MM.dd hh시mm분ss초");
        for (int i = 0; i < chatRooms.size()-1; i++) {
            for (int j = 1; j < chatRooms.size(); j++) {

                ChatRoom chatRoom1 = chatRooms.get(j-1);
                Date date1 = null;
                try { date1 = simpleDateFormat.parse(chatRoom1.last_chat_time); } catch (ParseException e) { e.printStackTrace(); }
                long time_long1 = date1.getTime();

                ChatRoom chatRoom2 = chatRooms.get(j);
                Date date2 = null;
                try { date2 = simpleDateFormat.parse(chatRoom2.last_chat_time); } catch (ParseException e) { e.printStackTrace(); }
                long time_long2 = date2.getTime();

                Log.d(TAG,"1. chat_room_name: "+chatRoom1.chat_room_name+" ♠ last_chat_time: "+chatRoom1.last_chat_time+" ♠ time_long1: "+time_long1);
                Log.d(TAG,"2. chat_room_name: "+chatRoom2.chat_room_name+" ♠ last_chat_time: "+chatRoom2.last_chat_time+" ♠ time_long2: "+time_long2);

                if(time_long1<time_long2){
                    ChatRoom temp_chat_room = chatRooms.get(j-1);
                    chatRooms.set(j-1,chatRooms.get(j));
                    chatRooms.set(j,temp_chat_room);
                }

            }
        }
        return chatRooms;
    } // 정렬 메소드

    /**
     * getMemberInfo() 메소드
     */
    public void getMemberInfo(final String member_nickname){
        Log.d(TAG,"getMemberInfo() 호출");

        // Post 인자
        Map<String,String> params = new HashMap<String,String>();
        params.put("mode","find_member_by_member_nickname");
        params.put("member_nickname", member_nickname);

        JSONObject jsonObject = new JSONObject(params); // Map을 json으로 만듬

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, MYURL.URL,jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) { // 응답 성공
                        Log.d(TAG,"응답 성공: "+response.toString());

                        try{
                            if("no".equals(response.getString("member_email"))){ // 해당 닉네임을 가진 멤버가 없음
                                Log.d(TAG,"해당 닉네임을 가진 멤버가 없음");
                                findViewById(R.id.search_result_ll).setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(),"해당 멤버가 없습니다. 다시 검색해주세요",Toast.LENGTH_LONG).show();
                                return;
                            }
                        }catch (JSONException e){
                            e.printStackTrace();
                        }

                        findViewById(R.id.search_result_ll).setVisibility(View.VISIBLE);

                        Gson gson = new Gson();
                        Member member = gson.fromJson(response.toString(),Member.class);
                        final String member_json = response.toString();

                        /*
                            멤버정보 적용
                         */
                        search_member_nickname_tv.setText(member.member_nickname);
                        if("default".equals(member.member_profile_img)){ // 기본 프로필인 경우
                            search_member_profile_img_iv.setImageResource(R.drawable.default_profile);
                        }else{ // 프로필 사진이 있는 경우
                            Glide.with(getApplicationContext())
                                    .load("http://35.224.156.8/uploads/"+member.member_profile_img)
                                    .thumbnail(0.1f)
                                    .placeholder(R.drawable.loading)
                                    .into(search_member_profile_img_iv);
                        }

                        Button chat_btn = findViewById(R.id.chat_btn); // 채팅하기 버튼
                        chat_btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) { // 채팅하기 버튼 클릭시 이벤트
                                search_et.setText("");
                                findViewById(R.id.search_ll).setVisibility(View.GONE);
                                findViewById(R.id.search_result_ll).setVisibility(View.GONE);

                                Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
                                intent.putExtra("chat_member",member_json);
                                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivity(intent);
                            }
                        });

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

} // ChatListActivity 클래스
