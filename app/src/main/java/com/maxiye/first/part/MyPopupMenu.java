package com.maxiye.first.part;

import android.content.Context;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.maxiye.first.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * 数据库助手
 *
 * {@code 第15条：最小化类和成员的可访问性}
 * {@code 第18条：组合优先于继承}
 * @author due
 * @date 2018/10/22
 *         ArrayList<HashMap<String, Object>> listData = new ArrayList<>(webList.length);
 *         for (String web : webList) {
 *             HashMap<String, Object> webItem = new HashMap<>(2);
 *             //String actived = webName.equals(web) ? "<span style='color: #13b294'>&emsp;&emsp;&emsp;☯</span>" : "";//⊙◎☉●✪☯⊕¤❤☺☻۞
 *             webItem.put("web", web);
 *             webItem.put("icon", iconCacheList.get(web));
 *             if (webName.equals(web)) {
 *                 String color = Integer.toHexString(getColor(R.color.myPrimaryColor));
 *                 web = "<span style='color: #" + color + "'>" + web + "</span>";
 *             }
 *             webItem.put("name", web);
 *             listData.add(webItem);
 *         }
 *         PopupWindow popupWindow = new MyPopupMenu.Builder(this)
 *                 .setMenuList(listData)
 *                 .setSize(450, ViewGroup.LayoutParams.WRAP_CONTENT)
 *                 .setItemClickListener((popMenu, position) -> {
 *                     setWebName((String) listData.get(position).get("web"));
 *                     getNewFlg = true;
 *                     initPage();
 *                     popMenu.dismiss();
 *                 })
 *                 .build();
 *         popupWindow.showAtLocation(findViewById(R.id.gif_activity_main_content), Gravity.TOP | Gravity.END, 0, 0);
 */
@SuppressWarnings({"unused"})
public class MyPopupMenu {
    private ArrayList<HashMap<String, Object>> menuList;
    private PopupWindow popupWindow;
    private ViewGroup parentView;
    private final Context context;
    private ItemClickListener itemClickListener;
    private ItemLongClickListener itemLongClickListener;
    private int height;
    private int width;

    private MyPopupMenu(Context ctx) {
        context = ctx;
    }

    public void dismiss() {
        popupWindow.dismiss();
    }

    public static class Builder {
        private final MyPopupMenu pagePopup;

        public Builder(Context context) {
            pagePopup = new MyPopupMenu(context);
        }

        public Builder setMenuList(ArrayList<HashMap<String, Object>> menus) {
            pagePopup.menuList = menus;
            return this;
        }

        public Builder setItemClickListener(ItemClickListener itemClickListener) {
            pagePopup.itemClickListener = itemClickListener;
            return this;
        }

        public Builder setItemLongClickListener(ItemLongClickListener itemLongClickListener) {
            pagePopup.itemLongClickListener = itemLongClickListener;
            return this;
        }

        public Builder setSize(int width, int height) {
            pagePopup.width = width;
            pagePopup.height = height;
            return this;
        }

        public Builder setParentView(ViewGroup parentView) {
            pagePopup.parentView = parentView;
            return this;
        }

        public PopupWindow build() {
            return pagePopup.build();
        }
    }

    private PopupWindow build() {
        popupWindow = new PopupWindow(width, height);
        popupWindow.setContentView(LayoutInflater.from(context).inflate(R.layout.my_popus_menu, parentView));
        popupWindow.setOutsideTouchable(true);
        RecyclerView rv = popupWindow.getContentView().findViewById(R.id.popupmenu_rv);
        DividerItemDecoration divider = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        divider.setDrawable(Objects.requireNonNull(context.getDrawable(R.drawable.gif_rv_divider)));
        // 分隔线
        rv.addItemDecoration(divider);
        rv.setLayoutManager(new LinearLayoutManager(context));
        GifWebRvAdapter ma = new GifWebRvAdapter();
        ma.setData(menuList);
        if (itemClickListener != null) {
            ma.setOnItemClickListener(position -> itemClickListener.onClick(this, position));
        }
        if (itemLongClickListener != null) {
            ma.setOnItemLongClickListener(position -> itemLongClickListener.onLongClick(this, position));
        }
        rv.setAdapter(ma);
        /*popupWindow.showAtLocation(findViewById(R.id.gif_activity_main_content), Gravity.TOP | Gravity.END, 0, 0);*/
        return popupWindow;
    }

    public interface ItemClickListener {
        /**
         * 项目点击事件
         * @param pageListPopupWindow 菜单列表
         * @param position int 位置
         */
        void onClick(MyPopupMenu pageListPopupWindow, int position);
    }

    public interface ItemLongClickListener {
        /**
         * 菜单长按事件
         * @param pageListPopupWindow 菜单列表
         * @param position int 位置
         * @return bool 是否已消费
         */
        boolean onLongClick(MyPopupMenu pageListPopupWindow, int position);
    }

}
