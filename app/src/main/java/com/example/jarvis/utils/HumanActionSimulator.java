package com.example.jarvis.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;

public class HumanActionSimulator extends AccessibilityService {
    private static final String TAG = "HumanActionSimulator";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int screenWidth = getScreenWidth();
        int screenHeight = getScreenHeight();

        int x = screenWidth / 2;
        int y = screenHeight / 2;
        performClick(x, y);
    }

    @Override
    public void onInterrupt() {
    }

    /**
     * 获取屏幕宽度
     *
     * @return 屏幕宽度
     */
    private int getScreenWidth() {
        return getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * 获取屏幕高度
     *
     * @return 屏幕高度
     */
    private int getScreenHeight() {
        return getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * 在指定坐标 (x, y) 执行点击操作
     *
     * @param x 点击位置的横坐标（单位：像素）
     * @param y 点击位置的纵坐标（单位：像素）
     */
    private void performClick(int x, int y) {
        // 创建点击路径
        Path clickPath = new Path();
        clickPath.moveTo(x, y);

        // 创建点击描述（0ms开始，持续50ms）
        GestureDescription.StrokeDescription strokeDescription = new GestureDescription.StrokeDescription(clickPath, 0, 50);
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(strokeDescription);

        // 执行点击
        dispatchGesture(builder.build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                // 手势完成
                super.onCompleted(gestureDescription);
                LogUtil.debug(TAG, "performClick", "操作成功", Boolean.TRUE);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                // 手势取消
                super.onCancelled(gestureDescription);
                LogUtil.debug(TAG, "performClick", "操作取消", Boolean.TRUE);
            }
        }, null);
    }

    /**
     * 在指定路径 (startX, startY) -> (endX, endY) 执行滑动操作
     *
     * @param startX 滑动开始位置的横坐标（单位：像素）
     * @param startY 滑动开始位置的纵坐标（单位：像素）
     * @param endX   滑动结束位置的横坐标（单位：像素）
     * @param endY   滑动结束位置的纵坐标（单位：像素）
     */
    private void performSwipe(int startX, int startY, int endX, int endY) {
        // 创建滑动路径
        Path swipePath = new Path();
        swipePath.moveTo(startX, startY);
        swipePath.lineTo(endX, endY);

        // 创建滑动描述
        GestureDescription.StrokeDescription strokeDescription = new GestureDescription.StrokeDescription(swipePath, 0, 500);
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(strokeDescription);

        // 执行滑动
        dispatchGesture(builder.build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                // 手势成功
                super.onCompleted(gestureDescription);
                LogUtil.debug(TAG, "performSwipe", "操作成功", Boolean.TRUE);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                // 操作取消
                super.onCancelled(gestureDescription);
                LogUtil.debug(TAG, "performSwipe", "操作取消", Boolean.TRUE);
            }
        }, null);
    }
}
