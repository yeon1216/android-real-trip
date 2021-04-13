package com.example.realtrip.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.realtrip.AppHelper;
import com.example.realtrip.FileUploadUtils;
import com.example.realtrip.MYURL;
import com.example.realtrip.R;
import com.example.realtrip.RegisterImgMoveCallback;
import com.example.realtrip.adapter.RegisterImgAdapter;
import com.example.realtrip.item.ReviewItem;
import com.example.realtrip.object.Member;
import com.example.realtrip.object.Tourist;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 리뷰 수정 클래스
 */
public class ReviewUpdateActivity extends AppCompatActivity implements RegisterImgAdapter.OnStartDragListener {


    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    private static final int PICK_FROM_ALBUM=0; // 앨범으로 접근
    private static final int PICK_FROM_CAMERA=2; // 카메라로 접근

    File temp_file; // 이미지 업로드를 위한 임시파일
    ArrayList<String> temp_file_path_arr; // 다중 이미지를 업로드하기위한 파일 경로 리스트

    EditText review_content_et; // 리뷰 작성 창

    ReviewItem reviewItem; // 리뷰 아이템 객체
    String tourist_json; // 여행지 json 문자열


    private long mLastClickTime = 0; // 리뷰 수정버튼 두번 클릭을 막기 위해

    ProgressDialog progressDialog; // 리뷰 수정중임을 표현하기 위해

    ItemTouchHelper itemTouchHelper; // 아이템 터치 리스터

