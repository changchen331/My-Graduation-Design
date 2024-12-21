package com.example.jarvis.utils;

import android.content.Context;
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
    public static void showLong(Context context, String message) {
        if (context == null || message == null) {
            LogUtil.warning(TAG, "showToast", "Context and message cannot be null", Boolean.TRUE);
            return;
        }

        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    /**
     * 显示 Toast 提示
     *
     * @param context 上下文，必须非空
     * @param message 提示信息，必须非空
     */
    public static void showShort(Context context, String message) {
        if (context == null || message == null) {
            LogUtil.warning(TAG, "showToast", "Context and message cannot be null", Boolean.TRUE);
            return;
        }

        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
