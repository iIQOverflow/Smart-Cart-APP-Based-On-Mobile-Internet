package com.example.intelligentcarapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.Toolbar;
import android.util.AndroidRuntimeException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.baidu.speech.asr.SpeechConstant;
import com.example.data.ExchangeInfo;
import com.example.intelligentcarapp.fragment.DeviceFragment;
import com.example.intelligentcarapp.fragment.RealTimeFragment;
import com.example.intelligentcarapp.fragment.ReportFragment;
import com.example.intelligentcarapp.fragment.UserSettingsFragment;
import com.example.intelligentcarapp.fragment.WeatherFragment;
import com.example.intelligentcarapp.voice.MyRecognition;
import com.example.intelligentcarapp.voice.MySpeech;
import com.example.intelligentcarapp.voice.MyWakeUp;
import com.example.intelligentcarapp.voice.MyWakeUpRecog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.speech.EventListener;

import java.util.LinkedHashMap;
import java.util.Map;

public class MainActivity extends BaseActivity implements BottomNavigationBar.OnTabSelectedListener {

    int lastSelectedPosition = 0;
    private RealTimeFragment realTimeFragment;
    private ReportFragment reportFragment;
    private WeatherFragment weatherFragment;
    private DeviceFragment deviceFragment;
    private UserSettingsFragment userSettingsFragment;

    private DrawerLayout mDrawerLayout;
    //语音合成s
    private MySpeech mMySpeech;
    public static final String TAG = "WakeUpRecognition";
    //语音识别参数定义
    private MyRecognition myRecognition;
    //语音唤醒参数定义
    private MyWakeUp myWakeUp;
    //语音唤醒后识别
    MyWakeUpRecog myWakeUpRecog;

    private Toolbar toolbar;

    //麦克风状态
    private boolean microphoneState = false;

    /**
     * 0: 方案1， 唤醒词说完后，直接接句子，中间没有停顿。
     * >0 : 方案2： 唤醒词说完后，中间有停顿，然后接句子。推荐4个字 1500ms
     * <p>
     * backTrackInMs 最大 15000，即15s
     */
    private int backTrackInMs = 1500;

    LocalBroadcastManager localBroadcastManager;

