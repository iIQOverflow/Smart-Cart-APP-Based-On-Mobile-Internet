package com.example.intelligentcarapp.voice;

import android.content.Context;
import android.util.AndroidRuntimeException;
import android.util.Log;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.TreeMap;

public class MyWakeUp {

    public static final String TAG = "WakeUpTest";
    private EventManager mWpEventManager;
    private Context context;
    private EventListener eventListener;
    /**
     * 唤醒构造方法
     * @param context 一个上下文对象
     */
    public MyWakeUp(Context context) {
        this.context = context;
        //create方法示是一个静态方法，还有一个重载方法EventManagerFactory.create(context, name, version)
        //由于百度文档没有给出每个参数具体含义，我们只能按照官网给的demo写了
        mWpEventManager = EventManagerFactory.create(context, "wp");
        //注册监听事件
        eventListener = new MyEventListener();
        mWpEventManager.registerListener(eventListener);
    }
    /**
     * 开启唤醒功能
     */
    public void start() {
        Map<String, Object> params = new TreeMap<String, Object>();

        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
        params.put(SpeechConstant.WP_WORDS_FILE, "assets:///WakeUp.bin");
        // "assets:///WakeUp.bin" 表示WakeUp.bin文件定义在assets目录下

        String json = null; // 这里可以替换成你需要测试的json
        json = new JSONObject(params).toString();
        mWpEventManager.send(SpeechConstant.WAKEUP_START, json, null, 0, 0);
        Log.d(TAG, "----->唤醒已经开始工作了");
    }
    /**
     * 关闭唤醒功能
     */
    public void stop() {
        // 具体参数的百度没有具体说明，大体需要以下参数
        // send(String arg1, byte[] arg2, int arg3, int arg4)
        mWpEventManager.send(SpeechConstant.WAKEUP_STOP, null, null, 0, 0); //
        Log.d(TAG, "----->唤醒已经停止");
    }

    public void release() {
        stop();
        mWpEventManager.unregisterListener(eventListener);
        mWpEventManager = null;
    }

    private class MyEventListener implements EventListener
    {
        @Override
        public void onEvent(String name, String params, byte[] data, int offset, int length) {
            try {
                if (params != null) {
                    //解析json文件
                    JSONObject json = new JSONObject(params);
                    if ("wp.data".equals(name)) { // 每次唤醒成功, 将会回调name=wp.data的时间, 被激活的唤醒词在params的word字段
                        String word = json.getString("word"); // 唤醒词
                        /*
                         * 这里大家可以根据自己的需求实现唤醒后的功能，这里我们简单打印出唤醒词
                         */
                        Log.d(TAG, word);
                    } else if ("wp.exit".equals(name)) {
                        // 唤醒已经停止
                    }
                }
            } catch (JSONException e) {
                throw new AndroidRuntimeException(e);
            }
        }
    }

}
