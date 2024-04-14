package com.example.jarvis.utils;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;

import java.util.LinkedList;
import java.util.List;

/**
 * 软键盘状态辅助工具类，用于监听软键盘的显示和隐藏状态。
 */
public class SoftKeyboardStateHelper implements ViewTreeObserver.OnGlobalLayoutListener {

    private final List<SoftKeyboardStateListener> listeners = new LinkedList<>(); // 软键盘状态监听器列表
    private final View activityRootView; // 当前活动的根视图
    private final int[] heights = new int[2]; // 临时存储可见部分高度的数组，用于判断软键盘状态
    private int lastSoftKeyboardHeightInPx; // 上次保存的软键盘高度（像素）
    private boolean isSoftKeyboardOpened; // 软键盘当前是否打开

    public SoftKeyboardStateHelper(View activityRootView) {
        this(activityRootView, false);
    }

    public SoftKeyboardStateHelper(View activityRootView, boolean isSoftKeyboardOpened) {
        this.activityRootView = activityRootView;
        this.isSoftKeyboardOpened = isSoftKeyboardOpened;
        // 注册全局布局监听器
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    /**
     * 重写全局布局监听器的方法，用于检测布局变化，从而推断软键盘的显示状态。
     */
    @Override
    public void onGlobalLayout() {
        // 获取当前视图的可见区域（r）
        final Rect r = new Rect();
        activityRootView.getWindowVisibleDisplayFrame(r);

//        final int heightDiff = activityRootView.getRootView().getHeight() - (r.bottom - r.top);
//        if (!isSoftKeyboardOpened && heightDiff > 100) { // if more than 100 pixels, its probably a keyboard...
//            isSoftKeyboardOpened = true;
//            notifyOnSoftKeyboardOpened(heightDiff);
//        } else if (isSoftKeyboardOpened && heightDiff < 100) {
//            isSoftKeyboardOpened = false;
//            notifyOnSoftKeyboardClosed();
//        }

        /*
         * 此代码段用于检测软键盘的弹出和收起状态。
         * 由于软键盘的弹出和收起可能会影响当前 Activity 的布局，我们需要一种方法来判断其状态。
         * 该方法通过比较当前 Activity 根视图（activityRootView）的可见部分高度变化来实现。
         * 当软键盘弹出时，它会遮挡部分屏幕，导致可见部分的高度减小；相反，当软键盘收起时，可见部分的高度会增加。
         * 通过观察可见部分的高度变化，我们可以推断出软键盘的状态。
         *
         * 为了解决不同设备上可见部分高度可能存在的差异，我们引入了一个长度为 2 的数组 temp，
         * 用于存储可见部分高度的两个可能值。这两个值通常一个大，一个小，分别对应软键盘收起和弹出时的高度。
         * 我们通过比较当前高度值（heightDiff）与 temp 数组中的值来判断软键盘的状态。
         * 如果 heightDiff 与 temp 中的任一值不同，并且另一值也与 heightDiff 不同，则将 heightDiff 存储在 temp 数组中。
         * 通过这种方式，我们可以确定软键盘开启时的高度（openHeightDiff）和关闭时的高度（closeHeightDiff）。
         *
         * 需要注意的是，输入框的出现和软键盘的弹出并不是同步的。在大多数情况下，输入框会先出现，
         * 然后软键盘才会弹出。因此，我们需要一个标志变量 isSoftKeyboardOpened 来锁定状态，
         * 以避免在输入框出现时立即误判软键盘已经收起并执行关闭操作。
         * 这个标志变量在类的构造时被初始化，默认值为 false。
         *
         * 最后，我们通过比较当前高度值（heightDiff）和锁定标志（isSoftKeyboardOpened）来判断软键盘的真实状态。
         * 当 heightDiff 等于 closeHeightDiff 且 isSoftKeyboardOpened 为 true 时，我们认为软键盘已经收起；
         * 当 heightDiff 不等于 closeHeightDiff 时，我们认为软键盘已经打开。
         * 这样的逻辑确保了我们能够准确地判断软键盘的状态，避免了不必要的布局调整。
         *
         * 这段代码是对网上找到的原始代码的改进和适配，以确保在不同设备和系统上都能正常工作。
         */
        final int heightDiff = (r.height()); //可见部分的高度
        if (heights[1] == 0) heights[1] = heightDiff;
        else if (heightDiff != heights[1] && heightDiff != heights[0]) heights[0] = heightDiff;
        int openHeightDiff = Math.min(heights[0], heights[1]); // 软键盘开启时的高度
        int closeHeightDiff = Math.max(heights[0], heights[1]); // 软键盘关闭时的高度

        // 判断软键盘的显示状态并通知监听器
        if (!isSoftKeyboardOpened && heightDiff == openHeightDiff) {
            isSoftKeyboardOpened = true;
            notifyOnSoftKeyboardOpened(heightDiff);
        } else if (isSoftKeyboardOpened && heightDiff == closeHeightDiff) {
            isSoftKeyboardOpened = false;
            notifyOnSoftKeyboardClosed();
        }
    }

    /**
     * 设置软键盘是否打开的状态。
     *
     * @param isSoftKeyboardOpened 软键盘是否打开的布尔值
     */
    public void setIsSoftKeyboardOpened(boolean isSoftKeyboardOpened) {
        this.isSoftKeyboardOpened = isSoftKeyboardOpened;
    }

    /**
     * 获取软键盘当前是否打开的状态。
     *
     * @return 软键盘是否打开的布尔值
     */
    public boolean isSoftKeyboardOpened() {
        return isSoftKeyboardOpened;
    }

    /**
     * 获取最后保存的键盘高度（像素）
     *
     * @return 最后保存的键盘高度，以像素为单位
     */
    public int getLastSoftKeyboardHeightInPx() {
        return lastSoftKeyboardHeightInPx;
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
     * 移除软键盘状态监听器
     *
     * @param listener 要移除的监听器
     */
    public void removeSoftKeyboardStateListener(SoftKeyboardStateListener listener) {
        listeners.remove(listener);
    }

    /**
     * 当软键盘打开时，调用此方法通知所有监听器
     *
     * @param keyboardHeightInPx 打开的软键盘高度，单位为像素
     */
    private void notifyOnSoftKeyboardOpened(int keyboardHeightInPx) {
        this.lastSoftKeyboardHeightInPx = keyboardHeightInPx;
        for (SoftKeyboardStateListener listener : listeners) {
            if (listener != null) listener.onSoftKeyboardOpened(keyboardHeightInPx);
        }
    }

    /**
     * 当软键盘关闭时，调用此方法通知所有监听器
     */
    private void notifyOnSoftKeyboardClosed() {
        for (SoftKeyboardStateListener listener : listeners) {
            if (listener != null) {
                listener.onSoftKeyboardClosed();
            }
        }
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
