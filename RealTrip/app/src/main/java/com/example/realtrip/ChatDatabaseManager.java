package com.example.realtrip;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.realtrip.object.Chat;
import com.example.realtrip.object.ChatRoom;
import com.example.realtrip.object.Member;

import java.util.ArrayList;

public class ChatDatabaseManager {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    static final String DB_CHAT = "Chat.db";
    static final String TABLE_CHAT_ROOM = "chat_room"; //Table 이름
    static final String TABLE_CHAT = "chat"; //Table 이름
    static final int DB_VERSION = 1;			//DB 버전

    Context myContext = null;

    private static ChatDatabaseManager myDBManager = null;
    private SQLiteDatabase mydatabase = null;

    /**
     * MovieDatabaseManager 싱글톤 패턴으로 구현
     */
    public static ChatDatabaseManager getInstance(Context context)
    {
        if(myDBManager == null)
        {
            myDBManager = new ChatDatabaseManager(context);
        }

        return myDBManager;
    }

    /**
     * 생성자
     */
    private ChatDatabaseManager(Context context)
    {

        Log.d(TAG,"ChatDatabaseManager 생성자");
        myContext = context;

        /**
         * 디비 오픈
         */
        mydatabase = context.openOrCreateDatabase(DB_CHAT, context.MODE_PRIVATE,null);

        String sql=null;
        Cursor cursor=null;

        /**
         * chat 테이블이 존재하는지 확인
         */
        sql = "SELECT COUNT(*) FROM sqlite_master WHERE name='chat';";

        cursor = mydatabase.rawQuery(sql , null);
        cursor.moveToFirst();

        if(cursor.getCount()==0){ // 테이블이 없음


            /**
             * chat 테이블 생성
             */
            sql = "CREATE TABLE chat(\n" +
                    "\tchat_no INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "\tchat_room_name VARCHAR(225) NOT NULL DEFAULT '',\n" +
                    "\tchat_member_no INTEGER,\n" +
                    "\tchat_content TEXT,\n" +
                    "\tchat_time  VARCHAR(225) NOT NULL DEFAULT ''\n" +
                    ");";
            mydatabase.execSQL(sql);
        }


        /**
         * chat_room 테이블이 존재하는지 확인
         */
        sql = "SELECT COUNT(*) FROM sqlite_master WHERE name='chat_room';";

        cursor = mydatabase.rawQuery(sql , null);
        cursor.moveToFirst();

        if(cursor.getCount()==0){ // 테이블이 없음
            /**
             * chat_room 테이블 생성
             */
            sql = "CREATE TABLE chat_room(\n" +
                    "\tchat_room_no INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "\tchat_member_no_arr VARCHAR(225) NOT NULL DEFAULT '',\n" +
                    "\tlast_chat_content TEXT,\n" +
                    "\tlast_chat_time VARCHAR(225) NOT NULL DEFAULT ''\n" +
                    ");";
            mydatabase.execSQL(sql);
        }





    } // 생성자

    /**
     * chat_room INSERT 메소드
     */
    public void chatRoomInsert(String chat_member_no_arr, String last_chat_content, String last_chat_time){

        String check_sql = "SELECT count(*) FROM chat_room WHERE chat_member_no_arr='"+chat_member_no_arr+"';";
        Cursor cursor = mydatabase.rawQuery(check_sql , null);
        cursor.moveToFirst();

        if(cursor.getCount()==0){ // 채팅방이 없음
            String sql = "INSERT INTO chat_room (chat_member_no_arr, last_chat_content, last_chat_time) VALUES(?,?,?);";
            Object[] params = {chat_member_no_arr,last_chat_content,last_chat_time};
            mydatabase.execSQL(sql,params);
            Log.d(TAG, "Insert chat_room Data " + chat_member_no_arr);
        }

    } // chatRoomInsert() 메소드

    /**
     * chat INSERT 메소드
     */
    public void chatInsert(String chat_room_name, int chat_member_no, String chat_content, String chat_time){
        String sql = "INSERT INTO chat (chat_room_name, chat_member_no, chat_content, chat_time) VALUES(?,?,?,?);";

        Object[] params = {chat_room_name,chat_member_no,chat_content,chat_time};
        mydatabase.execSQL(sql,params);
        Log.d(TAG, "Insert chat Data " + chat_content);

        /**
         * chat_room 마지막 채팅, 마지막 채팅 시간 수정
         */
//        ContentValues contentValues = new ContentValues();
//        contentValues.put("last_chat_content",chat_content);
//        contentValues.put("last_chat_time",chat_time);
//        String chat_room_name_arr[] = {chat_room_name};
//        int n = mydatabase.update("chat_room",contentValues,"chat_member_no_arr",chat_room_name_arr);
//        Log.d(TAG, "Update chat_room Data " + chat_content+", 업데이트 수: " +n);

    } // chatInsert() 메소드

    /**
     * chat_room SELECT 메소드
     */
    public ChatRoom chatRoomSelect(){

        return null;
    }

    /**
     * chat SELECT 메소드
     */
    public ArrayList<Chat> chatgetAllThisChatRoom(final Member login_member, final Member chat_member, String this_chat_room_name){
//        String sql = "INSERT INTO chat (chat_room_name, chat_member_no, chat_content, chat_time) VALUES(?,?,?,?);";
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT chat_no, chat_room_name, chat_member_no, chat_content, chat_time FROM chat WHERE chat_room_name = '"+this_chat_room_name+"' ORDER BY chat_no");
        ArrayList<Chat> chats = new ArrayList<>();

        Cursor cursor = mydatabase.rawQuery(sb.toString(),null);

        while(cursor.moveToNext()){
            int chat_no = cursor.getInt(cursor.getColumnIndex("chat_no"));
            String chat_room_name = cursor.getString(cursor.getColumnIndex("chat_room_name"));
            int chat_member_no = cursor.getInt(cursor.getColumnIndex("chat_member_no"));
            String chat_content = cursor.getString(cursor.getColumnIndex("chat_content"));
            String chat_time = cursor.getString(cursor.getColumnIndex("chat_time"));
            Log.d(TAG, "chat_no: " + chat_no + ", chat_room_name: " + chat_room_name+ ", chat_member_no: " + chat_member_no + ", chat_content: " + chat_content+ ", chat_time: " + chat_time);
            if(login_member.member_no==chat_member_no){
                chats.add(new Chat(chat_no,chat_room_name,chat_member_no,chat_content,chat_time,login_member.member_nickname,login_member.member_profile_img));
            }else{
                chats.add(new Chat(chat_no,chat_room_name,chat_member_no,chat_content,chat_time,chat_member.member_nickname,chat_member.member_profile_img));
            }
        }

        if (cursor != null) {
            cursor.close();
        }

        return chats;
    } // chatgetAllThisChatRoom() 메소드

    /**
     * 서버와 클라이언트 채팅 데이터 동기화
     */
    public void getChatFromServer(){

    } // getChatFromServer

} // 클래스
