package com.example.report;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.example.charts.LineChart;
import com.example.data.ExchangeInfo;
import com.example.intelligentcarapp.R;

import java.util.Calendar;

import lecho.lib.hellocharts.view.LineChartView;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HeartActivity extends AppCompatActivity implements View.OnClickListener{

    private TabLayout tabLayout = null;
    private ViewPager vp_pager;
    private TextView heartAnalyzeSuggestion;
    private int n = 10;//
    private String userAccount;
    private String[] weekData = new String[10];
    private String[] mouthData = new String[12];

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_heart);
        tabLayout = (TabLayout) findViewById(R.id.tablayout);
        vp_pager = (ViewPager) findViewById(R.id.tab_viewpager);;
        heartAnalyzeSuggestion = findViewById(R.id.heart_analyze_suggestion);
        findViewById(R.id.btn1).setOnClickListener(this);
        findViewById(R.id.btn2).setOnClickListener(this);
        initView();
        //获取账户
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        userAccount = pref.getString("account","");
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn1:
                tabLayout = (TabLayout) findViewById(R.id.tablayout);
                vp_pager = (ViewPager) findViewById(R.id.tab_viewpager);
                n = 10;
                initView();
                break;
            case R.id.btn2:
                tabLayout = (TabLayout) findViewById(R.id.tablayout);
                vp_pager = (ViewPager) findViewById(R.id.tab_viewpager);
                n = 12;
                initView();
                break;
        }
    }

    private void initView() {
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                final int position = tab.getPosition();
                Log.e("bbb","tab position:"+position+"----- n:"+n);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(n == 10){
                            heartAnalyzeSuggestion.setText(weekData[position]);
                            Log.e("bbb"," tab position data:"+weekData[position]);
                        }else{
                            heartAnalyzeSuggestion.setText(mouthData[position]);
                            Log.e("bbb"," tab position data:"+mouthData[position]);
                        }

                    }
                });
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        HeartActivity.MorePagerAdapter viewpager = new HeartActivity.MorePagerAdapter();
        vp_pager.setAdapter(viewpager);
        vp_pager.setOffscreenPageLimit(0);
        tabLayout.setupWithViewPager(vp_pager);
        tabLayout.getTabAt(n-1).select();
    }

    final class MorePagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return n;
        }

        @Override
        public Object instantiateItem(final ViewGroup container, final int position) {
            int offset = 0;int mday;int nday;//能用局部变量就不要用全局变量
            if(n == 10){//星期
                Calendar cur = Calendar.getInstance();
                Calendar c = Calendar.getInstance();

                int offsetDay = ( cur.get(Calendar.DAY_OF_WEEK)+7-1 ) % 7;
                c.add(Calendar.DAY_OF_YEAR,-1*offsetDay);
                c.add(Calendar.WEEK_OF_YEAR,-1*(n-position)+1);
                mday = calcDayOffset(c,cur);
                c.add(Calendar.WEEK_OF_YEAR,1);
                nday = calcDayOffset(c,cur);
                mday --;
                if(nday<0)
                    nday = 0;
            }else{//月

                Calendar cur = Calendar.getInstance();
                Calendar c = Calendar.getInstance();
                Calendar c2 = Calendar.getInstance();
                int offsetDay =cur.get(Calendar.DAY_OF_MONTH);
                c.add(Calendar.DAY_OF_MONTH,-1*offsetDay);
                c.add(Calendar.MONTH,-1*(n-position)+1);
                mday = calcDayOffset(c,cur);
                c2.add(Calendar.DAY_OF_MONTH,-1*offsetDay);
                c2.add(Calendar.MONTH,-1*(n-position)+2);
                nday = calcDayOffset(c2,cur);
                mday --;
                if(nday<0)
                    nday = 0;
            }


            final LineChartView lineChartView = new LineChartView(HeartActivity.this);
            final LineChart lineChart = new LineChart("心率");
            lineChart.setLineChartView(lineChartView);
            final int fmday = mday;
            final int fnday = nday;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //开始更新
                        startUpdate(container);
                        final float[] a = {5.1f,5.2f,5.5f};

                        final ExchangeInfo exchangeInfo = new ExchangeInfo();

                        String responseData = null;
                        if(ContextCompat.checkSelfPermission( HeartActivity.this,
                                Manifest.permission.INTERNET)!= PackageManager.PERMISSION_GRANTED){
                            ActivityCompat.requestPermissions(HeartActivity.this,new String[]
                                    {Manifest.permission.INTERNET},1);
                        }
                        OkHttpClient client = new OkHttpClient();
                        //-------------------天数-----------------------//
                        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                        RequestBody requestBody = RequestBody.create(JSON,"{\"account\":\"" +
                                userAccount+"\"," +"\"mday\":\"" +String.valueOf(fmday)+"\","+
                                "\"nday\":\"" +String.valueOf(fnday)+"\"}");
                        Request request = new Request.Builder()
                                .url("https://wozaidaxue.com/car/getinfo2")
                                .post(requestBody)
                                .build();
                        Response response = client.newCall(request).execute();
                        responseData = response.body().string();
                        Log.d("responseDataBySlot",responseData+"\n position"+position+"\n mday"+fmday+"\n nday"+fnday);//获取的json字符串

                        exchangeInfo.setDataHistoryByTimeSlot(responseData);
                        //---------------获取近n天的数据
                        lineChart.setLineChartView(lineChartView);
                        //数据的设置，坐标轴在LineChart中已经定义
                        //横坐标  x
                        lineChart.setAxisXLabels(exchangeInfo.getHeartTimeSlotXLabels());
                        //点坐标  y
                        lineChart.setAxisPoints(exchangeInfo.getHeartTimeSlotPoints());
                        lineChart.initLineChart();

                        if(n == 10){
                            weekData[position] = exchangeInfo.getHeartSuggestion();
                        }else{
                            mouthData[position] = exchangeInfo.getHeartSuggestion();
                        }
                        //结束更新
                        finishUpdate(container);
                        response.body().close();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();

            (container).addView(lineChart.getLineChartView());
            setPrimaryItem(container,n-1,lineChartView);
            return lineChartView;
        }

        private  int calcDayOffset(Calendar cal1, Calendar cal2) {
            int day1 = cal1.get(Calendar.DAY_OF_YEAR);
            int day2 = cal2.get(Calendar.DAY_OF_YEAR);

            int year1 = cal1.get(Calendar.YEAR);
            int year2 = cal2.get(Calendar.YEAR);
            if (year1 != year2) {  //同一年
                int timeDistance = 0;
                for (int i = year1; i < year2; i++) {
                    if (i % 4 == 0 && i % 100 != 0 || i % 400 == 0) {  //闰年
                        timeDistance += 366;
                    } else {  //不是闰年

                        timeDistance += 365;
                    }
                }
                return timeDistance + (day2 - day1);
            } else { //不同年
                return day2 - day1;
            }
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            (container).removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Calendar calendar = Calendar.getInstance();

            if(n == 10)//最近10周
                return getWeeks(n-position);
            else
                return getMonth(n-position);
        }

        private String getWeeks(int n){
            Calendar c = Calendar.getInstance();
            StringBuilder sb = new StringBuilder();

            int start_of_week = c.get(Calendar.DAY_OF_WEEK) - 1;
            if (start_of_week == 0)
                start_of_week = 7;
            c.add(Calendar.DATE, -start_of_week + 1);
            c.add(Calendar.WEEK_OF_MONTH, -1*n+1);
            sb.append(c.get(Calendar.MONTH)+1).append(".").append(c.get(Calendar.DAY_OF_MONTH)).append("-");

            c = Calendar.getInstance();
            int day_of_week = c.get(Calendar.DAY_OF_WEEK) - 1;
            if (day_of_week == 0)
                day_of_week = 7;
            c.add(Calendar.DATE, -day_of_week + 7);
            c.add(Calendar.WEEK_OF_MONTH, -1*n+1);
            sb.append(c.get(Calendar.MONTH)+1).append(".").append(c.get(Calendar.DAY_OF_MONTH));
            return sb.toString();
        }

        private String getMonth(int n){
            Calendar c = Calendar.getInstance();
            StringBuilder sb = new StringBuilder();
            c.add(Calendar.MONTH,-1*n+1);
            sb.append(c.get(Calendar.YEAR));
            sb.append("年");
            sb.append(c.get(Calendar.MONTH)+1);
            sb.append("月");
            return sb.toString();
        }

    }
}
