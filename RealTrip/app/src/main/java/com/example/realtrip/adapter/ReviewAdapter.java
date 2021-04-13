package com.example.realtrip.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.realtrip.AppHelper;
import com.example.realtrip.MYURL;
import com.example.realtrip.R;
import com.example.realtrip.activity.ReviewListActivity;
import com.example.realtrip.activity.ReviewUpdateActivity;
import com.example.realtrip.activity.ReviewWriteActivity;
import com.example.realtrip.activity.ShowReviewImgActivity;
import com.example.realtrip.item.ReviewItem;
import com.example.realtrip.object.Member;
import com.example.realtrip.object.Review;
import com.example.realtrip.object.Tourist;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.view.View.GONE;

/**
 * ReviewAdapter 클래스
 */
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    ArrayList<ReviewItem> reviewItems; // 리뷰 리스트
    Activity activity;
    Context context;

    /**
     * 생성자
     */
    public ReviewAdapter(ArrayList<ReviewItem> reviewItems, Activity activity){
        this.reviewItems = reviewItems;
        this.activity = activity;
        this.context = activity.getApplicationContext();
    }

    /**
     * onCreateViewHolder() 메소드
     */
    @NonNull
    @Override
    public ReviewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater  = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.recyclerview_review_item,parent,false); // 뷰 객체 생성
        ReviewAdapter.ViewHolder viewHolder = new ReviewAdapter.ViewHolder(view);
        return viewHolder;
    } // onCreateViewHolder() 메소드

    /**
     * onBindViewHolder() 메소드
     */
    @Override
    public void onBindViewHolder(@NonNull ReviewAdapter.ViewHolder holder, int position) {

        if(holder.getAdapterPosition()!=RecyclerView.NO_POSITION) { // 포지션 에러를 최소화 하기 위한 조건

            ReviewItem review_item = reviewItems.get(holder.getAdapterPosition());
            holder.review_write_time_tv.setText(review_item.review_write_time);
            holder.review_content_tv.setText(review_item.review_content);

            holder.review_write_member_nickname_tv.setText(review_item.member_nickname);

            if("default".equals(review_item.member_profile_img)){ // 멤버의 프로필 이미지가 기본인 경우
                holder.review_write_member_profile_img_iv.setImageResource(R.drawable.default_profile);
            }else{ // 기본 프로필 이미지가 아닌 경우
                Glide.with(context)
                        .load("http://35.224.156.8/uploads/"+review_item.member_profile_img)
                        .thumbnail(0.1f)
                        .placeholder(R.drawable.loading)
                        .into(holder.review_write_member_profile_img_iv);
            }

            /**
             * 로그인멤버와 작성자가 같으면 수정/삭제 보이게 하기
             */
            SharedPreferences sharedPreferences = context.getSharedPreferences("myAppData",0);
            Gson gson = new Gson();
            Member login_member = gson.fromJson(sharedPreferences.getString("login_member","no_login"),Member.class);
            if(login_member.member_no!=review_item.review_write_member_no){
                holder.review_remove_tv.setVisibility(GONE);
                holder.review_edit_tv.setVisibility(GONE);
            }

            /**
             * 감정분석
             * magnitude<=0.3 || 0<=score<=0.1   ==>> 중립
             * 0.1<score<=0.6   ==>> 긍정
             * 0.6<score<=1   ==>> 강한 긍정
             * -0.6<=score<0   ==>> 부정
             * -1<=score<0.6   ==>> 강한 부정
             */
            double score = Double.parseDouble(review_item.review_score);
            double magnitude = Double.parseDouble(review_item.review_magnitude);
            if(magnitude<=0.3 || (0<=score && score<=0.1)){ // 중립
                holder.review_sentiment_tv.setText("보    통");
            }else if((0.1<score && score<=0.6)){ // 긍정
                holder.review_sentiment_tv.setText("긍    정");
            }else if((0.6<score && score<=1)){ // 강한 긍정
                holder.review_sentiment_tv.setText("강한긍정");
            }else if((-0.6<=score && score<0)){ // 부정
                holder.review_sentiment_tv.setText("부    정");
            }else if((-1<=score && score<0.6)){ // 강한 부정
                holder.review_sentiment_tv.setText("강한부정");
            }

            /**
             * 이미지 적용
             */
            Log.d(TAG,"이미지 적용");
            Log.d(TAG,"review_item.review_img: "+review_item.review_img);
            if(review_item.review_img==null || "".equals(review_item.review_img.trim())){ // 이미지를 등록하지 않은 경우
                Log.d(TAG,"이미지 등록 안함");
                holder.review_img_ll.setVisibility(GONE);
            }else{ // 이미지를 등록한 경우
                Log.d(TAG,"이미지 등록 함");
                String[] review_img_arr = review_item.review_img.split("\\|");
                Log.d(TAG,"review_img_arr.length: "+review_img_arr.length);
                switch(review_img_arr.length){
                    case 1:
                        holder.review_img_2_iv.setVisibility(GONE);
                        holder.review_img_3_iv.setVisibility(GONE);
                        holder.review_img_4_iv.setVisibility(GONE);
                        holder.review_img_5_iv.setVisibility(GONE);
                        Glide.with(context)
                                .load("http://35.224.156.8/uploads/"+review_img_arr[0])
                                .thumbnail(0.1f)
                                .placeholder(R.drawable.loading)
                                .into(holder.review_img_1_iv);
                        break;
                    case 2:
                        holder.review_img_3_iv.setVisibility(GONE);
                        holder.review_img_4_iv.setVisibility(GONE);
                        holder.review_img_5_iv.setVisibility(GONE);
                        Glide.with(context)
                                .load("http://35.224.156.8/uploads/"+review_img_arr[0])
                                .thumbnail(0.1f)
                                .placeholder(R.drawable.loading)
                                .into(holder.review_img_1_iv);
                        Glide.with(context)
                                .load("http://35.224.156.8/uploads/"+review_img_arr[1])
                                .thumbnail(0.1f)
                                .placeholder(R.drawable.loading)
                                .into(holder.review_img_2_iv);
                        break;
                    case 3:
                        holder.review_img_4_iv.setVisibility(GONE);
                        holder.review_img_5_iv.setVisibility(GONE);
                        Glide.with(context)
                                .load("http://35.224.156.8/uploads/"+review_img_arr[0])
                                .thumbnail(0.1f)
                                .placeholder(R.drawable.loading)
                                .into(holder.review_img_1_iv);
                        Glide.with(context)
                                .load("http://35.224.156.8/uploads/"+review_img_arr[1])
                                .thumbnail(0.1f)
                                .placeholder(R.drawable.loading)
                                .into(holder.review_img_2_iv);
                        Glide.with(context)
                                .load("http://35.224.156.8/uploads/"+review_img_arr[2])
                                .thumbnail(0.1f)
                                .placeholder(R.drawable.loading)
                                .into(holder.review_img_3_iv);
                        break;
                    case 4:
                        holder.review_img_5_iv.setVisibility(GONE);
                        Glide.with(context)
                                .load("http://35.224.156.8/uploads/"+review_img_arr[0])
                                .thumbnail(0.1f)
                                .placeholder(R.drawable.loading)
                                .into(holder.review_img_1_iv);
                        Glide.with(context)
                                .load("http://35.224.156.8/uploads/"+review_img_arr[1])
                                .thumbnail(0.1f)
                                .placeholder(R.drawable.loading)
                                .into(holder.review_img_2_iv);
                        Glide.with(context)
                                .load("http://35.224.156.8/uploads/"+review_img_arr[2])
                                .thumbnail(0.1f)
                                .placeholder(R.drawable.loading)
                                .into(holder.review_img_3_iv);
                        Glide.with(context)
                                .load("http://35.224.156.8/uploads/"+review_img_arr[3])
                                .thumbnail(0.1f)
                                .placeholder(R.drawable.loading)
                                .into(holder.review_img_4_iv);
                        break;
                    case 5:
                        Glide.with(context)
                                .load("http://35.224.156.8/uploads/"+review_img_arr[0])
                                .thumbnail(0.1f)
                                .placeholder(R.drawable.loading)
                                .into(holder.review_img_1_iv);
                        Glide.with(context)
                                .load("http://35.224.156.8/uploads/"+review_img_arr[1])
                                .thumbnail(0.1f)
                                .placeholder(R.drawable.loading)
                                .into(holder.review_img_2_iv);
                        Glide.with(context)
                                .load("http://35.224.156.8/uploads/"+review_img_arr[2])
                                .thumbnail(0.1f)
                                .placeholder(R.drawable.loading)
                                .into(holder.review_img_3_iv);
                        Glide.with(context)
                                .load("http://35.224.156.8/uploads/"+review_img_arr[3])
                                .thumbnail(0.1f)
                                .placeholder(R.drawable.loading)
                                .into(holder.review_img_4_iv);
                        Glide.with(context)
                                .load("http://35.224.156.8/uploads/"+review_img_arr[4])
                                .thumbnail(0.1f)
                                .placeholder(R.drawable.loading)
                                .into(holder.review_img_5_iv);
                        break;
                }
            }

            // 이렇게 하니깐 시간이 오래 걸린다
//            getMemberInfo(review_item.review_write_member_no,holder.review_write_member_profile_img_iv,holder.review_write_member_nickname_tv);

        }

    } // onBindViewHolder() 메소드

    /**
     * getItemCount() 메소드
     */
    @Override
    public int getItemCount() {
        return reviewItems.size();
    } // getItemCount() 메소드

    /**
     * ViewHolder 클래스
     */
    public class ViewHolder extends RecyclerView.ViewHolder{

        View item_view;
        ImageView review_write_member_profile_img_iv;
        TextView review_write_member_nickname_tv;
        TextView review_write_time_tv;
        TextView review_content_tv;
        TextView review_sentiment_tv;
        LinearLayout review_img_ll;
        ImageView review_img_1_iv;
        ImageView review_img_2_iv;
        ImageView review_img_3_iv;
        ImageView review_img_4_iv;
        ImageView review_img_5_iv;
        TextView review_remove_tv;
        TextView review_edit_tv;

        ProgressDialog progressDialog;

        /**
         * ViewHolder 생성자
         */
        ViewHolder(View item_view){
            super(item_view);

            this.item_view = item_view;
            this.review_write_member_profile_img_iv = item_view.findViewById(R.id.review_write_member_profile_img_iv);
            this.review_write_member_nickname_tv = item_view.findViewById(R.id.review_write_member_nickname_tv);
            this.review_write_time_tv = item_view.findViewById(R.id.review_write_time_tv);
            this.review_content_tv = item_view.findViewById(R.id.review_content_tv);
            this.review_sentiment_tv = item_view.findViewById(R.id.review_sentiment_tv);
            this.review_img_ll = item_view.findViewById(R.id.review_img_ll);
            this.review_img_1_iv = item_view.findViewById(R.id.review_img_1_iv);
            this.review_img_2_iv = item_view.findViewById(R.id.review_img_2_iv);
            this.review_img_3_iv = item_view.findViewById(R.id.review_img_3_iv);
            this.review_img_4_iv = item_view.findViewById(R.id.review_img_4_iv);
            this.review_img_5_iv = item_view.findViewById(R.id.review_img_5_iv);
            this.review_remove_tv = item_view.findViewById(R.id.review_remove_tv);
            this.review_edit_tv = item_view.findViewById(R.id.review_edit_tv);

            /**
             * 이미지 레이아웃 클릭시 이벤트
             */
            review_img_ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(getAdapterPosition()!=RecyclerView.NO_POSITION) {
                        Log.d(TAG, "이미지 클릭");
                        Intent intent = new Intent(context, ShowReviewImgActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        intent.putExtra("img_arr", reviewItems.get(getAdapterPosition()).review_img);
                        activity.startActivity(intent);
                    }
                }
            });

            /**
             * 삭제버튼 클릭시
             */
            review_remove_tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(getAdapterPosition()!=RecyclerView.NO_POSITION){
                        Log.d(TAG, getAdapterPosition()+"번째 리뷰 삭제 클릭");
                        Log.d(TAG, "리뷰 번호: "+reviewItems.get(getAdapterPosition()).review_no);

                        /**
                         * 서버에 삭제 요청 보내기
                         */
                        reviewRemoveRequest(reviewItems.get(getAdapterPosition()).review_no, getAdapterPosition());

                        loading();

                    }
                }
            });

            /**
             * 수정버튼 클릭시
             */
            review_edit_tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(getAdapterPosition()!=RecyclerView.NO_POSITION){
                        Log.d(TAG, getAdapterPosition()+"번째 리뷰 수정 클릭");
                        /**
                         * 수정화면으로 화면 전환
                         */
                        Intent intent = new Intent(context, ReviewUpdateActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        Gson gson = new Gson();
                        intent.putExtra("tourist",gson.toJson(new Tourist(reviewItems.get(getAdapterPosition()).content_id)));
                        intent.putExtra("reviewItem",gson.toJson(reviewItems.get(getAdapterPosition())));
                        activity.startActivity(intent);
                    }
                }
            });

        } // ViewHolder 생성자

        /**
         * 리뷰 삭제 요청
         * @param review_no 리뷰 번호
         */
        public void reviewRemoveRequest(final int review_no, final int position){
            StringRequest stringRequest = new StringRequest(
                    Request.Method.POST,
                    MYURL.URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) { // 응답이 성공
                            Log.d(TAG+"rw_check","응답 성공: "+response);
                            if("0".equals(response)){ // 게시글 삭제 성공
                                loadingEnd();
                                Toast.makeText(context,"리뷰가 삭제되었습니다",Toast.LENGTH_LONG).show();
                                reviewItems.remove(position);
                                notifyItemRemoved(position);

                            }else{ // 게시글 작성 실패 또는 기타 에러
                                loadingEnd();
                                Toast.makeText(context,response.toString(),Toast.LENGTH_LONG).show();
                            }
                        }
                    },
                    new Response.ErrorListener() { // 응답 실패
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            loadingEnd();
                            Log.d(TAG,"응답 에러: "+error.toString());
                            Toast.makeText(context,error.toString(),Toast.LENGTH_SHORT).show();
                        }
                    }
            ){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> params = new HashMap<String,String>();
                    params.put("mode","remove_review");
                    params.put("review_no",String.valueOf(review_no));
                    return params;
                }
            };

            /**
             * requestQueue가 없으면 requestQueue 생성
             */
            if(AppHelper.requestQueue == null){ // requestQueue 가 없을 경우
                AppHelper.requestQueue = Volley.newRequestQueue(context); // requestQueue 생성
            }

            AppHelper.requestQueue.add(stringRequest); // 요청을 requestQueue에 담음
        } // reviewRemoveRequest() 메소드

        /**
         * 로딩 메소드
         */
        public void loading() {
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            progressDialog = new ProgressDialog(activity);
                            progressDialog.setIndeterminate(true);
                            progressDialog.setMessage("삭제중..");
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

    } // ViewHolder 클래스

} // ReviewAdapter 클래스
