package com.example.verify;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.intelligentcarapp.BaseActivity;
import com.example.intelligentcarapp.MainActivity;
import com.example.intelligentcarapp.R;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends BaseActivity {

    private Button registerConfirm;

    private EditText accountRegister;
    private EditText passwordRegister;
    private EditText passwordConfirmRegister;
    private EditText ageRegister;
    private EditText emailRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        accountRegister = findViewById(R.id.account_register);
        passwordRegister = findViewById(R.id.password_register);
        passwordConfirmRegister = findViewById(R.id.password_confirm_register);
        ageRegister = findViewById(R.id.age_register);
        emailRegister = findViewById(R.id.email_register);
        registerConfirm = findViewById(R.id.confirm_register);
        registerConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRegisterValid()){
                    register(accountRegister.getText().toString(),
                            passwordRegister.getText().toString(),
                            ageRegister.getText().toString(),
                            emailRegister.getText().toString());
                } else {

                }
            }
        });
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@163.com")
                || email.contains("@gmail.com")
                || email.contains("@126.com")
                || email.contains("@139.com")
                || email.contains("@hostmail.com")
                || email.contains("@sina.com.com")
                || email.contains("@yahoo.com.com")
                || email.contains("@sohu.com.com")
                || email.contains("@qq.com");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 6;
    }

    private boolean isAccountValid(String account) {
        //TODO: Replace this with your own logic
        return account.length() > 6;
    }

    private boolean isAgeValid(String age) {
        //TODO: Replace this with your own logic
        boolean ageValid = false;
        if (age == "" || age == null){
            return false;
        }
        try {
            if (Integer.valueOf(age) >= 10 && Integer.valueOf(age) <=150)
                ageValid = true;
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            return ageValid;
        }
    }

    private boolean isRegisterValid(){
        boolean valid = false;
        String account = accountRegister.getText().toString();
        String password = passwordRegister.getText().toString();
        String passwordConfirm = passwordConfirmRegister.getText().toString();
        String age = ageRegister.getText().toString();
        String email = emailRegister.getText().toString();
        Log.d("RegisterActivity","account:" + account);
        Log.d("RegisterActivity","password:" + password);
        Log.d("RegisterActivity","passwordConfirmed:" + passwordConfirm);
        Log.d("RegisterActivity","age:" + age);
        Log.d("RegisterActivity","mail:" + email);
        if (!isAccountValid(account)){
            Toast.makeText(RegisterActivity.this,"请输入有效的用户名，长度大于6位",
                    Toast.LENGTH_SHORT).show();
            accountRegister.setText(null);
        } else if (!isPasswordValid(password)) {
            Toast.makeText(RegisterActivity.this,"请输入有效的密码，长度大于6位",
                    Toast.LENGTH_SHORT).show();
            passwordRegister.setText(null);
        } else if (!password.equals(passwordConfirm)) {
            Toast.makeText(RegisterActivity.this,"两次密码不一致",
                    Toast.LENGTH_SHORT).show();
            passwordRegister.setText(null);
            passwordConfirmRegister.setText(null);
        } else if (!isAgeValid(age)) {
            Toast.makeText(RegisterActivity.this,"年龄不能大于150岁或者小于10岁且不能为空",
                    Toast.LENGTH_SHORT).show();
            ageRegister.setText(null);
        } else if (!isEmailValid(email)) {
            Toast.makeText(RegisterActivity.this,"请输入有效邮箱地址",
                    Toast.LENGTH_SHORT).show();
            emailRegister.setText(null);
        } else {
            valid = true;
        }

        return valid;
    }

    private void register(final String account,final String password,final String age,final String email){
        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    if (ContextCompat.checkSelfPermission(RegisterActivity.this,
                            Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(RegisterActivity.this, new String[]
                                {Manifest.permission.INTERNET}, 1);
                    }
                    OkHttpClient client = new OkHttpClient();


                    MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                    RequestBody requestBody = RequestBody.create(JSON," {\"account\":\"" +
                            account+"\",\"password\":\"" +
                            password+"\",\"age\":\"" +
                            age+"\",\"email\":\"" +
                            email+"\"}");
                    Log.d("register","{\"account\":\"" +
                            account+"\",\"password\":\"" +
                            password+"\",\"age\":\"" +
                            age+"\",\"email\":\"" +
                            email+"\"}");
                    Request request = new Request.Builder()
                            .url("https://wozaidaxue.com/car/register")
                            .post(requestBody)
                            .build();
                    final Response response = client.newCall(request).execute();
                    final String responseData = response.body().string();
                    Log.d("register", responseData);
                    //---------------------------------------进行注册的处理，false代表用户名重复 ，true 代表正确，可以跳转到下面页面
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (responseData.equals("false")){
                                Toast.makeText(RegisterActivity.this,
                                        "用户名重复！",Toast.LENGTH_SHORT).show();
                            } else if (responseData.equals("true")){
                                Intent intent = new Intent(RegisterActivity.this,
                                        LoginActivity.class);
                                Toast.makeText(RegisterActivity.this,
                                        "注册成功！",Toast.LENGTH_SHORT).show();
                                startActivity(intent);
                            } else {
                                Toast.makeText(RegisterActivity.this,
                                        "未知错误，请检查网络后重新注册",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    response.body().close();
                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        }).start();
    }
}
