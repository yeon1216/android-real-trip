package com.example.realtrip.object;

public class ChatBot {

    public int chatbot_no;
    public String chatbot_content;
    public String chatbot_time;
    public boolean is_chatbot;

    public ChatBot(){

    }

    public ChatBot(String chatbot_content, String chatbot_time, boolean is_chatbot) {
        this.chatbot_content = chatbot_content;
        this.chatbot_time = chatbot_time;
        this.is_chatbot = is_chatbot;
    }

    public ChatBot(int chatbot_no, String chatbot_content, String chatbot_time, boolean is_chatbot) {
        this.chatbot_no = chatbot_no;
        this.chatbot_content = chatbot_content;
        this.chatbot_time = chatbot_time;
        this.is_chatbot = is_chatbot;
    }

    public void setChatbot_no(int chatbot_no) {
        this.chatbot_no = chatbot_no;
    }

    public void setChatbot_content(String chatbot_content) {
        this.chatbot_content = chatbot_content;
    }

    public void setChatbot_time(String chatbot_time) {
        this.chatbot_time = chatbot_time;
    }

    public int getChatbot_no() {
        return chatbot_no;
    }

    public String getChatbot_content() {
        return chatbot_content;
    }

    public String getChatbot_time() {
        return chatbot_time;
    }

}
