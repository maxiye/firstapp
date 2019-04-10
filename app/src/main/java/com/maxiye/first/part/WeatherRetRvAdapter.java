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
import java.util.function.IntConsumer;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * 适配器
 *
 * {@code 第44条：优先使用标准的函数式接口}
 * @author due
 * @date 2018/10/08
 */
public class WeatherRetRvAdapter extends RecyclerView.Adapter<WeatherRetRvAdapter.ViewHolder> {
    private List<String[]> mData;
    /**
     * 每日天气点击事件
     * position int
     */
    private IntConsumer clickListener;
    private OnItemLongClickListener longClickListener;

    public void setData(List<String[]> data) {
        mData = data;
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public List<String[]> getData() {
        return mData;
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public String[] getItemData(int position) {
        return mData.get(position);
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public void setOnItemClickListener(IntConsumer onItemClickListener) {
        this.clickListener = onItemClickListener;
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.longClickListener = onItemLongClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.weather_ret_rvlist, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String[] item = mData.get(position);
        holder.mTextView.setText(item[0]);
        try {
            if (position > 0) {
                holder.mImageView.setImageDrawable(new GifDrawable(item[1]));
                holder.mImageView1.setImageDrawable(new GifDrawable(item[2]));
            } else {
                holder.mImageView.setImageDrawable(null);
                holder.mImageView1.setImageDrawable(null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (clickListener != null) {
            holder.itemView.setOnClickListener(view -> clickListener.accept(position));
        }
        if (longClickListener != null) {
            holder.itemView.setOnLongClickListener(view -> longClickListener.onLongClick(position));
        }
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

    public interface OnItemLongClickListener {
        /**
         * 长按事件
         * @param position int
         * @return boolean 是否消费
         */
        boolean onLongClick(int position);
    }

}
