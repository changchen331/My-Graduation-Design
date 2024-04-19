package com.example.jarvis.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class AppUtil {
    private static final String TAG = "AppUtil";

    /**
     * 打开指定应用
     *
     * @param context     应用上下文
     * @param packageName 要打开的应用的包名
     * @return 打开成功返回 true，否则返回 false
     */
    public static boolean openApp(Context context, String packageName) {
        try {
            // 获取应用的启动 Intent
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            if (launchIntent == null) {
                // 应用未安装或未找到对应的应用
                Log.e(TAG, "Application with package name " + packageName + " not found!");
                showToast(context, "应用未安装或未找到应用");
                return false;
            }
            // 启动应用
            context.startActivity(launchIntent);
            return true;
        } catch (Exception e) {
            // 启动应用时发生异常
            Log.e(TAG, "Failed to start application", e);
            showToast(context, "打开应用时发生错误");
            return false;
        }
    }

    /**
     * 显示 Toast 提示
     *
     * @param context 上下文
     * @param message 提示信息
     */
    private static void showToast(Context context, String message) {
        if (context != null && message != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
}
