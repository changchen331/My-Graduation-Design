package com.example.jarvis.utils;

import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import java.util.LinkedList;
import java.util.List;

/**
 * 软键盘状态辅助工具类，用于监听软键盘的显示和隐藏状态
 * 该工具通过监听全局布局变化来推断软键盘的显示状态，并通知注册的监听器
 */
public class KeyboardStateMonitor implements ViewTreeObserver.OnGlobalLayoutListener {
    private static final String TAG = "KeyboardStateMonitor";
    private static final int KEYBOARD_THRESHOLD_DP = 200; // 软键盘高度阈值
    private final List<SoftKeyboardStateListener> listeners = new LinkedList<>(); // 监听器列表，软键盘状态监听器
    private View activityView; // 当前活动的视图
    private boolean isSoftKeyboardOpened; // 软键盘当前是否打开

    /**
     * 构造函数
     *
     * @param activityView 当前活动的根视图
     */
    public KeyboardStateMonitor(View activityView) {
        this(activityView, false);
    }

    /**
     * 构造函数
     *
     * @param activityView         当前活动的根视图
     * @param isSoftKeyboardOpened 软键盘是否默认打开
     */
    public KeyboardStateMonitor(View activityView, boolean isSoftKeyboardOpened) {
        if (activityView == null) {
            Log.e(TAG, "The activity root view cannot be null.");
            return;
        }
        this.activityView = activityView;
        this.isSoftKeyboardOpened = isSoftKeyboardOpened;
        // 注册全局布局监听器
        activityView.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    /**
     * 重写全局布局监听器的方法，用于检测布局变化，从而推断软键盘的显示状态。
     */
    @Override
    public void onGlobalLayout() {
        // 获取当前视图的可见区域（rect）
        final Rect rect = new Rect();
        activityView.getWindowVisibleDisplayFrame(rect);

        // 可见部分的高度
        int heightDiff = activityView.getRootView().getHeight() - (rect.bottom - rect.top);

        // 判断软键盘的显示状态并通知监听器（大于 200 像素，可能为软键盘）
        if (!isSoftKeyboardOpened && heightDiff > KEYBOARD_THRESHOLD_DP) {
            isSoftKeyboardOpened = true;
            notifyOnSoftKeyboardOpened(heightDiff);
        } else if (isSoftKeyboardOpened && heightDiff < KEYBOARD_THRESHOLD_DP) {
            isSoftKeyboardOpened = false;
            notifyOnSoftKeyboardClosed();
        }
    }

    /**
     * 添加软键盘状态监听器
     *
     * @param listener 要添加的监听器
     */
    public void addSoftKeyboardStateListener(SoftKeyboardStateListener listener) {
        listeners.add(listener);
    }

    /**
     * 当软键盘打开时，调用此方法通知所有监听器
     *
     * @param keyboardHeightInPx 打开的软键盘高度，单位为像素
     */
    private void notifyOnSoftKeyboardOpened(int keyboardHeightInPx) {
        // 上次保存的软键盘高度（像素）
        for (SoftKeyboardStateListener listener : listeners) {
            if (listener != null) listener.onSoftKeyboardOpened(keyboardHeightInPx);
        }
    }

    /**
     * 当软键盘关闭时，调用此方法通知所有监听器
     */
    private void notifyOnSoftKeyboardClosed() {
        for (SoftKeyboardStateListener listener : listeners) {
            if (listener != null) listener.onSoftKeyboardClosed();
        }
    }

    /**
     * 移除全局布局监听器，避免内存泄漏
     */
    public void removeGlobalLayoutListener() {
        activityView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }


    /**
     * 软键盘状态监听器接口
     */
    public interface SoftKeyboardStateListener {
        /**
         * 软键盘打开时调用
         *
         * @param keyboardHeightInPx 打开的软键盘高度，单位为像素
         */
        void onSoftKeyboardOpened(int keyboardHeightInPx);

        /**
         * 软键盘关闭时调用
         */
        void onSoftKeyboardClosed();
    }
}
