package com.example.realtrip.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.baoyz.widget.PullRefreshLayout;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.realtrip.AppHelper;
import com.example.realtrip.MYURL;
import com.example.realtrip.R;
import com.example.realtrip.adapter.BoardAdapter;
import com.example.realtrip.adapter.ReviewAdapter;
import com.example.realtrip.item.ReviewItem;
import com.example.realtrip.object.Board;
import com.example.realtrip.object.Review;
import com.example.realtrip.object.Tourist;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.view.View.GONE;

/**
 * ReviewListActivity 클래스
 */
public class ReviewListActivity extends AppCompatActivity {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    ArrayList<ReviewItem> reviewItems; // 리뷰 리스트

    PullRefreshLayout pullRefreshLayout;

    Tourist tourist; // 여행지 객체
    String tourist_json; // 여행지 json 문자열

    /**
     * onCreate() 메소드
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_list);
        Log.d(TAG,"onCreate() 호출");

        /*
         * 여행지 받아오기
         */
        Intent intent = getIntent();
        tourist_json = intent.getStringExtra("tourist");
        Gson gson = new Gson();
        tourist = gson.fromJson(tourist_json, Tourist.class);

        /*
         * 여행지 데이터 뷰와 연결
         */
        ImageView tourist_img_iv = findViewById(R.id.tourist_img_iv);
        if(tourist.firstimage2!=null){
            Glide.with(this)
                    .load(tourist.firstimage2)
                    .thumbnail(0.1f)
                    .placeholder(R.drawable.loading_icon)
                    .into(tourist_img_iv);
        }
        TextView tourist_title_tv = findViewById(R.id.tourist_title_tv);
        tourist_title_tv.setText(tourist.title);

        /*
         * floating 버튼
         */
        FloatingActionButton floating_action_btn = findViewById(R.id.floating_action_btn); // 글 추가 플로팅 버튼
        floating_action_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 리뷰 추가 플로팅 버튼 클릭시 이벤트
                Intent intent = new Intent(getApplicationContext(), ReviewWriteActivity.class);
                intent.putExtra("tourist",tourist_json);
                startActivity(intent);
            }
        });

        /*
         * 여행지리스트 새로고침
         */
        pullRefreshLayout = findViewById(R.id.pull_refresh_layout); // 리뷰 동기화
        pullRefreshLayout.setRefreshStyle(PullRefreshLayout.STYLE_RING); // 프로그레스 써클
        pullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getReviewRequest();
            }
        });

        /*
         * 여행지 리스트 받아오기
         */
        getReviewRequest();
    } // onCreate() 메소드

    /**
     * onNewIntent() 메소드
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent.getStringExtra("write_review")!=null){
            recreate();
        }
    } // onNewIntent() 메소드

    /**
     * getReviewRequest() 메소드
     * - 해당 여행지의 리뷰들을 가지고옴
     */
    void getReviewRequest(){ // getBoardRequest() 메소드
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

                        for(int i=0; i<reviewItems.size();i++){
                            Log.d(TAG,"reviews[review_no: "+reviewItems.get(i).review_no+", review_content: "+reviewItems.get(i).review_content);
                        }

                        /*
                         * 리뷰 recyclerview 관련 코드
                         */
                        RecyclerView review_recyclerview = findViewById(R.id.review_recyclerview);
                        review_recyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        ReviewAdapter reviewAdapter = new ReviewAdapter(reviewItems,ReviewListActivity.this);
                        sort(reviewItems); // 리뷰를 최근순으로 정렬
                        review_recyclerview.setAdapter(reviewAdapter);

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

} // ReviewListActivity 클래스
