package com.example.realtrip.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.icu.text.SimpleDateFormat;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.realtrip.AppHelper;
import com.example.realtrip.BuildConfig;
import com.example.realtrip.FileLib;
import com.example.realtrip.FileUploadUtils;
import com.example.realtrip.MYURL;
import com.example.realtrip.R;
import com.example.realtrip.object.Member;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.view.View.GONE;

/**
 * UpdateProfileActivity 클래스
 * - 프로필(프로필사진, 닉네임)을 수정할 수 있는 클래스
 */
public class UpdateProfileActivity extends AppCompatActivity {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    private static final int PICK_FROM_ALBUM=0;
    private static final int CROP_FROM_ALBUM=1;
    private static final int PICK_FROM_CAMERA=2;
    private static final int CROP_FROM_CAMERA=3;

    File temp_file; // 이미지 파일을 위한 임시 파일

    EditText my_nickname_et; // 닉네임 입력 창
    ImageView my_profile_img_iv; // 프로필 사진 이미지뷰

    Member login_member; // 로그인 멤버

    ProgressBar progressBar; //프로필 사진 다운로드 기다리는 프로그래스 바

    @Override
    protected void onCreate(Bundle savedInstanceState) { // onCreate() 메소드
        Log.d(TAG,"onCreate() 호출");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

//        profile_icon_file = new File(FileLib.getInstance().getFileDir(getApplicationContext()),"temp.png");

        /**
         * 로그인한 멤버 정보 가지고 오기
         */
        SharedPreferences sharedPreferences = getSharedPreferences("myAppData",MODE_PRIVATE);
        final Gson gson = new Gson();
        login_member = gson.fromJson(sharedPreferences.getString("login_member","no_login"),Member.class);
        Log.d(TAG,"login_member.member_no: "+login_member.member_no);
        Log.d(TAG,"login_member.member_email: "+login_member.member_email);
        Log.d(TAG,"login_member.member_nickname: "+login_member.member_nickname);
        Log.d(TAG,"login_member.member_profile_img: "+login_member.member_profile_img);

        my_nickname_et = findViewById(R.id.my_nickname_et); // 닉네임 입력 창
        my_nickname_et.setText(login_member.member_nickname); // 로그인 멤버 닉네임 적용

        my_profile_img_iv = findViewById(R.id.my_profile_img_iv); // 프로필 사진 이미지뷰.

        /**
         * 프로필 사진 동그라미로 만드는 코드
         */
//        my_profile_img_iv.setBackground(new ShapeDrawable(new OvalShape()));
//        my_profile_img_iv.setClipToOutline(true);

        /**
         * 멤버 프로필 사진 적용
         */
        progressBar = findViewById(R.id.progress);
        if("default".equals(login_member.member_profile_img)){ // 로그인 멤버가 기본 프로필 사진인 경우
            progressBar.setVisibility(GONE);
            my_profile_img_iv.setVisibility(View.VISIBLE);
            my_profile_img_iv.setImageResource(R.drawable.default_profile);
        }else{ // 기본 프로필 사진이 아닌 경우
            Glide.with(this)
                    .load("http://35.224.156.8/uploads/"+login_member.member_profile_img)
                    .thumbnail(0.1f)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            progressBar.setVisibility(GONE);
                            my_profile_img_iv.setVisibility(View.VISIBLE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            progressBar.setVisibility(GONE);
                            my_profile_img_iv.setVisibility(View.VISIBLE);
                            return false;
                        }
                    })
                    .into(my_profile_img_iv);
        }


        /**
         * 앨범에서 사진 선택
         */
        Button pick_from_albam_btn = findViewById(R.id.pick_from_albam_btn);
        pick_from_albam_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToAlbum();
            }
        });

        /**
         * 사진 찍기 선택
         */
        Button pick_from_camera_btn = findViewById(R.id.pick_from_camera_btn);
        pick_from_camera_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto();
            }
        });


        /**
         * 기본 프로필 설정
         */
        Button apply_default_profile_img_btn = findViewById(R.id.apply_default_profile_img_btn); // 기본프로필 적용 텍스트뷰
        apply_default_profile_img_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 기본프로필 적용 텍스트뷰 클릭시 이벤트
                my_profile_img_iv.setImageResource(R.drawable.default_profile); // 기본 이미지로 변경
                // 디비의 로그인한 멤버 프로필 사진을 바꾸어주어야한다
                defaultProfileRequest();
                // 쉐어드에도 바꾸어주어야함
            }
        });

        /**
         * 확인 버튼 : 프로필 닉네임 수정버튼?
         */
        Button ok_btn = findViewById(R.id.ok_btn); // 확인 버튼
        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 확인 버튼 클릭시 이벤트
                Log.d(TAG,"확인 버튼 클릭");

                String nickname = my_nickname_et.getText().toString();

                /**
                 * 닉네임 수정 조건
                 * 1. 입력되어있는 닉네임과 원래 닉네임이 달라야함
                 * 2. 닉네임이 입력되어 있어야함
                 * 3. 닉네임 조건에 맞아야함
                 */
                if(nickname.equals(login_member.member_nickname)){ // 닉네임을 수정하지 않음
                    Log.d(TAG,"닉네임이 그대로임");
                    Intent intent = new Intent(getApplicationContext(),MyPageActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    return;
                }

                if(nickname.length()==0){ // 입력된 닉네임이 없음
                    Log.d(TAG,"입력된 닉네임이 없음");
                    Toast.makeText(getApplicationContext(),"닉네임을 입력해주세요",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(nickname.length()>8 || nickname.length()<3){
                    Log.d(TAG,"닉네임 조건이 맞지 않음");
                    Toast.makeText(getApplicationContext(),"닉네임은 3~8자로 입력해주세요",Toast.LENGTH_SHORT).show();
                    return;
                }

                // 닉네임 수정 요청
                updateNicknameRequest(nickname);
            }
        });

    } // onCreate() 메소드

    /**
     * 기본 프로필 적용 요청 메소드
     */
    private void defaultProfileRequest(){
        Log.d(TAG,"defaultProfileRequest() 호출");

        my_profile_img_iv.setVisibility(GONE);
        progressBar.setVisibility(View.VISIBLE);

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                MYURL.URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) { // 응답이 성공
                        Log.d(TAG,"응답 성공: "+response);
                        if("0".equals(response)){ // 기본 프로필 적용 성공
                            Toast.makeText(getApplicationContext(),"기본 프로필이 적용되었습니다",Toast.LENGTH_LONG).show();

                            my_profile_img_iv.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);

                            /**
                             * 쉐어드에 닉네임 저장
                             */
                            login_member.member_profile_img = "default";
                            Gson gson1 = new Gson();
                            String login_member_str = gson1.toJson(login_member);
                            SharedPreferences sharedPreferences1 = getSharedPreferences("myAppData",MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences1.edit();
                            editor.putString("login_member",login_member_str).commit();

                        }else{ // 기본 프로필 적용 실패
                            Toast.makeText(getApplicationContext(),response,Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() { // 응답 실패
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG,"응답 에러: "+error.toString());
                        Toast.makeText(getApplicationContext(),error.toString(),Toast.LENGTH_SHORT).show();
                    }
                }

        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String,String>();
                params.put("mode","apply_default_profile_img");
                params.put("email",login_member.member_email);
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
    } // defaultProfileRequest() 메소드

    /**
     * 닉네임 수정 요청 메소드
     */
    private void updateNicknameRequest(final String nickname){
        Log.d(TAG,"updatePassRequest() 호출");

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                MYURL.URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) { // 응답이 성공
                        Log.d(TAG,"응답 성공: "+response);
                        if("0".equals(response)){ // 닉네임 수정 성공
                            Toast.makeText(getApplicationContext(),"닉네임이 수정되었습니다",Toast.LENGTH_LONG).show();
                            /**
                             * 쉐어드에 닉네임 저장
                             */
                            login_member.member_nickname = nickname;
                            Gson gson1 = new Gson();
                            String login_member_str = gson1.toJson(login_member);
                            SharedPreferences sharedPreferences1 = getSharedPreferences("myAppData",MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences1.edit();
                            editor.putString("login_member",login_member_str).commit();

                            /**
                             * 화면 전환
                             */
                            Intent intent = new Intent(getApplicationContext(),MyPageActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivity(intent);
                            finish();
                        }else{ // 닉네임 수정 실패
                            Toast.makeText(getApplicationContext(),response,Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() { // 응답 실패
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG,"응답 에러: "+error.toString());
                        Toast.makeText(getApplicationContext(),error.toString(),Toast.LENGTH_SHORT).show();
                    }
                }

        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String,String>();
                params.put("mode","update_nickname");
                params.put("email",login_member.member_email);
                params.put("nickname",nickname);
                return params;
            }
        };

        /**
         * requestQueue가 없으면 requestQueue 생성
         */
        if(AppHelper.requestQueue == null){ // requestQueue 가 없을 경우
            AppHelper.requestQueue = Volley.newRequestQueue(getApplicationContext()); // requestQueue 생성
        }

//        stringRequest.setShouldCache(false); // 이건 무엇인지 알아보기
        AppHelper.requestQueue.add(stringRequest); // 요청을 requestQueue에 담음

    }

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
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
            startActivityForResult(intent,PICK_FROM_ALBUM);

        }else{ // 외부저장소 읽기 권한 없음
            Toast.makeText(getApplicationContext(),"외부 저장소 읽기 권한 없음",Toast.LENGTH_SHORT).show();
            if(ActivityCompat.shouldShowRequestPermissionRationale(UpdateProfileActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                Toast.makeText(getApplicationContext(),"외부 저장소 읽기 권한 설명이 필요함",Toast.LENGTH_SHORT).show();
            }else{
                ActivityCompat.requestPermissions(UpdateProfileActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
            }
        }
    } // goToAlbum() 메소드

    /**
     * 카메라에서 이미지 가져오기
     */
    private void takePhoto() {

        /**
         * 외부 저장소 쓰기 권한 체크
         */
        int write_external_storage_PermissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(write_external_storage_PermissionCheck != PackageManager.PERMISSION_GRANTED){ // 외부 저장소 쓰기 권한 없음
            Toast.makeText(getApplicationContext(),"외부 저장소 쓰기 권한 없음",Toast.LENGTH_SHORT).show();
            if(ActivityCompat.shouldShowRequestPermissionRationale(UpdateProfileActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                Toast.makeText(getApplicationContext(),"외부 저장소 쓰기 권한 설명이 필요함",Toast.LENGTH_SHORT).show();
            }else{
                ActivityCompat.requestPermissions(UpdateProfileActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
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
            if(ActivityCompat.shouldShowRequestPermissionRationale(UpdateProfileActivity.this,Manifest.permission.CAMERA)){
                Toast.makeText(getApplicationContext(),"카메라 사용 권한 설명이 필요함",Toast.LENGTH_SHORT).show();
            }else{
                ActivityCompat.requestPermissions(UpdateProfileActivity.this,new String[]{Manifest.permission.CAMERA},1);
            }
        }

    } // takePhoto() 메소드

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
            Uri profile_img_uri = data.getData(); // 이미지 Uri를 가지고 온다
            Log.d(TAG,"앨범 선택 uri: "+profile_img_uri.toString());

            // ~~~~~~~~~~~~~~~
            try{
                temp_file = new File(com.example.realtrip.FileUploadUtils.getInstance().getPath(this,profile_img_uri)); // uri로 file 생성
                ExifInterface exif = new ExifInterface(Uri.fromFile(temp_file).getPath());
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
                Log.d(TAG,"원래 orientation: "+orientation);
            }catch (URISyntaxException e){  // uri --> file 에서의 예외
                Log.d(TAG,e.toString());
            }catch (IOException e){
                Log.d(TAG,e.toString());
            }
            // ~~~~~~~~~~~~~~~

            Bitmap bitmap = null;
            try {
                // 빈 이미지 file 생성
                temp_file = createImageFile();

                // uri to bitmap
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), profile_img_uri);

                // bitmap to file
                OutputStream outputStream = new FileOutputStream(temp_file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.close();

            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            profileUpload();

        /**
         * 카메라에서 찍은 이미지
         */
        }else if(requestCode == PICK_FROM_CAMERA && data!=null){ // 카메라에서 찍은 사진
            profileUpload();
        /**
         * 앨범에서 가지고 온 사진을 크롭한 이미지
         */
        }else if(requestCode == CROP_FROM_ALBUM && data!=null){ // 앨범에서 선택한 사진인 경우
            Uri profile_img_uri = data.getData(); // 이미지 Uri를 가지고 온다
            Log.d(TAG,"앨범 선택 uri: "+profile_img_uri.toString());

        /**
         * 카메라에서 찍은 사진을 크롭한 이미지
         */
        }else if(requestCode == CROP_FROM_CAMERA && data!=null){ // 카메라에서 찍은 사진
            Uri profile_img_uri = data.getData(); // 이미지 Uri를 가지고 온다
            Log.d(TAG,"사진 찍고 uri: "+profile_img_uri.toString());
        }

    } // onActivityResult() 메소드

    /**
     * 1. 이미지 회전
     * 2. 이미지 압축
     * 3. 서버에 프로필사진 수정 요청
     */
    public void profileUpload(){

        /**
         * 이미지 회전
         */
        Bitmap bitmap = BitmapFactory.decodeFile(temp_file.getAbsolutePath());
        ExifInterface exif=null;
        try{
            exif = new ExifInterface(Uri.fromFile(temp_file).getPath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            Log.d(TAG,"원래 아니고 orientation: "+orientation);
            if(orientation!=0){
                // 이미지 회전
                Bitmap bitmap_rotated = rotateBitmap(bitmap,orientation);
                // 비트맵을 파일에 저장
                OutputStream outputStream = new FileOutputStream(temp_file);
                bitmap_rotated.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.close();
            }

        }catch (IOException e){e.printStackTrace();}


        /**
         * 이미지 압축
         */
        long temp_file_size = temp_file.length();

        Log.d(TAG,"파일 크기: "+temp_file_size+"byte");


        /**
         * 695850byte 가 내 눈으로 확인한 제일 큰 파일 업로드 가능 용량
         */
        if(temp_file_size>200000){
            temp_file = compressFile(temp_file);
        }

        Log.d(TAG,"setImage() 메소드 temp_file 절대경로: "+temp_file.getPath());

        FileUploadUtils.getInstance().profileImgUpload(temp_file, login_member.member_no, UpdateProfileActivity.this); // 서버에 프로필 사진 수정 요청

        /**
         *  tempFile 사용 후 null 처리를 해줘야 합니다.
         *  (resultCode != RESULT_OK) 일 때 tempFile 을 삭제하기 때문에
         *  기존에 데이터가 남아 있게 되면 원치 않은 삭제가 이뤄집니다.
         */
        temp_file=null;

    } // profileUpload() 메소드

    /**
     * orientation 값에 따른 이미지 회전 메소드
     */
    private Bitmap rotateBitmap(Bitmap bitmap,int orientation){
        Log.d(TAG,"rotateBitmap");
        Matrix matrix = new Matrix();
        switch(orientation){
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1,1);
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1,1);
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1,1);
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1,1);
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            case ExifInterface.ORIENTATION_UNDEFINED:
                matrix.setRotate(-90);
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            default:
                return bitmap;
        }
    }

    /**
     * createImageFile() 메소드
     * 폴더 및 파일 만들기
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
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

//    public void cropImage(Uri profile_img_uri) {
//        this.grantUriPermission("com.android.camera", profile_img_uri,
//                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        Intent intent = new Intent("com.android.camera.action.CROP");
//        intent.setDataAndType(profile_img_uri, "image/*");
//
//        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
//        grantUriPermission(list.get(0).activityInfo.packageName, profile_img_uri,
//                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        int size = list.size();
//        if (size == 0) {
//            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();
//            return;
//        } else {
//            Toast.makeText(this, "용량이 큰 사진의 경우 시간이 오래 걸릴 수 있습니다.", Toast.LENGTH_SHORT).show();
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//            intent.putExtra("crop", "true");
//            intent.putExtra("aspectX", 4);
//            intent.putExtra("aspectY", 3);
//            intent.putExtra("scale", true);
//            File croppedFileName = null;
//            try {
//                croppedFileName = createImageFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            File folder = new File(Environment.getExternalStorageDirectory() + "/test/");
//            File tempFile = new File(folder.toString(), croppedFileName.getName());
//
//            profile_img_uri = FileProvider.getUriForFile(UpdateProfileActivity.this,
//                    "com.example.realtrip.provider", tempFile);
//
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//
//
//            intent.putExtra("return-data", false);
//            intent.putExtra(MediaStore.EXTRA_OUTPUT, profile_img_uri);
//            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString()); //Bitmap 형태로 받기 위해 해당 작업 진행
//
//            Intent i = new Intent(intent);
//            ResolveInfo res = list.get(0);
//            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//            grantUriPermission(res.activityInfo.packageName, profile_img_uri,
//                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
//            i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
//            startActivityForResult(i, CROP_FROM_CAMERA);
//
//
//        }
//
//    } // cropImage() 메소드

//    // Android M에서는 Uri.fromFile 함수를 사용하였으나 7.0부터는 이 함수를 사용할 시 FileUriExposedException이
//    // 발생하므로 아래와 같이 함수를 작성합니다. 이전 포스트에 참고한 영문 사이트를 들어가시면 자세한 설명을 볼 수 있습니다.
//    private File createImageFile() throws IOException {
//        // Create an image file name
//        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
//        String imageFileName = "IP" + timeStamp + "_";
//        File storageDir = new File(Environment.getExternalStorageDirectory() + "/test/"); //test라는 경로에 이미지를 저장하기 위함
//        if (!storageDir.exists()) {
//            storageDir.mkdirs();
//        }
//        File image = File.createTempFile(
//                imageFileName,
//                ".jpg",
//                storageDir
//        );
//        return image;
//    }



//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        Log.d(TAG,"onActivityResult() 메소드   /   intent: "+data);
//
//        /**
//         * 예외사항 처리
//         */
//        if (resultCode != Activity.RESULT_OK) {
//            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();
//            if(temp_file != null) {
//                if (temp_file.exists()) {
//                    if (temp_file.delete()) {
//                        Log.e(TAG, temp_file.getAbsolutePath() + " 삭제 성공");
//                        temp_file = null;
//                    }
//                }
//            }
//            return;
//        }
//
//        if (requestCode == PICK_FROM_ALBUM && data!=null) { // 앨범에서 선택한 사진인 경우
//            Uri profile_img_uri = data.getData(); // 이미지 Uri를 가지고 온다
//            Log.d(TAG,"앨범 선택 uri: "+profile_img_uri.toString());
//            try{
//                File file = new File(com.example.realtrip.FileUploadUtils.getPath(this,profile_img_uri)); // uri로 file 생성
//                com.example.realtrip.FileUploadUtils.profileImgUpload(file, login_member.member_no, UpdateProfileActivity.this); // 서버에 이미지 파일 업로드
//                Log.d(TAG,"onActivityResult() : file.toString() : "+file.toString());
//            }catch (URISyntaxException e){  // uri --> file 에서의 예외
//                Log.d(TAG,e.toString());
//            }
//            my_profile_img_iv.setImageURI(profile_img_uri);
//
////            Cursor cursor = null;
////            try{
////                /**
////                 * Uri 스키마를
////                 * content:/// 에서 file:/// 로 변경한다
////                 *
////                 * 사진이 저장된 절대경로를 받아오는 과정
////                 *
////                 * temp_file 에 받아온 이미지를 저장
////                 */
////                String[] proj = {MediaStore.Images.Media.DATA};
////
////                assert profile_img_uri != null; // 이 조건이 false 이면 이 메시지와 함께 AssertionError가 발생
////                cursor = getContentResolver().query(profile_img_uri,proj,null,null,null);
////
////                assert cursor!=null; // 이 조건이 false 이면 이 메시지와 함께 AssertionError가 발생
////                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
////
////                cursor.moveToFirst();
////
////                temp_file = new File(cursor.getString(column_index));
////            }catch (Exception e){
////                e.printStackTrace();
////            }finally {
////                if(cursor!=null){
////                    cursor.close();
////                }
////            }
////            setImage();
//        }else if(requestCode == PICK_FROM_CAMERA && data!=null){ // 카메라에서 찍은 사진
//            Uri profile_img_uri = data.getData(); // 이미지 Uri를 가지고 온다
//            Log.d(TAG,"사진 찍고 uri: "+profile_img_uri.toString());
//            temp_file = new File(profile_img_uri.toString()); // uri --> file
//            com.example.realtrip.FileUploadUtils.profileImgUpload(temp_file, login_member.member_no, UpdateProfileActivity.this); // 서버에 이미지 파일 업로드
//            my_profile_img_iv.setImageURI(profile_img_uri);
//        }
//
//    } // onActivityResult() 메소드

