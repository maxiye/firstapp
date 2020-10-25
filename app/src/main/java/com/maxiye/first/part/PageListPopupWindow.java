package com.maxiye.first.part;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.maxiye.first.R;
import com.maxiye.first.util.MyLog;
import com.maxiye.first.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 数据库助手
 * {@code 第15条：最小化类和成员的可访问性}
 * {@code 第16条：在公有类中使用访问方法，而不是公有域}
 * {@code 第18条：组合优先于继承}
 * {@code 第20条：接口优于抽象类} 接口是定义混合类型（mixins）的理想选择
 * 通过包装者类模式（条目18），使用接口使得安全地增强类的功能成为可能。
 * {@code 第22条：接口应该只被用来定义类型。它们不能仅仅是用来导出常量}
 * {@code 第44条：优先使用标准的函数式接口}
 * 一般来说，最好使用{@link java.util.function.Function}中提供的标准接口，但请注意，在相对罕见的情况下，最好编写自己的函数式接口。
 * {@code 第65条：接口优于反射}
 * @author due
 * @date 2018/10/22
 */
public class PageListPopupWindow {

    private List<Map<String, Object>> list;
    private RecyclerView rv;
    private GifWebRvAdapter ma;
    private PopupWindow popupWindow;
    private View rootView;
    private final Context context;
    private ListGetter listGetter;
    private ListCountGetter listCountGetter;
    private ItemClickListener itemClickListener;
    private ItemLongClickListener itemLongClickListener;
    private int total;
    private int pageSize = 10;
    private int pages;
    private int page;
    /**
     * 筛选条件，eg. art_id=5
     */
    private String where;
    private int windowHeight;
    /**
     * 是否显示蒙层
     */
    private boolean showMask;
    private View archor;
    private View mask;

    private PageListPopupWindow(Context ctx) {
        context = ctx;
    }

    private int calcPages() {
        return total % pageSize == 0 ? total / pageSize : total / pageSize + 1;
    }

    /**
     * 重置列表，返回第一页，刷新内容，重新计算分页等
     */
    private void reset() {
        total = listCountGetter.getListCount(where);
        pages = calcPages();
        page = 1;
        EditText pageEdit = rootView.findViewById(R.id.popup_page);
        TextView totalPage = rootView.findViewById(R.id.popup_total_page);
        Button prev = rootView.findViewById(R.id.popup_prev_page);
        Button next = rootView.findViewById(R.id.popup_next_page);
        pageEdit.setText(String.valueOf(page));
        prev.setVisibility(View.GONE);
        next.setVisibility(pages > 1 ? View.VISIBLE : View.GONE);
        totalPage.setText(String.valueOf(pages));
        ma.setData(updateList(page, where));
        ma.notifyDataSetChanged();
    }

    /**
     * 更新列表数据
     * @param page 页面
     * @param where where条件
     * @return list
     */
    private List<Map<String, Object>> updateList(int page, String where) {
        List<Map<String, Object>> data = listGetter.getList(page, list, where);
        // 容错，未设置list，list数据更新
        if (list == null || list.size() == 0 || list.size() != data.size()) {
            list = data;
        }
        return data;
    }

    /**
     * 删除一个项目
     * @param position 位置
     */
    public void remove(int position) {
        list.remove(position);
        ma.notifyItemRemoved(position);
        ma.notifyItemRangeChanged(position, list.size());
    }

    /**
     * 获取特定位置的item的view
     * 使用rv.getChildAt只能获取可见的item，0表示当前屏幕可见第一个item
     * @param position 位置
     * @return view
     */
    public View getItemView(int position) {
        RecyclerView.LayoutManager layoutManager = rv.getLayoutManager();
        if (layoutManager != null) {
            return layoutManager.findViewByPosition(position);
        }
        return null;
    }

    public Map<String, Object> getItemData(int position) {
        return ma.getItemData(position);
    }

    /**
     * 窗口消失
     */
    public void dismiss() {
        popupWindow.dismiss();
    }

    /**
     * 获取总数
     * @return int
     */
    private int getTotal() {
        return total;
    }

    public String getWhere() {
        return where;
    }

    /**
     * 筛选数据
     * @param conditions String
     */
    public void filter(String conditions) {
        where = conditions;
        try {
            reset();
            if (getTotal() == 0) {
                throw new Exception("no data");
            }
        } catch (Exception e) {
            Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            where = null;
            reset();
            e.printStackTrace();
        }
    }

    /**
     * 是否有上一页
     * @return boolean
     */
    public boolean hasPrePage() {
        return page > 1;
    }

