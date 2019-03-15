package com.maxiye.first;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.maxiye.first.part.AppLvAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ApplistFragment.OnFrgActionListener} interface
 * to handle interaction events.
 * @author due
 */
public class ApplistFragment extends Fragment {
    private static final String ARG_1 = "arg_1";

    String keyword;

    private OnFrgActionListener mListener;
    private SharedPreferences sp;
    private PackageManager pm;
    private ArrayList<ApplicationInfo> appInfoArrayList;

    /**
     * 简写toast
     *
     * @param msg 消息
     */
    @SuppressWarnings("unused")
    private void alert(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            keyword = getArguments().getString(ARG_1);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_applist, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        assert getActivity() != null;
        ListView lv = getActivity().findViewById(R.id.applist_frg_lv);
        // 点按事件
        lv.setOnItemClickListener((parent, view, position, id) -> mListener.onItemClick(view));
        lv.setOnItemLongClickListener((parent, view, position, id) -> {
            mListener.onItemLongClick(view);
            // 取消点击事件
            return true;
        });
        lv.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int oldVisibleItem = 0;
            private boolean touchFlg = true;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                touchFlg = true;
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int numPerScreen = 15;
                if (firstVisibleItem > oldVisibleItem && touchFlg && lv.getCount() > numPerScreen) {
                    // 向上滑动
                    mListener.onListScroll(true);
                    touchFlg = false;
                }
                if (oldVisibleItem > firstVisibleItem && touchFlg) {
                    // 向下滑动
                    mListener.onListScroll(false);
                    touchFlg = false;
                }
                oldVisibleItem = firstVisibleItem;
            }
        });
        search();
    }

    public void search() {
        new GetAppsTask(this).execute(keyword);
    }

    static class GetAppsTask extends AsyncTask<String, Void, List<Map<String, Object>>> {
        private final WeakReference<ApplistFragment> frag;

        GetAppsTask(ApplistFragment applistFragment) {
            frag = new WeakReference<>(applistFragment);
        }

        @Override
        protected List<Map<String, Object>> doInBackground(String... strings) {
            ApplistFragment fragment = frag.get();
            if (fragment != null && fragment.getActivity() != null) {
                Activity activity = fragment.getActivity();
                if (fragment.appInfoArrayList == null) {
                    fragment.sp = activity.getSharedPreferences(SettingActivity.SETTING, Context.MODE_PRIVATE);
                    fragment.pm = activity.getPackageManager();
                    fragment.appInfoArrayList = new ArrayList<>(fragment.pm.getInstalledApplications(0));
                }
                boolean showSystemApps = fragment.sp.getBoolean(SettingActivity.SHOW_SYSTEM, false);
                //过滤
                List<Map<String, Object>> aiList = fragment.appInfoArrayList.stream()
                        .filter(ai -> showSystemApps || ((ai.flags & ApplicationInfo.FLAG_SYSTEM) == 0))
                        .filter(ai -> {
                            if (strings[0] != null && !strings[0].isEmpty()) {
                                String appName = fragment.pm.getApplicationLabel(ai).toString();
                                return (appName + ai.packageName).toLowerCase().contains(strings[0].toLowerCase());
                            }
                            return true;
                        })
                        .map(ai -> {
                            HashMap<String, Object> appInfo = new HashMap<>(3);
                            try {
                                PackageInfo pi = fragment.pm.getPackageInfo(ai.packageName, 0);
                                appInfo.put("name", fragment.pm.getApplicationLabel(ai) + " v" + pi.versionName + "(" + pi.versionCode + ")");
                                appInfo.put("pkg", ai.packageName);
                                appInfo.put("icon", fragment.pm.getApplicationIcon(ai));
                            } catch (PackageManager.NameNotFoundException e1) {
                                e1.printStackTrace();
                            }
                            return appInfo;
                        })
                        .collect(Collectors.toList());
                //写入文件
                //saveFileEx("app_list.txt", app_list);
                return aiList.size() > 0 ? aiList : null;
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            Activity activity = frag.get().getActivity();
            if (activity != null) {
                TextView tv = activity.findViewById(R.id.applist_frgtxt);
                ListView lv = activity.findViewById(R.id.applist_frg_lv);
                ImageView im = activity.findViewById(R.id.applist_loading_bfg);
                tv.setText("");
                lv.setAdapter(null);
                im.setVisibility(View.VISIBLE);
                im.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.load_rotate));
            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(List<Map<String, Object>> mapList) {
            Activity activity = frag.get().getActivity();
            if (activity != null) {
                ImageView im = activity.findViewById(R.id.applist_loading_bfg);
                TextView tv = activity.findViewById(R.id.applist_frgtxt);
                im.clearAnimation();
                im.setVisibility(View.GONE);
                if (mapList != null) {
                    ListView lv = activity.findViewById(R.id.applist_frg_lv);
                    lv.setAdapter(new AppLvAdapter(activity, R.layout.listview_applist, mapList));
                } else {
                    tv.setText("NULL");
                }
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFrgActionListener) {
            mListener = (OnFrgActionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFrgActionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    interface OnFrgActionListener {
        /**
         * 项目点击事件接口
         * @param view View
         */
        void onItemClick(View view);

        /**
         * 项目长按事件接口
         * @param view View
         */
        void onItemLongClick(View view);

        /**
         * 滚动事件
         * @param flg 上划true，下滑false
         */
        void onListScroll(boolean flg);
    }
}
