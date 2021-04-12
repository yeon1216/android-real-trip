package com.example.realtrip.object;

/**
 * Review 클래스
 */
public class Review {
    public int review_no;
    public String content_id;
    public int review_write_member_no;
    public String review_write_time;
    public String review_content;
    public String review_img;

    public Review() {
    }

    public Review(int review_no, String content_id, int review_write_member_no, String review_write_time, String review_content, String review_img) {
        this.review_no = review_no;
        this.content_id = content_id;
        this.review_write_member_no = review_write_member_no;
        this.review_write_time = review_write_time;
        this.review_content = review_content;
        this.review_img = review_img;
    }

    public void setReview_write_time(String review_write_time) {
        this.review_write_time = review_write_time;
    }

    public String getReview_write_time() {
        return review_write_time;
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

    public void setReview_content(String review_content) {
        this.review_content = review_content;
    }

    public void setReview_img(String review_img) {
        this.review_img = review_img;
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

    public String getReview_content() {
        return review_content;
    }

    public String getReview_img() {
        return review_img;
    }
} // Review 클래스
