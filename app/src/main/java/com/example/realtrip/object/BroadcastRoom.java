package com.example.realtrip.object;

public class BroadcastRoom {

    public int broadcast_room_no;
    public String broadcast_room_title;
    public int broadcast_member_no;
    public String broadcast_member_nickname;
    public String broadcast_member_profile_img;
    public String is_live;

    public BroadcastRoom() {
    }

    public BroadcastRoom(String broadcast_room_title, int broadcast_member_no, String broadcast_member_nickname, String broadcast_member_profile_img, String is_live) {
        this.broadcast_room_title = broadcast_room_title;
        this.broadcast_member_no = broadcast_member_no;
        this.broadcast_member_nickname = broadcast_member_nickname;
        this.broadcast_member_profile_img = broadcast_member_profile_img;
        this.is_live = is_live;
    }

    public BroadcastRoom(int broadcast_room_no, String broadcast_room_title, int broadcast_member_no, String broadcast_member_nickname, String broadcast_member_profile_img, String is_live) {
        this.broadcast_room_no = broadcast_room_no;
        this.broadcast_room_title = broadcast_room_title;
        this.broadcast_member_no = broadcast_member_no;
        this.broadcast_member_nickname = broadcast_member_nickname;
        this.broadcast_member_profile_img = broadcast_member_profile_img;
        this.is_live = is_live;
    }

    public void setIs_live(String is_live) {
        this.is_live = is_live;
    }

    public String getIs_live() {
        return is_live;
    }

    public void setBroadcast_room_no(int broadcast_room_no) {
        this.broadcast_room_no = broadcast_room_no;
    }

    public void setBroadcast_room_title(String broadcast_room_title) {
        this.broadcast_room_title = broadcast_room_title;
    }

    public void setBroadcast_member_no(int broadcast_member_no) {
        this.broadcast_member_no = broadcast_member_no;
    }

    public void setBroadcast_member_nickname(String broadcast_member_nickname) {
        this.broadcast_member_nickname = broadcast_member_nickname;
    }

    public void setBroadcast_member_profile_img(String broadcast_member_profile_img) {
        this.broadcast_member_profile_img = broadcast_member_profile_img;
    }

    public int getBroadcast_room_no() {
        return broadcast_room_no;
    }

    public String getBroadcast_room_title() {
        return broadcast_room_title;
    }

    public int getBroadcast_member_no() {
        return broadcast_member_no;
    }

    public String getBroadcast_member_nickname() {
        return broadcast_member_nickname;
    }

    public String getBroadcast_member_profile_img() {
        return broadcast_member_profile_img;
    }
}
