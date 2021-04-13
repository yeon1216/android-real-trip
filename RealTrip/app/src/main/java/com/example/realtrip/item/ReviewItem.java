package com.example.realtrip.item;

public class ReviewItem {

    public int review_no;
    public String content_id;
    public int review_write_member_no;
    public String review_write_time;
    public String review_content;
    public String review_img;
    public String review_score;
    public String review_magnitude;
    public String member_nickname;
    public String member_profile_img;

    public ReviewItem() {
    }

    public ReviewItem(int review_no, String content_id, int review_write_member_no, String review_write_time, String review_content, String review_img, String review_score, String review_magnitude, String member_nickname, String member_profile_img) {
        this.review_no = review_no;
        this.content_id = content_id;
        this.review_write_member_no = review_write_member_no;
        this.review_write_time = review_write_time;
        this.review_content = review_content;
        this.review_img = review_img;
        this.review_score = review_score;
        this.review_magnitude = review_magnitude;
        this.member_nickname = member_nickname;
        this.member_profile_img = member_profile_img;
    }

    public void setReview_score(String review_score) {
        this.review_score = review_score;
    }

    public void setReview_magnitude(String review_magnitude) {
        this.review_magnitude = review_magnitude;
    }

    public String getReview_score() {
        return review_score;
    }

    public String getReview_magnitude() {
        return review_magnitude;
    }

    public void setReview_no(int review_no) {
        this.review_no = review_no;
    }

    public void setContent_id(String content_id) {
        this.content_id = content_id;
    }

    public void setReview_write_member_no(int review_write_member_no) {
        this.review_write_member_no = review_write_member_no;
    }

    public void setReview_write_time(String review_write_time) {
        this.review_write_time = review_write_time;
    }

    public void setReview_content(String review_content) {
        this.review_content = review_content;
    }

    public void setReview_img(String review_img) {
        this.review_img = review_img;
    }

    public void setMember_nickname(String member_nickname) {
        this.member_nickname = member_nickname;
    }

    public void setMember_profile_img(String member_profile_img) {
        this.member_profile_img = member_profile_img;
    }

    public int getReview_no() {
        return review_no;
    }

    public String getContent_id() {
        return content_id;
    }

    public int getReview_write_member_no() {
        return review_write_member_no;
    }

    public String getReview_write_time() {
        return review_write_time;
    }

    public String getReview_content() {
        return review_content;
    }

    public String getReview_img() {
        return review_img;
    }

    public String getMember_nickname() {
        return member_nickname;
    }

    public String getMember_profile_img() {
        return member_profile_img;
    }
} // ReviewItem 클래스
