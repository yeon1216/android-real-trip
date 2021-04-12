package com.example.realtrip;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.realtrip.object.Board;
import com.google.api.client.util.Lists;
import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.ComputeEngineCredentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Test {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    /**
     * test() 메소드
     */
    public static void test(Context context){
        Log.d("yeon[테스트]","test() 메소드 실행");

//        new Thread(){
//            public void run(){
//                // Instantiates a client
//                try (LanguageServiceClient language = LanguageServiceClient.create()) {
//                    Log.d("yeon[테스트]","1");
//                    // The text to analyze
//                    String text = "Hello, world!";
//                    Document doc = Document.newBuilder()
//                            .setContent(text).setType(Document.Type.PLAIN_TEXT).build();
//                    Log.d("yeon[테스트]","2");
//
//                    // Detects the sentiment of the text
//                    Sentiment sentiment = language.analyzeSentiment(doc).getDocumentSentiment();
//                    Log.d("yeon[테스트]","3");
//
//                    System.out.printf("Text: %s%n", text);
//                    Log.d("yeon[테스트]","Text: "+text);
//                    System.out.printf("Sentiment: %s, %s%n", sentiment.getScore(), sentiment.getMagnitude());
//                    Log.d("yeon[테스트]","Sentiment // score: "+sentiment.getScore()+", magnitude: "+sentiment.getMagnitude());
//                    Log.d("yeon[테스트]","4");
//                }catch (IOException e){
//                    Log.d("yeon[테스트]","IOException: "+e.toString());
//                    Log.d("yeon[테스트]","5");
//                }
//            }
//        }.start();

    } // test() 메소드


    /**
     * 감정분석
     */
    public static void testSentimentRequest(Context context){
        Log.d("yeon[테스트]","testSentimentRequest");

        // 요청 인자
        Map<String,String> params = new HashMap<String,String>();
        params.put("type","PLAIN_TEXT");
        params.put("content","여러가지 쌈채소를 넉넉하게 제공. 배추 양배추 상추 깻잎 등... 가격도 저렴한데 밑반찬도 잘 나온다. 후식으로 식혜한잔 딱 마시면 만족스러운 식사ㅋㅋ 근데 점심에 인기가 많아서 웨이팅이 길다");

        Map<String,Map<String,String>> document = new HashMap<String,Map<String,String>>();
        document.put("document",params);

        JSONObject jsonObject = new JSONObject(document); // Map을 json으로 만듬

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, "https://language.googleapis.com/v1/documents:analyzeSentiment?key=AIzaSyDZ8o8IchoSQidv-52sAF--Bs1A82_nBvM", jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) { // 응답 성공
                        Log.d("yeon[테스트]","응답 성공 : "+response);
                        Log.d("yeon[테스트]","응답 성공 : "+response.toString());
                        try{
                            JSONObject documentSentiment = response.getJSONObject("documentSentiment");
                            String score = String.valueOf(documentSentiment.getDouble("score"));
                            String magnitude = String.valueOf(documentSentiment.getDouble("magnitude"));
                            Log.d("yeon[테스트]","score : "+score);
                            Log.d("yeon[테스트]","magnitude : "+magnitude);
                        }catch (JSONException e){
                            Log.d("yeon[테스트]","JSONException : "+e.toString());
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) { // 응답 실패
                Log.d("yeon[테스트]","응답 성공 : "+error.toString());
            }
        });

        /**
         * requestQueue가 없으면 requestQueue 생성
         */
        if(AppHelper.requestQueue == null){ // requestQueue 가 없을 경우
            AppHelper.requestQueue = Volley.newRequestQueue(context); // requestQueue 생성
        }

        AppHelper.requestQueue.add(jsonObjectRequest); // 요청 큐에 위 요청 추가

    } // testSentimentRequest() 메소드

    public static void authImplicit(){
        Log.d("yeon[테스트]","authImplicit() 호출");
        new Thread(){
            public void run(){
                // If you don't specify credentials when constructing the client, the client library will
                // look for credentials via the environment variable GOOGLE_APPLICATION_CREDENTIALS.
                Storage storage = StorageOptions.getDefaultInstance().getService();

                System.out.println("Buckets:");
                Page<Bucket> buckets = storage.list();
                for (Bucket bucket : buckets.iterateAll()) {
                    System.out.println(bucket.toString());
                }
            }
        }.start();
    }

    public static void authExplicit(final String jsonPath){
        Log.d("yeon[테스트]","authExplicit() 호출");
        new Thread(){
            public void run(){

//                try {
//                    // You can specify a credential file by providing a path to GoogleCredentials.
//                    // Otherwise credentials are read from the GOOGLE_APPLICATION_CREDENTIALS environment variable.
//                    GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(jsonPath))
//                            .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
//                    Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
//
//                    System.out.println("Buckets:");
//                    Page<Bucket> buckets = storage.list();
//                    for (Bucket bucket : buckets.iterateAll()) {
//                        System.out.println(bucket.toString());
//                    }
//                }catch (FileNotFoundException e){
//
//                }catch (IOException e){
//
//                }
            }
        }.start();
    }


    public static void authCompute(){
        new Thread(){
            public void run(){
                // Explicitly request service account credentials from the compute engine instance.
                GoogleCredentials credentials = ComputeEngineCredentials.create();
                Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

                System.out.println("Buckets:");
                Page<Bucket> buckets = storage.list();
                for (Bucket bucket : buckets.iterateAll()) {
                    System.out.println(bucket.toString());
                }
            }
        }.start();
    }

    public void authEngineStandard(){
        new Thread(){
            public void run(){
                // Explicitly request service account credentials from the app engine standard instance.
//                GoogleCredentials credentials = AppEngineCredentials.getApplicationDefault();
//                Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
//
//                System.out.println("Buckets:");
//                Page<Bucket> buckets = storage.list();
//                for (Bucket bucket : buckets.iterateAll()) {
//                    System.out.println(bucket.toString());
//                }
            }
        }.start();
    }
} // Test 클래스
