package com.example.realtrip.asynctask;

import android.app.Activity;
import android.os.AsyncTask;

import com.example.realtrip.activity.ChatBotActivity;

import ai.api.AIDataService;
import ai.api.AIServiceContext;
import ai.api.AIServiceException;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;

/**
 * RequestTask 클래스
 */
public class ChatBotRequestTask extends AsyncTask<AIRequest,Void, AIResponse> {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    Activity activity;
    AIDataService aiDataService;
    AIServiceContext customAIServiceContext;

    /**
     * 생성자
     */
    public ChatBotRequestTask(Activity activity, AIDataService aiDataService, AIServiceContext customAIServiceContext){
        this.activity = activity;
        this.aiDataService = aiDataService;
        this.customAIServiceContext = customAIServiceContext;
    } // 생성자

    /**
     * doInBackground() 메소드
     */
    @Override
    protected AIResponse doInBackground(AIRequest... aiRequests) {
        final AIRequest request = aiRequests[0];
        try {
            return aiDataService.request(request,customAIServiceContext);
        }catch (AIServiceException e){
            e.printStackTrace();
        }
        return null;
    } // doInBackground() 메소드

    /**
     * onPostExecute() 메소드
     */
    @Override
    protected void onPostExecute(AIResponse aiResponse) {
        super.onPostExecute(aiResponse);
        ((ChatBotActivity)activity).callback(aiResponse);
    } // onPostExecute() 메소드

} // ReqeustTask 클래스