//    public void setImage(){
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        Bitmap original_bitmap = BitmapFactory.decodeFile(temp_file.getAbsolutePath(),options);
//        my_profile_img_iv.setImageBitmap(original_bitmap);
//    }





    //    /**
//     * onActivityResult() 메소드
//     * - startActivityForResult() 메소드로 호출한 액티비티의 결과를 처리한다
//     * @param requestCode 액티비티를 실행하면서 전달한 요청 코드
//     * @param resultCode 실행한 액티비티가 설정한 결과코드
//     * @param data 결과 데이터
//     */
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) { // onActivityResult() 메소드
//        super.onActivityResult(requestCode, resultCode, data);
//        Log.d(TAG,"onActivityResult() 메소드   /   intent: "+data);
//
//        if (requestCode == RESULT_OK && requestCode == PICK_FROM_ALBUM && data!=null) { // 앨범에서 선택한 사진인 경우
//
//            Uri profile_img_uri = data.getData(); // 이미지 Uri를 가지고 온다
//            cropImageFromAlbum(profile_img_uri);
//            Log.d(TAG,"onActivityResult() : profileImageUri.toString() : "+profile_img_uri.toString());
//
////                my_profile_img_iv.setImageURI(profile_img_uri);
////
////                // 여기서 프로필 이미지를 서버에 올리고 해당 파일 uri를 로그인 멤버의 프로필 이미지에 저장해야함
////                try{
////                    File file = new File(com.example.realtrip.FileUploadUtils.getPath(this,profile_img_uri)); // uri로 file 생성
////                    com.example.realtrip.FileUploadUtils.profileImgUpload(file, login_member.member_no, UpdateProfileActivity.this); // 서버에 이미지 파일 업로드
////                    Log.d(TAG,"onActivityResult() : file : "+file);
////                    Log.d(TAG,"onActivityResult() : file.toString() : "+file.toString());
////                }catch (URISyntaxException e){  // uri --> file 에서의 예외
////                    Log.d(TAG,e.toString());
////                }
//        }else if(requestCode == CROP_FROM_ALBUM && data!=null){
//
//        }
//    } // onActivityResult() 메소드

//    /**
//     * cropImageFromAlbum() 메소드
//     * 카메라 앨범에서 선택한 이미지를 프로필 아이콘에 사용할 크기로 자른다.
//     */
//    private void cropImageFromAlbum(Uri input_uri){
//        Log.d(TAG,"startPickFromAlbum uri "+input_uri.toString());
//        Uri output_uri = Uri.fromFile(profile_icon_file);
//        Intent intent = getCropIntent(input_uri,output_uri);
//        startActivityForResult(intent,CROP_FROM_ALBUM);
//    }

//    /**
//     * getCropIntent() 메소드
//     * 이미지를 자르기 위한 Intent를 생성해서 반환한다.
//     * @param input_uri 이미지를 자르기전 Uri
//     * @param output_uri 이미지를 자른 결과 파일 Uri
//     * @return 이미지를 자르기 위한 인텐트
//     */
//    private Intent getCropIntent(Uri input_uri, Uri output_uri){
//        Intent intent = new Intent("com.android.camera.action.CROP");
//        intent.setDataAndType(input_uri,"image/*");
//        intent.putExtra("aspectX",1);
//        intent.putExtra("aspecty",1);
//        intent.putExtra("outputX",200);
//        intent.putExtra("outputY",200);
//        intent.putExtra("scale",true);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT,output_uri);
//        intent.putExtra("outputFormat",Bitmap.CompressFormat.PNG.toString());
//        return intent;
//    } // getCropIntent() 메소드

} // UpdateProfileActivity 클래스
