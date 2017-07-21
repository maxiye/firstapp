package com.maxiye.first;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BlankFragment.OnFrgActionListener} interface
 * to handle interaction events.
 */
public class BlankFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String ARG_1 = "arg_1";
    private static final int MSG_TYPE_START = 0;
    private static final int MSG_TYPE_LV = 1;
    private static final int MSG_TYPE_TV = 2;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_blank, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final ListView lv = (ListView) getActivity().findViewById(R.id.lv_bfg);
        final ImageView im = (ImageView) getActivity().findViewById(R.id.loading_bfg);
        final TextView tv = (TextView) getActivity().findViewById(R.id.frgtxt);
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
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
                        lv.setAdapter(new MyAdapter(getActivity(), R.layout.listview_applist, app_al));
                        msg.obj = null;
                        break;
                    case MSG_TYPE_TV:
                        String tvtxt = (String) msg.obj;
                        tv.setText(tvtxt);
                        break;
                }
                return false;
            }
        });
        //点按事件
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mListener.onItemClick(view);
            }
        });
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                mListener.onItemLongClick(view);
                return true;//取消点击事件
            }
        });
        thread = new Thread() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                getListData();
            }
        };
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
        for (int i = 0; i < ai_al.size(); i++) {
            ApplicationInfo ai = ai_al.get(i);
            if ((!show_system_apps && ((ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0))) {
                ai_al.remove(i);
                i--;//??????????????????????????????????????????????坑爹啊尼玛
                continue;
            }
            if (keyword != null && !keyword.isEmpty()) {
                String app_name = pm.getApplicationLabel(ai).toString();
                if (!(app_name + ai.packageName).toLowerCase().contains(keyword.toLowerCase())) {
                    ai_al.remove(i);
                    i--;
                }
            }
            //app_list += (ai.flags & ApplicationInfo.FLAG_SYSTEM)+"??";
        }
        //写入文件
        //SaveAppListEx("app_list.txt", app_list);
        List<Map<String, Object>> app_map_al = new ArrayList<>();
        try {
            ApplicationInfo app_info = pm.getApplicationInfo(keyword, 0);
            CharSequence appname = pm.getApplicationLabel(app_info) + "：" + app_info.packageName;
            handler.sendMessage(handler.obtainMessage(MSG_TYPE_TV, appname));
        } catch (PackageManager.NameNotFoundException e) {
            HashMap<String, Object> app_info;
            try {
                for (ApplicationInfo ai : ai_al) {
                    app_info = new HashMap<>();
                    PackageInfo pi = pm.getPackageInfo(ai.packageName, 0);
                    app_info.put("name", pm.getApplicationLabel(ai) + " v" + pi.versionName + "(" + pi.versionCode + ")");
                    app_info.put("pkg", ai.packageName);
                    app_info.put("icon", pm.getApplicationIcon(ai));
                    app_map_al.add(app_info);
                }
            } catch (PackageManager.NameNotFoundException ee) {
                ee.printStackTrace();
            }

            if (!app_map_al.isEmpty()) {
                handler.sendMessage(handler.obtainMessage(MSG_TYPE_LV, app_map_al));
            } else {
                handler.sendMessage(handler.obtainMessage(MSG_TYPE_TV, getString(R.string.not_found)));
            }
        }
    }

    /**
     * 保存applist到内部存储
     *
     * @param fname   Filename
     * @param content File content
     */
    @SuppressWarnings("unused")
    public void SaveAppList(String fname, String content) {
        //File file = new File(getActivity().getFilesDir(), fname);
        FileOutputStream fos;
        try {
            /*if (!file.exists()){
                file.createNewFile();
            }*/
            getActivity();
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
    }

    private class MyAdapter extends ArrayAdapter {
        private List<Map<String, Object>> data;
        private int res;
        private Context context;

        @SuppressWarnings("unchecked")
        MyAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Map<String, Object>> objects) {
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
                holder.icon = (ImageView) convertView.findViewById(R.id.app_icon);
                holder.name = (TextView) convertView.findViewById(R.id.app_name);
                holder.pkg = (TextView) convertView.findViewById(R.id.package_name);
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

    }

    private static class ViewHolder {
        TextView name;
        TextView pkg;
        ImageView icon;
    }
}
