package com.example.realtrip.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.baoyz.widget.PullRefreshLayout;
import com.example.realtrip.AppHelper;
import com.example.realtrip.MYURL;
import com.example.realtrip.R;
import com.example.realtrip.adapter.BroadcastRoomAdapter;
import com.example.realtrip.adapter.ReviewAdapter;
import com.example.realtrip.item.ReviewItem;
import com.example.realtrip.object.BroadcastRoom;
import com.example.realtrip.service.ChatService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * RealTripActivity 클래스
 * - 영상 스트리밍 화면
 */
public class RealTripActivity extends AppCompatActivity {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그
    RecyclerView broadcast_recyclerview; // 방송 리싸이클러뷰
    PullRefreshLayout pullRefreshLayout; // 새로고침

    ArrayList<BroadcastRoom> broadcastRooms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_trip);

        /*
         * floating 버튼
         */
        FloatingActionButton floating_action_btn = findViewById(R.id.floating_action_btn); // 챗봇화면으로 이동하는 플로팅 버튼
        floating_action_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 플로팅 버튼 클릭시 이벤트
                final EditText editText = new EditText(getApplicationContext());
                AlertDialog.Builder builder = new AlertDialog.Builder(RealTripActivity.this);
                builder.setTitle("방송 제목을 입력해주세요");
                builder.setMessage("");
                builder.setView(editText);

                builder.setPositiveButton("확인",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if(editText.getText().toString().length()==0){
                                    Toast.makeText(getApplicationContext(),"방송 제목을 입력해주세요",Toast.LENGTH_SHORT).show();
                                }else{
                                    String temp_broadcast_title = editText.getText().toString();
                                    Intent intent = new Intent(getApplicationContext(),BroadcastActivity.class);
                                    intent.putExtra("temp_broadcast_title",temp_broadcast_title);
                                    startActivity(intent);
                                }
                            }
                        });

                builder.setNeutralButton("취소",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });

                builder.show(); // 다이얼로그 보이게 하기
            }
        });

        broadcast_recyclerview = findViewById(R.id.broadcast_recyclerview);

        /*
         * 방송 리스트 새로고침
         */
        pullRefreshLayout = findViewById(R.id.pull_refresh_layout); // 리뷰 동기화
        pullRefreshLayout.setRefreshStyle(PullRefreshLayout.STYLE_RING); // 프로그레스 써클
        pullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getBroadcastRequest(); // 방송 리스트 가지고오기
            }
        });



    } // onCreate() 메소드

    @Override
    protected void onResume() { // onResume() 메소드
        super.onResume();

        getBroadcastRequest(); // 방송 리스트 가지고오기

        overridePendingTransition(0,0); // 이 화면이 켜질 때 애니메이션 효과 없애기
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view); // 바텀 네비게이션 바
        bottomNavigationView.getMenu().getItem(1).setChecked(true); // 리얼 트립 메뉴 active
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
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        return true;
                    case R.id.navigation_chat: // 챗 메뉴 클릭 시
                        intent = new Intent(getApplicationContext(), ChatListActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
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

    @Override
    protected void onPause() { // onPause() 메소드
        super.onPause();
        overridePendingTransition(0,0); // 애니메이션 효과를 없애기 위한 코드
    } // onPause() 메소드

    /**
     * onDestroy() 메소드
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy()");

    } // onDestroy() 메소드

    /**
     * getReviewRequest() 메소드
     * - 해당 여행지의 리뷰들을 가지고옴
     */
    void getBroadcastRequest(){ // getBoardRequest() 메소드
        Log.d(TAG,"getReviewRequest() 호출");

        broadcastRooms = new ArrayList<>();

        // 요청 인자
        Map<String,String> params = new HashMap<String,String>();
        params.put("mode","get_broadcast_list");

        JSONObject jsonObject = new JSONObject(params); // Map을 json으로 만듬

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(jsonObject);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST, MYURL.URL, jsonArray,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response_arr) { // 응답 성공
                        try{
                            for(int i=0; i<response_arr.length();i++){
                                JSONObject response = response_arr.getJSONObject(i);
                                Gson gson = new Gson();
                                BroadcastRoom broadcastRoom = gson.fromJson(response.toString(),BroadcastRoom.class);
                                if("live".equals(broadcastRoom.is_live)){
                                    broadcastRooms.add(gson.fromJson(response.toString(), BroadcastRoom.class));
                                }
                            }
                        }catch (JSONException e){
                            Log.d(TAG,"JSONException: "+e.toString());
                        }

                        /*
                         * 방송 recyclerview 관련 코드
                         */
                        broadcast_recyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        BroadcastRoomAdapter broadcastRoomAdapter = new BroadcastRoomAdapter(broadcastRooms,RealTripActivity.this);
                        sort(broadcastRooms); // 방송을 최근순으로 정렬
                        broadcast_recyclerview.setAdapter(broadcastRoomAdapter);

                        /*
                         * 새로고침 프로그래스 바 멈추기
                         */
                        pullRefreshLayout.setRefreshing(false);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) { // 응답 실패
                Log.d(TAG,"응답 실패: "+error.toString());
            }
        });

        /*
         * requestQueue가 없으면 requestQueue 생성
         */
        if(AppHelper.requestQueue == null){ // requestQueue 가 없을 경우
            AppHelper.requestQueue = Volley.newRequestQueue(getApplicationContext()); // requestQueue 생성
        }

        AppHelper.requestQueue.add(jsonArrayRequest); // 요청 큐에 위 요청 추가

    } // getBoardRequest() 메소드

    /**
     * sort() 메소드
     * 리뷰를 최근 순으로 정렬해주는 메소드
     */
    ArrayList<BroadcastRoom> sort(ArrayList<BroadcastRoom> broadcastRooms){
        for (int i = 0; i < broadcastRooms.size()-1; i++) {
            for (int j = 1; j < broadcastRooms.size(); j++) {
                if(broadcastRooms.get(j-1).broadcast_room_no < broadcastRooms.get(j).broadcast_room_no){
                    BroadcastRoom broadcastRoom = broadcastRooms.get(j-1);
                    broadcastRooms.set(j-1,broadcastRooms.get(j));
                    broadcastRooms.set(j,broadcastRoom);
                }
            }
        }
        return broadcastRooms;
    } // 정렬 메소드

} // RealTripActivity 클래스
