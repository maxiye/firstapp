package com.maxiye.first.part;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.maxiye.first.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * gif历史列表适配器
 * Created by due on 2018/5/14.
 */
public class GifWebRvAdapter extends RecyclerView.Adapter {
    private ArrayList<HashMap<String, Object>> mData;
    private OnItemClickListener clickListener;
    private OnItemLongClickListener longClickListener;

    public void setData(ArrayList<HashMap<String, Object>> data) {
        mData = data;
    }

    public ArrayList<HashMap<String, Object>> getData() {
        return mData;
    }

    public Map<String, Object> getItemData(int position) {
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
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.gif_history_pwin_rv_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        HashMap<String, Object> item = (HashMap<String, Object>) mData.get(position);
        viewHolder.mTextView.setText((String) item.get("name"));
        viewHolder.mImageView.setImageDrawable((Drawable) item.get("icon"));
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
        final ImageView mImageView;

        ViewHolder(View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.pw_text_view);
            mImageView = itemView.findViewById(R.id.pw_image_view);
        }
    }

    public interface OnItemClickListener {
        void onClick(int position);
    }
    public interface OnItemLongClickListener {
        boolean onLongClick(int position);
    }

}
