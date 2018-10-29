package com.example.intelligentcarapp.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.data.NormalData;
import com.example.intelligentcarapp.Constants;
import com.example.intelligentcarapp.R;
import com.example.intelligentcarapp.RealTimeDataReceiver;

public class RealTimeFragment extends Fragment {

    private static Context mContext;

    private static Activity mActivity;

    private IntentFilter intentFilter;

    private LocalReceiver localReceiver;

    LocalBroadcastManager localBroadcastManager;

    int cnt = 0;

    private TextView tempTextView;
    private TextView heartTextView;
    private TextView pressureHighTextView;
    private TextView pressureLowTextView;
    private TextView bloodOxygenTextView;
    private TextView microCycleTextView;
    private TextView heightTextView;
    private TextView scoreTextView;
    private TextView adviceTextView;

    public static RealTimeFragment newInstance(Context context,Activity activity) {
        RealTimeFragment fragment = new RealTimeFragment();
        Bundle args = new Bundle();
//        args.putString("agrs1", param1);
        mContext = context;
        mActivity = activity;
        fragment.setArguments(args);
        return fragment;
    }

    public RealTimeFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_real_time, container, false);
        Bundle bundle = getArguments();
        String agrs1 = bundle.getString("agrs1");
        //TextView tv = (TextView)view.findViewById(R.id.tv_location);
        //tv.setText(agrs1);
        heartTextView = view.findViewById(R.id.heart_now);
        tempTextView = view.findViewById(R.id.temp_now);
        microCycleTextView = view.findViewById(R.id.weight_now);
        bloodOxygenTextView = view.findViewById(R.id.fat_now);
        pressureHighTextView = view.findViewById(R.id.pressure_high_now);
        pressureLowTextView = view.findViewById(R.id.pressure_low_now);
        heightTextView = view.findViewById(R.id.height_now);

        scoreTextView = view.findViewById(R.id.score_now);
        adviceTextView = view.findViewById(R.id.advice_now);
        localBroadcastManager = LocalBroadcastManager.getInstance(mContext);
        intentFilter = new IntentFilter();
        intentFilter.addAction("realtime.fragment.LOCAL_BROADCAST");
        localReceiver = new LocalReceiver();
        localBroadcastManager.registerReceiver(localReceiver,intentFilter);

        return view;
    }

    public class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, final Intent intent) {
            final float temperature = intent.getFloatExtra("temperature",0);
            final int heart = intent.getIntExtra("heart",0);
            final int micro_circle = intent.getIntExtra("micro_circle",0);
            final float blood_oxygen = intent.getFloatExtra("blood_oxygen",0);
            final int pressure_high = intent.getIntExtra("pressure_high",0);
            final int pressure_low = intent.getIntExtra("pressure_low",0);

//            Log.d("data_receiver_realtime","temoerature:"+temperature + )

            final int score = getScore(heart,temperature,micro_circle,blood_oxygen,pressure_high,pressure_low);
            final String advice = getAdvice(heart,temperature,micro_circle,blood_oxygen,pressure_high,pressure_low);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                tempTextView.setText(String.valueOf(temperature));
                                Log.d("heart_realtime",String.valueOf(heart));
                                heartTextView.setText(String.valueOf(heart));
                                microCycleTextView.setText(String.valueOf(micro_circle));
                                bloodOxygenTextView.setText(String.valueOf(blood_oxygen));
                                pressureHighTextView.setText(String.valueOf(pressure_high));
                                pressureLowTextView.setText(String.valueOf(pressure_low));
                                scoreTextView.setText(String.valueOf(score));
                                adviceTextView.setText(advice);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }).start();
        }
    }

    /**
     *
     * @param heart 心率
     * @param temperature 温度
     * @param micro_circle 微循环
     * @param blood_oxygen 血样
     * @param pressure_high 血压高压
     * @param pressure_low 血压低压
     * @return
     */
    private int getScore(int heart,float temperature,int micro_circle,float blood_oxygen,int pressure_high,int pressure_low) {
        int score = 100;

        if (heart > NormalData.NormalHeartHigh || heart < NormalData.NormalHeartLow) {
            score = score - 16;
        }
        if (temperature > NormalData.NormalTemperatureHigh || temperature < NormalData.NormalTemperatureLow) {
            score = score - 18;
        }
        if (pressure_high > NormalData.NormalHighPressureHigh || pressure_high < NormalData.NormalHighPressureLow) {
            score = score - 19;
        }
        if (pressure_low > NormalData.NormalLowPressureHigh || pressure_low < NormalData.NormalLowPressureLow) {
            score = score - 16;
        }

        if (temperature < 33f || heart < 10) {
            score = 0;
        }
        return score;
    }

    private String getAdvice(int heart,float temperature,int weight,float fat,int pressure_high,int pressure_low) {
        StringBuilder stringBuilder = new StringBuilder();

        if (heart > NormalData.NormalHeartHigh) {
            stringBuilder.append("实时心率过高，请舒缓心情，及时就医\n");
        } else if (heart < NormalData.NormalHeartLow) {
            stringBuilder.append("实时心率过低，请检查身体，及时就医\n");
        }
        if (temperature > NormalData.NormalTemperatureHigh ) {
            stringBuilder.append("实时体温偏高\n");
        } else if (temperature < NormalData.NormalTemperatureLow && temperature > 32f) {
            stringBuilder.append("实时体温偏低\n");
        }
        if (pressure_high > NormalData.NormalHighPressureHigh) {
            stringBuilder.append("血压高压过高，建议您就近停车，并及时拨打急救电话\n");
        } else if (pressure_high < NormalData.NormalHighPressureLow) {
            stringBuilder.append("血压高压过低，建议您就近停车，并及时拨打急救电话\n");
        }
        if (pressure_low > NormalData.NormalLowPressureHigh) {
            stringBuilder.append("血压低压过高，建议您就近停车，并及时拨打急救电话\n");
        } else if (pressure_low < NormalData.NormalLowPressureLow) {
            stringBuilder.append("血压低压过低，建议您就近停车，并及时拨打急救电话\n");
        }

        if (stringBuilder.length() <= 5) {
            stringBuilder.append("各项健康指标良好\n");
        }

        stringBuilder.delete(stringBuilder.length(),stringBuilder.length());

        if (temperature <= 33f || heart < 10) {
            stringBuilder.delete(0,stringBuilder.length());
            stringBuilder.append("当前智能硬件测量不到数据，请握紧方向盘");
        }

        return stringBuilder.toString();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(localReceiver);
    }

}
