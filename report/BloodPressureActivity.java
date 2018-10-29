package com.example.report;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.charts.LineChart;
import com.example.data.ExchangeInfo;
import com.example.intelligentcarapp.R;

import lecho.lib.hellocharts.view.LineChartView;

public class BloodPressureActivity extends AppCompatActivity {

    private String userAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood_pressure);
        //获取账户
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        userAccount = pref.getString("account","");

        getBloodPressureDataWeek();

        getBloodPressureDataMonth();
    }

    private void getBloodPressureDataWeek() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    final ExchangeInfo exchangeInfo = new ExchangeInfo(BloodPressureActivity.this,
                            BloodPressureActivity.this,userAccount,"7");
                    final String data = exchangeInfo.getHealthDataJson();
                    exchangeInfo.setDataHistory(data);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LineChart lineChartWeek = new LineChart("血压");
                            LineChartView lineChartView = findViewById(R.id.pressure_week);
                            lineChartWeek.setLineChartView(lineChartView);
                            lineChartWeek.setAxisXLabels(exchangeInfo.getPressureWeekXLabels());
                            lineChartWeek.setAxisPoints(exchangeInfo.getBloodHighPressureWeek());
                            lineChartWeek.initLineChart();
                            lineChartWeek.addLines(exchangeInfo.getBloodLowPressureWeek());
                        }
                    });
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void getBloodPressureDataMonth() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    final ExchangeInfo exchangeInfo = new ExchangeInfo(BloodPressureActivity.this,
                            BloodPressureActivity.this,userAccount,"30");
                    final String data = exchangeInfo.getHealthDataJson();
                    exchangeInfo.setDataHistory(data);
                    Log.d("BloodFatActivity_data",data);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LineChart lineChartMonth = new LineChart("血压");
                            LineChartView lineChartView = findViewById(R.id.pressure_month);
                            lineChartMonth.setLineChartView(lineChartView);
                            lineChartMonth.setAxisXLabels(exchangeInfo.getPressureMonthXLabels());
                            lineChartMonth.setAxisPoints(exchangeInfo.getBloodHighPressureMonth());
                            lineChartMonth.initLineChart();
                            lineChartMonth.addLines(exchangeInfo.getBloodLowPressureMonth());
                        }
                    });
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
