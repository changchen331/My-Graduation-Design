package com.example.jarvis.utils;

import static android.content.Context.VIBRATOR_SERVICE;

import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

/**
 * 提供手机振动功能的工具类
 */
public class VibratorUtil {
    private static final String TAG = "VibratorUtil";

    // 禁止实例化工具类
    private VibratorUtil() {
        // 私有构造函数防止实例化
    }

    /**
     * 使手机产生振动
     *
     * @param context  应用的上下文对象，必须非空
     * @param duration 振动持续的时间，单位为毫秒，建议在 1 到 5000 毫秒之间
     */
    public static void vibrate(Context context, long duration) {
        // 检查传入的 context 是否为 null
        if (context == null) {
            LogUtil.warning(TAG, "vibrate", "Context is null, cannot vibrate", Boolean.TRUE);
            return;
        }

        // 获取 Vibrator 服务的实例
        Vibrator vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
        // 检查设备是否支持振动功能
        if (vibrator == null || !vibrator.hasVibrator()) {
            LogUtil.warning(TAG, "vibrate", "Vibrator is not available on this device", Boolean.TRUE);
            return;
        }
        // 执行振动
        try {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
        } catch (Exception e) {
            // 振动时发生异常
            LogUtil.error(TAG, "vibrate", "Failed to vibrate device", e);
        }

    }
}
