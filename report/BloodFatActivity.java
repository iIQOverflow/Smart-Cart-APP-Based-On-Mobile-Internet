package com.example.report;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import com.example.charts.LineChart;
import com.example.data.ExchangeInfo;
import com.example.intelligentcarapp.R;

import lecho.lib.hellocharts.view.LineChartView;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import java.util.Calendar;


public class BloodFatActivity extends AppCompatActivity implements View.OnClickListener{

    private TabLayout tabLayout = null;
    private ViewPager vp_pager;
    private int n = 10;//
    private int mday;
    private int nday;
    private String userAccount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_blood_fat);
        tabLayout = (TabLayout) findViewById(R.id.tablayout);
        vp_pager = (ViewPager) findViewById(R.id.tab_viewpager);;
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
        MorePagerAdapter viewpager = new MorePagerAdapter();
        vp_pager.setAdapter(viewpager);
        vp_pager.setOffscreenPageLimit(1);
        tabLayout.setupWithViewPager(vp_pager);
        tabLayout.getTabAt(n-1).select();
    }


    final class MorePagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return n;
        }


        @Override
        public Object instantiateItem(final ViewGroup container, int position) {
            int flag = 0;
            if(n == 10){
                flag = Calendar.WEEK_OF_MONTH;
            }else{
                flag = Calendar.MONTH;
            }
            Calendar cur = Calendar.getInstance();Calendar c = Calendar.getInstance();
            c.add(flag,-1*(n-position) + 1);
            mday = calcDayOffset(c,cur);
            c.add(flag,1);
            nday = calcDayOffset(c,cur);

            //通过mday，nday请求/car/getinfo2，map.get("account"),map.get("nday"),map.get("mday")

            final LineChartView lineChartView = new LineChartView(BloodFatActivity.this);
            final LineChart lineChart = new LineChart("血脂");
            lineChart.setLineChartView(lineChartView);

            new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        //开始更新
                        startUpdate(container);
                        final float[] a = {5.1f,5.2f,5.5f};
                        final ExchangeInfo exchangeInfo = new ExchangeInfo(BloodFatActivity.this,
                                BloodFatActivity.this,userAccount,String.valueOf(mday),String.valueOf(nday));
                        final String data = exchangeInfo.getHealthDataJsonByTimeSlot();
                        exchangeInfo.setDataHistoryByTimeSlot(data);

                        lineChart.setLineChartView(lineChartView);
                        //数据的设置，坐标轴在LineChart中已经定义
                        //横坐标  x
                        lineChart.setAxisXLabels(exchangeInfo.getFatTimeSlotXLabels());
                        //点坐标  y
                        lineChart.setAxisPoints(exchangeInfo.getFatTimeSlotPoints());
                        lineChart.initLineChart();
                        //结束更新
                        finishUpdate(container);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();
//            TextView tv = new TextView(BloodFatActivity.this);
//            //tv.setText("布局"+position);
//            tv.setText("n:"+n+"position:"+position+"mday:" + mday+"nday:"+nday);//通过n和position来共同确定TimeSlot
//            tv.setTextSize(30.0f);
//            tv.setGravity(Gravity.CENTER);
            (container).addView(lineChart.getLineChartView());
            setPrimaryItem(container,n-1,lineChartView);
            return lineChartView;
        }

        public  int calcDayOffset(Calendar cal1, Calendar cal2) {
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
