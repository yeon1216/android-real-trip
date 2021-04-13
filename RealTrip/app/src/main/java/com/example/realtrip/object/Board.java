package com.example.realtrip.object;

/**
 * Board 클래스
 * - 게시글 객체 클래스
 */
public class Board {

    public int board_no; // 게시글 번호
    public int board_write_member_no; // 게시글 작성자 멤버 번호
    public String board_content; // 게시글 내용
    public String board_write_time; // 게시글 작성 시간
    public String board_location; // 게시글 장소

    /**
     * 기본 생성자
     */
    public Board() { }

    /**
     * 장소 제외한 생성자
     */
    public Board(int board_no, int board_write_member_no, String board_content, String board_write_time) {
        this.board_no = board_no;
        this.board_write_member_no = board_write_member_no;
        this.board_content = board_content;
        this.board_write_time = board_write_time;
    }

    /**
     * 생성자
     */
    public Board(int board_no, int board_write_member_no, String board_content, String board_write_time, String board_location) {
        this.board_no = board_no;
        this.board_write_member_no = board_write_member_no;
        this.board_content = board_content;
        this.board_write_time = board_write_time;
        this.board_location = board_location;
    }

} // Board 클래스
