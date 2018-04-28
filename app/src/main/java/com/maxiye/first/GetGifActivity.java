package com.maxiye.first;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetGifActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    protected static ArrayList<String[]> gifList = new ArrayList<>();

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_gif);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());
        Log.w("end", "onCreateOver");
    }

    /**
     * 简写toast
     *
     * @param msg 消息
     */
    private void alert(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_get_gif, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private int artId = 1032248;
        private int page = 1;
        private boolean endFlg = false;
        private Handler handler;
        private static final int MSG_TYPE_START = 100;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_get_gif, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, new Date().getTime()));
            handler = new Handler(msg -> {
                switch (msg.what) {
                    case MSG_TYPE_START:
                        loadGif(getArguments().getInt(ARG_SECTION_NUMBER), rootView);
                        break;
                }
                return false;
            });
            new Thread(this::loadGifList).start();
            return rootView;
        }

        void loadGif(int position, View root) {
            int startOffset = (position - 1) * 3;
            Log.w("info", "loadGif");
            if (root != null) {
                ImageView iv1 = root.findViewById(R.id.gif_1);
                iv1.setImageBitmap(getHttpGif(getGifInfo(startOffset++)[0]));
                TextView tv1 = root.findViewById(R.id.gtxt_1);
                tv1.setText(getGifInfo(startOffset++)[1]);
                ImageView iv2 = root.findViewById(R.id.gif_2);
                iv2.setImageBitmap(getHttpGif(getGifInfo(startOffset++)[0]));
                TextView tv2 = root.findViewById(R.id.gtxt_2);
                tv2.setText(getGifInfo(startOffset++)[1]);
                ImageView iv3 = root.findViewById(R.id.gif_3);
                iv3.setImageBitmap(getHttpGif(getGifInfo(startOffset)[0]));
                TextView tv3 = root.findViewById(R.id.gtxt_3);
                tv3.setText(getGifInfo(startOffset)[1]);
            }
            new Thread(this::loadGifList).start();

        }

        private void loadGifList() {
            if (endFlg) {
                return;
            }
            String baseUrl = "http://wap.gamersky.com/news/Content-" + artId;
            String reg = "alt=\".+\"[^>]*?src=\"(http://[^\"]+.gif)\"[^>]*>(<br>\\r\\n([^<]+))?<\\/p>";
            Pattern pt = Pattern.compile(reg);
            try {
                String url = baseUrl + (page > 1 ? "_" + page : "") + ".html";
                System.out.println(url);
                URL req = new URL(url);
                InputStream is = req.openStream();
                StringBuilder content = new StringBuilder();
                BufferedReader read = new BufferedReader(new InputStreamReader(is, "utf-8"));
                String line;//循环读取
                while ((line = read.readLine()) != null) {
                    content.append(line).append("\r\n");
                }
                read.close();
                Matcher mt = pt.matcher(content.toString());
                while(mt.find()) {
                    String name = (mt.group(3) == null ? UUID.randomUUID().toString() : mt.group(3)) + ".gif";
                    System.out.println(name);
                    String[] gifInfo = new String[2];
                    System.out.println(mt.group(1));
                    gifInfo[0] = mt.group(1);
                    gifInfo[1] = name;
                    gifList.add(gifInfo);
                }
                page++;
                handler.sendMessage(handler.obtainMessage(MSG_TYPE_START, ""));
            } catch (Exception e) {
                endFlg = true;
                e.printStackTrace();
            } finally {
                System.out.println(gifList.toString());
            }
        }

        public String[] getGifInfo(int offset) {
            Log.w("", "getGifInfo");
            if (gifList.size() <= offset) {
                loadGifList();
            }
            return gifList.get(offset);
        }

        private Bitmap getHttpGif(String url) {
            Bitmap bitmap;
            try {
                URL gurl = new URL(url);
                InputStream fis = gurl.openStream();
                bitmap = BitmapFactory.decodeStream(fis);
            } catch (Exception e) {
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_fingerprint_orange_24dp);
                e.printStackTrace();
            }
            return bitmap;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {

            // Show 3 total pages.
            return 31;
        }
    }
}
