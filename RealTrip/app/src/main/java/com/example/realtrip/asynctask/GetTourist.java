package com.example.realtrip.asynctask;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baoyz.widget.PullRefreshLayout;
import com.example.realtrip.MYURL;
import com.example.realtrip.R;
import com.example.realtrip.activity.HomeActivity;
import com.example.realtrip.adapter.BoardAdapter;
import com.example.realtrip.adapter.TouristAdapter;
import com.example.realtrip.object.Tourist;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class GetTourist extends AsyncTask<String,Void,String> {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    Activity activity;
    ArrayList<Tourist> tourists;

    public GetTourist(Activity activity){
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }



    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected String doInBackground(String... strings) {
        Log.d(TAG,"doInBackground() 호출");

        /**
         * request_type : 공공데이터 api에 어떤 요청을 할지 정하기
         * >> 0 : arabased (지역기반 검색)
         * >> 1 : search_keword (키워드 검색)
         */
        int request_type = Integer.parseInt(strings[0]);
        String serverURL=MYURL.TOUR_URL_AREABASED; // 서버 url 주소

        if(request_type==0){ // arabased 요청
            serverURL = MYURL.TOUR_URL_AREABASED; // 서버 url 주소

            int areacode = Integer.parseInt(strings[1]);

            if(areacode!=0){ // 지역 검색
                serverURL = serverURL+areacode;
            }

        }else if(request_type==1){ // search_keword 요청
            String keyword=null;
            try{
                keyword = URLEncoder.encode(strings[1],"UTF-8");
            }catch (UnsupportedEncodingException e){
                Log.d(TAG,e.toString());
                e.printStackTrace();
            }
            serverURL = MYURL.TOUR_URL_SEARCH_KEYWORD+keyword; // 서버 url 주소
        }

        try{
            Log.d(TAG,"serverURL: "+serverURL);
            URL url = new URL(serverURL);
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
            Log.d(TAG,"Error "+e);
            return "서버 접근 안됨";
        }

    } // doInBackGround()

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        tourists = new ArrayList<>();
        try{
            // MYURL.test_json : json 문자열
            Log.d(TAG,"응답: "+s);
            JSONObject jsonObject = new JSONObject(s);
            JSONObject response = jsonObject.getJSONObject("response");
            JSONObject body = response.getJSONObject("body");
            int totalCount = body.getInt("totalCount");
            Log.d(TAG,"totalCount: "+totalCount);
            if(totalCount==0){
                Toast.makeText(activity.getApplicationContext(), "검색 결과가 없습니다.",Toast.LENGTH_SHORT).show();
            }else if(totalCount==1){
                JSONObject items = body.getJSONObject("items");
                JSONObject item = (JSONObject)items.get("item");
                Gson gson = new Gson();
                Tourist tourist = gson.fromJson(item.toString(),Tourist.class);

                tourists.add(tourist);
            }else{
                JSONObject items = body.getJSONObject("items");
                JSONArray item = (JSONArray)items.get("item");
                Log.d(TAG,"items.length(): "+items.length()+"");
                for(int i=0; i<item.length(); i++){

                    JSONObject tourist_json = item.getJSONObject(i);
                    Gson gson = new Gson();
                    Tourist tourist = gson.fromJson(tourist_json.toString(),Tourist.class);

                    tourists.add(tourist);
                }
            }
        }catch (JSONException e){
            Log.d(TAG,e.toString());
        }

        /**
         * 리싸이클러뷰 적용
         */
        RecyclerView tourist_recyclerview = activity.findViewById(R.id.tourist_recyclerview);
        tourist_recyclerview.setLayoutManager(new GridLayoutManager(activity.getApplicationContext(),2));
        TouristAdapter touristAdapter = new TouristAdapter(tourists, activity);
        tourist_recyclerview.setAdapter(touristAdapter);
        /**
         * 새로고침 프로그래스 바 멈추기
         */
        PullRefreshLayout pullRefreshLayout =activity.findViewById(R.id.pull_refresh_layout);
        pullRefreshLayout.setRefreshing(false);
    }
} // 클래스
