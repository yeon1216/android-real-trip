package com.example.realtrip.object;

/**
 * 멤버 객체를 위한 클래스
 */
public class Member {

    public int member_no; // 멤버 번호
    public String member_email; // 멤버 이메일
    public String member_pw; // 멤버 비밀번호
    public String member_nickname; // 멤버 닉네임
    public String member_profile_img; // 멤버 프로필 사진

    public Member(){ } // 기본 생성자

    public Member(int member_no, String member_email, String member_nickname, String member_profile_img) { // 비밀번호를 제외한 생성자
        this.member_no = member_no;
        this.member_email = member_email;
        this.member_nickname = member_nickname;
        this.member_profile_img = member_profile_img;
    }

    public Member(int member_no, String member_email, String member_pw, String member_nickname, String member_profile_img) { // 모든 인자를 다 가지는 생성자
        this.member_no = member_no;
        this.member_email = member_email;
        this.member_pw = member_pw;
        this.member_nickname = member_nickname;
        this.member_profile_img = member_profile_img;
    }

} // Member 클래스
