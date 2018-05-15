package com.maxiye.first;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BlankFragment.OnFrgActionListener} interface
 * to handle interaction events.
 */
public class BlankFragment extends Fragment {
    public static final String ARG_1 = "arg_1";
    private final int MSG_TYPE_START = 0;
    private final int MSG_TYPE_LV = 1;
    private final int MSG_TYPE_TV = 2;

    protected String keyword;

    private OnFrgActionListener mListener;
    private Handler handler;
    protected Thread thread;
    private SharedPreferences sp;
    private PackageManager pm;
    protected ArrayList<ApplicationInfo> ai_al;

    public BlankFragment() {
        // Required empty public constructor
    }

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
        final ListView lv = getActivity().findViewById(R.id.lv_bfg);
        final ImageView im = getActivity().findViewById(R.id.loading_bfg);
        final TextView tv = getActivity().findViewById(R.id.frgtxt);
        handler = new Handler(msg -> {
            tv.setText("");
            im.clearAnimation();//清除动画后才能设为不可见
            im.setVisibility(View.INVISIBLE);
            switch (msg.what) {
                case MSG_TYPE_START:
                    lv.setAdapter(null);
                    im.setVisibility(View.VISIBLE);
                    im.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.load_rotate));
                    break;
                case MSG_TYPE_LV:
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> app_al = (List<Map<String, Object>>) msg.obj;
                    lv.setAdapter(new AppLvAdapter(getActivity(), R.layout.listview_applist, app_al));
                    msg.obj = null;
                    break;
                case MSG_TYPE_TV:
                    String tvtxt = (String) msg.obj;
                    tv.setText(tvtxt);
                    break;
            }
            return false;
        });
        //点按事件
        lv.setOnItemClickListener((parent, view, position, id) -> mListener.onItemClick(view));
        lv.setOnItemLongClickListener((parent, view, position, id) -> {
            mListener.onItemLongClick(view);
            return true;//取消点击事件
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
                if (firstVisibleItem > oldVisibleItem && touchFlg) {
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
        thread = new Thread(() -> {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            getListData();
        });
        thread.start();
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    /**
     * 获取Listview数据
     */
    private void getListData() {
        handler.sendMessage(Message.obtain(handler, MSG_TYPE_START));
        if (ai_al == null) {
            sp = getActivity().getSharedPreferences(SettingActivity.SETTING, Context.MODE_PRIVATE);
            pm = getActivity().getPackageManager();
            ai_al = new ArrayList<>(pm.getInstalledApplications(0));
        }
        boolean show_system_apps = sp.getBoolean(SettingActivity.SHOW_SYSTEM, false);
        //过滤
        //String app_list = "";
        List<ApplicationInfo> ai_al_c = ai_al.stream()
                .filter(ai -> show_system_apps || ((ai.flags & ApplicationInfo.FLAG_SYSTEM) == 0))
                .filter(ai -> {
                    if (keyword != null && !keyword.isEmpty()) {
                        String app_name = pm.getApplicationLabel(ai).toString();
                        return (app_name + ai.packageName).toLowerCase().contains(keyword.toLowerCase());
                    }
                    return true;
                })
//                .forEach(ai -> app_list += (ai.flags & ApplicationInfo.FLAG_SYSTEM)+"??")
                .collect(Collectors.toList());
        //写入文件
        //SaveAppListEx("app_list.txt", app_list);
        Message msg;
        try {
            ApplicationInfo app_info = pm.getApplicationInfo(keyword, 0);
            CharSequence appname = pm.getApplicationLabel(app_info) + "：" + app_info.packageName;
            msg = handler.obtainMessage(MSG_TYPE_TV, appname);
        } catch (PackageManager.NameNotFoundException e) {
            List<Map<String, Object>> app_map_al = ai_al_c.stream().map(ai -> {
                HashMap<String, Object> app_info = new HashMap<>();
                try {
                    PackageInfo pi = pm.getPackageInfo(ai.packageName, 0);
                    app_info.put("name", pm.getApplicationLabel(ai) + " v" + pi.versionName + "(" + pi.versionCode + ")");
                    app_info.put("pkg", ai.packageName);
                    app_info.put("icon", pm.getApplicationIcon(ai));
                } catch (PackageManager.NameNotFoundException e1) {
                    e1.printStackTrace();
                }
                return app_info;
            }).collect(Collectors.toList());
            msg = app_map_al.isEmpty() ? handler.obtainMessage(MSG_TYPE_TV, getString(R.string.not_found)) : handler.obtainMessage(MSG_TYPE_LV, app_map_al);
        }
        handler.sendMessage(msg);
    }

    /**
     * 保存applist到内部存储
     *
     * @param fname   Filename
     * @param content File content
     */
    @SuppressWarnings("unused")
    public void SaveAppList(String fname, String content) {
        FileOutputStream fos;
        try {
            fos = getActivity().openFileOutput(fname, Context.MODE_PRIVATE);
            fos.write(content.getBytes());
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    /**
     * 保存applist到外部存储
     *
     * @param fname   Filename
     * @param content File content
     */
    @SuppressWarnings("unused")
    private void SaveAppListEx(String fname, String content) {
        if (isExternalStorageWritable()) {
            File file = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fname);
            try {
                if (!file.exists() && !file.createNewFile()) {
                    throw new IOException();
                }
                RandomAccessFile raf = new RandomAccessFile(file, "rwd");
                //raf.seek(file.length());//追加模式
                raf.write(content.getBytes());
                raf.close();
            } catch (Exception e) {
                e.printStackTrace();
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
        void onItemClick(View view);
        void onItemLongClick(View view);
        void onListScroll(boolean flg);
    }
}
