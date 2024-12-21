package com.example.jarvis.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class ToastUtil {
    private static final String TAG = "ToastUtil";

    // 禁止实例化工具类
    private ToastUtil() {
        // 私有构造函数防止实例化
    }

    /**
     * 显示 Toast 提示
     *
     * @param context 上下文，必须非空
     * @param message 提示信息，必须非空
     */
    public static void showToast(Context context, String message, Boolean needLong) {
        if (context == null || message == null) {
            LogUtil.warning(TAG, "showToast", "Context and message cannot be null", Boolean.TRUE);
            return;
        }

        if (needLong) Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        else Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
