package com.maxiye.first;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BlankFragment.OnFrgActionListener} interface
 * to handle interaction events.
 * Use the {@link BlankFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BlankFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String ARG_1 = "param1";
    public static final String ARG_2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFrgActionListener mListener;
    private SharedPreferences sp;

    public BlankFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BlankFragment.
     */
    public static BlankFragment newInstance(String param1, String param2) {
        BlankFragment fragment = new BlankFragment();
        Bundle args = new Bundle();
        args.putString(ARG_1, param1);
        args.putString(ARG_2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_1);
            mParam2 = getArguments().getString(ARG_2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blank, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sp = getActivity().getSharedPreferences(SettingActivity.SETTING, Context.MODE_PRIVATE);
        boolean show_system_apps = sp.getBoolean(SettingActivity.SHOW_SYSTEM, false);
        PackageManager pm = getActivity().getPackageManager();
        ArrayList<ApplicationInfo> al = new ArrayList<>(pm.getInstalledApplications(0));
        //过滤
        String app_list = "";
        for (int i = 0; i < al.size(); i++) {
            ApplicationInfo ai = al.get(i);
            if ((!show_system_apps && ((ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0))) {
                al.remove(i);
                i--;//??????????????????????????????????????????????坑爹啊尼玛
            }
            //app_list += (ai.flags & ApplicationInfo.FLAG_SYSTEM)+"??";
        }
        //写入文件
        //SaveAppListEx("app_list.txt", app_list);
        ListView lv = (ListView) getActivity().findViewById(R.id.lv_bfg);
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
        ArrayList<CharSequence> strs = new ArrayList<>();
        if (mParam1 != null && !mParam1.isEmpty()) {
            TextView tv = (TextView) getView().findViewById(R.id.frgtxt);
            try {
                ApplicationInfo app_info = pm.getApplicationInfo(mParam1, 0);
                CharSequence appname = pm.getApplicationLabel(app_info) + "：" + app_info.packageName;
                tv.setText(appname);
            } catch (PackageManager.NameNotFoundException e) {
                for (ApplicationInfo ai : al) {
                    String app_name = pm.getApplicationLabel(ai).toString();
                    if ((app_name + ai.packageName).toLowerCase().indexOf(mParam1.toLowerCase()) >= 0) {
                        try {
                            PackageInfo pi = pm.getPackageInfo(ai.packageName, 0);
                            strs.add(app_name + " v" + pi.versionName + "(" + pi.versionCode + ")：" + ai.packageName);
                        } catch (PackageManager.NameNotFoundException ee) {
                            strs.add(app_name + "：" + ai.packageName);
                        }
                    }
                }
                if (!strs.isEmpty()) {
                    //tv.setText(strs.toString());
                    lv.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, strs.toArray(new String[strs.size()])));
                } else {
                    tv.setText(R.string.not_found);
                }


            }
        } else {
            for (ApplicationInfo ai : al) {
                try {
                    PackageInfo pi = pm.getPackageInfo(ai.packageName, 0);
                    strs.add(pm.getApplicationLabel(ai) + " v" + pi.versionName + "(" + pi.versionCode + ")：" + ai.packageName);
                } catch (PackageManager.NameNotFoundException e) {
                    strs.add(pm.getApplicationLabel(ai) + "：" + ai.packageName);
                }
            }
            lv.setAdapter(new ArrayAdapter<>(getActivity(),
            android.R.layout.simple_list_item_1, strs.toArray(new String[strs.size()])));
        }

    }

    private void SaveAppList(String fname, String content) {
        //File file = new File(getActivity().getFilesDir(), fname);
        FileOutputStream fos;
        try {
            /*if (!file.exists()){
                file.createNewFile();
            }*/
            fos = getActivity().openFileOutput(fname, getActivity().MODE_PRIVATE);
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
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    private void SaveAppListEx(String fname, String content) {
        if (isExternalStorageWritable()) {
            File file = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fname);
            try {
                if (!file.exists()) {
                    file.createNewFile();
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
    public interface OnFrgActionListener {
        void onItemClick(View view);

        void onItemLongClick(View view);
    }
}
