package com.example.jarvis.utils;

import static android.content.Context.VIBRATOR_SERVICE;

import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

/**
 * ✔ 提供手机振动功能的工具类。
 */
public class VibratorUtil {
    private static final String TAG = "VibratorUtil";

    /**
     * 使手机产生振动。
     *
     * @param context  应用的上下文对象，必须非空。
     * @param duration 振动持续的时间，单位为毫秒，建议在 1 到 5000 毫秒之间。
     */
    public static void vibrate(Context context, long duration) {
        // 检查传入的 context 是否为 null
        if (context == null) {
            Log.e(TAG, "Context is null, cannot vibrate.");
            return;
        }

        // 获取 Vibrator 服务的实例
        Vibrator vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
        // 检查设备是否支持振动功能
        if (vibrator == null || !vibrator.hasVibrator()) {
            Log.e(TAG, "Vibrator is not available on this device.");
            return;
        }
        // 执行振动
        try {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
        } catch (Exception e) {
            // 振动时发生异常
            Log.e(TAG, "Failed to vibrate device.", e);
        }

    }
}
