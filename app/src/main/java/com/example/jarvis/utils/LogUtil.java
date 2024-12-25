package com.example.jarvis.utils;

import android.util.Log;

public class LogUtil {

    private LogUtil() {
        // 私有构造函数防止实例化
    }

    public static void debug(String TAG, String methodName, String message, Boolean isLogPrint) {
        if (isLogPrint) Log.d(TAG, methodName + ": " + message);
    }

    public static void info(String TAG, String methodName, String message, Boolean isLogPrint) {
        if (isLogPrint) Log.i(TAG, methodName + ": " + message);
    }

    public static void warning(String TAG, String methodName, String message, Boolean isLogPrint) {
        if (isLogPrint) Log.w(TAG, methodName + ": " + message);
    }

    public static void error(String TAG, String methodName, String message, Throwable throwable) {
        Log.e(TAG, methodName + ": " + message, throwable);
    }
}
