package com.example.intelligentcarapp.voice;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AndroidRuntimeException;
import android.util.Log;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class MyRecognition {

    public static final String TAG = "RecognitionTest";
    private boolean logTime = true;
    private boolean enableOffline = false; // 测试离线命令词，需要改成true
    private Context context;
    private EventManager mRecogManager;
    private EventListener eventListener;

    public MyRecognition(Context context) {
        this.context = context;
        //create方法示是一个静态方法，还有一个重载方法EventManagerFactory.create(context, name, version)
        //由于百度文档没有给出每个参数具体含义，我们只能按照官网给的demo写了
        mRecogManager = EventManagerFactory.create(context, "asr");
        //注册监听事件
        eventListener = new MyRecognition.MyEventListener();
        mRecogManager.registerListener(eventListener);
        if (enableOffline) {
            loadOfflineEngine(); // 测试离线命令词请开启, 测试 ASR_OFFLINE_ENGINE_GRAMMER_FILE_PATH 参数时开启
        }
    }

    public void start() {
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        String event = null;
        event = SpeechConstant.ASR_START; // 替换成测试的event

        if (enableOffline) {
            params.put(SpeechConstant.DECODER, 2);
        }
        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
        // params.put(SpeechConstant.NLU, "enable");
        // params.put(SpeechConstant.VAD_ENDPOINT_TIMEOUT, 0); // 长语音
        // params.put(SpeechConstant.IN_FILE, "res:///com/baidu/android/voicedemo/16k_test.pcm");
        // params.put(SpeechConstant.VAD, SpeechConstant.VAD_DNN);
        // params.put(SpeechConstant.PROP ,20000);
        // params.put(SpeechConstant.PID, 1537); // 中文输入法模型，有逗号
        // 请先使用如‘在线识别’界面测试和生成识别参数。 params同ActivityRecog类中myRecognizer.start(params);

        String json = null; // 可以替换成自己的json
        json = new JSONObject(params).toString(); // 这里可以替换成你需要测试的json
        mRecogManager.send(event, json, null, 0, 0);
    }

    public void stop() {
        mRecogManager.send(SpeechConstant.ASR_STOP, null, null, 0, 0); //
    }

    public void loadOfflineEngine() {
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        params.put(SpeechConstant.DECODER, 2);
        params.put(SpeechConstant.ASR_OFFLINE_ENGINE_GRAMMER_FILE_PATH, "assets://baidu_speech_grammar.bsg");
        mRecogManager.send(SpeechConstant.ASR_KWS_LOAD_ENGINE, new JSONObject(params).toString(), null, 0, 0);
    }

    public void unloadOfflineEngine() {
        mRecogManager.send(SpeechConstant.ASR_KWS_UNLOAD_ENGINE, null, null, 0, 0); //
    }

    public class MyEventListener implements EventListener
    {
        @Override
        public void onEvent(String name, String params, byte[] data, int offset, int length) {
            try {
                if (params != null && params.contains("final_result")) {
                    String finalAsrResult = parseAsrJson(params);
                    Log.d(TAG, finalAsrResult);
                }
            } catch (Exception e) {
                throw new AndroidRuntimeException(e);
            }
        }
    }

    public String parseAsrJson(String parseJson) {
        String finalAsrResult = null;
        if (parseJson != null && parseJson.contains("final_result")) {
            try {
                JSONObject json = new JSONObject(parseJson);
                JSONArray array = json.optJSONArray("results_recognition");
                if (array != null) {
                    finalAsrResult = array.getString(0);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        return finalAsrResult;
    }

    public void release() {
        if (mRecogManager == null) {
            return;
        }
        if (enableOffline) {
            unloadOfflineEngine(); // 测试离线命令词请开启, 测试 ASR_OFFLINE_ENGINE_GRAMMER_FILE_PATH 参数时开启
        }
    }
}
