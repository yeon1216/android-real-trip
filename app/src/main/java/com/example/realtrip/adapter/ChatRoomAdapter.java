package com.example.realtrip.adapter;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.example.realtrip.object.Chat;
import com.example.realtrip.object.ChatRoom;
import com.example.realtrip.object.Member;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * ChatAdapter 클래스
 */
public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ViewHolder> {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    ArrayList<ChatRoom> chatRooms;
    Activity activity;
    Context context;

    public ChatRoomAdapter(ArrayList<ChatRoom> chatRooms, Activity activity){
        this.chatRooms = chatRooms;
        this.activity = activity;
        this.context = activity.getApplicationContext();
    }

    @NonNull
    @Override
    public ChatRoomAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater  = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.recyclerview_chat_room_item,parent,false); // 뷰 객체 생성
        ChatRoomAdapter.ViewHolder viewHolder = new ChatRoomAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRoomAdapter.ViewHolder holder, int position) {
        if(holder.getAdapterPosition()!=RecyclerView.NO_POSITION){
            ChatRoom chatRoom = chatRooms.get(holder.getAdapterPosition());
            final String chat_member_nickname= chatRoom.member_nickname;

            Log.d(TAG,"chat_room_no: "+chatRoom.chat_room_no+", chat_room_name: "+chatRoom.chat_room_name+", last_chat_content: "+chatRoom.last_chat_content+", last_chat_time: "+chatRoom.last_chat_time+", member_nickname: "+chatRoom.member_nickname+", member_profile_img"+chatRoom.member_profile_img);

            holder.item_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   Log.d(TAG,"채팅방 클릭!!");
                    getMemberInfo(chat_member_nickname);
                }
            });

            /*
             * 데이터와 view를 연결
             */
            if("default".equals(chatRoom.member_profile_img)){ // 기본 프로필인 경우
                holder.chat_member_profile_img_iv.setImageResource(R.drawable.default_profile);
            }else{ // 프로필 사진이 있는 경우
                Glide.with(context)
                        .load("http://35.224.156.8/uploads/"+chatRoom.member_profile_img)
                        .thumbnail(0.1f)
                        .placeholder(R.drawable.loading)
                        .into(holder.chat_member_profile_img_iv);
            }
            holder.chat_member_nickname_tv.setText(chatRoom.member_nickname);
            holder.last_chat_time_tv.setText(chatRoom.last_chat_time);
            holder.last_chat_tv.setText(chatRoom.last_chat_content);
        }
    }

    @Override
    public int getItemCount() {
        return chatRooms.size();
    }

    /**
     * ViewHolder 클래스
     */
    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView chat_member_profile_img_iv;
        TextView chat_member_nickname_tv;
        TextView last_chat_tv;
        TextView last_chat_time_tv;
        View item_view;

        /**
         * ViewHolder 생성자
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.item_view = itemView;
            this.chat_member_nickname_tv = itemView.findViewById(R.id.chat_member_nickname_tv);
            this.chat_member_profile_img_iv = itemView.findViewById(R.id.chat_member_profile_img_iv);
            this.last_chat_tv = itemView.findViewById(R.id.last_chat_tv);
            this.last_chat_time_tv = itemView.findViewById(R.id.last_chat_time_tv);

        }
    }

    /**
     * chat item을 추가하는 메소드
     */
    public void addItem(ChatRoom chatRoom){
        Log.d(TAG,"addItem() 호출 , 채팅방 이름: "+chatRoom.chat_room_name+", 마지막 채팅: "+chatRoom.last_chat_content+", 채팅 상대 닉네임: "+chatRoom.member_nickname);
        chatRooms.add(0,chatRoom);
        notifyItemInserted(0);
//        notifyItemInserted(chatRooms.size()-1);
    } // addItem() 메소드

    /**
     * 소켓서버에서 메시지가 왔을 때 처리하는 메소드
     */
    public void getMessageFromSocketServer(String message){
        Log.d(TAG,"getMessageFromSocketServer()");
        String[] response = message.split("ㅣ");
        if("message".equals(response[0])){ // 메시지 수신
            final String chat_room_name = response[1];
            final String chat_member_no = response[2];
            final String chat_content = response[3];
            final String chat_time = response[4];

            boolean is_chat_room = false;

            for (int i = 0; i < getItemCount(); i++) {
                ChatRoom chatRoom = chatRooms.get(i);

                if(chat_room_name.equals(chatRoom.chat_room_name)){ // 해당 채팅방이 있음  >>  채팅방 수정
                    Log.d(TAG,"해당 채팅방이 있음  >>  채팅방 수정");
                    chatRoom.setLast_chat_time(chat_time);
                    chatRoom.setLast_chat_content(chat_content);
                    notifyItemChanged(i);
                    /*
                        해당 채팅방을 처음으로 보내보자
                     */
                    if(getItemCount()>1){
                        ChatRoom first_chat_room = chatRooms.get(0);
                        Collections.swap(chatRooms,0,i);
                        notifyItemChanged(i);
                        notifyItemChanged(0);
                    }
                    is_chat_room=true;
                    break;
                }

            }

            if(!is_chat_room){ // 해당 채팅방이 없음  >>  채팅방 생성
                Log.d(TAG,"해당 채팅방이 없음  >>  채팅방 생성");
                // Post 인자
                Map<String,String> params = new HashMap<String,String>();
                params.put("mode","find_member");
                params.put("member_no", chat_member_no);

                JSONObject jsonObject = new JSONObject(params); // Map을 json으로 만듬

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, MYURL.URL,jsonObject,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) { // 응답 성공
                                Log.d(TAG,"응답 성공: "+response.toString());

                                Gson gson = new Gson();
                                Member member = gson.fromJson(response.toString(),Member.class);

                                ChatRoom new_chat_room = new ChatRoom(chat_room_name,chat_content,chat_time,member.member_nickname,member.member_profile_img);
                                addItem(new_chat_room);

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) { // 응답 실패
                        Log.d(TAG,"응답 실패: "+error.toString());
                    }
                }
                );

                /*
                 * requestQueue가 없으면 requestQueue 생성
                 */
                if(AppHelper.requestQueue == null){ // requestQueue 가 없을 경우
                    AppHelper.requestQueue = Volley.newRequestQueue(context); // requestQueue 생성
                }

                AppHelper.requestQueue.add(jsonObjectRequest); // 요청 큐에 위 요청 추가
            }

        }
    } // getMessageFromSocketServer() 메소드

    /**
     * getMemberInfo() 메소드
     */
    public void getMemberInfo(String member_nickname){
        Log.d(TAG,"getMemberInfo() 호출");

        // Post 인자
        Map<String,String> params = new HashMap<String,String>();
        params.put("mode","find_member_by_member_nickname");
        params.put("member_nickname", member_nickname);

        JSONObject jsonObject = new JSONObject(params); // Map을 json으로 만듬

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, MYURL.URL,jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) { // 응답 성공
                        Log.d(TAG,"응답 성공: "+response.toString());

                        Gson gson = new Gson();
                        Member member = gson.fromJson(response.toString(),Member.class);

                        Intent intent = new Intent(context,ChatActivity.class);
                        intent.putExtra("chat_member",response.toString());
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        activity.startActivity(intent);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) { // 응답 실패
                Log.d(TAG,"응답 실패: "+error.toString());
            }
        }
        );

        /**
         * requestQueue가 없으면 requestQueue 생성
         */
        if(AppHelper.requestQueue == null){ // requestQueue 가 없을 경우
            AppHelper.requestQueue = Volley.newRequestQueue(context); // requestQueue 생성
        }

        AppHelper.requestQueue.add(jsonObjectRequest); // 요청 큐에 위 요청 추가

    } // getMemberInfo() 메소드

} // ChatAdapter 클래스