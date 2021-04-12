package com.example.realtrip.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.realtrip.R;

/**
 * BoardWriteActivity 클래스
 * - 이미지 등록
 */
public class BoardWriteActivity extends AppCompatActivity {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    @Override
    protected void onCreate(Bundle savedInstanceState) { // onCreate() 메소드
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_write);

        Button next_btn = findViewById(R.id.next_btn); // 다음 버튼
        next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 다음 버튼 클릭시 이벤트
                Intent intent = new Intent(getApplicationContext(),BoardWrite2Activity.class);
                startActivity(intent);
            }
        });
    } // onCreate() 메소드

} // BoardWriteActivity 클래스
