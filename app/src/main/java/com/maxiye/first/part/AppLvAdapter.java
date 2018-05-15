package com.maxiye.first.part;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.maxiye.first.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库助手
 * Created by due on 2018/5/15.
 */
public class AppLvAdapter extends ArrayAdapter {
    private List<Map<String, Object>> data;
    private int res;
    private Context context;

    @SuppressWarnings("unchecked")
    public AppLvAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Map<String, Object>> objects) {
        super(context, resource, objects);
        this.context = context;
        this.res = resource;
        this.data = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(res, null);
            holder = new ViewHolder();
            holder.icon = convertView.findViewById(R.id.app_icon);
            holder.name = convertView.findViewById(R.id.app_name);
            holder.pkg = convertView.findViewById(R.id.package_name);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        HashMap<String, Object> app_info = (HashMap<String, Object>) data.get(position);
        holder.icon.setImageDrawable((Drawable) app_info.get("icon"));
        holder.name.setText((String) app_info.get("name"));
        holder.pkg.setText((String) app_info.get("pkg"));
        convertView.setTag(holder);
        return convertView;
    }

    private static class ViewHolder {
        TextView name;
        TextView pkg;
        ImageView icon;
    }
}

