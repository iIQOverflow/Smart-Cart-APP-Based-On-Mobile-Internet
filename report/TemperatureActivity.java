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

public class TemperatureActivity extends AppCompatActivity {

    private String userAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature);
        //获取账户
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        userAccount = pref.getString("account","");

        getTemperatureDataWeek();

        getTemperatureDataMonth();
    }

    private void getTemperatureDataWeek() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    final ExchangeInfo exchangeInfo = new ExchangeInfo(TemperatureActivity.this,
                            TemperatureActivity.this,userAccount,"7");
                    final String data = exchangeInfo.getHealthDataJson();
                    exchangeInfo.setDataHistory(data);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LineChart lineChartWeek = new LineChart("体温");
                            LineChartView lineChartView = findViewById(R.id.temp_week);
                            lineChartWeek.setLineChartView(lineChartView);
                            lineChartWeek.setAxisXLabels(exchangeInfo.getTempWeekXLabels());
                            lineChartWeek.setAxisPoints(exchangeInfo.getTemperatureWeek());
                            lineChartWeek.initLineChart();
                        }
                    });
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void getTemperatureDataMonth() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    final ExchangeInfo exchangeInfo = new ExchangeInfo(TemperatureActivity.this,
                            TemperatureActivity.this,userAccount,"30");
                    final String data = exchangeInfo.getHealthDataJson();
                    exchangeInfo.setDataHistory(data);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LineChart lineChartMonth = new LineChart("体温");
                            LineChartView lineChartView = findViewById(R.id.temp_month);
                            lineChartMonth.setLineChartView(lineChartView);
                            lineChartMonth.setAxisXLabels(exchangeInfo.getTempMonthXLabels());
                            lineChartMonth.setAxisPoints(exchangeInfo.getTemperatureMonth());
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
