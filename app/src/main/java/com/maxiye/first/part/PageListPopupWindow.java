package com.maxiye.first.part;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.maxiye.first.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * 数据库助手
 * Created by due on 2018/10/22.
 */
public class PageListPopupWindow {
    public ArrayList<HashMap<String, Object>> list = new ArrayList<>();
    public RecyclerView rv;
    public GifWebRvAdapter ma;
    public PopupWindow popupWindow;
    public View rootView;
    private Context context;
    private ListGetter listGetter;
    private ItemClickListener itemClickListener;
    private ItemLongClickListener itemLongClickListener;
    private int total;
    private int pageSize;
    private int windowHeight;

    private PageListPopupWindow(Context ctx) {
        context = ctx;
    }

    public static class Builder {
        private final PageListPopupWindow page;

        public Builder(Context context) {
            page = new PageListPopupWindow(context);
        }

        public Builder setListGetter(ListGetter mlistGetter) {
            page.listGetter = mlistGetter;
            return this;
        }

        public Builder setItemClickListener(ItemClickListener itemClickListener) {
            page.itemClickListener = itemClickListener;
            return this;
        }

        public Builder setItemLongClickListener(ItemLongClickListener itemLongClickListener) {
            page.itemLongClickListener = itemLongClickListener;
            return this;
        }

        public Builder setTotal(int total) {
            page.total = total;
            return this;
        }

        public Builder setPageSize(int pageSize) {
            page.pageSize = pageSize;
            return this;
        }

        public Builder setWindowHeight(int windowHeight) {
            page.windowHeight = windowHeight;
            return this;
        }

        public PopupWindow build() {
            return page.build();
        }
    }

    @SuppressLint("SetTextI18n,InflateParams")
    private PopupWindow build() {
        popupWindow = new PopupWindow(ViewGroup.LayoutParams.MATCH_PARENT, windowHeight);
        rootView = LayoutInflater.from(context).inflate(R.layout.gif_history_popupwindow_view, null);
        popupWindow.setFocusable(true);
        popupWindow.setContentView(rootView);
        popupWindow.setOutsideTouchable(true);
        rv = popupWindow.getContentView().findViewById(R.id.popupwindow_rv);
        rv.setLayoutManager(new LinearLayoutManager(context));
        DividerItemDecoration divider = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        divider.setDrawable(Objects.requireNonNull(context.getDrawable(R.drawable.gif_rv_divider)));
        rv.addItemDecoration(divider);//分隔线
        ma = new GifWebRvAdapter();
        //设置页面相关
        EditText page = rootView.findViewById(R.id.popup_page);
        page.setText("1");
        if (total > pageSize) {
            TextView totalPage = rootView.findViewById(R.id.popup_total_page);
            Button prev = rootView.findViewById(R.id.popup_prev_page);
            Button next = rootView.findViewById(R.id.popup_next_page);
            int pages = total % pageSize == 0 ? total / pageSize : total / pageSize + 1;
            prev.setOnClickListener(v -> {
                int nowPage = Integer.parseInt(page.getText().toString());
                if (nowPage > 1) {
                    int pre = nowPage - 1;
                    ma.setData(listGetter.getList(pre, list));
                    ma.notifyDataSetChanged();
                    page.setText(pre + "");
                    next.setVisibility(View.VISIBLE);
                    prev.setVisibility(pre == 1 ? View.GONE : View.VISIBLE);
                }
            });
            next.setOnClickListener(v -> {
                int nxt = Integer.parseInt(page.getText().toString()) + 1;
                ma.setData(listGetter.getList(nxt, list));
                ma.notifyDataSetChanged();
                page.setText(nxt + "");
                prev.setVisibility(View.VISIBLE);
                next.setVisibility(nxt < pages ? View.VISIBLE : View.GONE);
            });
            page.setOnEditorActionListener((textView, i, keyEvent) -> {
                String nowPageStr = page.getText().toString();
                nowPageStr = nowPageStr.equals("") ? "1" : nowPageStr;
                int nowPage = Integer.parseInt(nowPageStr);
                nowPage = nowPage > pages ? pages : nowPage;
                Log.w("FavoriteGoPage", "page:" + nowPage);
                page.setText(nowPage + "");
                ma.setData(listGetter.getList(nowPage, list));
                ma.notifyDataSetChanged();
                prev.setVisibility(nowPage > 1 ? View.VISIBLE : View.GONE);
                next.setVisibility(nowPage < pages ? View.VISIBLE : View.GONE);
                InputMethodManager imm = ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE));
                if (imm != null)
                    imm.toggleSoftInput(InputMethodManager.RESULT_UNCHANGED_SHOWN, InputMethodManager.HIDE_NOT_ALWAYS);
                page.clearFocus();
                return true;
            });
            page.setVisibility(View.VISIBLE);
            totalPage.setVisibility(View.VISIBLE);
            totalPage.setText(" / " + pages);
            next.setVisibility(View.VISIBLE);
        }
        ma.setData(listGetter.getList(1, list));
        if (itemClickListener != null) ma.setOnItemClickListener(position -> itemClickListener.onClick(this, position));
        if (itemLongClickListener != null) ma.setOnItemLongClickListener(position -> itemLongClickListener.onLongClick(this, position));
        rv.setAdapter(ma);
        return popupWindow;
    }

    public interface ListGetter {
        ArrayList<HashMap<String, Object>> getList(int page, ArrayList<HashMap<String, Object>> list);
    }

    public interface ItemClickListener {
        void onClick(PageListPopupWindow pageListPopupWindow, int position);
    }

    public interface ItemLongClickListener {
        boolean onLongClick(PageListPopupWindow pageListPopupWindow, int position);
    }

}