    public void prePage() {
        if (page > 1) {
            --page;
            ma.setData(updateList(page, where));
            ma.notifyDataSetChanged();
            EditText pageEdit = rootView.findViewById(R.id.popup_page);
            Button prev = rootView.findViewById(R.id.popup_prev_page);
            Button next = rootView.findViewById(R.id.popup_next_page);
            pageEdit.setText(String.valueOf(page));
            next.setVisibility(View.VISIBLE);
            prev.setVisibility(page == 1 ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * 是否有下一页
     * @return boolean
     */
    public boolean hasNextPage() {
        Button next = rootView.findViewById(R.id.popup_next_page);
        return next.getVisibility() == Button.VISIBLE;
    }

    public void nextPage() {
        ++page;
        ma.setData(updateList(page, where));
        ma.notifyDataSetChanged();
        EditText pageEdit = rootView.findViewById(R.id.popup_page);
        Button prev = rootView.findViewById(R.id.popup_prev_page);
        Button next = rootView.findViewById(R.id.popup_next_page);
        pageEdit.setText(String.valueOf(page));
        prev.setVisibility(View.VISIBLE);
        next.setVisibility(page < pages ? View.VISIBLE : View.GONE);
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

        /**
         * @param archor 遮罩层依附的view
         * @return Builder
         */
        public Builder setMask(View archor) {
            pagePopup.archor = archor;
            pagePopup.showMask = true;
            return this;
        }

        public PopupWindow build() {
            return pagePopup.build();
        }
    }

    @SuppressLint("SetTextI18n,InflateParams")
    private PopupWindow build() {
        popupWindow = new PopupWindow(ViewGroup.LayoutParams.MATCH_PARENT, windowHeight);
        if (showMask) {
            addMask(archor);
            popupWindow.setOnDismissListener(this::removeMask);
        }
        rootView = LayoutInflater.from(context).inflate(R.layout.gif_history_popupwindow_view, null);
        popupWindow.setFocusable(true);
        popupWindow.setContentView(rootView);
        popupWindow.setOutsideTouchable(true);
        rv = popupWindow.getContentView().findViewById(R.id.popupwindow_rv);
        rv.setLayoutManager(new LinearLayoutManager(context));
        DividerItemDecoration divider = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        divider.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(context, R.drawable.gif_rv_divider)));
        // 分隔线
        rv.addItemDecoration(divider);
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
                    ma.setData(updateList(page, where));
                    ma.notifyDataSetChanged();
                    pageEdit.setText(page + "");
                    next.setVisibility(View.VISIBLE);
                    prev.setVisibility(page == 1 ? View.GONE : View.VISIBLE);
                }
            });
            next.setOnClickListener(v -> {
                ++page;
                ma.setData(updateList(page, where));
                ma.notifyDataSetChanged();
                pageEdit.setText(page + "");
                prev.setVisibility(View.VISIBLE);
                next.setVisibility(page < pages ? View.VISIBLE : View.GONE);
            });
            pageEdit.setOnEditorActionListener((textView, i, keyEvent) -> {
                String nowPageStr = pageEdit.getText().toString();
                int nowPage = StringUtil.notBlank(nowPageStr) ? Integer.parseInt(nowPageStr) : 1;
                page = Math.min(nowPage, pages);
                MyLog.w("FavoriteGoPage", "pagePopup:" + page);
                pageEdit.setText(page + "");
                ma.setData(updateList(page, where));
                ma.notifyDataSetChanged();
                prev.setVisibility(page > 1 ? View.VISIBLE : View.GONE);
                next.setVisibility(page < pages ? View.VISIBLE : View.GONE);
                InputMethodManager imm = ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE));
                if (imm != null) {
                    imm.toggleSoftInput(InputMethodManager.RESULT_UNCHANGED_SHOWN, InputMethodManager.HIDE_NOT_ALWAYS);
                }
                pageEdit.clearFocus();
                return true;
            });
            pageCtrl.setVisibility(View.VISIBLE);
            totalPage.setText(String.valueOf(pages));
        }
        list = new ArrayList<>(pageSize);
        ma.setData(updateList(1, where));
        if (itemClickListener != null) {
            ma.setOnItemClickListener(position -> itemClickListener.onClick(this, position));
        }
        if (itemLongClickListener != null) {
            ma.setOnItemLongClickListener(position -> itemLongClickListener.onLongClick(this, position));
        }
        rv.setAdapter(ma);
        return popupWindow;
    }

    private void addMask(View archor) {
        WindowManager.LayoutParams wl = new WindowManager.LayoutParams();
        wl.width = WindowManager.LayoutParams.MATCH_PARENT;
        wl.height = WindowManager.LayoutParams.MATCH_PARENT;
        wl.format = PixelFormat.TRANSLUCENT;//不设置这个弹出框的透明遮罩显示为黑色
        wl.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;//该Type描述的是形成的窗口的层级关系
        wl.token = archor.getWindowToken();//获取当前Activity中的View中的token,来依附Activity
        mask = new View(context);
        mask.setBackgroundColor(0x7f000000);
        mask.setFitsSystemWindows(false);
        /*
         * 通过WindowManager的addView方法创建View，产生出来的View根据WindowManager.LayoutParams属性不同，效果也就不同了。
         * 比如创建系统顶级窗口，实现悬浮窗口效果！
         */
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            windowManager.addView(mask, wl);
        }
    }

    private void removeMask() {
        if (showMask && mask != null) {
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (windowManager != null) {
                windowManager.removeViewImmediate(mask);
                mask = null;
            }
        }
    }

    public interface ListGetter {
        /**
         * 列表数据获取接口
         * @param page int 页码
         * @param list ArrayList 列表数据容器
         * @param where string sql筛选条件
         * @return ArrayList 列表数据
         */
        List<Map<String, Object>> getList(int page, List<Map<String, Object>> list, String where);
    }

    public interface ListCountGetter {
        /**
         * 获取数据总数接口
         * @param where 查询条件，eg. id=10
         * @return int 数据总数
         */
        int getListCount(String where);
    }

    public interface ItemClickListener {
        /**
         * 项目点击事件
         * @param pageListPopupWindow 列表
         * @param position int 位置
         */
        void onClick(PageListPopupWindow pageListPopupWindow, int position);
    }

    public interface ItemLongClickListener {
        /**
         * 项目长按事件
         * @param pageListPopupWindow 列表
         * @param position int 位置
         * @return boolean 是否消费
         */
        boolean onLongClick(PageListPopupWindow pageListPopupWindow, int position);
    }

    public List<Map<String, Object>> getList() {
        return list;
    }

}
