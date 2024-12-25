package com.example.jarvis;

import android.app.Application;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        // 创建语音配置对象
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=54e93acb");

        super.onCreate();
    }
}
