package com.example.data;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;

import com.example.charts.LineChart;
import com.example.charts.LineChartsAdapter;
import com.example.intelligentcarapp.R;
import com.example.report.BloodFatActivity;
import com.example.report.DriverInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.LinkedList;

import lecho.lib.hellocharts.view.LineChartView;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ExchangeInfo {

    private Context mContext;

    private Activity mActivity;

    private String mAccount;

    private String mHealthData;

    private String mDay;

    private String startDay;
    private String endDay;

    //每周的身体状况
    private float[] temperatureWeek;
    private float[] weightWeek;
    private float[] heartBeatWeek;
    private float[] bloodHighPressureWeek;
    private float[] bloodLowPressureWeek;
    private float[] bloodFatWeek;

    //每月的身体状况
    private float[] temperatureMonth;
    private float[] weightMonth;
    private float[] heartBeatMonth;
    private float[] bloodHighPressureMonth;
    private float[] bloodLowPressureMonth;
    private float[] bloodFatMonth;

    //x轴Label
    private String[] tempWeekXLabels;
    private String[] weightWeekXLabels;
    private String[] heartWeekXLabels;
    private String[] pressureWeekXLabels;
    private String[] fatWeekXLabels;

    private String[] tempMonthXLabels;
    private String[] weightMonthXLabels;
    private String[] heartMonthXLabels;
    private String[] pressureMonthXLabels;
    private String[] fatMonthXLabels;

    private String[] fatTimeSlotXLabels;

    private float[] fatTimeSlotPoints;

    private String[] heartTimeSlotXLabels;

    private float[] heartTimeSlotPoints;

    private String heartSuggestion;

    public String getHeartSuggestion() {
        return heartSuggestion;
    }

    private void setHeartSuggestion(String heartSuggestion) {
        this.heartSuggestion = heartSuggestion;
    }

    public String getStartDay() {
        return startDay;
    }

    public void setStartDay(String startDay) {
        this.startDay = startDay;
    }

    public String getEndDay() {
        return endDay;
    }

    public void setEndDay(String endDay) {
        this.endDay = endDay;
    }

    public Context getmContext() {
        return mContext;
    }

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }

    public Activity getmActivity() {
        return mActivity;
    }

    public void setmActivity(Activity mActivity) {
        this.mActivity = mActivity;
    }

    public String getmAccount() {
        return mAccount;
    }

    public void setmAccount(String mAccount) {
        this.mAccount = mAccount;
    }

    public String getmHealthData() {
        return mHealthData;
    }

    public void setmHealthData(String mHealthData) {
        this.mHealthData = mHealthData;
    }

    public String[] getHeartTimeSlotXLabels() {
        return heartTimeSlotXLabels;
    }

    public void setHeartTimeSlotXLabels(String[] heartTimeSlotXLabels) {
        this.heartTimeSlotXLabels = heartTimeSlotXLabels;
    }

    public float[] getHeartTimeSlotPoints() {
        return heartTimeSlotPoints;
    }

    public void setHeartTimeSlotPoints(float[] heartTimeSlotPoints) {
        this.heartTimeSlotPoints = heartTimeSlotPoints;
    }

    public String[] getFatTimeSlotXLabels() {
        return fatTimeSlotXLabels;
    }

    public void setFatTimeSlotXLabels(String[] fatTimeSlotXLabels) {
        this.fatTimeSlotXLabels = fatTimeSlotXLabels;
    }

    public float[] getFatTimeSlotPoints() {
        return fatTimeSlotPoints;
    }

    public void setFatTimeSlotPoints(float[] fatTimeSlotPoints) {
        this.fatTimeSlotPoints = fatTimeSlotPoints;
    }

    public ExchangeInfo(){

    }

    public ExchangeInfo(Context context,Activity activity,String account,String nday){
        this.mContext = context;
        this.mActivity = activity;
        this.mAccount = account;
        this.mDay = nday;
    }
    public ExchangeInfo(Context context,Activity activity,String account,String startDay,String endDay){
        this.mContext = context;
        this.mActivity = activity;
        this.mAccount = account;
        this.startDay = startDay;
        this.endDay = endDay;
    }


    public static void postInfo(final Context mContext,final Activity mActivity,final String account, final String weight, final String temp,
                          final String heart, final String pressure,
                          final String fat, final String urgent){
        try{
            if(ContextCompat.checkSelfPermission(mContext,
                    Manifest.permission.INTERNET)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(mActivity,new String[]
                        {Manifest.permission.INTERNET},1);
            }
            OkHttpClient client = new OkHttpClient();

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(JSON,"{\"account\":\"" +
                    account+"\",\"weight\":\"" +
                    weight+"\",\"temp\":\"" +
                    temp+"\",\"heart\":\"" +
                    heart+"\",\"pressure\":\"" +
                    pressure+"\",\"fat\":\"" +
                    fat+"\",\"urgent\":\"" +
                    urgent+"\"}");

            Request request = new Request.Builder()
                    .url("https://wozaidaxue.com/car/postinfo")
                    .post(requestBody)
                    .build();
            Response response = client.newCall(request).execute();
            String responseData = response.body().string();
            Log.d("postinfo",responseData);
            //---------------------------------------true代表成功，false代表未知错误

            response.body().close();

        }catch(Exception e){
            e.printStackTrace();

        }
    }
    public String getHealthDataJson() {
        String responseData = null;
        try{
            if(ContextCompat.checkSelfPermission(mContext,
                    Manifest.permission.INTERNET)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(mActivity,new String[]
                        {Manifest.permission.INTERNET},1);
            }
            OkHttpClient client = new OkHttpClient();

            //-------------------天数-----------------------//
            final String nday = getmDay();

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(JSON,"{\"account\":\"" +
                    mAccount+"\",\"nday\":\"" +
                    nday+"\"}");
            Request request = new Request.Builder()
                    .url("https://wozaidaxue.com/car/getinfo")
                    .post(requestBody)
                    .build();
            Response response = client.newCall(request).execute();
            responseData = response.body().string();
            Log.d("responseData",responseData);//获取的json字符串
            //---------------获取近n天的数据
            response.body().close();
        }catch(Exception e){
            e.printStackTrace();

        }
        return responseData;
    }

    public String getHealthDataJsonByTimeSlot() {
        String responseData = null;
        try{
            if(ContextCompat.checkSelfPermission(mContext,
                    Manifest.permission.INTERNET)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(mActivity,new String[]
                        {Manifest.permission.INTERNET},1);
            }
            OkHttpClient client = new OkHttpClient();

            //-------------------天数-----------------------//

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(JSON,"{\"account\":\"" +
                    mAccount+"\"," +"\"mday\":\"" +startDay+"\","+
                    "\"nday\":\"" +endDay+"\"}");
            Request request = new Request.Builder()
                    .url("https://wozaidaxue.com/car/getinfo2")
                    .post(requestBody)
                    .build();
            Response response = client.newCall(request).execute();
            responseData = response.body().string();
            Log.d("responseDataBySlot",responseData);//获取的json字符串
            //---------------获取近n天的数据
            response.body().close();
        }catch(Exception e){
            e.printStackTrace();

        }
        return responseData;
    }

    public void setDataHistoryByTimeSlot(String data) {
        Type listType = new TypeToken<LinkedList<DriverInfo>>(){}.getType();
        Gson gson = new Gson();
        LinkedList<DriverInfo> healthInfo = gson.fromJson(data, listType);
        fatTimeSlotPoints = new float[healthInfo.size()];
        heartTimeSlotPoints = new float[healthInfo.size()];
        fatTimeSlotXLabels = new String[healthInfo.size()];
        heartTimeSlotXLabels = new String[healthInfo.size()];
        Log.d("ReportFragment_size",String.valueOf(healthInfo.size()));
        for (int i = 0;i < healthInfo.size();i++) {
            fatTimeSlotPoints[i] = healthInfo.get(i).getFat() / 1000f;
            fatTimeSlotXLabels[i] = String.valueOf(i + 1);
            heartTimeSlotPoints[i] = healthInfo.get(i).getHeart();
            heartTimeSlotXLabels[i] = String.valueOf(i + 1);
        }

        String heartSuggestion =  healthInfo.get(healthInfo.size()-1).getSuggestion();
        setHeartSuggestion(heartSuggestion);
    }

    //--------------------------解析json--------------------------//
    public void setDataHistory(String data) {
        Type listType = new TypeToken<LinkedList<DriverInfo>>(){}.getType();
        Gson gson = new Gson();
        LinkedList<DriverInfo> healthInfo = gson.fromJson(data, listType);
        //Log.d("bloodfat",healthInfo.get(0).getFat() + "");
        arrayInitialized(healthInfo.size());
        Log.d("ReportFragment_size",String.valueOf(healthInfo.size()));
        if (mDay.equals("7")) {
            for (int i = 0;i < healthInfo.size();i++) {
                temperatureWeek[i] = healthInfo.get(i).getTemp() / 1000f;
                tempWeekXLabels[i] = String.valueOf(i + 1);

                weightWeek[i] = healthInfo.get(i).getWeight() / 1000f;
                weightWeekXLabels[i] = String.valueOf(i + 1);

                heartBeatWeek[i] = healthInfo.get(i).getHeart();
                heartWeekXLabels[i] = String.valueOf(i + 1);

                bloodHighPressureWeek[i] = healthInfo.get(i).getPressure() / 1000;
                bloodLowPressureWeek[i] = healthInfo.get(i).getPressure() % 1000;
                pressureWeekXLabels[i] = String.valueOf(i + 1);

                bloodFatWeek[i] = healthInfo.get(i).getFat() / 1000f;
                fatWeekXLabels[i] = String.valueOf(i + 1);
            }
        } else if (mDay.equals("30")) {
            for (int i = 0;i < healthInfo.size();i++) {
                temperatureMonth[i] = healthInfo.get(i).getTemp() / 1000f;
                Log.d("ReportFragment_temp",String.valueOf(temperatureWeek[i]));
                tempMonthXLabels[i] = String.valueOf(i + 1);

                weightMonth[i] = healthInfo.get(i).getWeight() / 1000f;
                weightMonthXLabels[i] = String.valueOf(i + 1);

                heartBeatMonth[i] = healthInfo.get(i).getHeart();
                heartMonthXLabels[i] = String.valueOf(i + 1);

                bloodHighPressureMonth[i] = healthInfo.get(i).getPressure() / 1000;
                bloodLowPressureMonth[i] = healthInfo.get(i).getPressure() % 1000;
                pressureMonthXLabels[i] = String.valueOf(i + 1);

                bloodFatMonth[i] = healthInfo.get(i).getFat() / 1000f;
                fatMonthXLabels[i] = String.valueOf(i + 1);
            }
        }
    }

    private void arrayInitialized(int size) {
        temperatureWeek = new float[size];
        weightWeek = new float[size];
        heartBeatWeek = new float[size];
        bloodHighPressureWeek = new float[size];
        bloodLowPressureWeek = new float[size];
        bloodFatWeek = new float[size];

        temperatureMonth = new float[size];
        weightMonth = new float[size];
        heartBeatMonth = new float[size];
        bloodHighPressureMonth = new float[size];
        bloodLowPressureMonth = new float[size];
        bloodFatMonth = new float[size];

        tempWeekXLabels = new String[size];
        weightWeekXLabels = new String[size];
        heartWeekXLabels = new String[size];
        pressureWeekXLabels = new String[size];
        fatWeekXLabels = new String[size];

        tempMonthXLabels = new String[size];
        weightMonthXLabels = new String[size];
        heartMonthXLabels = new String[size];
        pressureMonthXLabels = new String[size];
        fatMonthXLabels = new String[size];
    }

    public String getmDay() {
        return mDay;
    }

    public void setmDay(String mDay) {
        this.mDay = mDay;
    }

    public float[] getTemperatureWeek() {
        return temperatureWeek;
    }

    public float[] getWeightWeek() {
        return weightWeek;
    }

    public float[] getHeartBeatWeek() {
        return heartBeatWeek;
    }

    public float[] getBloodHighPressureWeek() {
        return bloodHighPressureWeek;
    }

    public float[] getBloodLowPressureWeek() {
        return bloodLowPressureWeek;
    }

    public float[] getBloodFatWeek() {
        return bloodFatWeek;
    }

    public float[] getTemperatureMonth() {
        return temperatureMonth;
    }

    public float[] getWeightMonth() {
        return weightMonth;
    }

    public float[] getHeartBeatMonth() {
        return heartBeatMonth;
    }

    public float[] getBloodHighPressureMonth() {
        return bloodHighPressureMonth;
    }

    public float[] getBloodLowPressureMonth() {
        return bloodLowPressureMonth;
    }

    public float[] getBloodFatMonth() {
        return bloodFatMonth;
    }

    public String[] getTempWeekXLabels() {
        return tempWeekXLabels;
    }

    public String[] getWeightWeekXLabels() {
        return weightWeekXLabels;
    }

    public String[] getHeartWeekXLabels() {
        return heartWeekXLabels;
    }

    public String[] getPressureWeekXLabels() {
        return pressureWeekXLabels;
    }

    public String[] getFatWeekXLabels() {
        return fatWeekXLabels;
    }

    public String[] getTempMonthXLabels() {
        return tempMonthXLabels;
    }

    public String[] getWeightMonthXLabels() {
        return weightMonthXLabels;
    }

    public String[] getHeartMonthXLabels() {
        return heartMonthXLabels;
    }

    public String[] getPressureMonthXLabels() {
        return pressureMonthXLabels;
    }

    public String[] getFatMonthXLabels() {
        return fatMonthXLabels;
    }
}
