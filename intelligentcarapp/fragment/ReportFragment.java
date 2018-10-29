package com.example.intelligentcarapp.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.charts.LineChart;
import com.example.charts.LineChartsAdapter;
import com.example.data.ExchangeInfo;
import com.example.intelligentcarapp.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lecho.lib.hellocharts.view.LineChartView;

public class ReportFragment extends Fragment {

    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    @SuppressLint("StaticFieldLeak")
    private static Activity mActivity;

    private String userAccount;

    private ExchangeInfo exchangeInfo;

    private RecyclerView recyclerView;

    private LineChart[] lineCharts = {
            new LineChart("体温"),
            new LineChart("体重"),
            new LineChart("心率"),
            new LineChart("血压"),
            new LineChart("血脂"),

    };

    private List<LineChart> lineChartList = new ArrayList<>();

    private LineChartsAdapter adapter;

    public static ReportFragment newInstance(Context context,Activity activity) {
        ReportFragment fragment = new ReportFragment();
        Bundle args = new Bundle();
//        args.putString("account", account);
//        args.putString("data",data);
        mContext = context;
        mActivity = activity;
        fragment.setArguments(args);
        return fragment;
    }

    public ReportFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_data_report, container, false);
//        Bundle bundle = getArguments();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        userAccount = pref.getString("account","");
//        TextView tv = (TextView)view.findViewById(R.id.tv_location);
//        tv.setText(agrs1);
        //滚动界面设置
        try {
            Log.d("ReportFragment",userAccount);
            recyclerView = view.findViewById(R.id.recycler_view);
            getMonthData();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }

    private void getMonthData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    exchangeInfo = new ExchangeInfo(mContext,
                            mActivity,userAccount,"30");
                    final String data = exchangeInfo.getHealthDataJson();
                    exchangeInfo.setDataHistory(data);
                    Log.d("ReportFragment_data",data);
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            initLineChartView();
                            initLineCharts(exchangeInfo);//初始化线性表
                            GridLayoutManager layoutManager = new GridLayoutManager(
                                    mContext,1);
                            recyclerView.setLayoutManager(layoutManager);
                            adapter = new LineChartsAdapter(lineChartList);
                            recyclerView.setAdapter(adapter);
                            //获取账户
                            adapter.setUserAccount(userAccount);
                        }
                    });

                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void initLineChartView() {

        LineChartView temperatureLineChart = new LineChartView(mContext);
        lineCharts[0].setLineChartView(temperatureLineChart);

        LineChartView weightLineChart = new LineChartView(mContext);
        lineCharts[1].setLineChartView(weightLineChart);

        LineChartView heartBeatLineChart = new LineChartView(mContext);
        lineCharts[2].setLineChartView(heartBeatLineChart);

        LineChartView bloodPressureLineChart = new LineChartView(mContext);
        lineCharts[3].setLineChartView(bloodPressureLineChart);

        LineChartView bloodFatLineChart = new LineChartView(mContext);
        lineCharts[4].setLineChartView(bloodFatLineChart);
    }


    private void initLineCharts(ExchangeInfo exchangeInfo) {

        //lineCharts[0].setAxisXLabels(exchangeInfo.getTempMonthXLabels());
        lineCharts[0].setAxisPoints(exchangeInfo.getTemperatureMonth());
        lineCharts[0].initLineChart();
        //lineCharts[0].setViewport(40,36);


        //lineCharts[1].setAxisXLabels(exchangeInfo.getWeightMonthXLabels());
        lineCharts[1].setAxisPoints(exchangeInfo.getWeightMonth());
        lineCharts[1].initLineChart();
        //lineCharts[1].setViewport(40,36);

        //lineCharts[2].setAxisXLabels(exchangeInfo.getHeartMonthXLabels());
        lineCharts[2].setAxisPoints(exchangeInfo.getHeartBeatMonth());
        lineCharts[2].initLineChart();
        //lineCharts[2].setViewport(190,40);

       // lineCharts[3].setAxisXLabels(exchangeInfo.getPressureMonthXLabels());
        lineCharts[3].setAxisPoints(exchangeInfo.getBloodHighPressureMonth());
        lineCharts[3].initLineChart();
        lineCharts[3].addLines(exchangeInfo.getBloodLowPressureMonth());
        //lineCharts[3].setViewport(40,36);

        //lineCharts[4].setAxisXLabels(exchangeInfo.getFatMonthXLabels());
        lineCharts[4].setAxisPoints(exchangeInfo.getBloodFatMonth());
        lineCharts[4].initLineChart();
        //lineCharts[4].setViewport(6,5);

        lineChartList.clear();
        lineChartList.addAll(Arrays.asList(lineCharts).subList(0, 5));
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("ReportFragment","on Resume");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("ReportFragment","on Start");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("ReportFragment","on Destroy");
    }
}
