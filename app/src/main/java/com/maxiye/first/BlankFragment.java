package com.maxiye.first;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BlankFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BlankFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BlankFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String ARG_1 = "param1";
    public static final String ARG_2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFrgActionListener mListener;

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
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        PackageManager pm = getActivity().getPackageManager();
        ArrayList<ApplicationInfo> al = (ArrayList)pm.getInstalledApplications(0);
        ListView lv = (ListView) getActivity().findViewById(R.id.l_v);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mListener.onItemClick(view);
            }
        });
        ArrayList<CharSequence> strs = new ArrayList<>();
        if (mParam1 != null && !mParam1.isEmpty()){
            TextView tv = (TextView)getView().findViewById(R.id.frgtxt);
            try {
                ApplicationInfo app_info = pm.getApplicationInfo(mParam1,1);
                CharSequence appname = pm.getApplicationLabel(app_info)+"："+app_info.packageName;
                tv.setText(appname);
            }catch (PackageManager.NameNotFoundException e){
                for (ApplicationInfo ai:al){
                    if((ai.flags & ApplicationInfo.FLAG_SYSTEM) == 0){
                        String app_name = pm.getApplicationLabel(ai).toString();
                        if((app_name+ai.packageName).toLowerCase().indexOf(mParam1.toLowerCase())>=0){
                            try{
                                PackageInfo pi = pm.getPackageInfo(ai.packageName,0);
                                strs.add(app_name+" v"+pi.versionName+"("+pi.versionCode+")："+ai.packageName);
                            }catch (PackageManager.NameNotFoundException ee){
                                strs.add(app_name+"："+ai.packageName);
                            }
                        }
                    }
                }
                if(!strs.isEmpty()){
//                    tv.setText(strs.toString());
                    lv.setAdapter(new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1, (String[])strs.toArray(new String[strs.size()])));
                }else{
                    tv.setText(R.string.not_found);
                }


            }
        }else{
            for (ApplicationInfo ai:al){
                if((ai.flags & ApplicationInfo.FLAG_SYSTEM) == 0){
                    try{
                        PackageInfo pi = pm.getPackageInfo(ai.packageName,0);
                        strs.add(pm.getApplicationLabel(ai)+" v"+pi.versionName+"("+pi.versionCode+")："+ai.packageName);
                    }catch (PackageManager.NameNotFoundException e){
                        strs.add(pm.getApplicationLabel(ai)+"："+ai.packageName);
                    }
                }

            }
            lv.setAdapter(new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_list_item_1, (String[])strs.toArray(new String[strs.size()])));
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
        public void onItemClick(View view);
    }
}