    /**
     * onCreate() 메소드
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_update);
        Log.d(TAG,"onCreate() 호출");

        /**
         * temp_file_path_arr에 현재 들어있는 파일 경로들 보기
         */
        TextView activity_name_tv = findViewById(R.id.activity_name_tv);
        activity_name_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"temp_file_path_arr.size(): "+temp_file_path_arr.size());
                for(int i=0;i<temp_file_path_arr.size();i++){
                    Log.d(TAG,i+"번째 이미지 파일: "+temp_file_path_arr.get(i));
                }
            }
        });

        temp_file_path_arr = new ArrayList<>(); // temp_file_path_arr 생성

        /**
         * 인텐트에서 리뷰 아이템, 여행지 json 문자열 받아오기
         */
        Intent intent = getIntent();
        String reviewItem_str = intent.getStringExtra("reviewItem"); // 리뷰 아이템 json 문자열
        Gson gson = new Gson();
        reviewItem = gson.fromJson(reviewItem_str,ReviewItem.class);
        tourist_json = intent.getStringExtra("tourist"); // 여행지 json 문자열

        /**
         * 기존 리뷰 내용 적용
         */
        review_content_et = findViewById(R.id.review_content_et); // 리뷰 작성 창
        review_content_et.setText(reviewItem.review_content);

        /**
         * 기존 리뷰 내용 적용
         */
        if(!"".equals(reviewItem.review_img)){
            String[] review_img_arr = reviewItem.review_img.split("\\|");
            for (int i=0; i<review_img_arr.length; i++){
                temp_file_path_arr.add(review_img_arr[i]);
            }
        }

        Button pick_from_camera_btn = findViewById(R.id.pick_from_camera_btn); // 카메라에서 사진 찍기 버튼
        pick_from_camera_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto();
            }
        });
        Button pick_from_albam_btn = findViewById(R.id.pick_from_albam_btn); // 앨범에서 이미지 가지고 오기 버튼
        pick_from_albam_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToAlbum();
            }
        });

        Button update_btn = findViewById(R.id.update_btn); // 작성 버튼
        update_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 작성 버튼 클릭시 이벤트
                Log.d(TAG+"rw_check","작성 버튼 클릭");

                /**
                 * 작성한 글 가지고 오기
                 */
                String review_content = review_content_et.getText().toString();

                /**
                 * 작성한 글이 없다면 리턴
                 */
                if(review_content.length()==0){
                    Toast.makeText(getApplicationContext(),"리뷰를 작성해주세요",Toast.LENGTH_SHORT).show();
                    return;
                }

                /**
                 * 두번 클릭을 막기위한 코드
                 */
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                /**
                 * 프로그래스바 로딩 시작
                 */
                loading();

                /**
                 * 작성된 리뷰 감정분석하여 서버에 등록하기
                 */
                sentimentRequest(review_content); // 감정분석 메소드
            }
        });

    } // onCreate() 메소드

    /**
     * onResume() 메소드
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume() 호출");

        ImageView register_img_iv = findViewById(R.id.register_img_iv);
        RecyclerView register_img_recyclerview = findViewById(R.id.register_img_recyclerview);
        register_img_recyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.HORIZONTAL,false));

        if(temp_file_path_arr.size()!=0){ // 선택한 이미지가 있는 경우
            register_img_iv.setVisibility(View.GONE);
            register_img_recyclerview.setVisibility(View.VISIBLE);

            /**
             * 리싸이클러뷰 세팅
             */
            RegisterImgAdapter registerImgAdapter = new RegisterImgAdapter(temp_file_path_arr,ReviewUpdateActivity.this,this,"update_review"); // 어댑터 생성
            RegisterImgMoveCallback registerImgMoveCallback = new RegisterImgMoveCallback(registerImgAdapter);
            itemTouchHelper = new ItemTouchHelper(registerImgMoveCallback);
            itemTouchHelper.attachToRecyclerView(register_img_recyclerview); //
            register_img_recyclerview.setAdapter(registerImgAdapter); // 어댑터 설정

        }
    } // onResume() 메소드

    /**
     * onActivityResult() 메소드
     * - startActivityForResult() 메소드로 호출한 액티비티의 결과를 처리한다
     * @param requestCode 액티비티를 실행하면서 전달한 요청 코드
     * @param resultCode 실행한 액티비티가 설정한 결과코드
     * @param data 결과 데이터
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG,"onActivityResult() 메소드   /   intent: "+data);

        /**
         * 예외사항 처리
         */
        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();
            if(temp_file != null) {
                if (temp_file.exists()) {
                    if (temp_file.delete()) {
                        Log.e(TAG, temp_file.getAbsolutePath() + " 삭제 성공");
                        temp_file = null;
                    }
                }
            }
            return;
        }

        /**
         * 앨범에서 선택한 이미지
         */
        if (requestCode == PICK_FROM_ALBUM && data!=null) { // 앨범에서 선택한 사진인 경우
            Log.d(TAG,"PICK_FROM_ALBUM");

            ClipData clipData = data.getClipData();
            Log.d(TAG,"clipData: "+ String.valueOf(clipData.getItemCount()));
            if(clipData.getItemCount()+temp_file_path_arr.size()>5){
                Toast.makeText(getApplicationContext(),"사진은 5개까지 선택 가능합니다.",Toast.LENGTH_SHORT).show();
                return;
            }
            if(clipData.getItemCount()>5){ // 5개 넘개 선택한 경우
                Toast.makeText(getApplicationContext(),"사진은 5개까지 선택 가능합니다.",Toast.LENGTH_SHORT).show();
                return;
            }else if(clipData.getItemCount()==1){ // 1개만 선택한 경우
                String data_str = String.valueOf(clipData.getItemAt(0).getUri());
                Log.d(TAG,"clipData choice: "+ String.valueOf(clipData.getItemAt(0).getUri()));
                Log.d(TAG,"single choice: "+ clipData.getItemAt(0).getUri().getPath());
                temp_file_path_arr.add(data_str);
            }else if(clipData.getItemCount()>1 && clipData.getItemCount()<=5){ // 여러개의 이미지를 선택한 경우
                for (int i=0;i<clipData.getItemCount();i++){
                    Log.d(TAG,"여러개 선택: "+i+"번째>>"+ clipData.getItemAt(i).getUri());
//                    temp_file_path_arr.add(String.valueOf(clipData.getItemAt(i).getUri()));
                    try{
                        temp_file_path_arr.add(FileUploadUtils.getInstance().getPath(getApplicationContext(),clipData.getItemAt(i).getUri()));
                    }catch (URISyntaxException e){}

                }
            }

//            Uri profile_img_uri = data.getData(); // 이미지 Uri를 가지고 온다

//            Log.d(TAG,"앨범 선택 uri: "+profile_img_uri.toString());
//            Log.d(TAG,"앨범 선택 uri: "+data.getParcelableExtra(Intent.EXTRA_STREAM));

//            // ~~~~~~~~~~~~~~~  원래 orientation을 알기 위한 코드 ~~~~~~~~~~~~~~~ ~~~~~~~~~~~~~~~ ~~~~~~~~~~~~~~~
//            try{
//                temp_file = new File(com.example.realtrip.FileUploadUtils.getPath(this,profile_img_uri)); // uri로 file 생성
//                ExifInterface exif = new ExifInterface(Uri.fromFile(temp_file).getPath());
//                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
//                Log.d(TAG,"원래 orientation: "+orientation);
//            }catch (URISyntaxException e){  // uri --> file 에서의 예외
//                Log.d(TAG,e.toString());
//            }catch (IOException e){
//                Log.d(TAG,e.toString());
//            }
//            // ~~~~~~~~~~~~~~~ ~~~~~~~~~~~~~~~ ~~~~~~~~~~~~~~~ ~~~~~~~~~~~~~~~ ~~~~~~~~~~~~~~~ ~~~~~~~~~~~~~~~
//
//            Bitmap bitmap = null;
//            try {
//                // 빈 이미지 file 생성
//                temp_file = createImageFile();
//
//                // uri to bitmap
//                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), profile_img_uri);
//
//                // bitmap to file
//                OutputStream outputStream = new FileOutputStream(temp_file);
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
//                outputStream.close();
//
//            } catch (FileNotFoundException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }

//            profileUpload();

            /**
             * 카메라에서 찍은 이미지
             */
        }else if(requestCode == PICK_FROM_CAMERA && data!=null){ // 카메라에서 찍은 사진
            Log.d(TAG,"PICK_FROM_CAMERA");
            ClipData clipData = data.getClipData();
            temp_file_path_arr.add(Uri.fromFile(temp_file).getPath());
//            try{
//                temp_file_path_arr.add(FileUploadUtils.getInstance().getPath(getApplicationContext(),clipData.getItemAt(0).getUri()));
//            }catch (URISyntaxException e){}
        }

    } // onActivityResult() 메소드

    /**
     * 감정분석 메소드
     */
    public void sentimentRequest(final String review_content){
        Log.d(TAG+"rw_check","sentimentRequest");

        // 요청 인자
        Map<String,String> params = new HashMap<String,String>();
        params.put("type","PLAIN_TEXT");
        params.put("content",review_content);

        Map<String,Map<String,String>> document = new HashMap<String,Map<String,String>>();
        document.put("document",params);

        JSONObject jsonObject = new JSONObject(document); // Map을 json으로 만듬

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, "https://language.googleapis.com/v1/documents:analyzeSentiment?key=AIzaSyDZ8o8IchoSQidv-52sAF--Bs1A82_nBvM", jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) { // 응답 성공
                        Log.d(TAG+"rw_check","응답 성공 : "+response.toString());

                        try{
                            JSONObject documentSentiment = response.getJSONObject("documentSentiment");
                            String score = String.valueOf(documentSentiment.getDouble("score"));
                            String magnitude = String.valueOf(documentSentiment.getDouble("magnitude"));
                            Log.d(TAG,"score : "+score);
                            Log.d(TAG,"magnitude : "+magnitude);

                            /**
                             * 작성중인 멤버 가지고 오기
                             */
                            SharedPreferences sharedPreferences = getSharedPreferences("myAppData",MODE_PRIVATE);
                            int login_member_no = (new Gson()).fromJson(sharedPreferences.getString("login_member","no_login"), Member.class).member_no; // 현재 로그인 한 멤버의 멤버번호

                            /**
                             * 서버에 저장
                             */
                            updateReviewRequest(review_content,login_member_no, score, magnitude);

                        }catch (JSONException e){
                            Log.d(TAG,"JSONException : "+e.toString());
                            loadingEnd(); // 프로그래스바 로딩 종료
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) { // 응답 실패
                Log.d(TAG,"응답 실패 : "+error.toString());
                loadingEnd(); // 프로그래스바 로딩 종료
            }
        });

        /**
         * requestQueue가 없으면 requestQueue 생성
         */
        if(AppHelper.requestQueue == null){ // requestQueue 가 없을 경우
            AppHelper.requestQueue = Volley.newRequestQueue(getApplicationContext()); // requestQueue 생성
        }

        AppHelper.requestQueue.add(jsonObjectRequest); // 요청 큐에 위 요청 추가

    } // sentimentRequest() 메소드

    /**
     * 리뷰수정 요청 메소드
     */
    void updateReviewRequest(final String review_content, final int login_member_no, final String score, final String magnitude){
        Log.d(TAG+"rw_check","writeBoardRequest() 메소드 호출");
        Log.d(TAG,"review_content: "+review_content+", login_member_no: "+login_member_no+", content_id: "+reviewItem.content_id);

        /**
         * 이미지 파일 얻기
         * 1. uri로 비트맵 만들기
         * 2. 비트맵을 base64를 통해 문자열로 만들기
         */
        String img_name_arr = "";
        String encode_img_arr = "";
        for(int i=0;i<temp_file_path_arr.size();i++){
            Log.d(TAG+"rw_check",i+"번째 이미지 파일 경로: "+temp_file_path_arr.get(i));

            if("test".equals(temp_file_path_arr.get(i).substring(0,4))){ // 서버에 이미 올려진 이미지의 경우
                if(i==temp_file_path_arr.size()-1){
                    img_name_arr = img_name_arr +temp_file_path_arr.get(i);
                    encode_img_arr = encode_img_arr + "no";
                }else{
                    img_name_arr = img_name_arr +temp_file_path_arr.get(i) +"|";
                    encode_img_arr = encode_img_arr + "no|";
                }
            }else{ // 서버에 올려야 할 이미지
                Bitmap temp_bitmap=null;

                if("content".equals(temp_file_path_arr.get(i).substring(0,7))){ // uri
                    Uri temp_img_uri = Uri.parse(temp_file_path_arr.get(i));
                    temp_bitmap = resizeImg(getApplicationContext(),temp_img_uri,100);
                }else{ // file_path
                    File temp_file = compressFile(new File(temp_file_path_arr.get(i)));
                    temp_bitmap = BitmapFactory.decodeFile(temp_file.getAbsolutePath());
                }

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                temp_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

                // 3. bitmap to bitmap_str
                String img_name = "test"+login_member_no+String.valueOf(Calendar.getInstance().getTimeInMillis())+i;
                String temp_encode_img = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
                Log.d(TAG,i+"번째 이미지 파일 이름: "+img_name);
                Log.d(TAG,i+"번째 이미지 파일: "+temp_encode_img);
                if(i==temp_file_path_arr.size()-1){
                    img_name_arr = img_name_arr +img_name;
                    encode_img_arr = encode_img_arr + temp_encode_img;
                }else{
                    img_name_arr = img_name_arr +img_name +"|";
                    encode_img_arr = encode_img_arr + temp_encode_img +"|";
                }
            }



        }
        final String img_file_name_arr = img_name_arr;
        final String img_file_arr = encode_img_arr;
        Log.d(TAG,"img_file_name_arr: "+img_file_name_arr);
        Log.d(TAG,"img_file_arr: "+img_file_arr);

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                MYURL.URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) { // 응답이 성공
                        Log.d(TAG+"rw_check","응답 성공: "+response);
                        loadingEnd(); // 프로그래스바 로딩 종료
                        if("0".equals(response)){ // 게시글 작성 성공
                            Toast.makeText(getApplicationContext(),"리뷰가 수정되었습니다",Toast.LENGTH_LONG).show();
                            /**
                             * 화면 전환
                             */
                            Intent intent = new Intent(getApplicationContext(), ReviewListActivity.class);
                            intent.putExtra("tourist",tourist_json);
                            intent.putExtra("write_review","write_review");
                            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivity(intent);
                            finish();
                        }else{ // 게시글 작성 실패 또는 기타 에러
                            Toast.makeText(getApplicationContext(),response.toString(),Toast.LENGTH_LONG).show();
                            loadingEnd(); // 프로그래스바 로딩 종료
                        }
                    }
                },
                new Response.ErrorListener() { // 응답 실패
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG,"응답 에러: "+error.toString());
                        Toast.makeText(getApplicationContext(),error.toString(),Toast.LENGTH_SHORT).show();
                        loadingEnd(); // 프로그래스바 로딩 종료
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String,String>();
                Log.d(TAG,"인자 확인");
                Log.d(TAG,"리뷰번호: "+reviewItem.review_no);
                Log.d(TAG,"reviewItem.content_id: "+reviewItem.content_id);
                Log.d(TAG,"review_content: "+review_content);
                Log.d(TAG,"img_file_name_arr: "+img_file_name_arr);
                Log.d(TAG,"img_file_arr: "+img_file_arr);
                params.put("mode","update_review");
                params.put("review_no",Integer.toString(reviewItem.review_no));
                params.put("content_id",reviewItem.content_id);
                params.put("review_content",review_content);
                params.put("review_score",score);
                params.put("review_magnitude",magnitude);
                params.put("review_write_member_no",Integer.toString(login_member_no));
                params.put("img_file_name_arr",img_file_name_arr);
                params.put("img_file_arr",img_file_arr);
                return params;
            }
        };

        /**
         * requestQueue가 없으면 requestQueue 생성
         */
        if(AppHelper.requestQueue == null){ // requestQueue 가 없을 경우
            AppHelper.requestQueue = Volley.newRequestQueue(getApplicationContext()); // requestQueue 생성
        }

        AppHelper.requestQueue.add(stringRequest); // 요청을 requestQueue에 담음

    } // updateReviewRequest() 메소드



    /**
     * 앨범에서 이미지 가지고 오기
     */
    private void goToAlbum(){
        /**
         * 외부 저장소 읽기 권한 체크
         */
        int read_external_storage_PermissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        if(read_external_storage_PermissionCheck == PackageManager.PERMISSION_GRANTED){ // 외부 저장소 읽기 권한 있음

            /**
             * 갤러리에 접근
             */

            Intent intent = new Intent();
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
//            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
            startActivityForResult(Intent.createChooser(intent,"다중선택은 '포토'를 선택하세요"), PICK_FROM_ALBUM);

        }else{ // 외부저장소 읽기 권한 없음
            Toast.makeText(getApplicationContext(),"외부 저장소 읽기 권한 없음",Toast.LENGTH_SHORT).show();
            if(ActivityCompat.shouldShowRequestPermissionRationale(ReviewUpdateActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                Toast.makeText(getApplicationContext(),"외부 저장소 읽기 권한 설명이 필요함",Toast.LENGTH_SHORT).show();
            }else{
                ActivityCompat.requestPermissions(ReviewUpdateActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
            }
        }
    } // goToAlbum() 메소드

    /**
     * 카메라에서 이미지 가져오기
     */
    private void takePhoto() {

        if(temp_file_path_arr.size()>=5){
            Toast.makeText(getApplicationContext(),"사진은 5개까지 선택 가능합니다.",Toast.LENGTH_SHORT).show();
            return;
        }

        /**
         * 외부 저장소 쓰기 권한 체크
         */
        int write_external_storage_PermissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(write_external_storage_PermissionCheck != PackageManager.PERMISSION_GRANTED){ // 외부 저장소 쓰기 권한 없음
            Toast.makeText(getApplicationContext(),"외부 저장소 쓰기 권한 없음",Toast.LENGTH_SHORT).show();
            if(ActivityCompat.shouldShowRequestPermissionRationale(ReviewUpdateActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                Toast.makeText(getApplicationContext(),"외부 저장소 쓰기 권한 설명이 필요함",Toast.LENGTH_SHORT).show();
            }else{
                ActivityCompat.requestPermissions(ReviewUpdateActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            }
        }

        /**
         * 외부 저장소 읽기 권한 체크
         */
        int camera_PermissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
        if(camera_PermissionCheck == PackageManager.PERMISSION_GRANTED){ // 외부 저장소 읽기 권한 있음

            /**
             * 갤러리에 접근
             */
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            try {
                temp_file = createImageFile();
            } catch (IOException e) {
                Toast.makeText(this, "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                finish();
                e.printStackTrace();
            }
            if (temp_file != null) {
                /**
                 *  안드로이드 OS 누가 버전 이후부터는 file:// URI 의 노출을 금지로 FileUriExposedException 발생
                 *  Uri 를 FileProvider 도 감싸 주어야 합니다.
                 *  참고 자료 http://programmar.tistory.com/4 , http://programmar.tistory.com/5
                 */
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) { // 누가버전 이상인 경우
                    Uri photo_uri = FileProvider.getUriForFile(this,"com.example.realtrip.provider", temp_file);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photo_uri);
                    startActivityForResult(intent, PICK_FROM_CAMERA);
                } else {
                    Uri photo_uri = Uri.fromFile(temp_file);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photo_uri);
                    startActivityForResult(intent, PICK_FROM_CAMERA);
                }
            }

        }else{ // 외부저장소 읽기 권한 없음
            Toast.makeText(getApplicationContext(),"카메라 사용 권한 없음",Toast.LENGTH_SHORT).show();
            if(ActivityCompat.shouldShowRequestPermissionRationale(ReviewUpdateActivity.this,Manifest.permission.CAMERA)){
                Toast.makeText(getApplicationContext(),"카메라 사용 권한 설명이 필요함",Toast.LENGTH_SHORT).show();
            }else{
                ActivityCompat.requestPermissions(ReviewUpdateActivity.this,new String[]{Manifest.permission.CAMERA},1);
            }
        }

    } // takePhoto() 메소드

    /**
     * 임시 이미지 파일 생성 메소드
     * @return 파일
     */
    private File createImageFile() throws IOException{
        // 이미지 파일 이름 ( yeon_{시간}_ )
        String time_stamp = new SimpleDateFormat("HHmmss").format(new Date());
        String image_file_name = "realtrip_" + time_stamp + "_";

        // 이미지가 저장될 폴더 이름 ( yeon )
        File storage_dir = new File(Environment.getExternalStorageDirectory() + "/DCIM/realtrip/");
        if (!storage_dir.exists()) storage_dir.mkdirs();

        // 빈 파일 생성
        File image = File.createTempFile(image_file_name, ".jpg", storage_dir);
        Log.d(TAG, "createImageFile : " + image.getAbsolutePath());

        return image;
    } // createImageFile() 메소드

    /**
     * 이미지 압축
     */
    private Bitmap resizeImg(Context context, Uri uri, int resize){
        Bitmap resizeBitmap=null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options); // 1번

            int width = options.outWidth;
            int height = options.outHeight;
            int samplesize = 1;

            while (true) {//2번
                if (width / 2 < resize || height / 2 < resize)
                    break;
                width /= 2;
                height /= 2;
                samplesize *= 2;
            }

            options.inSampleSize = samplesize;
            Bitmap bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options); //3번
            resizeBitmap=bitmap;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return resizeBitmap;
    }

    /**
     * 이미지 압축
     */
    private File compressFile(File file){
        try {

            int required_size=100;

            // 새로운 이미지 파일을 생성
            File new_file = createImageFile();

            /**
             * 압축 할 비트맵
             */
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(file), null, o);

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while(o.outWidth / scale / 2 >= required_size && o.outHeight / scale / 2 >= required_size) {
                scale *= 2;
            }


            /**
             * 압축한 비트맵
             */
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;

            Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file), null, o2);

            // 비트맵을 파일에 저장
            OutputStream outputStream = new FileOutputStream(new_file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
            Log.d(TAG,"압축 후 파일 크기: "+new_file.length()+"byte");
            return new_file;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 로딩 메소드
     */
    public void loading() {
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        progressDialog = new ProgressDialog(ReviewUpdateActivity.this);
                        progressDialog.setIndeterminate(true);
                        progressDialog.setMessage("잠시만 기다려 주세요");
                        progressDialog.show();
                    }
                }, 0);
    }

    /**
     * 로딩종료 메소드
     */
    public void loadingEnd() {
        new android.os.Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                    }
                }, 0);
    }

    /**
     * 드래그를 시작하는 이벤트를 만드는 메소드
     */
    @Override
    public void onStartDrag(RegisterImgAdapter.ViewHolder holder) {
        itemTouchHelper.startDrag(holder);
    } // onStartDrag() 메소드

} // ReviewUpdateActivity 클래스
