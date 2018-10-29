package com.example.report;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.charts.LineChart;
import com.example.data.ExchangeInfo;
import com.example.intelligentcarapp.R;

import lecho.lib.hellocharts.view.LineChartView;

public class WeightActivity extends AppCompatActivity {

    private String userAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight);
        //获取账户
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        userAccount = pref.getString("account","");

        getWeightDataWeek();

        getWeightDataMonth();
    }

    private void getWeightDataWeek() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    final ExchangeInfo exchangeInfo = new ExchangeInfo(WeightActivity.this,
                            WeightActivity.this,userAccount,"7");
                    final String data = exchangeInfo.getHealthDataJson();
                    exchangeInfo.setDataHistory(data);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LineChart lineChartWeek = new LineChart("体重");
                            LineChartView lineChartView = findViewById(R.id.weight_week);
                            lineChartWeek.setLineChartView(lineChartView);
                            lineChartWeek.setAxisXLabels(exchangeInfo.getWeightWeekXLabels());
                            lineChartWeek.setAxisPoints(exchangeInfo.getWeightWeek());
                            lineChartWeek.initLineChart();
                        }
                    });
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void getWeightDataMonth() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    final ExchangeInfo exchangeInfo = new ExchangeInfo(WeightActivity.this,
                            WeightActivity.this,userAccount,"30");
                    final String data = exchangeInfo.getHealthDataJson();
                    exchangeInfo.setDataHistory(data);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LineChart lineChartMonth = new LineChart("体重");
                            LineChartView lineChartView = findViewById(R.id.weight_month);
                            lineChartMonth.setLineChartView(lineChartView);
                            lineChartMonth.setAxisXLabels(exchangeInfo.getWeightMonthXLabels());
                            lineChartMonth.setAxisPoints(exchangeInfo.getWeightMonth());
                            lineChartMonth.initLineChart();
                        }
                    });
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
