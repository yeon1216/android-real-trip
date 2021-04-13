package com.example.realtrip.object;

public class ChatRoom {

    public int chat_room_no;
    public String chat_room_name;
    public String last_chat_content;
    public String last_chat_time;
    public String member_nickname;
    public String member_profile_img;

    public ChatRoom() {
    }

    public ChatRoom(String chat_room_name, String last_chat_content, String last_chat_time, String member_nickname, String member_profile_img) {
        this.chat_room_name = chat_room_name;
        this.last_chat_content = last_chat_content;
        this.last_chat_time = last_chat_time;
        this.member_nickname = member_nickname;
        this.member_profile_img = member_profile_img;
    }

    public ChatRoom(int chat_room_no, String chat_room_name, String last_chat_content, String last_chat_time, String member_nickname, String member_profile_img) {
        this.chat_room_no = chat_room_no;
        this.chat_room_name = chat_room_name;
        this.last_chat_content = last_chat_content;
        this.last_chat_time = last_chat_time;
        this.member_nickname = member_nickname;
        this.member_profile_img = member_profile_img;
    }

    public void setMember_nickname(String member_nickname) {
        this.member_nickname = member_nickname;
    }

    public void setMember_profile_img(String member_profile_img) {
        this.member_profile_img = member_profile_img;
    }

    public String getMember_nickname() {
        return member_nickname;
    }

    public String getMember_profile_img() {
        return member_profile_img;
    }

    public void setChat_room_no(int chat_room_no) {
        this.chat_room_no = chat_room_no;
    }

    public void setChat_room_name(String chat_room_name) {
        this.chat_room_name = chat_room_name;
    }

    public void setLast_chat_content(String last_chat_content) {
        this.last_chat_content = last_chat_content;
    }

    public void setLast_chat_time(String last_chat_time) {
        this.last_chat_time = last_chat_time;
    }

    public int getChat_room_no() {
        return chat_room_no;
    }

    public String getChat_room_name() {
        return chat_room_name;
    }

    public String getLast_chat_content() {
        return last_chat_content;
    }

    public String getLast_chat_time() {
        return last_chat_time;
    }
}
