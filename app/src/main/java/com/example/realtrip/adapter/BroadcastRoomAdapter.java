package com.example.realtrip.adapter;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.realtrip.AppHelper;
import com.example.realtrip.MYURL;
import com.example.realtrip.R;
import com.example.realtrip.activity.ChatActivity;
import com.example.realtrip.activity.PlayerActivity;
import com.example.realtrip.object.BroadcastRoom;
import com.example.realtrip.object.ChatRoom;
import com.example.realtrip.object.Member;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * ChatAdapter 클래스
 */
public class BroadcastRoomAdapter extends RecyclerView.Adapter<BroadcastRoomAdapter.ViewHolder> {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    ArrayList<BroadcastRoom> broadcastRooms;
    Activity activity;
    Context context;

    public BroadcastRoomAdapter(ArrayList<BroadcastRoom> broadcastRooms, Activity activity){
        this.broadcastRooms = broadcastRooms;
        this.activity = activity;
        this.context = activity.getApplicationContext();
    }

    @NonNull
    @Override
    public BroadcastRoomAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater  = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.recyclerview_broadcast_room_item,parent,false); // 뷰 객체 생성
        BroadcastRoomAdapter.ViewHolder viewHolder = new BroadcastRoomAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull BroadcastRoomAdapter.ViewHolder holder, int position) {
        if(holder.getAdapterPosition()!=RecyclerView.NO_POSITION){
            BroadcastRoom broadcastRoom = broadcastRooms.get(holder.getAdapterPosition());
            final int temp_broadcast_member_no = broadcastRoom.broadcast_member_no;
            holder.item_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   Log.d(TAG,"방송방 클릭!!  temp_broadcast_member_no: "+temp_broadcast_member_no);
                    Intent intent = new Intent(context,PlayerActivity.class);
                    intent.putExtra("temp_broadcast_member_no",temp_broadcast_member_no);
                    activity.startActivity(intent);


                }
            });

            /*
             * 데이터와 view를 연결
             */
            if("default".equals(broadcastRoom.broadcast_member_profile_img)){ // 기본 프로필인 경우
                holder.broadcast_member_profile_img_iv.setImageResource(R.drawable.default_profile);
            }else{ // 프로필 사진이 있는 경우
                Glide.with(context)
                        .load("http://35.224.156.8/uploads/"+broadcastRoom.broadcast_member_profile_img)
                        .thumbnail(0.1f)
                        .placeholder(R.drawable.loading)
                        .into(holder.broadcast_member_profile_img_iv);
            }
            holder.broadcast_member_nickname_tv.setText(broadcastRoom.broadcast_member_nickname);
            holder.broadcast_title_tv.setText(broadcastRoom.broadcast_room_title);
        }
    }

    @Override
    public int getItemCount() {
        return broadcastRooms.size();
    }

    /**
     * ViewHolder 클래스
     */
    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView broadcast_member_profile_img_iv;
        TextView broadcast_member_nickname_tv;
        TextView broadcast_title_tv;
        View item_view;

        /**
         * ViewHolder 생성자
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.item_view = itemView;
            this.broadcast_member_nickname_tv = itemView.findViewById(R.id.broadcast_member_nickname_tv);
            this.broadcast_member_profile_img_iv = itemView.findViewById(R.id.broadcast_member_profile_img_iv);
            this.broadcast_title_tv = itemView.findViewById(R.id.broadcast_title_tv);

        }
    }

    /**
     * broadcast item을 추가하는 메소드
     */
    public void addItem(BroadcastRoom broadcastRoom){
        broadcastRooms.add(0,broadcastRoom);
        notifyItemInserted(0);
//        notifyItemInserted(chatRooms.size()-1);
    } // addItem() 메소드

} // ChatAdapter 클래스