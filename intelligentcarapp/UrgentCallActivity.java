package com.example.intelligentcarapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class UrgentCallActivity extends Activity {

    private TextView countDown;
    private Runnable conrunnble;
    private boolean uegentPhoneEnable = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_urgent_call);
        countDown = findViewById(R.id.countdown);

        //这里Handler的postDelayed方法，等待10000毫秒在执行run方法。
        //在Activity中我们经常需要使用Handler方法更新UI或者执行一些耗时事件，
        //并且Handler中post方法既可以执行耗时事件也可以做一些UI更新的事情，比较好用，推荐使用
//        new Handler().postDelayed(new Runnable(){
//            public void run(){
//                //等待10000毫秒后销毁此页面，并提示登陆成功
//                UrgentCallActivity.this.finish();
//            }
//        }, 5000);

        final Handler handler = new Handler();

        conrunnble = new Runnable() {

            long total = 5000;

            long interval = 1000;
            @Override
            public void run() {
                if(total > 0) {
                    handler.postDelayed(conrunnble,interval);

                    total = total - interval;

                    countDown.setText(String.valueOf(total / 1000));
                } else {
                    callUrgentPhone();
                    UrgentCallActivity.this.finish();
                }
            }
        };

        handler.post(conrunnble);
    }

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

}