    private int heart;
    private float temperature;
    private int weight;
    private int lose_face;
    private int lose_face_en = 1;
    private int fatigue_driving_en = 1;
    private float fat;
    private int pressure_high;
    private int pressure_low;
    private int fatigue_driving;
    private int cnt = 0;
    private int cnt2 = 0;
    private boolean call = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        //onViewClick();
        try {
            //语音合成
            mMySpeech = new MySpeech(this);
            mMySpeech.initTTs();

            myRecognition = new MyRecognition(this); //语音识别
            myWakeUp = new MyWakeUp(this); //语音唤醒

            //语音唤醒后识别
            EventListener mWakeUpListener = new WakeUpListener();
            EventListener mRecognitionListener = new RecognitionListener();
            myWakeUpRecog = new MyWakeUpRecog(this, mWakeUpListener,
                    mRecognitionListener);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            //Set bottom menu
            //.addItem(new BottomNavigationItem(R.drawable.weather_info, "出行").setActiveColor(R.color.green))
            BottomNavigationBar bottomNavigationBar = findViewById(R.id.bottom_navigation_bar);
            bottomNavigationBar
                    .addItem(new BottomNavigationItem(R.drawable.data_now, "实时").setActiveColor(R.color.orange))
                    .addItem(new BottomNavigationItem(R.drawable.data_report, "报告").setActiveColor(R.color.blue))
                    .addItem(new BottomNavigationItem(R.drawable.device_settings, "脉象").setActiveColor(R.color.blue))
                    .addItem(new BottomNavigationItem(R.drawable.user_settings, "用户").setActiveColor(R.color.purple))
                    .setFirstSelectedPosition(lastSelectedPosition)
                    .initialise();
            bottomNavigationBar.setTabSelectedListener(this);
            setDefaultFragment();
        } catch (Exception e) {
            e.printStackTrace();
        }

        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("realtime.fragment.LOCAL_BROADCAST");
        LocalDataReceiver localReceiver = new LocalDataReceiver();
        localBroadcastManager.registerReceiver(localReceiver, intentFilter);

    }

    private int lose_face_sum =0;
    private int fatigue_driving_sum =0;

    public class LocalDataReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, final Intent intent) {
            heart = intent.getIntExtra("heart",0);
            temperature = intent.getFloatExtra("temperature", 0);
            lose_face = intent.getIntExtra("lose_face",0);
            weight = intent.getIntExtra("weight", 0);
            fat = intent.getFloatExtra("fat", 0);
            pressure_high = intent.getIntExtra("pressure_high", 0);
            pressure_low = intent.getIntExtra("pressure_low", 0);
            fatigue_driving = intent.getIntExtra("fatigue_driving", 0);
            lose_face_sum += lose_face;
            fatigue_driving_sum += fatigue_driving;
            Log.d("DataNow", "heart:" + heart + " " + "temperature:" + temperature + " "
                    + "lose_face:" + lose_face + "weight" + weight + " " + fat + " " + pressure_high + " " + pressure_low
                    + " " + "fatigue_driving: " + fatigue_driving);

            if(cnt++ > 6){
                cnt = 0;
                fatigue_driving_en = 1;
            }

            if(cnt2++ > 6){
                cnt2 = 0;
                lose_face_en = 1;
            }

            if (heart > 150 && heart < 180 && call) {
                call = false;
                mMySpeech.speak("您当前心率为" + heart + ",已经超过正常范围，即将为您拨打报警电话！");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(10000);
                            callUrgentPhone();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            } else if (temperature > 38f) {
                mMySpeech.speak("您当前体温为" + temperature + "摄氏度" + ",非常危险！");
            } else if (lose_face == 1 && lose_face_en == 1) {
                lose_face_en = 0;
                cnt2 = 0;
                mMySpeech.speak("当前摄像头捕捉不到人脸，请调整姿势");
//                new Handler().postDelayed(new Runnable(){
//                    public void run(){
//                        Intent intent2 = new Intent();
//                        intent2.setClass(MainActivity.this, UrgentCallActivity.class);//跳转到加载界面
//                        startActivity(intent2);
//                    }
//                }, 5000);

                //callUrgentPhone();
            } else if ( fatigue_driving == 1 && fatigue_driving_en == 1) {
                fatigue_driving_en = 0;
                cnt = 0;
                mMySpeech.speak("警告！检测到您可能存在疲劳驾驶行为！");


            }

            if (cnt++ >= 10 && heart >= 10 && temperature >= 35f && fat >= 2) {
                cnt = 0;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String pressure = String.valueOf(pressure_high * 1000 + pressure_low);
                        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                        String userAccount = pref.getString("account","");
                        Log.d("postInfo",userAccount +  " " + String.valueOf(weight * 1000) + " " +
                                String.valueOf((int)temperature * 1000) +  " " + String.valueOf(heart) + " " +
                                pressure +  " " + String.valueOf((int)fat * 1000) +  " " + String.valueOf(fatigue_driving));
                        ExchangeInfo.postInfo(MainActivity.this,MainActivity.this,userAccount,String.valueOf(weight * 1000),
                                String.valueOf((int)temperature * 1000),String.valueOf(heart),pressure,String.valueOf((int)fat * 1000),String.valueOf(fatigue_driving));
                    }
                }).start();
            }
        }
    }

    /**
     * set the default fragment
     */
    private void setDefaultFragment() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        realTimeFragment = RealTimeFragment.newInstance(this, this);
        transaction.replace(R.id.ll_content, realTimeFragment);
        transaction.commit();
    }

    @Override
    public void onTabSelected(int position) {
        Log.d(TAG, "onTabSelected() called with: " + "position = [" + position + "]");
        FragmentManager fm = this.getFragmentManager();
        //开启事务
        FragmentTransaction transaction = fm.beginTransaction();
        switch (position) {
            case 0:
                if (realTimeFragment != null) {
                    realTimeFragment = null;
                }
                toolbar.setTitle("实时");
                realTimeFragment = RealTimeFragment.newInstance(this, this);
                transaction.replace(R.id.ll_content, realTimeFragment);
                break;
            case 1:
                if (reportFragment != null) {
                    reportFragment = null;
                }
                toolbar.setTitle("报告");
                reportFragment = ReportFragment.newInstance(this, this);
                transaction.replace(R.id.ll_content, reportFragment);
                break;
            case 2:
                if (deviceFragment == null) {
                    deviceFragment = DeviceFragment.newInstance(this,this);
                }
                toolbar.setTitle("脉象");
                transaction.replace(R.id.ll_content, deviceFragment);
                break;
            case 3:
                if (userSettingsFragment == null) {
                    userSettingsFragment = UserSettingsFragment.newInstance(this);
                }
                toolbar.setTitle("用户");
                transaction.replace(R.id.ll_content, userSettingsFragment);
                break;
            default:
                break;
        }
//                if (weatherFragment == null) {
//                    weatherFragment = WeatherFragment.newInstance();
//                }
//                toolbar.setTitle("出行");
//                transaction.replace(R.id.ll_content, weatherFragment);
//                break;
        // 事务提交
        transaction.commit();
    }

    @Override
    public void onTabUnselected(int position) {
        Log.d(TAG, "onTabUnselected() called with: " + "position = [" + position + "]");
    }

    @Override
    public void onTabReselected(int position) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @SuppressLint("RestrictedApi")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.voice:
                microphoneState = !microphoneState;
                if (microphoneState) {
                    ActionMenuItemView menuItem = findViewById(R.id.voice);
                    menuItem.setIcon(getDrawable(R.drawable.microphone_on));
                    Toast.makeText(this, "语音唤醒已开启", Toast.LENGTH_SHORT).show();
                    mMySpeech.speak("语音唤醒已开启");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                myWakeUpRecog.start();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                } else {
                    ActionMenuItemView menuItem = findViewById(R.id.voice);
                    menuItem.setIcon(getDrawable(R.drawable.microphone_off));
                    Toast.makeText(this, "语音唤醒已关闭", Toast.LENGTH_SHORT).show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                myWakeUpRecog.stop();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
                break;
//            case R.id.backup:
//            new Thread(new Runnable(){
//                @Override
//                public void run(){
//                    try{
//                        myWakeUpRecog.start();
//                    }catch(Exception e){
//                        e.printStackTrace();
//                    }
//                }
//            }).start();
//                break;
//            case R.id.delete:
////                Toast.makeText(this,"You clicked Delete",
////                        Toast.LENGTH_SHORT).show();
////                recogStart();
//                new Thread(new Runnable(){
//                    @Override
//                    public void run(){
//                        try{
//                            myWakeUpRecog.stop();
//                        }catch(Exception e){
//                            e.printStackTrace();
//                        }
//                    }
//                }).start();
//                break;
//            case R.id.settings:
////                Toast.makeText(this,"You clicked Settings",
////                        Toast.LENGTH_SHORT).show();
//                try {
//                    Intent intent = new Intent(this,BloodFatActivity.class);
//                    startActivity(intent);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
            default:
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder dialog = new
                AlertDialog.Builder(MainActivity.this);
        dialog.setMessage("确定退出程序吗");
        dialog.setCancelable(true);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ActivityController.finishAll();
            }
        });
        dialog.setNegativeButton("不了", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog.show();
    }

    /**
     * 唤醒监听类
     */
    private class WakeUpListener implements EventListener {
        @Override
        public void onEvent(String name, String params, byte[] data, int offset, int length) {
            try {
                if (params != null) {
                    //解析json文件
                    final JSONObject json = new JSONObject(params);
                    if ("wp.data".equals(name)) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    // 每次唤醒成功, 将会回调name=wp.data的时间, 被激活的唤醒词在params的word字段
                                    String word = json.getString("word"); // 唤醒词
                                    Log.d(TAG, word);

                                    // 此处 开始正常识别流程
                                    Map<String, Object> recogParams = new LinkedHashMap<>();
                                    recogParams.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
                                    recogParams.put(SpeechConstant.VAD, SpeechConstant.VAD_DNN);
                                    // 如识别短句，不需要需要逗号，使用1536搜索模型。其它PID参数请看文档
                                    recogParams.put(SpeechConstant.PID, 1536);
                                    if (backTrackInMs > 0) { // 方案1， 唤醒词说完后，直接接句子，中间没有停顿。
                                        recogParams.put(SpeechConstant.AUDIO_MILLS, System.currentTimeMillis() - backTrackInMs);

                                    }
                                    myWakeUpRecog.recognitionCancel();
                                    myWakeUpRecog.recognitionStart(recogParams);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
// else if ("wp.exit".equals(name)) {
//                        // 唤醒已经停止
//
//                    }
                }
            } catch (JSONException e) {
                throw new AndroidRuntimeException(e);
            }
        }
    }

    /**
     * 识别监听类
     */
    private class RecognitionListener implements EventListener {
        @Override
        public void onEvent(String name, final String params, byte[] data, int offset, int length) {
            try {
                if (params != null && params.contains("final_result")) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String finalAsrResult = null;
                                JSONObject json = new JSONObject(params);
                                JSONArray array = json.optJSONArray("results_recognition");
                                if (array != null) {
                                    finalAsrResult = array.getString(0);
                                }
                                Log.d("finalAsrResult", finalAsrResult);
                                commandAction(finalAsrResult);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            } catch (Exception e) {
                throw new AndroidRuntimeException(e);
            }
        }
    }

    private void commandAction(String command) {
        if (command != null) {
            if (command.contains("在吗") || command.contains("在码")) {
                switch ((int) (1 + Math.random() * (2 - 1 + 1))) {
                    case 1:
                        mMySpeech.speak("在的");
                        break;
                    case 2:
                        mMySpeech.speak("嗯");
                        break;
                    default:
                        break;
                }

            } else if (command.contains("体温") || command.contains("提问")) {
                mMySpeech.speak("您当前体温为" + temperature + "度");
            } else if (command.contains("心率")) {
                mMySpeech.speak("您当前心率为" + heart);
            } else if (command.contains("血压")) {
                mMySpeech.speak("您当前高压为" + pressure_high + "," + "低压为" + pressure_low);
            } else if (command.contains("血脂") || command.contains("靴子") || command.contains("设置")) {
                mMySpeech.speak("您当前的血脂为" + fat);
            } else if (command.contains("体重")) {
                mMySpeech.speak("您当前的体重为" + weight + "千克");
            }
        }
    }

