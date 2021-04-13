package com.example.realtrip.object;

public class Chat {

    public int chat_no;
    public String chat_room_name;
    public int chat_member_no;
    public String chat_content;
    public String chat_time;

    public String member_nickname;
    public String member_profile_img;

    public Chat() {
    }

    public Chat(String chat_room_name, int chat_member_no, String chat_content, String chat_time, String member_nickname, String member_profile_img) {
        this.chat_room_name = chat_room_name;
        this.chat_member_no = chat_member_no;
        this.chat_content = chat_content;
        this.chat_time = chat_time;
        this.member_nickname = member_nickname;
        this.member_profile_img = member_profile_img;
    }

    public Chat(int chat_no, String chat_room_name, int chat_member_no, String chat_content, String chat_time, String member_nickname, String member_profile_img) {
        this.chat_no = chat_no;
        this.chat_room_name = chat_room_name;
        this.chat_member_no = chat_member_no;
        this.chat_content = chat_content;
        this.chat_time = chat_time;
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

    public void setChat_no(int chat_no) {
        this.chat_no = chat_no;
    }

    public void setChat_room_name(String chat_room_name) {
        this.chat_room_name = chat_room_name;
    }

    public void setChat_member_no(int chat_member_no) {
        this.chat_member_no = chat_member_no;
    }

    public void setChat_content(String chat_content) {
        this.chat_content = chat_content;
    }

    public void setChat_time(String chat_time) {
        this.chat_time = chat_time;
    }

    public int getChat_no() {
        return chat_no;
    }

    public String getChat_room_name() {
        return chat_room_name;
    }

    public int getChat_member_no() {
        return chat_member_no;
    }

    public String getChat_content() {
        return chat_content;
    }

    public String getChat_time() {
        return chat_time;
    }
}
