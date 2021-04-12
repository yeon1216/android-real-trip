package com.example.realtrip.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;
import com.example.realtrip.R;
import com.example.realtrip.asynctask.GetTourist;
import com.example.realtrip.object.Tourist;
import com.example.realtrip.service.ChatService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

/**
 * HomeActivity 클래스
 * - 여행 후기 SNS
 * - 설계하자!!
 */
public class HomeActivity extends AppCompatActivity {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    EditText search_et; // 검색창
    LinearLayout search_ll; // 검색 레이아웃

//    ArrayList<Board> boards; // 게시글 리스트
    ArrayList<Tourist> tourists; // 여행지 리스트

    PullRefreshLayout pullRefreshLayout;

    String remember_request_type_for_refresh="0";
    String remember_areacode_or_keyword_for_refresh="0";

    /**
     * onCreate() 메소드
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) { // onCreate() 메소드
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Log.d(TAG,"onCreate()");

        /*
            절전모드를 사용하지 않는 예외앱으로 처리하기 위한 코드
         */
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(POWER_SERVICE); // 시스템으로부터 파워매니저 얻기
        boolean isWhiteListing = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) { // 마쉬멜로 버전 이상 폰이라면
            /*
                PowerManager isIgnoringBatteryOptimizations() 메소드
                배터리 최적화가 설정되어 있으면 true 반환, 설정 안되어 있으면 false 반환
             */
            isWhiteListing = pm.isIgnoringBatteryOptimizations(getApplicationContext().getPackageName());
        }
        if (!isWhiteListing) { // 베터리 최적화가 설정되어있지 않으면 베터리 최적화 설정
            Intent intent = new Intent();
            intent.setAction(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
            startActivity(intent);
        }

        search_ll = findViewById(R.id.search_ll); // 검색 레이아웃

        /*
         * 검색 아이콘
         */
        ImageView search_iv = findViewById(R.id.search_iv); // 검색 아이콘
        search_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 검색 아이콘 클릭시 이벤트
                Log.d(TAG,"검색 아이콘 클릭");


                if(search_ll.getVisibility()==View.VISIBLE){ // 검색창이 보이는 상태이면
                    search_ll.setVisibility(View.GONE);
                }else{ // 검색창이 보이지 않는 상태이면
                    search_ll.setVisibility(View.VISIBLE);
                }

            }
        });

        /*
         * 검색 창
         */
        search_et = findViewById(R.id.search_et); // 검색 창
        search_et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) { // 키보드에 있는 검색 버튼 클릭시 이벤트
                if(actionId == EditorInfo.IME_ACTION_SEARCH){
                    String keyword = search_et.getText().toString(); // 입력한 검색어 받아오기

                    /*
                     * 아마 검색 했을 때 키보드 숨기는 코드
                     */
                    InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(search_et.getWindowToken(),0);

                    /*
                     * 키워드를 통해 검색
                     */
                    getTouristRequest("1",keyword);
                }
                return false;
            }
        });

        /*
         * 한국 관광공사 api에서 여행지 리스트 가지고 오기
         */
        getTouristRequest("0","0");

        /*
         * 여행지리스트 새로고침
         */
        pullRefreshLayout = findViewById(R.id.pull_refresh_layout); // 게시글 동기화
        pullRefreshLayout.setRefreshStyle(PullRefreshLayout.STYLE_RING); // 프로그레스 써클
        pullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getTouristRequest(remember_request_type_for_refresh,remember_areacode_or_keyword_for_refresh);
            }
        });

        /*
         * 장소버튼 클릭
         */
        clickArea();



    } // onCreate() 메소드

    /**
     * onNewIntent() 메소드
     */
    @Override
    protected void onNewIntent(Intent intent) { // onNewIntent() 메소드
        super.onNewIntent(intent);
        Log.d(TAG,"onNewIntent() 메소드");
        /*
            챗봇에서 검색한 경우
         */
        if(intent.getStringExtra("from_chat_bot")!=null){
            getTouristRequest("1",intent.getStringExtra("keyword"));
            search_ll.setVisibility(View.VISIBLE); // 검색 레이아웃 보이게 하기
            search_et.setText(intent.getStringExtra("keyword"));
        }
    } // onNewIntent() 메소드

    /**
     * onPause() 메소드
     */
    @Override
    protected void onResume() { // onResume() 메소드
        super.onResume();

        Log.d(TAG,"onResume() 메소드");

        overridePendingTransition(0,0); // 액티비티 전환시 애니메이션 효과를 없애기 위한 코드

        /*
         * 바텀 네비게이션 클릭 이벤트 코드
         */
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view); // 바텀 네비게이션 바
        bottomNavigationView.getMenu().getItem(0).setChecked(true); // 홈 메뉴 activie
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) { // 바텀 네비게이션 메뉴 클릭시 이벤트
                Intent intent;
                switch (menuItem.getItemId()) {
                    case R.id.navigation_home: // 홈 메뉴 클릭 시
                        intent = new Intent(getApplicationContext(),HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        return true;
                    case R.id.navigation_real_trip: // 리얼 트립 메뉴 클릭 시
                        intent = new Intent(getApplicationContext(),RealTripActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
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

        /*
            챗봇에서 검색한 경우
         */
        if(getIntent().getStringExtra("from_chat_bot")!=null){
            getTouristRequest("1",getIntent().getStringExtra("keyword"));
            search_ll.setVisibility(View.VISIBLE); // 검색 레이아웃 보이게 하기
            search_et.setText(getIntent().getStringExtra("keyword"));
        }

    } // onResume() 메소드

    /**
     * onPause() 메소드
     */
    @Override
    protected void onPause() { // onPause() 메소드
        super.onPause();
        overridePendingTransition(0,0); // 화면전환시 애니메이션 효과를 없애기 위해서
    } // onPause() 메소드

    /**
     * onDestroy() 메소드
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy()     ChatService.serviceIntent : "+ChatService.serviceIntent);

        /*
         * ChatService를 백그라운드에서 실행시키기위해 ChatService를 종료시킴
         */
//        if (ChatService.serviceIntent!=null) {
//            stopService(ChatService.serviceIntent);
//            ChatService.serviceIntent = null;
//        }


    } // onDestroy() 메소드

    /**
     * getTouristRequest() 메소드
     * - 여행지 리스트를 리싸이클러뷰에 보여줌
     * - GetTourist asynctask 실행
     */
    public void getTouristRequest(String request_type, String areacode_or_keyword){
        GetTourist getTourist = new GetTourist(HomeActivity.this);
        getTourist.execute(request_type, areacode_or_keyword); // GetTourist asynctask 실행
        remember_request_type_for_refresh = request_type;
        remember_areacode_or_keyword_for_refresh = areacode_or_keyword;
    } // getTouristRequest() 메소드

    /**
     * clickArea() 메소드
     * - 지역 버튼을 클릭하여 지역기반 검색 요청을 진행함
     *
     * 지역 : areaCode
     * 서울 1
     * 인천 2
     * 대전 3
     * 대구 4
     * 광주 5
     * 부산 6
     * 울산 7
     * 세종특별자치시 8
     * 경기도 31
     * 강원도 32
     * 충청북도 33
     * 충청남도 34
     * 경상북도 35
     * 경상남도 36
     * 전라북도 37
     * 전라남도 38
     * 제주도 39
     */
    public void clickArea(){
        findViewById(R.id.area_0_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_et.setText("");
                search_ll.setVisibility(View.GONE);
                getTouristRequest("0","0");
            }
        });
        findViewById(R.id.area_1_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_et.setText("");
                search_ll.setVisibility(View.GONE);
                getTouristRequest("0","1");
            }
        });
        findViewById(R.id.area_2_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_et.setText("");
                search_ll.setVisibility(View.GONE);
                getTouristRequest("0","2");
            }
        });
        findViewById(R.id.area_3_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_et.setText("");
                search_ll.setVisibility(View.GONE);
                getTouristRequest("0","3");
            }
        });
        findViewById(R.id.area_4_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_et.setText("");
                search_ll.setVisibility(View.GONE);
                getTouristRequest("0","4");
            }
        });
        findViewById(R.id.area_5_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_et.setText("");
                search_ll.setVisibility(View.GONE);
                getTouristRequest("0","5");
            }
        });
        findViewById(R.id.area_6_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_et.setText("");
                search_ll.setVisibility(View.GONE);
                getTouristRequest("0","6");
            }
        });
        findViewById(R.id.area_7_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_et.setText("");
                search_ll.setVisibility(View.GONE);
                getTouristRequest("0","7");
            }
        });
        findViewById(R.id.area_8_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_et.setText("");
                search_ll.setVisibility(View.GONE);
                getTouristRequest("0","8");
            }
        });
        findViewById(R.id.area_31_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_et.setText("");
                search_ll.setVisibility(View.GONE);
                getTouristRequest("0","31");
            }
        });
        findViewById(R.id.area_32_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_et.setText("");
                search_ll.setVisibility(View.GONE);
                getTouristRequest("0","32");
            }
        });
        findViewById(R.id.area_33_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_et.setText("");
                search_ll.setVisibility(View.GONE);
                getTouristRequest("0","33");
            }
        });
        findViewById(R.id.area_34_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_et.setText("");
                search_ll.setVisibility(View.GONE);
                getTouristRequest("0","34");
            }
        });
        findViewById(R.id.area_35_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_et.setText("");
                search_ll.setVisibility(View.GONE);
                getTouristRequest("0","35");
            }
        });
        findViewById(R.id.area_36_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_et.setText("");
                search_ll.setVisibility(View.GONE);
                getTouristRequest("0","36");
            }
        });
        findViewById(R.id.area_37_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_et.setText("");
                search_ll.setVisibility(View.GONE);
                getTouristRequest("0","37");
            }
        });
        findViewById(R.id.area_38_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_et.setText("");
                search_ll.setVisibility(View.GONE);
                getTouristRequest("0","38");
            }
        });
        findViewById(R.id.area_39_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_et.setText("");
                search_ll.setVisibility(View.GONE);
                getTouristRequest("0","39");
            }
        });
    } // clickArea() 메소드

} // HomeActivity 클래스

