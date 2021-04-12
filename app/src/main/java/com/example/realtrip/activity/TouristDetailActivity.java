package com.example.realtrip.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.realtrip.AppHelper;
import com.example.realtrip.MYURL;
import com.example.realtrip.R;
import com.example.realtrip.adapter.ReviewAdapter;
import com.example.realtrip.item.ReviewItem;
import com.example.realtrip.object.Tourist;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.view.View.GONE;

/**
 * TouristDetailActivity 클래스
 * 여행지 상세보기 클래스
 */
public class TouristDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    ProgressBar progressBar;
    ImageView tourist_img_iv;

    Handler handler;

    GoogleMap mMap;

    Tourist tourist; // 여행지 객체
    String tourist_json; // 여행지 json 문자열

    ArrayList<ReviewItem> reviewItems;

    /**
     * onCreate() 메소드
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tourist_detail);
        Log.d(TAG,"onCreate() 호출");

        FragmentManager fragmentManager = getFragmentManager();
        MapFragment mapFragment = (MapFragment)fragmentManager.findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /*
         * intent로 여행지 정보 받아오기
         */
        Intent intent = getIntent();
        tourist_json = intent.getStringExtra("tourist");
        Gson gson = new Gson();
        tourist = gson.fromJson(tourist_json,Tourist.class);

        /*
         * 뷰와 데이터 연결
         */
        tourist_img_iv = findViewById(R.id.tourist_img_iv); // 여행지 사진
        progressBar = findViewById(R.id.progress);
        if(tourist.firstimage==null){
            Log.d(TAG,"tourist.firstimage==null");
            progressBar.setVisibility(GONE);
            tourist_img_iv.setVisibility(View.VISIBLE);
            tourist_img_iv.setImageResource(R.drawable.no_image);
        }else{
            Log.d(TAG,"tourist.firstimage!=null");
            Log.d(TAG,"tourist.firstimage: "+tourist.firstimage);
//            new Thread(){
//                public void run(){
//                    try{sleep(2000);}catch (Exception e){}
//                    handler.sendEmptyMessage(0);
//                }
//            }.start();
//            handler = new Handler(){
//                @Override
//                public void handleMessage(@NonNull Message msg) {
//                    super.handleMessage(msg);
//                    if(msg.what==0){
//                        progressBar.setVisibility(GONE);
//                        tourist_img_iv.setVisibility(View.VISIBLE);
//                    }
//                }
//            };
            progressBar.setVisibility(GONE);
            tourist_img_iv.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(tourist.firstimage+"")
                    .thumbnail(0.1f)
                    .placeholder(R.drawable.loading3)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            Log.d(TAG,"onException");
                            progressBar.setVisibility(GONE);
                            tourist_img_iv.setVisibility(View.VISIBLE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            Log.d(TAG,"onResourceReady");
                            progressBar.setVisibility(GONE);
                            tourist_img_iv.setVisibility(View.VISIBLE);
                            return false;
                        }
                    })
                    .into(tourist_img_iv);

        }

        TextView tourist_title_tv = findViewById(R.id.tourist_title_tv); // 여행지 이름
        tourist_title_tv.setText(tourist.title);
        TextView tourist_title_tv2 = findViewById(R.id.tourist_title_tv2); // 여행지 이름
        tourist_title_tv2.setText(tourist.title);
        TextView tourist_addr_tv = findViewById(R.id.tourist_addr_tv); // 여행지 주소
        tourist_addr_tv.setText("주소 : "+tourist.addr1);
        TextView tourist_addr_tv2 = findViewById(R.id.tourist_addr_tv2); // 여행지 주소
        tourist_addr_tv2.setText("주소 : "+tourist.addr1);
        TextView tourist_tel_tv = findViewById(R.id.tourist_tel_tv); // 여행지 전화번호
        if(tourist.tel ==null){
            tourist_tel_tv.setVisibility(GONE);
        }else{
            tourist_tel_tv.setText("전화번호 : "+tourist.tel);
        }

        TextView show_map_tv = findViewById(R.id.show_map_tv); // 지도 보이기 텍스트뷰
        show_map_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 지도 보이기 텍스트뷰 클릭시 이벤트
                findViewById(R.id.map_ll).setVisibility(View.VISIBLE);
            }
        });

        TextView hide_map_tv = findViewById(R.id.hide_map_tv); // 지도 숨기기 텍스트뷰
        hide_map_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 지도 숨기기 텍스트뷰 클릭시 이벤트
                findViewById(R.id.map_ll).setVisibility(View.GONE);
            }
        });

        Button show_review_btn = findViewById(R.id.show_review_btn); // 리뷰 보기
        show_review_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 리뷰 보기 클릭시 이벤트
                Intent intent1 = new Intent(getApplicationContext(),ReviewListActivity.class);
                intent1.putExtra("tourist", tourist_json);
                startActivity(intent1);
            }
        });
        Button write_review_btn = findViewById(R.id.write_review_btn); // 리뷰 작성
        write_review_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 리뷰 작성 클릭시 이벤트
                Intent intent1 = new Intent(getApplicationContext(),ReviewWriteActivity.class);
                intent1.putExtra("tourist", tourist_json);
                startActivity(intent1);
            }
        });

        final TextView show_review_analysis_detail_tv = findViewById(R.id.show_review_analysis_detail_tv); // 리뷰분석 상세보기 텍스트뷰
        show_review_analysis_detail_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(findViewById(R.id.chart_web_view).getVisibility()==View.VISIBLE){ // 리뷰분석 숨기기
                    findViewById(R.id.chart_web_view).setVisibility(View.GONE);
                    show_review_analysis_detail_tv.setText("리뷰분석 상세보기");
                }else{ // 리뷰분석 보기
                    findViewById(R.id.chart_web_view).setVisibility(View.VISIBLE);
                    show_review_analysis_detail_tv.setText("숨기기");
                    Toast.makeText(getApplicationContext(),"화면을 밑으로 스크롤하세요 ↓",Toast.LENGTH_SHORT).show();
                }

            }
        });
    } // onCreate() 메소드

    /**
     * onResume() 메소드
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume() 호출");
        /**
         * 해당 여행지 감정분석 결과 가지고 오기
         */
        getReviewRequest();
    } // onResume() 메소드

    /**
     * 리뷰목록 가지고 오는 요청
     */
    private void getReviewRequest(){
        Log.d(TAG,"getReviewRequest() 호출");
        reviewItems = new ArrayList<>();

        // 요청 인자
        Map<String,String> params = new HashMap<String,String>();
        params.put("mode","get_review_list");
        params.put("content_id",tourist.contentid);

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
                                reviewItems.add(gson.fromJson(response.toString(), ReviewItem.class));
                            }
                        }catch (JSONException e){
                            Log.d(TAG,"JSONException: "+e.toString());
                        }

                        /**
                         * 리뷰 갯수 확인, 리뷰가 없으면 리턴
                         */
                        if(reviewItems.size()==0){
                            Toast.makeText(getApplicationContext(),"리뷰가 없습니다.",Toast.LENGTH_SHORT).show();
                            return;
                        }

                        /**
                         * 1. 리뷰의 score 와 magnitude 를 가지고 review count 하기
                         * magnitude<=0.3 || 0<=score<=0.1   ==>> 중립
                         * 0.1<score<=0.6   ==>> 긍정
                         * 0.6<score<=1   ==>> 강한 긍정
                         * -0.6<=score<0   ==>> 부정
                         * -1<=score<0.6   ==>> 강한 부정
                         *
                         */
                        String score_arr="";
                        String magnitude_arr="";
                        int[] review_analysis = new int[5];
                        for(int i=0; i<reviewItems.size();i++){
                            ReviewItem review_item = reviewItems.get(i);
                            if(reviewItems.size()-1==i){
                                score_arr=score_arr+review_item.review_score;
                                magnitude_arr = magnitude_arr+review_item.review_magnitude;
                            }else{
                                score_arr=score_arr+review_item.review_score+",";
                                magnitude_arr = magnitude_arr+review_item.review_magnitude+",";
                            }
                            double score = Double.parseDouble(review_item.review_score);
                            double magnitude = Double.parseDouble(review_item.review_magnitude);
                            if(magnitude<=0.3 || (0<=score && score<=0.1)){ // 중립
                                review_analysis[2]++;
                            }else if((0.1<score && score<=0.6)){ // 긍정
                                review_analysis[3]++;
                            }else if((0.6<score && score<=1)){ // 강한 긍정
                                review_analysis[4]++;
                            }else if((-0.6<=score && score<0)){ // 부정
                                review_analysis[1]++;
                            }else if((-1<=score && score<0.6)){ // 강한 부정
                                review_analysis[0]++;
                            }
                        }

                        TextView very_positive_review_count_tv = findViewById(R.id.very_positive_review_count_tv);
                        TextView positive_review_count_tv = findViewById(R.id.positive_review_count_tv);
                        TextView neutrality_review_count_tv = findViewById(R.id.neutrality_review_count_tv);
                        TextView negative_review_count_tv = findViewById(R.id.negative_review_count_tv);
                        TextView very_negative_review_count_tv = findViewById(R.id.very_negative_review_count_tv);

                        if(review_analysis[4]==0){
                            very_positive_review_count_tv.setText("없음");
                        }else{
                            very_positive_review_count_tv.setText(review_analysis[4]+"개");
                        }
                        if(review_analysis[3]==0){
                            positive_review_count_tv.setText("없음");
                        }else{
                            positive_review_count_tv.setText(review_analysis[3]+"개");
                        }
                        if(review_analysis[2]==0){
                            neutrality_review_count_tv.setText("없음");
                        }else{
                            neutrality_review_count_tv.setText(review_analysis[2]+"개");
                        }
                        if(review_analysis[1]==0){
                            negative_review_count_tv.setText("없음");
                        }else{
                            negative_review_count_tv.setText(review_analysis[1]+"개");
                        }
                        if(review_analysis[0]==0){
                            very_negative_review_count_tv.setText("없음");
                        }else{
                            very_negative_review_count_tv.setText(review_analysis[0]+"개");
                        }

                        /**
                         * 2. 리뷰의 score 와 magnitude 를 가지고 그래프 만들기
                         */
                        WebView chart_web_view=findViewById(R.id.chart_web_view);
                        chart_web_view.getSettings().setJavaScriptEnabled(true);
                        chart_web_view.loadUrl("http://35.224.156.8/chart.php?score="+score_arr+"&magnitude="+magnitude_arr);

                        Toast.makeText(getApplicationContext(),"리뷰분석 완료",Toast.LENGTH_SHORT).show();
                        findViewById(R.id.review_analysis_ll).setVisibility(View.VISIBLE);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) { // 응답 실패
                Log.d(TAG,"응답 실패: "+error.toString());
            }
        });

        /**
         * requestQueue가 없으면 requestQueue 생성
         */
        if(AppHelper.requestQueue == null){ // requestQueue 가 없을 경우
            AppHelper.requestQueue = Volley.newRequestQueue(getApplicationContext()); // requestQueue 생성
        }

        AppHelper.requestQueue.add(jsonArrayRequest); // 요청 큐에 위 요청 추가
    } // getReviewRequest() 메소드

    /**
     * sort() 메소드
     * 리뷰를 최근 순으로 정렬해주는 메소드
     */
    ArrayList<ReviewItem> sort(ArrayList<ReviewItem> reviewItems){
        for (int i = 0; i < reviewItems.size()-1; i++) {
            for (int j = 1; j < reviewItems.size(); j++) {
                if(reviewItems.get(j-1).review_no < reviewItems.get(j).review_no){
                    ReviewItem temp_review_item = reviewItems.get(j-1);
                    reviewItems.set(j-1,reviewItems.get(j));
                    reviewItems.set(j,temp_review_item);
                }
            }
        }
        return reviewItems;
    } // 정렬 메소드

    /**
     * onMapReady() 메소드
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG,"onMapReady() 호출");
        mMap = googleMap;
        /**
         * 좌표(위도, 경도) 생성
         */
        Log.d(TAG,"tourist.mapx: "+tourist.mapx);
        Log.d(TAG,"tourist.mapy: "+tourist.mapy);
        LatLng point = new LatLng(Double.parseDouble(tourist.mapy), Double.parseDouble(tourist.mapx));

        /**
         * 마커 생성
         */
        MarkerOptions mOptions2 = new MarkerOptions();
        mOptions2.title(tourist.title);
        mOptions2.snippet(tourist.addr1);
        mOptions2.position(point);

        /**
         * 마커 생성
         */
        mMap.addMarker(mOptions2);

        /**
         * 해당 좌표(point)로 카메라 줌
         */
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point,12));

    } // onMapReady() 메소드



}// TouristDetailActivity 클래스
