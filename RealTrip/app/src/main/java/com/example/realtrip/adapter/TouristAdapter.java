package com.example.realtrip.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.realtrip.R;
import com.example.realtrip.activity.TouristDetailActivity;
import com.example.realtrip.object.Tourist;
import com.google.gson.Gson;

import java.util.ArrayList;

public class TouristAdapter extends RecyclerView.Adapter<TouristAdapter.ViewHolder> {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    ArrayList<Tourist> tourists;
    Activity activity;
    Context context;

    /**
     * 생성자
     */
    public TouristAdapter(ArrayList<Tourist> tourists,Activity activity){
        this.tourists = tourists;
        this.activity = activity;
        this.context = activity.getApplicationContext();
    } // 생성자

    /**
     * onCreateViewHolder() 메소드
     */
    @NonNull
    @Override
    public TouristAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater  = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.recyclerview_tourist_item,parent,false); // 뷰 객체 생성
        TouristAdapter.ViewHolder viewHolder = new TouristAdapter.ViewHolder(view);
        return viewHolder;
    } // onCreateViewHolder() 메소드

    /**
     * onBindViewHolder() 메소드
     */
    @Override
    public void onBindViewHolder(@NonNull TouristAdapter.ViewHolder holder, int position) {
        if(holder.getAdapterPosition()!=RecyclerView.NO_POSITION){ // 포지션 에러를 최소화 하기 위한 조건
            final Tourist tourist = tourists.get(holder.getAdapterPosition());

            holder.item_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) { // 아이템 클릭시 이벤트
                    Gson gson = new Gson();
                    String tourist_str = gson.toJson(tourist);
                    Intent intent = new Intent(context, TouristDetailActivity.class);
                    intent.putExtra("tourist",tourist_str);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    activity.startActivity(intent);
                }
            });

            /**
             * 이미지 적용
             */
            if(tourist.firstimage2==null){
                holder.tourist_img_iv.setImageResource(R.drawable.no_image8);
            }else{
                Glide.with(context)
                        .load(tourist.firstimage2)
                        .thumbnail(0.1f)
                        .placeholder(R.drawable.loading3)
                        .into(holder.tourist_img_iv);
            }

            /**
             * 여행지 title
             */
            holder.tourist_title_tv.setText(tourist.title);

            /**
             * 여행지 장소
             */
            switch (Integer.parseInt(tourist.areacode)){
                case 1:
                    holder.tourist_area_tv.setText("장소 : 서울");
                    break;
                case 2:
                    holder.tourist_area_tv.setText("장소 : 인천");
                    break;
                case 3:
                    holder.tourist_area_tv.setText("장소 : 대전");
                    break;
                case 4:
                    holder.tourist_area_tv.setText("장소 : 대구");
                    break;
                case 5:
                    holder.tourist_area_tv.setText("장소 : 광주");
                    break;
                case 6:
                    holder.tourist_area_tv.setText("장소 : 부산");
                    break;
                case 7:
                    holder.tourist_area_tv.setText("장소 : 울산");
                    break;
                case 8:
                    holder.tourist_area_tv.setText("장소 : 세종특별자치시");
                    break;
                case 31:
                    holder.tourist_area_tv.setText("장소 : 경기도");
                    break;
                case 32:
                    holder.tourist_area_tv.setText("장소 : 강원도");
                    break;
                case 33:
                    holder.tourist_area_tv.setText("장소 : 충청북도");
                    break;
                case 34:
                    holder.tourist_area_tv.setText("장소 : 충청남도");
                    break;
                case 35:
                    holder.tourist_area_tv.setText("장소 : 경상북도");
                    break;
                case 36:
                    holder.tourist_area_tv.setText("장소 : 경상남도");
                    break;
                case 37:
                    holder.tourist_area_tv.setText("장소 : 전라북도");
                    break;
                case 38:
                    holder.tourist_area_tv.setText("장소 : 전라남도");
                    break;
                case 39:
                    holder.tourist_area_tv.setText("장소 : 제주도");
                    break;

            }
        }
    } // onBindViewHolder() 메소드

    /**
     * getItemCount() 메소드
     */
    @Override
    public int getItemCount() {
        return tourists.size();
    } // getItemCount() 메소드


    /**
     * 뷰홀더 클래스
     */
    public class ViewHolder extends RecyclerView.ViewHolder{

        View item_view;
        ImageView tourist_img_iv;
        TextView tourist_title_tv;
        TextView tourist_area_tv;

        /**
         * 뷰 홀더 생성자
         */
        ViewHolder(View item_view){
            super(item_view);
            this.item_view = item_view;
            tourist_img_iv = item_view.findViewById(R.id.tourist_img_iv);
            tourist_title_tv = item_view.findViewById(R.id.tourist_title_tv);
            tourist_area_tv = item_view.findViewById(R.id.tourist_area_tv);

            /**
             * 아이템 클릭시 이벤트
             */
            item_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        } // ViewHolder 생성자


    } // ViewHolder 클래스

} // TouristAdapter 클래스
