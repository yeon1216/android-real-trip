package com.example.realtrip.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.realtrip.MYURL;
import com.example.realtrip.R;
import com.example.realtrip.adapter.ChatAdapter;
import com.example.realtrip.adapter.ChatBotAdapter;
import com.example.realtrip.asynctask.ChatBotRequestTask;
import com.example.realtrip.asynctask.GetTourist;
import com.example.realtrip.object.Chat;
import com.example.realtrip.object.ChatBot;
import com.example.realtrip.object.Member;
import com.example.realtrip.object.Tourist;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.AIServiceContext;
import ai.api.AIServiceContextBuilder;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;

/**
 * ChatBotActivity 클래스
 */
public class ChatBotActivity extends AppCompatActivity {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    AIDataService aiDataService;
    AIServiceContext customAIServiceContext;
    AIRequest aiRequest;
    UUID uuid; // (Universally Unique IDentifier : 범용고유식별자)

    EditText chatbot_et; // 챗봇에게 할 말 입력창

    RecyclerView chatbot_recyclerview;
    ArrayList<ChatBot> chatBots;
    ChatBotAdapter chatBotAdapter;

    boolean is_search; // 검색중인지 여부

    /**
     * onCreate() 메소드
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bot);

        Log.d(TAG,"onCreate()");

        is_search = false;

        /*
         * 쉐어드에 저장된 멤버 정보 가지고 오기
         */
        SharedPreferences sharedPreferences = getSharedPreferences("myAppData",MODE_PRIVATE);
        Gson gson = new Gson();
        Member login_member = gson.fromJson(sharedPreferences.getString("login_member","no_login"),Member.class);



        uuid = UUID.randomUUID(); // (Universally Unique IDentifier : 범용고유식별자)

        initChatbot(); // 봇이 사용자와 통신할 준비를 하는 메소드

        chatbot_et = findViewById(R.id.chatbot_et); // 챗봇에게 할 말 입력창

        Button send_btn = findViewById(R.id.send_btn); // 전송 버튼
        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 전송 버튼 클릭시 이벤트
                String msg = chatbot_et.getText().toString();
                if(msg.trim().isEmpty()){
                    Toast.makeText(getApplicationContext(),"대화를 작성해주세요",Toast.LENGTH_SHORT).show();
                }else{
                    Log.d(TAG,"chatbot_et : "+msg);
                    chatbot_et.setText("");
                    if(is_search){ //  검색
                        is_search=false;

                        /*
                            키워드를 통해 검색
                         */
                        Intent intent = new Intent(getApplicationContext(),HomeActivity.class);
                        intent.putExtra("from_chat_bot","from_chat_bot");
                        intent.putExtra("keyword",msg);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);

                        chatBotAdapter.addItem(new ChatBot(msg,timeToString(Calendar.getInstance()),false));
                        chatbot_recyclerview.scrollToPosition(chatBotAdapter.getItemCount()-1);

                        chatBotAdapter.addItem(new ChatBot("\'"+msg+"\' 검색 완료",timeToString(Calendar.getInstance()),true));
                        chatbot_recyclerview.scrollToPosition(chatBotAdapter.getItemCount()-1);
                    }else{


                        aiRequest.setQuery(msg);
                        ChatBotRequestTask requestTask = new ChatBotRequestTask(ChatBotActivity.this,aiDataService,customAIServiceContext);
                        requestTask.execute(aiRequest); // chatbotrequesttask 실행

                        chatBotAdapter.addItem(new ChatBot(msg,timeToString(Calendar.getInstance()),false));
                        chatbot_recyclerview.scrollToPosition(chatBotAdapter.getItemCount()-1);
                    }


                }
            }
        });

        /*
            chatbot_recyclerview 적용
         */
        chatBots = new ArrayList<>();
        chatbot_recyclerview = findViewById(R.id.chatbot_recyclerview);
        chatbot_recyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        sort(chatBots); // 리뷰를 최근순으로 정렬
        chatBotAdapter = new ChatBotAdapter(chatBots,ChatBotActivity.this);
        chatbot_recyclerview.setAdapter(chatBotAdapter);

        chatBotAdapter.addItem(new ChatBot("안녕하세요. "+login_member.member_nickname+"님!! \n\n저는 리얼트립봇이에요. \n\n여행관련 질문을 해주세요!!\n\nex> 여행지 추천해줘!!",timeToString(Calendar.getInstance()),true));
        chatbot_recyclerview.scrollToPosition(chatBotAdapter.getItemCount()-1);

    } // onCreate() 메소드

    /**
     * 봇이 사용자와 통신할 준비를 하는 메소드
     *   1. client access token 을 사용하여 Dialogflow agent 를 구성
     *   2. 고유 id를 사용하여 봇이 사용자와 통신할 준비가 됨
     */
    private void initChatbot(){
        final AIConfiguration config = new AIConfiguration("d0911e8828b345278338521d8850bc8b", AIConfiguration.SupportedLanguages.Korean); // realtrip chatbot
        aiDataService = new AIDataService(config);
        customAIServiceContext = AIServiceContextBuilder.buildFromSessionId(uuid.toString());
        aiRequest = new AIRequest();
    } // initChatbot() 메소드

    /**
     * 챗봇으로부터 받는 응답
     */
    public void callback(AIResponse aiResponse){
        if(aiResponse!=null){
            String botReply = aiResponse.getResult().getFulfillment().getSpeech();
            Log.d(TAG,"Bot Reply: "+botReply);

            String[] strArr = botReply.split(",");
            if("여행지 추천".equals(strArr[0])){
                getTourist(strArr[1]);
            }else if("검색어를 입력해주세요!!".equals(strArr[0])){
                is_search = true;
//                ChatBot chatBot = chatBots.get(chatBotAdapter.getItemCount()-1);
//                String keyword = chatBot.chatbot_content;
//                Log.d(TAG,"검색중 keyword: "+keyword);
//                chatBotAdapter.addItem(new ChatBot("\'"+keyword+"\' 검색 완료",timeToString(Calendar.getInstance()),true));
//                chatbot_recyclerview.scrollToPosition(chatBotAdapter.getItemCount()-1);
                chatBotAdapter.addItem(new ChatBot(botReply,timeToString(Calendar.getInstance()),true));
                chatbot_recyclerview.scrollToPosition(chatBotAdapter.getItemCount()-1);
            }else{
                chatBotAdapter.addItem(new ChatBot(botReply,timeToString(Calendar.getInstance()),true));
                chatbot_recyclerview.scrollToPosition(chatBotAdapter.getItemCount()-1);
            }

        }else{
            Log.d(TAG,"Bot Reply: null");
        }
    } // callback() 메소드

