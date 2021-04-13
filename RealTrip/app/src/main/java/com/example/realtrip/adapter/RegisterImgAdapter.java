package com.example.realtrip.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.MotionEventCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.realtrip.AppHelper;
import com.example.realtrip.MYURL;
import com.example.realtrip.R;
import com.example.realtrip.RegisterImgMoveCallback;
import com.example.realtrip.activity.BoardDetailActivity;
import com.example.realtrip.object.Board;
import com.example.realtrip.object.Member;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * BoardAdapter 클래스
 * - 게시글 리싸이클러뷰를 위한 어댑터
 */
public class RegisterImgAdapter extends RecyclerView.Adapter<RegisterImgAdapter.ViewHolder>
                                implements RegisterImgMoveCallback.OnItemMoveListener {


    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    ArrayList<String> temp_file_path_arr; // 게시글 리스트
    Activity activity;
    Context context;
    OnStartDragListener onStartDragListener;

    String write_or_update;

    /**
     * BoardAdapter 생성자
     */
    public RegisterImgAdapter(ArrayList<String> temp_file_path_arr, Activity activity, OnStartDragListener onStartDragListener, String write_or_update){
        this.temp_file_path_arr = temp_file_path_arr;
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.onStartDragListener = onStartDragListener;
        this.write_or_update = write_or_update;
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
        View view = inflater.inflate(R.layout.recyclerview_register_img_item,parent,false); // 뷰 객체 생성
        RegisterImgAdapter.ViewHolder viewHolder = new RegisterImgAdapter.ViewHolder(view);
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
            if("test".equals(temp_file_path.substring(0,4))){
                Glide.with(context)
                        .load("http://35.224.156.8/uploads/"+temp_file_path)
                        .thumbnail(0.1f)
                        .placeholder(R.drawable.loading)
                        .into(holder.register_img_iv);
            }else{
                holder.register_img_iv.setImageURI(Uri.parse(temp_file_path));
            }
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

        ImageView register_img_iv;
        Button register_img_order_edit;
        Button register_img_remove;


        /**
         * ViewHolder 생성자
         * @param item_view 아이템 뷰
         */
        ViewHolder(final View item_view){
            super(item_view);
            this.register_img_iv = item_view.findViewById(R.id.register_img_iv);
            this.register_img_order_edit = item_view.findViewById(R.id.register_img_order_edit);
            this.register_img_remove = item_view.findViewById(R.id.register_img_remove);

            /**
             * 순서바꾸기 버튼 클릭 이벤트
             */
            register_img_order_edit.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if(MotionEventCompat.getActionMasked(motionEvent)==MotionEvent.ACTION_DOWN){
                        onStartDragListener.onStartDrag(ViewHolder.this);
                        Toast.makeText(context,"사진을 좌우로 움직여보세요",Toast.LENGTH_SHORT).show();
                    }
                    return false;
                }
            });

            /**
             * 이미지 삭제
             */
            register_img_remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(getAdapterPosition()!=RecyclerView.NO_POSITION){
                        Log.d(TAG,getAdapterPosition()+"번째 사진 삭제버튼 클릭");
                        temp_file_path_arr.remove(getAdapterPosition());
                        notifyItemRemoved(getAdapterPosition());
                        Toast.makeText(context,"사진이 삭제되었습니다",Toast.LENGTH_SHORT).show();

                    }
                }
            });

        } // ViewHolder 생성자

    } // ViewHolder 클래스

    /**
     * 아이템 순서를 바꾸는 메소드
     */
    @Override
    public void onItemMove(int from_position, int to_position) {
        Log.d(TAG,"onItemMove() 호출");
        Collections.swap(temp_file_path_arr,from_position,to_position);
        notifyItemMoved(from_position,to_position);
    }

    /**
     * 드래그를 시작하기 위한 인터페이스
     */
    public interface OnStartDragListener{
        void onStartDrag(RegisterImgAdapter.ViewHolder holder);
    }

} // BoardAdapter 클래스
