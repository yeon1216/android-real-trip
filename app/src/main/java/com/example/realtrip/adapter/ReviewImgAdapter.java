package com.example.realtrip.adapter;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.realtrip.R;

import java.util.ArrayList;

/**
 * BoardAdapter 클래스
 * - 게시글 리싸이클러뷰를 위한 어댑터
 */
public class ReviewImgAdapter extends RecyclerView.Adapter<ReviewImgAdapter.ViewHolder> {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    ArrayList<String> temp_file_path_arr; // 게시글 리스트
    Activity activity;
    Context context;


    /**
     * BoardAdapter 생성자
     */
    public ReviewImgAdapter(ArrayList<String> temp_file_path_arr, Activity activity){
        this.temp_file_path_arr = temp_file_path_arr;
        this.activity = activity;
        this.context = activity.getApplicationContext();
    } // BoardAdapter 생성자

    /**
     * onCreateViewHolder() 메소드 :
     * @return 뷰홀더 반환
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { // onCreateViewHolder() 메소드
        /**
         * inflater 구현
         * inflater로 아이템 뷰를 객체로 만든다
         */
        LayoutInflater inflater  = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.recyclerview_review_img_item,parent,false); // 뷰 객체 생성
        ReviewImgAdapter.ViewHolder viewHolder = new ReviewImgAdapter.ViewHolder(view);
        return viewHolder;
    } // onCreateViewHolder() 메소드

    /**
     * onBindViewHolder() 메소드 : item.xml 에 값을 연결
     * @param holder ViewHolder 객체
     * @param position 아이템 포지션 (근데 이거 잘 안맞음. holder에서 getPosition해서 사용하는게 더 좋은거같음)
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) { // onBindViewHolder() 메소드
        if(holder.getAdapterPosition()!=RecyclerView.NO_POSITION){ // 포지션 에러를 최소화 하기 위한 조건
            String temp_file_path = temp_file_path_arr.get(holder.getAdapterPosition());

            Glide.with(context)
                    .load("http://35.224.156.8/uploads/"+temp_file_path)
                    .thumbnail(0.1f)
                    .placeholder(R.drawable.loading)
                    .into(holder.review_img_iv);
        }
    } // onBindViewHolder() 메소드

    /**
     * getItemCount() 메소드
     * @return 아이템 갯수 반환
     */
    @Override
    public int getItemCount() { // getItemCount() 메소드
        return temp_file_path_arr.size();
    } // getItemCount() 메소드

    /**
     * ViewHolder 클래스
     */
    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView review_img_iv;
        Button save_btn;

        /**
         * ViewHolder 생성자
         * @param item_view 아이템 뷰
         */
        ViewHolder(View item_view){
            super(item_view);
            this.review_img_iv = item_view.findViewById(R.id.review_img_iv);
            this.save_btn = item_view.findViewById(R.id.save_btn);

            /**
             * 저장 버튼 클릭시
             */
            if(getAdapterPosition()!=RecyclerView.NO_POSITION){
                save_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(TAG,"저장 버튼 클릭");
                        Log.d(TAG,getAdapterPosition()+"번째 사진");
                    }
                });
            }

        } // ViewHolder 생성자

    } // ViewHolder 클래스

} // BoardAdapter 클래스