//    /**
//     * getTouristRequest() 메소드
//     * - 여행지 리스트를 리싸이클러뷰에 보여줌
//     * - GetTourist asynctask 실행
//     */
//    public void getTouristRequest(String request_type, String areacode_or_keyword){
//        GetTourist getTourist = new GetTourist(HomeActivity.this);
//        getTourist.execute(request_type, areacode_or_keyword); // GetTourist asynctask 실행
//        remember_request_type_for_refresh = request_type;
//        remember_areacode_or_keyword_for_refresh = areacode_or_keyword;
//    } // getTouristRequest() 메소드



    /**
     * sort() 메소드
     * 채팅을 최근 순으로 정렬해주는 메소드
     */
    ArrayList<ChatBot> sort(ArrayList<ChatBot> chatBots){
        for (int i = 0; i < chatBots.size()-1; i++) {
            for (int j = 1; j < chatBots.size(); j++) {
                if(chatBots.get(j-1).chatbot_no > chatBots.get(j).chatbot_no){
                    ChatBot temp_chatbot = chatBots.get(j-1);
                    chatBots.set(j-1,chatBots.get(j));
                    chatBots.set(j,temp_chatbot);
                }
            }
        }
        return chatBots;
    } // 정렬 메소드

    /**
     * Calendar를 년월일시분초로 반환 메소드
     */
    public String timeToString(Calendar time) {
        String timeToString = (time.get(Calendar.YEAR)) + "." + (time.get(Calendar.MONTH) + 1) + "."
                + (time.get(Calendar.DAY_OF_MONTH)) + " " + (time.get(Calendar.HOUR_OF_DAY)) + "시"
                + (time.get(Calendar.MINUTE))+"분"+(time.get(Calendar.SECOND))+"초";
        return timeToString.substring(2);
    } // timeToString() 메소드

    /**
     * 한국관광공사에서 지역기반 여행지 얻어오기
     */
    public void getTourist(final String location){
        new AsyncTask<String,Void,String>(){
            /**
             * doInBackground() 메소드
             */
            @Override
            protected String doInBackground(String... strings) {

                try{
                    URL url = null;
                    if("전국".equals(strings[0])){
                        url = new URL(MYURL.TOUR_URL_AREABASED_CHAT_BOT);
                    }else{
                        url = new URL(MYURL.TOUR_URL_AREABASED_CHAT_BOT+getAreaCode(strings[0].trim()));
                        Log.d(TAG,"doInBackground() getAreaCode(strings[0]): "+getAreaCode(strings[0].trim()));
                    }
                    HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection(); //url을 연결한 HttpURLConnection 객체 생성
                    httpURLConnection.setRequestMethod("GET"); // post 통신 방식
                    BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(),"UTF-8"));
                    String result = "";
                    String line;
                    while((line = br.readLine()) != null) {
                        result = result + line + "\n";
                    }
                    return result;
                }catch (Exception e){
                    Log.d(TAG,"error: "+e.toString());
                    return "서버접근안됨";
                }

            } // doInBackground() 메소드

            /**
             * onPostExecute() 메소드
             */
            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                try{
                    Log.d(TAG,"응답: "+s);
                    JSONObject jsonObject = new JSONObject(s);
                    JSONObject response = jsonObject.getJSONObject("response");
                    JSONObject body = response.getJSONObject("body");
                    int totalCount = body.getInt("totalCount");
                    Log.d(TAG,"totalCount: "+totalCount);
                    if(totalCount==0){ // 검색된 여행지가 없음

                        chatBotAdapter.addItem(new ChatBot("\'"+location.trim()+"\'(으)로 검색된 여행지가 없습니다.",timeToString(Calendar.getInstance()),true));
                        chatbot_recyclerview.scrollToPosition(chatBotAdapter.getItemCount()-1);

                    }else if(totalCount==1){ // 검색된 여행지가 하나임

                        JSONObject items = body.getJSONObject("items");
                        JSONObject item = (JSONObject)items.get("item");
                        Gson gson = new Gson();
                        Tourist tourist = gson.fromJson(item.toString(),Tourist.class);
                        chatBotAdapter.addItem(new ChatBot("\'"+location.trim()+"\'(으)로 검색된 여행지입니다!!\n\n1. "+tourist.title,timeToString(Calendar.getInstance()),true));
                        chatbot_recyclerview.scrollToPosition(chatBotAdapter.getItemCount()-1);

                    }else{ // 검색된 여행지가 두개 이상임

                        JSONObject items = body.getJSONObject("items");
                        JSONArray item = (JSONArray)items.get("item");
                        Log.d(TAG,"items.length(): "+items.length()+"");
                        String result = "\'"+location.trim()+"\'(으)로 검색된 여행지입니다!!\n\n";
                        for(int i=0; i<item.length(); i++){
                            JSONObject tourist_json = item.getJSONObject(i);
                            Gson gson = new Gson();
                            Tourist tourist = gson.fromJson(tourist_json.toString(),Tourist.class);
                            result = result + (i+1)+". "+tourist.title+"\n";
                        }
                        result = result + "\n여행지를 검색해보고싶으면 다음과같이 입력해주세요!!\n\n>> 여행지 검색해줘 ~";
                        chatBotAdapter.addItem(new ChatBot(result,timeToString(Calendar.getInstance()),true));
                        chatbot_recyclerview.scrollToPosition(chatBotAdapter.getItemCount()-1);

                    }
                }catch (JSONException e){
                    Log.d(TAG,e.toString());
                }
            } // onPostExecute() 메소드

        }.execute(location); // asynctask 실행
    } // getTourist() 메소드

    /**
     * getAreaCode() 메소드
     *
     * 지역 : areaCode
     * 서울 1
     * 인천 2
     * 대전 3
     * 대구 4
     * 광주 5
     * 부산 6
     * 울산 7
     * 세종특별자치시 8
     * 경기도 31
     * 강원도 32
     * 충청북도 33
     * 충청남도 34
     * 경상북도 35
     * 경상남도 36
     * 전라북도 37
     * 전라남도 38
     * 제주도 39
     */
    public int getAreaCode(String location){
        Log.d(TAG,"getAreaCode() location: "+location);
        if("서울".equals(location)){
            return 1;
        }else if("인천".equals(location)){
            return 2;
        }else if("대전".equals(location)){
            return 3;
        }else if("대구".equals(location)){
            return 4;
        }else if("광주".equals(location)){
            return 5;
        }else if("부산".equals(location)){
            return 6;
        }else if("울산".equals(location)){
            return 7;
        }else if("세종특별자치시".equals(location)){
            return 8;
        }else if("경기도".equals(location)){
            return 31;
        }else if("강원도".equals(location)){
            return 32;
        }else if("충청북도".equals(location)){
            return 33;
        }else if("충청남도".equals(location)){
            return 34;
        }else if("경상북도".equals(location)){
            return 35;
        }else if("경상남도".equals(location)){
            return 36;
        }else if("전라북도".equals(location)){
            return 37;
        }else if("전라남도".equals(location)){
            return 38;
        }else if("제주도".equals(location)){
            return 39;
        }

        return 1;
    } // getAreaCode() 메소드

} // ChatBotActivity 클래스
