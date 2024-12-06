package com.example.jarvis.utils;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 管理软键盘的显示和隐藏的工具类
 */
public class KeyboardUtil {
    private static final String TAG = "KeyboardUtil";

    /**
     * 显示软键盘
     *
     * @param view 需要获取焦点的视图
     */
    public static void showSoftInput(View view) {
        if (view == null) {
            Log.e(TAG, "View is null, cannot show soft input.");
            return;
        }

        // 显示软键盘
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * 隐藏软键盘
     *
     * @param context 应用上下文，需传入应用程序的上下文，避免内存泄露
     */
    public static void hideSoftInput(Context context) {
        if (context == null) {
            Log.e(TAG, "Context is null, cannot hide soft input.");
            return;
        }

        // 隐藏软键盘
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            View view = getCurrentFocus(context); // 获取当前 Activity 中拥有焦点的视图
            if (view == null) view = new View(context); // 若无焦点视图，则创建一个新视图以隐藏软键盘
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0); // 隐藏软键盘
        }
    }

    /**
     * 获取当前上下文中拥有焦点的视图
     *
     * @param context 用于检索当前焦点视图的上下文
     * @return 如果找到焦点视图，则返回；否则返回 null
     */
    private static View getCurrentFocus(Context context) {
        return (context instanceof AppCompatActivity) ? ((AppCompatActivity) context).getCurrentFocus() : null;
    }
}
