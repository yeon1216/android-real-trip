package com.example.realtrip.object;

/**
 * BoardLike 클래스
 * - 게시글 좋아하는 사람 저장
 */
public class BoardLike {
    public int board_like_no; // 게시글 좋아요 번호
    public int board_no; // 게시글 번호
    public int board_like_member_no; // 게시글 좋아하는 멤버 번호

    public BoardLike() { } // 기본 생성자

    public BoardLike(int board_like_no, int board_no, int board_like_member_no) { // 생성자
        this.board_like_no = board_like_no;
        this.board_no = board_no;
        this.board_like_member_no = board_like_member_no;
    }
} // BoardLike 클래스
