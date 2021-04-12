package com.example.realtrip;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.realtrip.object.Member;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;

public class FileUploadUtils {

    private volatile static FileUploadUtils instance;
    Handler handler;
    String response_str;
    String error;

    /**
     * 파일 업로드 객체를 얻는 메소드
     * @return FileUploadUtils 객체
     */
    public static FileUploadUtils getInstance(){
        if(instance==null){
            synchronized (FileUploadUtils.class){
                if(instance==null){
                    instance = new FileUploadUtils();

                }
            }
        }
        return instance;
    }



    /**
     * 파일 업로드 기본 예제
     * @param file 업로드 할 파일
     */
    public void send2Server(File file){
        Log.d("yeon[이미지 업로드]","send2Server() 메소드 호출");

        final RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("fileToUpload", file.getName(), RequestBody.create(MultipartBody.FORM, file))
                .build();
        Request request = new Request.Builder()
                .url("http://35.224.156.8/upload.php") // Server URL 은 본인 IP를 입력
                .post(requestBody)
                .build();
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d("yeon[이미지 업로드]","서버 연결 실패 : "+e.toString());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(response.isSuccessful()){
                    Log.d("yeon[이미지 업로드]","응답 성공 : "+response.body().string());

                }else{
                    Log.d("yeon[이미지 업로드]","응답 실패 : "+response.body().string());
                }
            }
        });
    } // 서버에 이미지 등록하는 메소드

    /**
     * 프로필 사진 업로드
     * @param file 프로필 이미지
     * @param login_member_no 로그인한 멤버 번호
     */
    public void profileImgUpload(final File file, int login_member_no, final Activity activity){
        Log.d("yeon[이미지 업로드]","send2Server() 메소드 호출");

        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                ProgressBar progressBar = activity.findViewById(R.id.progress);
                ImageView my_profile_img_iv = activity.findViewById(R.id.my_profile_img_iv);
                if(msg.what==-1){
                    progressBar.setVisibility(View.VISIBLE);
                    my_profile_img_iv.setVisibility(View.GONE);
                }else if(msg.what==0){
                    Toast.makeText(activity.getApplicationContext(),"프로필 사진 수정 완료",Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    my_profile_img_iv.setVisibility(View.VISIBLE);
                    my_profile_img_iv.setImageURI(Uri.fromFile(file));
                }else if(msg.what==1){
                    Toast.makeText(activity.getApplicationContext(),"프로필 사진 수정 실패 : "+response_str,Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    my_profile_img_iv.setVisibility(View.VISIBLE);
                }else if(msg.what==2){
                    Toast.makeText(activity.getApplicationContext(),"프로필 사진 수정 실패 : "+response_str,Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    my_profile_img_iv.setVisibility(View.VISIBLE);
                }
            }
        };

        new Thread(){
            public void run(){
                handler.sendEmptyMessage(-1);
            }
        }.start();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("fileToUpload", file.getName(), RequestBody.create(MultipartBody.FORM, file))
                .addFormDataPart("mode","update_profile_img")
                .addFormDataPart("login_member_no",Integer.toString(login_member_no))
                .build();
        Request request = new Request.Builder()
                .url("http://35.224.156.8/query.php") // Server URL 은 본인 IP를 입력
                .post(requestBody)
                .build();
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d("yeon[이미지 업로드]","서버 연결 실패 : "+e.toString());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(response.isSuccessful()){
                    Log.d("yeon[이미지 업로드]","응답 성공 : "+response.body());

                    try{
                        response_str = response.body().string();
                        Log.d("yeon[이미지 업로드]","response_str : "+response_str);
                        String[] response_arr = response_str.split("/");
                        if(response_arr.length==2){
                            Log.d("yeon[이미지 업로드]","response_arr[0] : "+response_arr[0]);
                            Log.d("yeon[이미지 업로드]","response_arr[1] : "+response_arr[1]);
                            if("0".equals(response_arr[0])){ // 프로필 사진 수정 성공
                                Log.d("yeon[이미지 업로드]","프로필 사진 수정 성공");

                                new Thread(){
                                    public void run(){
                                        handler.sendEmptyMessage(0);
                                    }
                                }.start();

                                /**
                                 * 쉐어드에 멤버 동기화
                                 */
                                SharedPreferences sharedPreferences = activity.getApplicationContext().getSharedPreferences("myAppData",MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                Gson gson = new Gson();
                                Member login_member = gson.fromJson(sharedPreferences.getString("login_member","no_login"), Member.class);
                                login_member.member_profile_img = response_arr[1];
                                String login_member_json = gson.toJson(login_member);
                                editor.putString("login_member",login_member_json).commit();
                            }else if("-1".equals(response_arr[0])){ // 프로필 사진 수정 실패

                                Log.d("yeon[이미지 업로드]","프로필 사진 수정 실패 error: "+response_arr[1]);
                                response_str = response_arr[1];
                                new Thread(){
                                    public void run(){
                                        handler.sendEmptyMessage(2);
                                    }
                                }.start();
                            }
                        }else{
                            Log.d("yeon[이미지 업로드]","프로필 사진 수정 실패 error: "+response_str);
                            new Thread(){
                                public void run(){
                                    handler.sendEmptyMessage(1);
                                }
                            }.start();
                        }
                    }catch (Exception e){
                        Log.d("yeon[이미지 업로드]","response.body().toString() 에러 : "+e.toString());
                    }


                }else{
                    Log.d("yeon[이미지 업로드]","응답 실패 : "+response.body().string());
                }
            }
        });
    } // 서버에 이미지 등록하는 메소드


    /*
     * uri --> file 메소드
     */
    @SuppressLint("NewApi")
    public String getPath(Context context, Uri uri) throws URISyntaxException {
        final boolean needToCheckUri = Build.VERSION.SDK_INT >= 19;
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        // deal with different Uris.
        if (needToCheckUri && DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{ split[1] };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { MediaStore.Images.Media.DATA };
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * uri --> file 메소드 끝
     */

} // 클래스
