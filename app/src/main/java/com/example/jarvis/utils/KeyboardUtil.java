package com.example.jarvis.utils;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;


/**
 * TODO 在软键盘开启的状态下 点击确认按钮 在退出 Dialog 之后 宿主界面还是处于软键盘开启的状态
 * TODO 弹出软键盘之后 光标消失
 * TODO 在 Dialog 开启的状态下 无法改变 宿主界面的背景颜色
 */
public class KeyboardUtil {
    private static final String TAG = "KeyboardUtil";

    /**
     * 显示软键盘
     *
     * @param view 需要获取焦点的视图
     */
    public static void showSoftInput(View view) {
        // 检查 editText 是否为 null
        if (view == null) {
            Log.e(TAG, "View is null, cannot show soft input.");
            return;
        }

        // 显示软键盘
        try {
            // 获取输入方法管理器
            InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT); // 显示软键盘
        } catch (Exception e) {
            Log.e(TAG, "Failed to show soft input.", e);
        }
    }

    /**
     * 隐藏软键盘
     *
     * @param context 应用上下文
     */
    public static void hideSoftInput(Context context) {
        if (context == null) {
            Log.e(TAG, "Context is null, cannot hide soft input.");
            return;
        }

        // 隐藏软键盘
        try {
            // 获取输入方法管理器
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                // 获取当前 Activity 中拥有焦点的视图
                View view = ((AppCompatActivity) context).getCurrentFocus();
                if (view == null) view = new View(context); // 创建一个新视图，用作隐藏软键盘的令牌
                imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY); // 隐藏软键盘
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to hide soft input.", e);
        }
    }
}
