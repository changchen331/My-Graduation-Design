package com.example.jarvis.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.LocaleList;
import android.speech.RecognizerIntent;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class SpeechToTextUtil {
    private static final String TAG = "SpeechRecognitionUtil";

    public static final Integer SPEECH_RECOGNITION_REQUEST_CODE = 331; // 确保这是唯一的请求码

    private final Activity activity; // 用于回调的 Activity

    public SpeechToTextUtil(Activity activity) {
        this.activity = activity;
    }

    /**
     * 开始监听用户的语音输入并识别成文本
     */
    public void startListening() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECORD_AUDIO}, SPEECH_RECOGNITION_REQUEST_CODE);
            return;
        }

        Intent speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, LocaleList.getDefault());
//        speechIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Your speech here");

        try {
            activity.startActivityForResult(speechIntent, SPEECH_RECOGNITION_REQUEST_CODE);
        } catch (Exception e) {
            LogUtil.error(TAG, "startListening", "Error starting speech recognition", e);
        }
    }

    /**
     * 处理语音识别的结果
     * 在 Activity 的 onActivityResult 方法中调用
     *
     * @param resultCode 结果码，指示语音识别的状态
     * @param data       包含识别结果的 Intent
     */
    public String onSpeechResult(int resultCode, Intent data) {
        // 检查请求码和数据是否有效
        if (resultCode == Activity.RESULT_OK && null != data && activity != null) {
            // 获取识别到的文本结果
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            return result != null && !result.isEmpty() ? result.get(0) : null;
        }
        return null;
    }
}
