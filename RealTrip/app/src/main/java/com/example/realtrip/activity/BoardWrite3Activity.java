package com.example.realtrip.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.realtrip.R;

/**
 * BoardWrite3Activity 클래스
 * - 장소 등록
 */
public class BoardWrite3Activity extends AppCompatActivity {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    @Override
    protected void onCreate(Bundle savedInstanceState) { // onCreate() 메소드
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_write3);
    } // onCreate() 메소드

} // BoardWrite3Activity 클래스
