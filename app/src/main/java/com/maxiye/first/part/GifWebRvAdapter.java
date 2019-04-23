package com.maxiye.first.part;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.maxiye.first.R;

import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

/**
 * gif历史列表适配器
 * {@code 第27条：消除未检查警告} extends RecyclerView.Adapter<GifWebRvAdapter.ViewHolder>
 * {@code 第44条：优先使用标准的函数式接口} IntConsumer
 * 不要试图使用基本的函数式接口来装箱基本类型的包装类而不是基本类型的函数式接口
 *
 * {@code 第51条：仔细设计方法签名} 对于参数类型，优先选择接口而不是类
 * @author due
 * @date 2018/5/14
 */
public class GifWebRvAdapter extends RecyclerView.Adapter<GifWebRvAdapter.ViewHolder> {
    private List<Map<String, Object>> mData;
    /**
     * 单击绑定接口
     * position int
     */
    private IntConsumer clickListener;
    private OnItemLongClickListener longClickListener;

    @SuppressWarnings({"WeakerAccess"})
    public void setData(List<Map<String, Object>> data) {
        mData = data;
    }
    @SuppressWarnings("unused")
    public List<Map<String, Object>> getData() {
        return mData;
    }

    @SuppressWarnings({"WeakerAccess"})
    public Map<String, Object> getItemData(int position) {
        return mData.get(position);
    }

    @SuppressWarnings({"WeakerAccess"})
    public void setOnItemClickListener(IntConsumer onItemClickListener) {
        this.clickListener = onItemClickListener;
    }

    void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.longClickListener = onItemLongClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.gif_history_pwin_rv_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> item = mData.get(position);
        if (!item.isEmpty()) {
            holder.mTextView.setText(Html.fromHtml((String) item.get("name"), Html.FROM_HTML_MODE_COMPACT));
            holder.mImageView.setImageDrawable((Drawable) item.get("icon"));
            if (clickListener != null) {
                holder.itemView.setOnClickListener(view -> clickListener.accept(position));
            }
            if (longClickListener != null) {
                holder.itemView.setOnLongClickListener(view -> longClickListener.onLongClick(position));
            }
        } else {
            holder.mTextView.setText("");
            holder.mImageView.setImageDrawable(null);
        }
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    /**
     * {@code 第24条：优先考虑静态成员类}
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView mTextView;
        final ImageView mImageView;

        ViewHolder(View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.pw_text_view);
            mImageView = itemView.findViewById(R.id.pw_image_view);
        }
    }

    public interface OnItemLongClickListener {
        /**
         * 长按点击接口
         * @param position int
         * @return boolean
         */
        boolean onLongClick(int position);
    }

}
