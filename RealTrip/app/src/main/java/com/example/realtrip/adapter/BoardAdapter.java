package com.example.realtrip.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.realtrip.AppHelper;
import com.example.realtrip.activity.BoardDetailActivity;
import com.example.realtrip.R;
import com.example.realtrip.MYURL;
import com.example.realtrip.object.Board;
import com.example.realtrip.object.Member;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * BoardAdapter 클래스
 * - 게시글 리싸이클러뷰를 위한 어댑터
 */
public class BoardAdapter extends RecyclerView.Adapter<BoardAdapter.ViewHolder> {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    ArrayList<Board> boards; // 게시글 리스트
    Activity activity;
    Context context;


    /**
     * BoardAdapter 생성자
     */
    public BoardAdapter(ArrayList<Board> boards, Activity activity){
        this.boards = boards;
        this.activity = activity;
        this.context = activity.getApplicationContext();
    } // BoardAdapter 생성자

    /**
     * onCreateViewHolder() 메소드 :
     * @param parent
     * @param viewType
     * @return 뷰홀더 반환
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { // onCreateViewHolder() 메소드
        /**
         * inflater 구현
         * inflater로 아이템 뷰를 객체로 만든다
         */
        LayoutInflater inflater  = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.recyclerview_board_item,parent,false); // 뷰 객체 생성
        BoardAdapter.ViewHolder viewHolder = new BoardAdapter.ViewHolder(view);
        return viewHolder;
    } // onCreateViewHolder() 메소드

    /**
     * onBindViewHolder() 메소드 : item.xml 에 값을 연결
     * @param holder ViewHolder 객체
     * @param position 아이템 포지션 (근데 이거 잘 안맞음. holder에서 getPosition해서 사용하는게 더 좋은거같음)
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) { // onBindViewHolder() 메소드
        if(holder.getAdapterPosition()!=RecyclerView.NO_POSITION){ // 포지션 에러를 최소화 하기 위한 조건
            Board temp_board = boards.get(holder.getAdapterPosition());
            int board_write_member_no = temp_board.board_write_member_no;
            holder.board_content_tv.setText(temp_board.board_content);
            holder.board_write_time_tv.setText(temp_board.board_write_time);

            getMemberInfo(board_write_member_no, holder.board_write_member_profile_img_iv, holder.board_write_member_nickname_tv);
        }
    } // onBindViewHolder() 메소드

    /**
     * getItemCount() 메소드
     * @return 아이템 갯수 반환
     */
    @Override
    public int getItemCount() { // getItemCount() 메소드
        return boards.size();
    } // getItemCount() 메소드

    /**
     * ViewHolder 클래스
     */
    public class ViewHolder extends RecyclerView.ViewHolder{

        Context context; // 컨텍스트
        ImageView board_img; // 게시글 사진 이미지뷰
        ImageView board_write_member_profile_img_iv; // 게시글 작성자 프로필 사진 이미지뷰
        TextView board_write_member_nickname_tv; // 게시글 작성자 닉네임 텍스트뷰
        TextView board_content_tv; // 게시글 내용 텍스트뷰
        TextView board_write_time_tv; // 게시글 작성 시간 텍스트뷰

        /**
         * ViewHolder 생성자
         * @param item_view 아이템 뷰
         */
        ViewHolder(View item_view){
            super(item_view);
            this.context = item_view.getContext();
            this.board_img = item_view.findViewById(R.id.board_img); // 게시글 사진 이미지뷰
            this.board_write_member_profile_img_iv = item_view.findViewById(R.id.board_write_member_profile_img_iv); // 게시글 작성자 프로필 사진 이미지뷰
            this.board_write_member_nickname_tv = item_view.findViewById(R.id.board_write_member_nickname_tv); // 게시글 작성자 닉네임 텍스트뷰
            this.board_content_tv = item_view.findViewById(R.id.board_content_tv); // 게시글 내용 텍스트뷰
            this.board_write_time_tv = item_view.findViewById(R.id.board_write_time_tv); // 게시글 작성 시간 텍스트뷰

            /**
             * 작성자 프로필 사진 동그라미로 만드는 코드
             */
            board_write_member_profile_img_iv.setBackground(new ShapeDrawable(new OvalShape()));
            board_write_member_profile_img_iv.setClipToOutline(true);

            /**
             * 아이템 클릭시 게시글 세부로 이동
             */
            item_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, BoardDetailActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    context.startActivity(intent);
                }
            });

        } // ViewHolder 생성자

    } // ViewHolder 클래스

    /**
     * getMemberInfo() 메소드
     */
    public void getMemberInfo(int board_write_member_no, final ImageView board_write_member_profile_img_iv, final TextView board_write_member_nickname_tv){
        Log.d(TAG,"getMemberInfo() 호출");

        // Post 인자
        Map<String,String> params = new HashMap<String,String>();
        params.put("mode","find_member");
        params.put("member_no",Integer.toString(board_write_member_no));

        JSONObject jsonObject = new JSONObject(params); // Map을 json으로 만듬

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, MYURL.URL,jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) { // 응답 성공
                        Log.d(TAG,"응답 성공: "+response.toString());
                        Gson gson = new Gson();
                        Member member = gson.fromJson(response.toString(),Member.class);
                        if("default".equals(member.member_profile_img)){ // 멤버의 프로필 이미지가 기본인 경우
                            board_write_member_profile_img_iv.setImageResource(R.drawable.default_profile);
                        }else{ // 기본 프로필 이미지가 아닌 경우
                            Glide.with(context)
                                    .load("http://35.224.156.8/uploads/"+member.member_profile_img)
                                    .thumbnail(0.1f)
                                    .listener(new RequestListener<String, GlideDrawable>() {
                                        @Override
                                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
//                            progressBar.setVisibility(GONE);
                                            board_write_member_profile_img_iv.setVisibility(View.VISIBLE);
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
//                            progressBar.setVisibility(GONE);
                                            board_write_member_profile_img_iv.setVisibility(View.VISIBLE);
                                            return false;
                                        }
                                    })
                                    .into(board_write_member_profile_img_iv);
                        }
                        board_write_member_nickname_tv.setText(member.member_nickname);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) { // 응답 실패
                Log.d(TAG,"응답 실패: "+error.toString());

            }
        }
        );

        AppHelper.requestQueue.add(jsonObjectRequest); // 요청 큐에 위 요청 추가

    } // getMemberInfo() 메소드

} // BoardAdapter 클래스