//    public void onViewClick() {
//        CardView tempNowView = findViewById(R.id.temp_now_layout);
//        tempNowView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this,
//                        TemperatureActivity.class);
//                intent.putExtra("account",userAccount);
//                startActivity(intent);
//            }
//        });
//
//        CardView weightNowView = findViewById(R.id.weight_now_layout);
//        weightNowView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this,
//                        WeightActivity.class);
//                intent.putExtra("account",userAccount);
//                startActivity(intent);
//            }
//        });
//
//        CardView heartNowView = findViewById(R.id.heart_now_layout);
//        heartNowView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this,
//                        HeartActivity.class);
//                intent.putExtra("account",userAccount);
//                startActivity(intent);
//            }
//        });
//
//        CardView pressureNowView = findViewById(R.id.pressure_now_layout);
//        pressureNowView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this,
//                        BloodPressureActivity.class);
//                intent.putExtra("account",userAccount);
//                startActivity(intent);
//            }
//        });
//
//        CardView fatNowView = findViewById(R.id.fat_now_layout);
//        fatNowView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this,
//                        BloodFatActivity.class);
//                intent.putExtra("account",userAccount);
//                startActivity(intent);
//            }
//        });
//    }

    private void callUrgentPhone() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" +
                pref.getString("urgent_phone", "17863935933")));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        microphoneState = false;
        mMySpeech.release();
        super.onDestroy();
        myWakeUp.release();
        myRecognition.stop();
        myRecognition.release();
        myWakeUpRecog.release();
    }
}