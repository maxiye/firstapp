package com.maxiye.first.part;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.maxiye.first.R;

import java.io.IOException;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * 适配器
 * Created by due on 2018/10/08.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class WeatherRetRvAdapter extends RecyclerView.Adapter {
    private List<String[]> mData;
    private OnItemClickListener clickListener;
    private OnItemLongClickListener longClickListener;

    public void setData(List<String[]> data) {
        mData = data;
    }

    public List<String[]> getData() {
        return mData;
    }

    public String[] getItemData(int position) {
        return mData.get(position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.clickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.longClickListener = onItemLongClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.weather_ret_rvlist, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        String[] item = mData.get(position);
        viewHolder.mTextView.setText(item[0]);
        try {
            if (position > 0) {
                viewHolder.mImageView.setImageDrawable(new GifDrawable(item[1]));
                viewHolder.mImageView1.setImageDrawable(new GifDrawable(item[2]));
            } else {
                viewHolder.mImageView.setImageDrawable(null);
                viewHolder.mImageView1.setImageDrawable(null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (clickListener != null)
            viewHolder.itemView.setOnClickListener(view -> clickListener.onClick(position));
        if (longClickListener != null)
            viewHolder.itemView.setOnLongClickListener(view -> longClickListener.onLongClick(position));
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView mTextView;
        final GifImageView mImageView;
        final GifImageView mImageView1;

        ViewHolder(View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.weather_txt);
            mImageView = itemView.findViewById(R.id.weather_ico);
            mImageView1 = itemView.findViewById(R.id.weather_ico1);
        }
    }

    public interface OnItemClickListener {
        void onClick(int position);
    }
    public interface OnItemLongClickListener {
        boolean onLongClick(int position);
    }

}
