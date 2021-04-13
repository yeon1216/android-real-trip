package com.example.realtrip.object;

/**
 * BoardImg 클래스
 * - 게시글 사진을 저장해두는 클래스
 */
public class BoardImg {
    public int board_img_no; // 게시글 사진 번호
    public int board_no; // 게시글 번호
    public String board_img; // 게시글 사진

    public BoardImg() { } // 기본 생성자

    public BoardImg(int board_img_no, int board_no, String board_img) { // 생성자
        this.board_img_no = board_img_no;
        this.board_no = board_no;
        this.board_img = board_img;
    }

} // BoardImg 클래스
