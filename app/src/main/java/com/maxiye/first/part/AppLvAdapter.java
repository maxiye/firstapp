package com.maxiye.first.part;

import android.content.Context;
import android.graphics.drawable.Drawable;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * app列表适配器
 *
 * {@code 第28条：列表优先于数组（泛型数组）}
 * 数组在运行时才知道并检查元素类型。
 * 如果你将一个String放入Long数组里，你将会得到一个ArrayStoreException异常。
 * 泛型则是通过擦除来实现的[JLS, 4.6]。这意味着泛型仅在编译时进行类型约束的检查并且在运行是忽略（或擦除）元素类型。擦除机制使得泛型类型可以自由地与那些未使用泛型（条目26）的遗留代码互用，确保平滑转换到Java 5的泛型。
 * 元素类型信息在运行时从泛型里擦除了。
 * 数组和泛型有着不同的类型规则。数组是协变的并可具化的；泛型是受约束并且可擦除的。
 * 因此，数组提供了运行时类型安全性但不保证编译时类型安全性，泛型则反过来。通常，数组和泛型不能很好混用。
 * @author due
 * @date 2018/5/15
 */
public class AppLvAdapter extends ArrayAdapter<Map<String, Object>> {
    /**
     * 列表代替数组
     * {@code 第29条：优先考虑泛型} extends ArrayAdapter<Map<String, Object>> 而不使用类型强转
     * {@code 第30条：优先使用泛型方法}
     * 总而言之，泛型方法就像泛型类型，比起那些要求客户端将参数及返回值进行显示强转的方法，它们更安全更简单。
     * 就像类型一样，你应该保证你的方法不用强转就能用，这意味着要将这些方法泛型化，你也应该将现有方法泛型化，让新用户用起来更简单，而且不用破坏现有客户端（条目26）。
     */
    private final List<Map<String, Object>> data;
    private final int res;
    private final Context context;

    /**
     * {@code 第27条：消除未检查警告} extends ArrayAdapter<Map<String, Object>>
     * SuppressWarnings("unchecked")
     * @param context Context
     * @param objects List<Map<String, Object>>
     */
    AppLvAdapter(@NonNull Context context, @NonNull List<Map<String, Object>> objects) {
        super(context, R.layout.listview_applist, objects);
        this.context = context;
        this.res = R.layout.listview_applist;
        this.data = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(res, null);
            holder = new ViewHolder();
            holder.icon = convertView.findViewById(R.id.applist_app_icon);
            holder.name = convertView.findViewById(R.id.applist_app_name);
            holder.pkg = convertView.findViewById(R.id.applist_package_name);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        HashMap<String, Object> appInfo = (HashMap<String, Object>) data.get(position);
        holder.icon.setImageDrawable((Drawable) appInfo.get("icon"));
        holder.name.setText((String) appInfo.get("name"));
        holder.pkg.setText((String) appInfo.get("pkg"));
        convertView.setTag(holder);
        return convertView;
    }

    private static class ViewHolder {
        TextView name;
        TextView pkg;
        ImageView icon;
    }
}

