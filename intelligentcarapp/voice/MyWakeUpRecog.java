package com.example.intelligentcarapp.voice;

import android.content.Context;
import android.util.Log;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;

import org.json.JSONObject;

import java.util.Map;
import java.util.TreeMap;

public class MyWakeUpRecog {

    public static final String TAG = "WakeUpRecognition";
    private EventListener mWakeUpListener;
    private EventListener mRecognitionListener;
    private Context mContext;
    private EventManager mWakeUpManager;
    private EventManager mRecognitionManager;
    private String command;

    public MyWakeUpRecog(Context context,EventListener wakeUpListener,EventListener recognitionListener) {
        this.mContext = context;

        //wp初始化
        mWakeUpManager = EventManagerFactory.create(context, "wp");
        this.mWakeUpListener = wakeUpListener;
        mWakeUpManager.registerListener(mWakeUpListener);

        //re初始化
        mRecognitionManager = EventManagerFactory.create(context, "asr");
        this.mRecognitionListener = recognitionListener;
        mRecognitionManager.registerListener(mRecognitionListener);
    }

    public void start() {
        Map<String, Object> params = new TreeMap<String, Object>();

        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
        params.put(SpeechConstant.WP_WORDS_FILE, "assets:///WakeUp.bin");
        // "assets:///WakeUp.bin" 表示WakeUp.bin文件定义在assets目录下

        String json = null; // 这里可以替换成你需要测试的json
        json = new JSONObject(params).toString();
        mWakeUpManager.send(SpeechConstant.WAKEUP_START, json, null, 0, 0);
        Log.d(TAG, "----->唤醒已经开始工作了");
    }

    /**
     * 关闭唤醒功能
     */
    public void stop() {
        // 具体参数的百度没有具体说明，大体需要以下参数
        // send(String arg1, byte[] arg2, int arg3, int arg4)
        mWakeUpManager.send(SpeechConstant.WAKEUP_STOP, null, null, 0, 0); //
        mRecognitionManager.send(SpeechConstant.ASR_STOP, "{}", null, 0, 0);
        Log.d(TAG, "----->唤醒已经停止");
    }

    public void release() {
        stop();
        mWakeUpManager.unregisterListener(mWakeUpListener);
        mRecognitionManager.unregisterListener(mRecognitionListener);
        mWakeUpManager = null;
        mRecognitionManager = null;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    /**
     * 取消本次识别，取消后将立即停止不会返回识别结果。
     * cancel 与stop的区别是 cancel在stop的基础上，完全停止整个识别流程，
     */
    public void recognitionCancel() {
        if (mRecognitionManager != null) {
            mRecognitionManager.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0);
        }
    }

    /**
     * 启动本次识别
     * @param params
     */
    public void recognitionStart(Map<String, Object> params) {
        String json = new JSONObject(params).toString();
        mRecognitionManager.send(SpeechConstant.ASR_START, json, null, 0, 0);
    }

    /**
     * 提前结束录音等待识别结果。
     */
    public void recognitionStop() {
        mRecognitionManager.send(SpeechConstant.ASR_STOP, "{}", null, 0, 0);
    }

}
