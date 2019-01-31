package com.maxiye.first.part;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.maxiye.first.R;
import com.maxiye.first.util.MyLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * 数据库助手
 * Created by due on 2018/10/22.
 */
public class PageListPopupWindow {
    public ArrayList<HashMap<String, Object>> list;
    public RecyclerView rv;
    public GifWebRvAdapter ma;
    public PopupWindow popupWindow;
    public View rootView;
    private final Context context;
    private ListGetter listGetter;
    private ListCountGetter listCountGetter;
    private ItemClickListener itemClickListener;
    private ItemLongClickListener itemLongClickListener;
    private int total;
    private int pageSize;
    private int pages;
    private int page;
    public String where;//筛选条件
    private int windowHeight;

    private PageListPopupWindow(Context ctx) {
        context = ctx;
    }

    private int calcPages() {
        return total % pageSize == 0 ? total / pageSize : total / pageSize + 1;
    }

    public void reset() {
        total = listCountGetter.getListCount(where);
        pages = calcPages();
        page = 1;
        if (pages > 1) {
            EditText pageEdit = rootView.findViewById(R.id.popup_page);
            TextView totalPage = rootView.findViewById(R.id.popup_total_page);
            Button prev = rootView.findViewById(R.id.popup_prev_page);
            Button next = rootView.findViewById(R.id.popup_next_page);
            pageEdit.setText(String.valueOf(page));
            prev.setVisibility(View.GONE);
            next.setVisibility(pages > 1 ? View.VISIBLE : View.GONE);
            totalPage.setText(String.valueOf(pages));
        }
        ma.setData(listGetter.getList(page, list, where));
        ma.notifyDataSetChanged();
    }

    public void remove(int position) {
        list.remove(position);
        ma.notifyItemRemoved(position);
        ma.notifyItemRangeChanged(position, list.size());
    }

    public static class Builder {
        private final PageListPopupWindow pagePopup;

        public Builder(Context context) {
            pagePopup = new PageListPopupWindow(context);
        }

        public Builder setListGetter(ListGetter mlistGetter) {
            pagePopup.listGetter = mlistGetter;
            return this;
        }

        public Builder setListCountGetter(ListCountGetter listCountGetter) {
            pagePopup.listCountGetter = listCountGetter;
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

        public Builder setPageSize(int pageSize) {
            pagePopup.pageSize = pageSize;
            return this;
        }

        public Builder setWindowHeight(int windowHeight) {
            pagePopup.windowHeight = windowHeight;
            return this;
        }

        public PopupWindow build() {
            return pagePopup.build();
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
        EditText pageEdit = rootView.findViewById(R.id.popup_page);
        page = 1;
        pageEdit.setText("1");
        total = listCountGetter.getListCount(where);
        if (total > pageSize) {
            TextView totalPage = rootView.findViewById(R.id.popup_total_page);
            LinearLayout pageCtrl = rootView.findViewById(R.id.page_popup_control);
            Button prev = rootView.findViewById(R.id.popup_prev_page);
            Button next = rootView.findViewById(R.id.popup_next_page);
            pages = calcPages();
            prev.setOnClickListener(v -> {
                if (page > 1) {
                    --page;
                    ma.setData(listGetter.getList(page, list, where));
                    ma.notifyDataSetChanged();
                    pageEdit.setText(page + "");
                    next.setVisibility(View.VISIBLE);
                    prev.setVisibility(page == 1 ? View.GONE : View.VISIBLE);
                }
            });
            next.setOnClickListener(v -> {
                ++page;
                ma.setData(listGetter.getList(page, list, where));
                ma.notifyDataSetChanged();
                pageEdit.setText(page + "");
                prev.setVisibility(View.VISIBLE);
                next.setVisibility(page < pages ? View.VISIBLE : View.GONE);
            });
            pageEdit.setOnEditorActionListener((textView, i, keyEvent) -> {
                String nowPageStr = pageEdit.getText().toString();
                int nowPage = nowPageStr.equals("") ? 1 : Integer.parseInt(nowPageStr);
                page = nowPage > pages ? pages : nowPage;
                MyLog.w("FavoriteGoPage", "pagePopup:" + page);
                pageEdit.setText(page + "");
                ma.setData(listGetter.getList(page, list, where));
                ma.notifyDataSetChanged();
                prev.setVisibility(page > 1 ? View.VISIBLE : View.GONE);
                next.setVisibility(page < pages ? View.VISIBLE : View.GONE);
                InputMethodManager imm = ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE));
                if (imm != null)
                    imm.toggleSoftInput(InputMethodManager.RESULT_UNCHANGED_SHOWN, InputMethodManager.HIDE_NOT_ALWAYS);
                pageEdit.clearFocus();
                return true;
            });
            pageCtrl.setVisibility(View.VISIBLE);
            totalPage.setText(String.valueOf(pages));
        }
        list = new ArrayList<>(pageSize);
        ma.setData(listGetter.getList(1, list, where));
        if (itemClickListener != null) ma.setOnItemClickListener(position -> itemClickListener.onClick(this, position));
        if (itemLongClickListener != null) ma.setOnItemLongClickListener(position -> itemLongClickListener.onLongClick(this, position));
        rv.setAdapter(ma);
        return popupWindow;
    }

    public interface ListGetter {
        ArrayList<HashMap<String, Object>> getList(int page, ArrayList<HashMap<String, Object>> list, String where);
    }

    public interface ListCountGetter {
        int getListCount(String where);
    }

    public interface ItemClickListener {
        void onClick(PageListPopupWindow pageListPopupWindow, int position);
    }

    public interface ItemLongClickListener {
        boolean onLongClick(PageListPopupWindow pageListPopupWindow, int position);
    }

}
