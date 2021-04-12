package com.example.realtrip.object;

/**
 * BoardReply 클래스
 * - 게시글 댓글 클래스
 */
public class BoardReply {
    public int board_reply_no; // 게시글 댓글 번호
    public int board_no; // 게시글 번호
    public int board_reply_write_member_no; // 게시글 작성자 멤버 번호
    public String board_reply_content; // 게시글 댓글 내용
    public String board_reply_time; // 게시글 댓글 작성 시간

    public BoardReply() { } // 기본 생성자

    public BoardReply(int board_reply_no, int board_no, int board_reply_write_member_no, String board_reply_content, String board_reply_time) { // 생성자
        this.board_reply_no = board_reply_no;
        this.board_no = board_no;
        this.board_reply_write_member_no = board_reply_write_member_no;
        this.board_reply_content = board_reply_content;
        this.board_reply_time = board_reply_time;
    }
} // BoardReply 클래스
