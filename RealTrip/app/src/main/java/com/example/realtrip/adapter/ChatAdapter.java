package com.example.realtrip.adapter;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.realtrip.R;
import com.example.realtrip.object.Chat;
import com.example.realtrip.object.Member;
import com.google.gson.Gson;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/**
 * ChatAdapter 클래스
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    ArrayList<Chat> chats;
    Activity activity;
    Context context;

    Member login_member;

    public ChatAdapter(ArrayList<Chat> chats, Activity activity){
        this.chats = chats;
        this.activity = activity;
        this.context = activity.getApplicationContext();
        /**
         * 쉐어드에 저장된 로그인 멤버 정보 가지고 오기
         */
        SharedPreferences sharedPreferences = context.getSharedPreferences("myAppData",MODE_PRIVATE);
        Gson gson = new Gson();
        this.login_member = gson.fromJson(sharedPreferences.getString("login_member","no_login"),Member.class);
    }

    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater  = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.recyclerview_chat_item,parent,false); // 뷰 객체 생성
        ChatAdapter.ViewHolder viewHolder = new ChatAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ViewHolder holder, int position) {
        if(holder.getAdapterPosition()!=RecyclerView.NO_POSITION){
            Chat chat = chats.get(holder.getAdapterPosition());

            /**
             * 데이터와 view를 연결
             */
            if(login_member.member_no == chat.chat_member_no){ // 로그인한 멤버의 채팅인 경우

                holder.chat_member_chat_tv.setVisibility(View.GONE);
                holder.chat_member_chat_time_tv.setVisibility(View.GONE);
                holder.chat_member_info_ll.setVisibility(View.GONE);
                holder.login_member_chat_tv.setVisibility(View.VISIBLE);
                holder.login_member_chat_time_tv.setVisibility(View.VISIBLE);

                holder.login_member_chat_tv.setText(chat.chat_content);
                holder.login_member_chat_time_tv.setText(chat.chat_time);

            }else{ // 로그인하지 않은 멤버의 채팅인 경우
                holder.chat_member_chat_tv.setVisibility(View.VISIBLE);
                holder.chat_member_chat_time_tv.setVisibility(View.VISIBLE);
                holder.chat_member_info_ll.setVisibility(View.VISIBLE);
                holder.login_member_chat_tv.setVisibility(View.GONE);
                holder.login_member_chat_time_tv.setVisibility(View.GONE);

                if("default".equals(chat.member_profile_img)){ // 기본 프로필인 경우
                    holder.chat_member_profile_img_iv.setImageResource(R.drawable.default_profile);
                }else{ // 프로필 사진이 있는 경우
                    Glide.with(context)
                            .load("http://35.224.156.8/uploads/"+chat.member_profile_img)
                            .thumbnail(0.1f)
                            .placeholder(R.drawable.loading)
                            .into(holder.chat_member_profile_img_iv);
                }
                holder.chat_member_nickname_tv.setText(chat.member_nickname);
                holder.chat_member_chat_tv.setText(chat.chat_content);
                holder.chat_member_chat_time_tv.setText(chat.chat_time);
            }
        }
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    /**
     * ViewHolder 클래스
     */
    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView login_member_chat_tv;
        TextView chat_member_chat_tv;
        TextView login_member_chat_time_tv;
        TextView chat_member_chat_time_tv;
        TextView chat_member_nickname_tv;
        ImageView chat_member_profile_img_iv;
        LinearLayout chat_member_info_ll;

        /**
         * ViewHolder 생성자
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.login_member_chat_tv = itemView.findViewById(R.id.login_member_chat_tv);
            this.chat_member_chat_tv = itemView.findViewById(R.id.chat_member_chat_tv);
            this.login_member_chat_time_tv = itemView.findViewById(R.id.login_member_chat_time_tv);
            this.chat_member_chat_time_tv = itemView.findViewById(R.id.chat_member_chat_time_tv);
            this.chat_member_nickname_tv = itemView.findViewById(R.id.chat_member_nickname_tv);
            this.chat_member_profile_img_iv = itemView.findViewById(R.id.chat_member_profile_img_iv);
            this.chat_member_info_ll = itemView.findViewById(R.id.chat_member_info_ll);

        }
    }

    /**
     * chat item을 추가하는 메소드
     */
    public void addItem(Chat chat){
        Log.d(TAG,"addItem() 호출 , 채팅 내용: "+chat.chat_content+", 채팅 작성 멤버 닉네임: "+chat.member_nickname);
        chats.add(chat);
        notifyItemInserted(chats.size()-1);
    }

} // ChatAdapter 클래스