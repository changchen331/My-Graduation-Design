package com.example.jarvis.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 提供打开应用的工具类
 */
public class AppUtil {
    private static final String TAG = "AppUtil";

    /**
     * 打开指定应用。
     *
     * @param context     应用上下文，必须非空
     * @param packageName 要打开的应用的包名，必须非空
     */
    public static void openApp(Context context, String packageName) {
        if (context == null || packageName == null) {
            LogUtil.warning(TAG, "openApp", "Context and package name cannot be null", Boolean.TRUE);
            return;
        }

        try {
            // 获取应用的启动 Intent
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            if (launchIntent == null) {
                // 应用未安装或未找到对应的应用
                LogUtil.warning(TAG, "openApp", "Application with package name " + packageName + " not found", Boolean.TRUE);
                ToastUtil.showToast(context, "应用未安装或未找到应用", Boolean.TRUE);
                return;
            }
            // 启动应用
            context.startActivity(launchIntent);
        } catch (Exception e) {
            // 启动应用时发生异常
            LogUtil.error(TAG, "openApp", "Failed to start application", e);
            ToastUtil.showToast(context, "打开应用时发生错误", Boolean.TRUE);
        }
    }
}
