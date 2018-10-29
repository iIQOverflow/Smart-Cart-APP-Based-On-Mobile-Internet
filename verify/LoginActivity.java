package com.example.verify;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.data.ExchangeInfo;
import com.example.intelligentcarapp.BaseActivity;
import com.example.intelligentcarapp.BlueToothActivity;
import com.example.intelligentcarapp.MainActivity;
import com.example.intelligentcarapp.R;

import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends BaseActivity {

    private SharedPreferences pref;

    private SharedPreferences.Editor editor;

    private EditText accountEdit;

    private EditText passwordEdit;

    private Button login;

    private Button register;

    private Button thanks;

    private CheckBox rememberPass;

    private String healthData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        accountEdit = findViewById(R.id.account);
        passwordEdit = findViewById(R.id.password);
        rememberPass = findViewById(R.id.remember_pass);
        register = findViewById(R.id.register);
        login = findViewById(R.id.login);
        thanks = findViewById(R.id.thanks);
        boolean isRemember = pref.getBoolean("remember_password",false);
        if (isRemember) {
            String account = pref.getString("account","");
            String password = pref.getString("password","");
            accountEdit.setText(account);
            passwordEdit.setText(password);
            rememberPass.setChecked(true);
        }
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String account = accountEdit.getText().toString();
                String password = passwordEdit.getText().toString();
                if (isLoginValid() && isAccountValid(account) && isPasswordValid(password)) {
                    Login(account,password);
                } else {

                }
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });

        thanks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,ThanksActivity.class);
                startActivity(intent);
            }
        });

        //获取相关权限
        initPermission();
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 6;
    }

    private boolean isAccountValid(String account) {
        //TODO: Replace this with your own logic
        return account.length() > 6;
    }

    private boolean isLoginValid(){
        boolean valid = false;
        String account = accountEdit.getText().toString();
        String password = passwordEdit.getText().toString();
        if (!isAccountValid(account)){
            //LoginDialog("请输入有效的用户名，长度大于6位");
            Toast.makeText(LoginActivity.this,"请输入有效的用户名，长度大于6位",
                    Toast.LENGTH_SHORT).show();
            accountEdit.setText(null);
            passwordEdit.setText(null);
        } else if (!isPasswordValid(password)) {
            Toast.makeText(LoginActivity.this,"请输入有效的密码，长度大于6位",
                    Toast.LENGTH_SHORT).show();
            passwordEdit.setText(null);
        } else {
            valid = true;
        }

        return valid;
    }

    private void Login(final String account,final String password){
        new Thread(new Runnable(){
            @Override
            public void run(){
                try{
                    if(ContextCompat.checkSelfPermission(LoginActivity.this,
                            Manifest.permission.INTERNET)!= PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(LoginActivity.this,new String[]
                                {Manifest.permission.INTERNET},1);
                    }
                    OkHttpClient client = new OkHttpClient();

                    MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                    RequestBody requestBody = RequestBody.create(JSON,"{ \"account\":\"" +
                            account+"\" , \"password\":\"" +
                            password+"\" }");

                    Request request = new Request.Builder()
                            .url("https://wozaidaxue.com/car/login")
                            .post(requestBody)
                            .build();
                    Response response = client.newCall(request).execute();
                    final String responseData = response.body().string();
                    //---------------------------------------进行登录的处理，false代表用户名或密码错误 ，true 代表正确，可以跳转到下面页面
                    getAllWeekData();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(responseData.equals("true")){
                                editor = pref.edit();
                                if(rememberPass.isChecked()){
                                    editor.putBoolean("remember_password",true);

                                    editor.putString("account",account);
                                    editor.putString("password",password);

                                } else {
                                    editor.clear();
                                    editor.putString("account",account);
                                }
                                //用户信息存储本地到数据库
                                editor.putString("email","fengjianyongcn@gmail.com");
                                editor.putString("device","RX000001");
                                editor.putBoolean("sex",true); //是否为男性
                                editor.putInt("height",178);
                                editor.putInt("birth",1996);
                                editor.putString("urgent_phone","17863931392");

                                editor.apply();
                                final String healthData = getHealthData();
                                Intent intent = new Intent(LoginActivity.this,BlueToothActivity.class);
//                                intent.putExtra("account",accountEdit.getText().toString());
//                                intent.putExtra("responseData",healthData);
                                Log.d("LoginActivity",account);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this,"账号或密码错误！",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    response.body().close();

                }catch(Exception e){
                    e.printStackTrace();

                }
            }
        }).start();
    }

    private void getAllWeekData() {
        try{
            final ExchangeInfo exchangeInfo = new ExchangeInfo(LoginActivity.this,
                    LoginActivity.this,accountEdit.getText().toString(),"30");
            setResponseData(exchangeInfo.getHealthDataJson());

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initPermission() {
        String[] permissions = {
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_SETTINGS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CALL_PHONE

        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                // 进入到这里代表没有权限.
            }
        }
        String[] tmpList = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 此处为android 6.0以上动态授权的回调，用户自行实现。
    }

    public void setResponseData(String responseData) {
        this.healthData = responseData;
    }

    public String getHealthData() {
        return healthData;
    }
}
