package com.example.jarvis.utils;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class HumanActionSimulator extends AccessibilityService {
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 在这里处理无障碍事件，例如读取屏幕上的元素或执行操作
    }

    @Override
    public void onInterrupt() {
        // 当服务中断时调用
    }

    // 模拟点击
    public void performClick(float x, float y) {
//        performGlobalAction(GLOBAL_ACTION_CLICK);

    }

    // 模拟长按
    public void performLongClick(float x, float y) {
//        performGlobalAction(GLOBAL_ACTION_LONG_CLICK);
    }

    // 模拟滑动
    public void performSwipe(float startX, float startY, float endX, float endY) {
        // AccessibilityService 不直接支持滑动操作，需要通过查找元素和坐标来实现
        // 这里只是一个示意性的函数，实际实现需要更复杂的逻辑
    }

    // 自动注入文本
    public void performSetText(String text) {
        // 此方法需要找到对应的输入框节点并设置文本
        // 以下代码仅为示例，实际使用时需要根据实际情况进行查找和设置
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            // 假设已经找到输入框节点
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT);
            nodeInfo.setText(text);
        }
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        // 在这里设置服务的配置，如反馈类型等
        setServiceInfo(getServiceInfo());
    }
}
