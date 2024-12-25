package com.example.jarvis.utils;

import android.content.Context;

import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechRecognizer;

import java.util.Locale;

/**
 * 语音识别工具类
 * 提供语音识别功能的初始化、开始监听、停止监听等功能
 */
public class VoiceRecognitionUtil {
    private static final String TAG = "VoiceRecognitionUtil";

    private final Context context;

    private InitListener initListener; // 初始化监听器，用于接收初始化结果
    private SpeechRecognizer speechRecognizer; // 语音识别对象
    private RecognizerListener recognizerListener; // 识别结果监听器，用于接收识别结果

    public VoiceRecognitionUtil(Context context) {
        this.context = context;
    }

    /**
     * 初始化语音识别对象
     *
     * @param recognizerListener 识别结果监听器，用于接收识别过程中的事件和结果
     * @param initListener       初始化监听器，用于接收初始化完成事件
     */
    public void init(RecognizerListener recognizerListener, InitListener initListener) {
        this.recognizerListener = recognizerListener;
        this.initListener = initListener;
        initVoiceRecognizer();
    }

    /**
     * 初始化语音识别对象，并设置相关参数
     */
    private void initVoiceRecognizer() {
        // 获取系统默认的语言和地区设置
        Locale locale = Locale.getDefault();
        String language = locale.getLanguage() + "-" + locale.getCountry();
        LogUtil.info(TAG, "initVoiceRecognizer", "系统默认language:" + language, Boolean.TRUE);

        // 创建语音识别对象
        speechRecognizer = SpeechRecognizer.createRecognizer(context, initListener);
        if (speechRecognizer != null) {
            speechRecognizer.setParameter(SpeechConstant.RESULT_TYPE, "plain"); // 设置识别结果的类型为纯文本

            // 用户多长时间未开始说话则当做超时处理（取值范围{1000～10000}）
            speechRecognizer.setParameter(SpeechConstant.VAD_BOS, "10000"); // 设置语音开始检测的静音时长（毫秒）
            // 用户停止说话多长时间内即认为不再输入（取值范围{1000～10000}）
            speechRecognizer.setParameter(SpeechConstant.VAD_EOS, "10000"); // 设置语音结束检测的静音时长（毫秒）

            // 根据系统语言设置语音识别语言
            if ("zh-CN".equalsIgnoreCase(language))
                speechRecognizer.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            else speechRecognizer.setParameter(SpeechConstant.LANGUAGE, "en_us");

            LogUtil.info(TAG, "initVoiceRecognizer", "语音识别对象完成初始化", Boolean.TRUE);
        } else LogUtil.info(TAG, "initVoiceRecognizer", "语音识别对象为空", Boolean.TRUE);
    }

    /**
     * 开始监听语音输入
     */
    public void startListening() {
        if (speechRecognizer != null)
            speechRecognizer.startListening(recognizerListener); // 开始监听语音输入，并设置识别结果监听器
    }

    /**
     * 停止监听语音输入
     */
    public void stopListening() {
        if (speechRecognizer != null) speechRecognizer.stopListening(); // 停止监听语音输入
    }
}
