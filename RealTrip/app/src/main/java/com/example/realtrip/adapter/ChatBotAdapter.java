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
import com.example.realtrip.object.ChatBot;
import com.example.realtrip.object.Member;
import com.google.gson.Gson;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/**
 * ChatAdapter 클래스
 */
public class ChatBotAdapter extends RecyclerView.Adapter<ChatBotAdapter.ViewHolder> {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    public ArrayList<ChatBot> chatBots;
    Activity activity;
    Context context;

    Member login_member;

    /**
     * ChatBotAdapter 생성자
     */
    public ChatBotAdapter(ArrayList<ChatBot> chatBots, Activity activity){
        this.chatBots = chatBots;
        this.activity = activity;
        this.context = activity.getApplicationContext();

        /*
            쉐어드에 저장된 로그인 멤버 정보 가지고 오기
         */
        SharedPreferences sharedPreferences = context.getSharedPreferences("myAppData",MODE_PRIVATE);
        Gson gson = new Gson();
        this.login_member = gson.fromJson(sharedPreferences.getString("login_member","no_login"),Member.class);
    } // ChatBotAdapter 생성자

    @NonNull
    @Override
    public ChatBotAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater  = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.recyclerview_chatbot_item,parent,false); // 뷰 객체 생성
        ChatBotAdapter.ViewHolder viewHolder = new ChatBotAdapter.ViewHolder(view);
        return viewHolder;
    }

    /**
     * onBindViewHolder() 메소드
     */
    @Override
    public void onBindViewHolder(@NonNull ChatBotAdapter.ViewHolder holder, int position) {
        if(holder.getAdapterPosition()!=RecyclerView.NO_POSITION){
            ChatBot chatBot = chatBots.get(holder.getAdapterPosition());

            /*
                데이터와 뷰 연결
             */
            if(chatBot.is_chatbot){ // 챗봇이 작성한 글
                holder.chatbot_chat_tv.setVisibility(View.VISIBLE);
                holder.chatbot_chat_time_tv.setVisibility(View.VISIBLE);
                holder.chatbot_info_ll.setVisibility(View.VISIBLE);

                holder.login_member_chat_tv.setVisibility(View.GONE);
                holder.login_member_chat_time_tv.setVisibility(View.GONE);

                holder.chatbot_chat_tv.setText(chatBot.chatbot_content);
                holder.chatbot_chat_time_tv.setText(chatBot.chatbot_time);
            }else{ // 유저가 작성한 글
                holder.chatbot_chat_tv.setVisibility(View.GONE);
                holder.chatbot_chat_time_tv.setVisibility(View.GONE);
                holder.chatbot_info_ll.setVisibility(View.GONE);

                holder.login_member_chat_tv.setVisibility(View.VISIBLE);
                holder.login_member_chat_time_tv.setVisibility(View.VISIBLE);

                holder.login_member_chat_tv.setText(chatBot.chatbot_content);
                holder.login_member_chat_time_tv.setText(chatBot.chatbot_time);
            }

        }
    } // onBindViewHolder() 메소드

    /**
     * getItemCount() 메소드
     */
    @Override
    public int getItemCount() {
        return chatBots.size();
    } // getItemCount() 메소드

    /**
     * ViewHolder 클래스
     */
    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView login_member_chat_tv;
        TextView login_member_chat_time_tv;

        TextView chatbot_chat_tv;
        TextView chatbot_chat_time_tv;
        TextView chatbot_name_tv;
        ImageView chatbot_img_iv;
        LinearLayout chatbot_info_ll;

        /**
         * ViewHolder 생성자
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.login_member_chat_tv = itemView.findViewById(R.id.login_member_chat_tv);
            this.chatbot_chat_tv = itemView.findViewById(R.id.chatbot_chat_tv);
            this.login_member_chat_time_tv = itemView.findViewById(R.id.login_member_chat_time_tv);
            this.chatbot_chat_time_tv = itemView.findViewById(R.id.chatbot_chat_time_tv);
            this.chatbot_name_tv = itemView.findViewById(R.id.chatbot_name_tv);
            this.chatbot_img_iv = itemView.findViewById(R.id.chatbot_img_iv);
            this.chatbot_info_ll = itemView.findViewById(R.id.chatbot_info_ll);

        } // ViewHolder 생성자

    } // ViewHolder 클래스

    /**
     * chatbot item 을 추가하는 메소드
     */
    public void addItem(ChatBot chatBot){
        Log.d(TAG,"addItem() 호출 , 채팅 내용: "+chatBot.chatbot_content+", 채팅 작성 멤버 닉네임: "+chatBot.chatbot_time);
        chatBots.add(chatBot);
        notifyItemInserted(getItemCount()-1);
    } // addItem() 메소드

} // ChatAdapter 클래스