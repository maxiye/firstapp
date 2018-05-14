package com.maxiye.first.part;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.maxiye.first.R;

/**
 * 适配器
 * Created by due on 2018/5/14.
 */
public class MyRVAdapter extends RecyclerView.Adapter {
    private String[] mData;
    private OnItemClickListener clickListener;

    public void setData(String[] data) {
        mData = data;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.clickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.popupwindow_listview, null));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.mTextView.setText(mData[position]);
        viewHolder.mImageView.setImageResource(android.R.drawable.ic_menu_gallery);
        if (clickListener != null) {
            viewHolder.itemView.setOnClickListener(view -> clickListener.onClick(position));
        }
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.length;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTextView;
        ImageView mImageView;

        ViewHolder(View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.pw_text_view);
            mImageView = itemView.findViewById(R.id.pw_image_view);
        }
    }

    public interface OnItemClickListener {
        void onClick(int position);
    }

}
