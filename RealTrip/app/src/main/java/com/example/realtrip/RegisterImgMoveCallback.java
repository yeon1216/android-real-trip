package com.example.realtrip;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

/**
 * RegisterImgMoveCallback 클래스
 * 이미지 드래그를 위한 클래스
 * + swipe 도 가능함
 */
public class RegisterImgMoveCallback extends ItemTouchHelper.Callback {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    private final OnItemMoveListener mItemMoveListener;



    /**
     * 생성자
     */
    public RegisterImgMoveCallback(OnItemMoveListener onItemMoveListener){
        mItemMoveListener = onItemMoveListener;
    }

    /**
     * 어떤 방향으로 움직일지 정하는 메소드
     */
    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        Log.d(TAG,"getMovementFlags() 호출");
        int dragFlags = ItemTouchHelper.START | ItemTouchHelper.END;
//            int swipeFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        return makeMovementFlags(dragFlags,0);
    }

    /**
     * itemToch
     */
    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        Log.d(TAG,"onMove() 호출");
        mItemMoveListener.onItemMove(viewHolder.getAdapterPosition(),target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        Log.d(TAG,"onSwiped() 호출");
    }

    public interface OnItemMoveListener{
        void onItemMove(int from_position, int to_position);
    }


} // RegisterImgMoveCallback 클래스
