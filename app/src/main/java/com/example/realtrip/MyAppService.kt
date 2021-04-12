package com.example.realtrip

import android.app.Activity
import android.content.Context
import android.util.Log

import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.example.realtrip.asynctask.LoginCheck

import java.util.HashMap

/**
 * MyAppService 클래스
 * - 앱의 각종 서비스를 수행하는 클래스
 */
class MyAppService {
    internal var TAG = "yeon[" + this.javaClass.simpleName + "]" // log를 위한 태그
    internal var myAppData: MyAppData

    init {
        myAppData = MyAppData()
    } // MyAppService 기본 생성자


    /**
     * Volley 예시 메소드
     */
    fun sendRequest() {
        val stringRequest = object : StringRequest(
                Request.Method.POST,
                "http://18.221.242.79/query.php",
                Response.Listener { },
                Response.ErrorListener { }

        ) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                return super.getParams()
            }
        }

        stringRequest.setShouldCache(false) // 이건 무엇인지 알아보기
        AppHelper.requestQueue.add(stringRequest)

    }

} // MyAppService 클래스


