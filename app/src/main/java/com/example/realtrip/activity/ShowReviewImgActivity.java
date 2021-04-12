package com.example.realtrip.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.realtrip.R;
import com.example.realtrip.adapter.ReviewImgAdapter;

import java.util.ArrayList;

/**
 * ShowReviewImgActivity 클래스
 */
public class ShowReviewImgActivity extends AppCompatActivity {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그
    ArrayList<String> temp_file_path_arr;

    /**
     * onCreate() 메소드
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_review_img);



        temp_file_path_arr = new ArrayList<>();

        Intent intent = getIntent();
        String img_arr_str = intent.getStringExtra("img_arr");
        String[] img_arr = img_arr_str.split("\\|");
        for(int i=0; i<img_arr.length;i++){
            Log.d(TAG,i+"번째 이미지: "+img_arr[i]);
            temp_file_path_arr.add(img_arr[i]);
        }

        if(temp_file_path_arr.size()>1){
            Toast.makeText(getApplicationContext(),"사진을 밀어보세요 ----->>>",Toast.LENGTH_SHORT).show();
        }

        RecyclerView review_img_recyclerview = findViewById(R.id.review_img_recyclerview);
        review_img_recyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.HORIZONTAL,false));
        ReviewImgAdapter reviewImgAdapter = new ReviewImgAdapter(temp_file_path_arr,ShowReviewImgActivity.this);
        review_img_recyclerview.setAdapter(reviewImgAdapter);

    } // onCreate() 메소드
} // ShowReviewImgActivity 클래스
