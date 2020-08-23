package com.maxiye.first.part;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.maxiye.first.R;
import com.maxiye.first.SettingActivity;
import com.maxiye.first.util.StringUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


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
                if (firstVisibleItem > oldVisibleItem && touchFlg && lv.getCount() > 15) {
                    // 向上滑动
                    mListener.onListScroll(OnFrgActionListener.Slide.UP);
                    touchFlg = false;
                }
                if (oldVisibleItem > firstVisibleItem && touchFlg) {
                    // 向下滑动
                    mListener.onListScroll(OnFrgActionListener.Slide.DOWN);
                    touchFlg = false;
                }
                oldVisibleItem = firstVisibleItem;
            }
        });
        search(keyword);
    }

    public void search(String search) {
        new GetAppsTask(this).execute(keyword = search);
    }

    static class GetAppsTask extends AsyncTask<String, Void, List<Map<String, Object>>> {
        private final WeakReference<ApplistFragment> frag;

        GetAppsTask(ApplistFragment applistFragment) {
            frag = new WeakReference<>(applistFragment);
        }

        /**
         * {@code 第36条：使用EnumSet来替换Bit域} {@link java.util.EnumSet} ApplicationInfo.FLAG_SYSTEM
         * EnumSet.of(...).contains(ApplicationInfo.FLAG_SYSTEM)
         * {@code 第45条：明智审慎地使用Stream}
         *
         * {@code 第46条：在流中优先使用无副作用的函数}
         *
         * {@code 第47条：优先使用Collection而不是Stream来作为方法的返回类型}
         * {@link java.util.Collection}接口是{@link Iterable}的子类型，并且具有{@link Collection#stream}方法，因此它提供迭代和流访问。
         * 因此，Collection或适当的子类型通常是公共序列返回方法的最佳返回类型。 数组还使用{@link java.util.Arrays#asList(Object[])}和{@link java.util.stream.Stream#of}方法提供简单的迭代和流访问。
         * 因为{@link java.util.stream.Stream}接口包含了{@link Iterable}接口中唯一的抽象方法{@link Stream#iterator}，Stream的方法规范与Iterable兼容。阻止程序员使用for-each循环在流上迭代的唯一原因是Stream无法继承Iterable。
         * 不要在内存中存储大的序列，只是为了将它作为集合返回。
         * 如果在将来的Java版本中，Stream接口声明被修改为继承Iterable，那么应该随意返回流，因为它们将允许流和迭代处理。
         *
         * {@code 第48条：谨慎使用流并行} {@link Stream#parallel}
         * 并行性带来的性能收益在ArrayList、HashMap、HashSet和ConcurrentHashMap实例、数组、int类型范围和long类型的范围的流上最好。
         * 这些数据结构的共同之处在于，它们都可以精确而廉价地分割成任意大小的子程序，这使得在并行线程之间划分工作变得很容易。
         * 用于执行此任务的流泪库使用的抽象是spliterator，它由spliterator方法在Stream和Iterable上返回。
         * collection.parallelStream().collect(Collectors.toSet())
         * 对 parallelStream ， Collectors.toSet()先把输入分成多个部分，每部分生成一个 Set ，最后再把多个 Set 合成一个，性能更好还是更坏，取决于你的数据。
         *
         * Stream的collect方法执行的操作，称为可变缩减（mutable reductions），不适合并行性，因为组合集合的开销非常大。
         * @param strings 参数列表
         * @return List
         */
        @Override
        protected List<Map<String, Object>> doInBackground(String... strings) {
            ApplistFragment fragment = frag.get();
            String keyword = strings[0];
            if (fragment != null && fragment.getActivity() != null) {
                Activity activity = fragment.getActivity();
                PackageManager packageManager = activity.getPackageManager();
                SharedPreferences sharedPreferences = activity.getSharedPreferences(SettingActivity.SETTING, Context.MODE_PRIVATE);
                if (fragment.appInfoArrayList == null) {
                    fragment.appInfoArrayList = new ArrayList<>(packageManager.getInstalledApplications(0));
                }
                boolean showSystemApps = sharedPreferences.getBoolean(SettingActivity.SHOW_SYSTEM, false);
                //过滤
                List<Map<String, Object>> aiList = fragment.appInfoArrayList.stream()
                        // 并行化处理
//                        .parallel()
                        .filter(ai -> showSystemApps || ((ai.flags & ApplicationInfo.FLAG_SYSTEM) == 0))
                        .filter(ai -> {
                            if (StringUtil.notBlank(keyword)) {
                                String appName = packageManager.getApplicationLabel(ai).toString();
                                return (appName + ai.packageName).toLowerCase().contains(keyword.toLowerCase());
                            }
                            return true;
                        })
                        .map(ai -> {
                            HashMap<String, Object> appInfo = new HashMap<>(3);
                            try {
                                PackageInfo pi = packageManager.getPackageInfo(ai.packageName, 0);
                                appInfo.put("name", packageManager.getApplicationLabel(ai) + " v" + pi.versionName + "(" + pi.versionCode + ")");
                                appInfo.put("pkg", ai.packageName);
                                appInfo.put("icon", packageManager.getApplicationIcon(ai));
                            } catch (PackageManager.NameNotFoundException e1) {
                                e1.printStackTrace();
                            }
                            return appInfo;
                        })
                        // 证实paralleStream的forEach接口确实不能保证同步，同时也提出了解决方案：使用collect和reduce接口。
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
    public void onAttach(@NonNull Context context) {
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
     * {@code 第25条：将源文件限制为单个顶级类}
     * {@code 第51条：仔细设计方法签名}
     * 与布尔型参数相比，优先使用两个元素枚举类型
     */
    public interface OnFrgActionListener {
        enum Slide {
            /**
             * 滑动方向枚举
             */
            UP,
            DOWN
        }
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
         * @param slide 上划UP，下滑DOWN
         */
        void onListScroll(Slide slide);
    }
}
