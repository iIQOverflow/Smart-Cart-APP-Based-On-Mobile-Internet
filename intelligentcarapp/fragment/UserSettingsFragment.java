package com.example.intelligentcarapp.fragment;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.intelligentcarapp.R;

public class UserSettingsFragment extends Fragment {

    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    public static UserSettingsFragment newInstance(Context context) {
        UserSettingsFragment fragment = new UserSettingsFragment();
        Bundle args = new Bundle();
        mContext = context;
//        args.putString("agrs1", param1);
        fragment.setArguments(args);
        return fragment;
    }

    public UserSettingsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_settings, container, false);
//        Bundle bundle = getArguments();
//        String agrs1 = bundle.getString("agrs1");

        //对该页面的TextView进行初始化
        TextView nameTextView = view.findViewById(R.id.user_name);
        TextView emailTextView = view.findViewById(R.id.user_email);
        TextView deviceTextView = view.findViewById(R.id.user_device);
        TextView sexTextView = view.findViewById(R.id.user_sex);
        TextView heightTextView = view.findViewById(R.id.user_height);
        TextView birthTextView = view.findViewById(R.id.user_birth);
        TextView urgentPhoneTextView = view.findViewById(R.id.user_urgent_phone);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        nameTextView.setText(pref.getString("account",""));
        emailTextView.setText(pref.getString("email",""));
        deviceTextView.setText(pref.getString("device",""));
        if (pref.getBoolean("sex",true)) {
            sexTextView.setText("男");
        } else {
            sexTextView.setText("女");
        }
        heightTextView.setText(pref.getInt("height",0) + "cm");
        birthTextView.setText(pref.getInt("birth",1900) + "年");
        urgentPhoneTextView.setText(pref.getString("urgent_phone","17863935933"));

        return view;
    }

}
