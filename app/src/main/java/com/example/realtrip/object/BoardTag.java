package com.example.realtrip.object;

/**
 * BoardTag 클래스
 * - 게시글 태그를 저장하는 클래스
 */
public class BoardTag {
    public int board_tag_no; // 게시글 태그 번호
    public int board_no; // 게시글 번호
    public String board_tag; // 게시글 태그

    public BoardTag() { } // 기본 생성자

    public BoardTag(int board_tag_no, int board_no, String board_tag) { // 생성자
        this.board_tag_no = board_tag_no;
        this.board_no = board_no;
        this.board_tag = board_tag;
    }
} // BoardTag 클래스
