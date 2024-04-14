package com.example.jarvis;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jarvis.model.AppInfo;
import com.example.jarvis.utils.AppInfoFetcher;
import com.example.jarvis.utils.AppSelectRecyclerViewAdapter;
import com.example.jarvis.utils.SoftKeyboardStateHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private List<AppInfo> apps; // 应用信息列表
    private ArrayList<String> questions; // 补充问题列表
    private int position = 0; // 应用选择下标
    private String keyboardText = ""; // 手动输入框内容
    private boolean isASRActivated = false; // 语音识别是否激活
    private boolean isASRTextActivated = false; // 语音识别文本是否激活
    private boolean isKeyboardSend = false; // 是否发送手动输入的文本

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 语音输入文本框
        TextView main_asr_text = findViewById(R.id.main_asr_text);
        // 语音输入按钮
        ImageButton main_asr = findViewById(R.id.main_asr);

        // 点击语音输入按钮
        main_asr.setOnClickListener(v -> {
            if (!isASRActivated) {
                // 语音识别文本休眠
                isASRTextActivated = false;
                main_asr_text.setTextColor(MainActivity.this.getColor(R.color.asr_text_empty));

                main_asr_text.setText(R.string.asr_text); // 重置语音识别文本
                main_asr_text.setVisibility(View.VISIBLE); // 显示语音识别框

                // 语音识别激活
                isASRActivated = true;
                main_asr.setActivated(true);

                // 开始录音（应该有一个开始录音的方法）
                /*code*/
                vibrate(200); // 模拟开始录音（震动）
            } else {
                // 结束录音（应该有一个结束录音的方法）
                /*code*/
                vibrate(300); // 模拟结束录音（震动）

                main_asr.setVisibility(View.INVISIBLE); // 隐藏语音识别按钮（等待语音识别产生结果）

                // 语音识别
                /*code*/
                String text = "我要去医院"; // 模拟语音识别结果

                // 语音识别文本激活
                isASRTextActivated = true;
                main_asr_text.setTextColor(MainActivity.this.getColor(R.color.asr_text_filled));
                main_asr_text.setText(text); // 显示语音识别文本

                // 语音识别休眠
                isASRActivated = false;
                main_asr.setActivated(false);
                main_asr.setVisibility(View.VISIBLE);  // 显示语音输入按钮
            }
        });

        // 发送语音文本按钮
        ImageButton main_send = findViewById(R.id.main_send);
        // 点击发送语音文本按钮
        main_send.setOnClickListener(v -> {
            // 已经输入语音
            if (isASRTextActivated) {
                main_asr_text.setVisibility(View.INVISIBLE); // 隐藏语音识别框
                sendAndGet(main_asr_text.getText().toString()); // 发送语音识别文本 + 接收返回数据

                // 语音识别文本休眠
                isASRTextActivated = false;
                main_asr_text.setTextColor(MainActivity.this.getColor(R.color.asr_text_empty));

                main_asr_text.setText(R.string.asr_text); // 重置语音识别文本
                showApplicationSelectionPopWindow(main_asr_text); // 显示应用选择弹窗
            }
        });

        // 键盘输入按钮
        ImageButton main_keyboard = findViewById(R.id.main_keyboard);
        // 设置键盘输入按钮是否可见（解开注释则不可见，否则默认可见）
        main_keyboard.setVisibility(Integer.parseInt(getResources().getString(R.string.visibility)));
        // 点击键盘输入按钮
        main_keyboard.setOnClickListener(v -> {
            main_asr_text.setVisibility(View.INVISIBLE); // 隐藏语音输入文本展示框
            main_asr_text.setText(R.string.asr_text); // 重置语音识别文本

            // 语音识别文本休眠
            isASRTextActivated = false;
            main_asr_text.setTextColor(MainActivity.this.getColor(R.color.asr_text_empty));

            // 语音识别休眠
            isASRActivated = false;
            main_asr.setActivated(false);

            showKeyboardPopWindow(main_asr_text); // 显示文本输入弹窗
        });
    }

    /**
     * 发送输入文本 + 接收返回数据
     *
     * @param text 输入文本
     */
    private void sendAndGet(String text) {
        // 对输入的文本进行处理
        String textTrimmed = text.trim();
        // 用户是否输入了文本
        if (!textTrimmed.isEmpty()) {
            apps = getApplicationInformation(MainActivity.this); // 获取所有应用信息
            MainActivity.this.position = 0; // 重置选择应用的下标（默认为第一个应用）

            // 发送需求 + 所有应用信息
            /*code*/

            // 接收后端返回的应用列表
            /*code*/
            // 模拟接收后端返回的应用列表
//            List<String> targetApplications = Arrays.asList("滴滴出行", "美团", "百度地图");
//            apps = apps.stream().filter(app -> targetApplications.contains(app.getAppName())).collect(Collectors.toList());
            // 模拟接收后端返回的补充问题
            ArrayList<String> strings = new ArrayList<>();
            strings.add("您的目的地：");
            strings.add("您的出发地：");
            strings.add("您的出发时间：");
            questions = strings;
        }
    }

    /**
     * 获取所有应用信息
     *
     * @param context 当前上下文环境
     * @return 应用信息列表，或者在无法获取时返回空列表
     */
    private List<AppInfo> getApplicationInformation(Context context) {
        // 检查 context 是否为 null
        if (context == null) {
            // 提供一个空列表，因为没有有效的 context 来获取应用信息
            return Collections.emptyList();
        }

        // 创建 AppInfoFetcher 实例
        AppInfoFetcher appInfoFetcher = new AppInfoFetcher(context);
        try {
            return appInfoFetcher.getAllInstalledApps();
        } catch (Exception e) {
            // 处理获取应用信息时可能发生的异常
            Log.e(TAG, "Failed to get installed apps", e);
            return Collections.emptyList(); // 返回空列表
        }
    }

    /**
     * 显示应用选择弹窗
     *
     * @param parentView 指定显示窗口
     */
    private void showApplicationSelectionPopWindow(View parentView) {
        // 处理无效的参数
        if (parentView == null || apps == null || apps.isEmpty()) return;

        // 背景变暗
        darkenBackground(0.3f);

        // 应用选择弹窗
        @SuppressLint("InflateParams") View activity_application_selection = getLayoutInflater().inflate(R.layout.activity_application_selection, null);
        // 应用选择视图
        RecyclerView recyclerView = activity_application_selection.findViewById(R.id.application_selection);

        // 创建 GridLayoutManager 实例
        GridLayoutManager gridLayoutManager = new GridLayoutManager(MainActivity.this, 1);
        // 设置 recyclerView 为横向滚动
        gridLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        // 将 gridLayoutManager 设置为 recyclerView 的布局管理器
        recyclerView.setLayoutManager(gridLayoutManager);

        // 创建 RecyclerViewAdapter 实例
        AppSelectRecyclerViewAdapter appSelectRecyclerViewAdapter = new AppSelectRecyclerViewAdapter(apps);
        // 点击 项视图（itemView）获取所选应用的下标
        appSelectRecyclerViewAdapter.setOnItemClickListener((view, position) -> {
            if (position >= 0 && position < apps.size()) {
                MainActivity.this.position = position;
            }
        });
        // 将 recyclerViewAdapter 设置为 recyclerView 的适配器
        recyclerView.setAdapter(appSelectRecyclerViewAdapter);

        // 创建 PopupWindow 实例
        PopupWindow main_application_selection = new PopupWindow(activity_application_selection, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, false);
        // 软键盘不会遮挡弹窗
        main_application_selection.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        // 显示应用选择弹窗
        main_application_selection.showAtLocation(parentView, Gravity.BOTTOM, 0, 0);

        // 应用选择弹窗取消按钮
        Button application_selection_cancel = activity_application_selection.findViewById(R.id.application_selection_cancel);
        // 点击应用选择弹窗取消按钮
        application_selection_cancel.setOnClickListener(v -> {
            // 退出应用选择弹窗
            main_application_selection.dismiss();
        });

        // 应用选择弹窗确认按钮
        Button application_selection_confirm = activity_application_selection.findViewById(R.id.application_selection_confirm);
        // 点击应用选择弹窗确认按钮
        application_selection_confirm.setOnClickListener(v -> {
            if (position >= 0 && position < apps.size()) {
                // 获取选择的应用信息
                AppInfo selectedApp = apps.get(position);
                // 发送选择的应用信息
                /*code*/
                // 跳转至补充对话界面
                jumpToExtra(selectedApp);
            }
            // 退出应用选择弹窗
            main_application_selection.dismiss();
        });

        // 设置退出弹窗时的监听器（恢复背景）
        main_application_selection.setOnDismissListener(() -> darkenBackground(1f));
    }

    /**
     * 启动 ExtraActivity 并传递 AppInfo 对象。
     *
     * @param appInfo 要传递的 AppInfo 对象。
     */
    private void jumpToExtra(AppInfo appInfo) {
        // 创建指向 ExtraActivity 的 Intent
        Intent intent = new Intent(MainActivity.this, ExtraActivity.class);
        // 将 AppInfo 对象添加到 Intent 额外数据中
        intent.putExtra("selected_applications", appInfo);
        intent.putStringArrayListExtra("extra_questions", questions);
        // 启动 ExtraActivity
        startActivity(intent);
    }

    /**
     * 显示文本输入弹窗
     *
     * @param parentView 指定显示窗口
     */
    private void showKeyboardPopWindow(View parentView) {
        // 处理无效的视图参数
        if (parentView == null) return;

        // 背景变暗
        darkenBackground(0.3f);

        // 手动输入文本弹窗
        @SuppressLint("InflateParams") View activity_main_keyboard = getLayoutInflater().inflate(R.layout.activity_main_keyboard, null);

        // 创建 PopupWindow 实例
        PopupWindow main_keyboard_window = new PopupWindow(activity_main_keyboard, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, true);
        // 软键盘不会遮挡输入框
        main_keyboard_window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        // 手动输入文本输入框
        EditText main_window_keyboard_edit = activity_main_keyboard.findViewById(R.id.main_keyboard_edit);
        // 设置文本输入框的内容
        main_window_keyboard_edit.setText(keyboardText);

        // 显示手动输入弹窗
        main_keyboard_window.showAtLocation(parentView, Gravity.BOTTOM, 0, 0);
        // 请求文本输入框的焦点 + 弹出软键盘（异步）
        popUpSoftKeyboard(main_window_keyboard_edit);
        // 选中之前输入的文本
        if (main_window_keyboard_edit.getText().length() > 0) {
            main_window_keyboard_edit.setSelection(0, main_window_keyboard_edit.getText().length());
        }

        // 监听编辑文本时的动作（按下回车）
        main_window_keyboard_edit.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // 关闭键盘弹窗
                dismissKeyboard(Boolean.TRUE, main_window_keyboard_edit, main_keyboard_window);
            }
            return false;
        });

        // 手动输入文本发送按钮
        ImageButton main_window_keyboard_send = activity_main_keyboard.findViewById(R.id.main_keyboard_send);
        // 点击发送按钮
        main_window_keyboard_send.setOnClickListener(v -> {
            // 关闭键盘弹窗
            dismissKeyboard(Boolean.TRUE, main_window_keyboard_edit, main_keyboard_window);
        });

        // 监听软键盘状态变化
        new SoftKeyboardStateHelper(main_window_keyboard_edit).addSoftKeyboardStateListener(new SoftKeyboardStateHelper.SoftKeyboardStateListener() {
            @Override
            public void onSoftKeyboardOpened(int keyboardHeightInPx) {
                // 软键盘打开
            }

            @Override
            public void onSoftKeyboardClosed() {
                // 软键盘关闭
                // 关闭键盘弹窗
                dismissKeyboard(Boolean.FALSE, main_window_keyboard_edit, main_keyboard_window);
            }
        });

        // 设置退出弹窗时的监听器
        main_keyboard_window.setOnDismissListener(() -> {
            // 是否要发送输入文本
            if (isKeyboardSend) {
                // 发送文本
                sendAndGet(main_window_keyboard_edit.getText().toString());
                // 还原输入框的内容
                keyboardText = "";
                // 显示应用选择弹窗
                showApplicationSelectionPopWindow(parentView);
                // 还原标记
                isKeyboardSend = false;
            } else {
                // 恢复背景
                darkenBackground(1f);
                // 记录已输入但没有发送的文本
                keyboardText = main_window_keyboard_edit.getText().toString();
            }
        });
    }

    /**
     * 请求焦点 + 弹出软键盘（异步）
     *
     * @param view 键盘输入框
     */
    private void popUpSoftKeyboard(View view) {
        // 检查 view 是否为 null
        if (view != null) {
            // 将焦点设置到该输入框上
            view.requestFocus();
            // 弹出软键盘（异步）
            view.postDelayed(() -> {
                // 获取输入方法管理器
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (inputMethodManager != null) {
                    // 弹出软键盘
                    inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
                }
            }, 100);
        }
    }

    /**
     * 关闭键盘弹窗
     *
     * @param isKeyboardSend 标记是否需要发送手动输入文本
     * @param editText       文本框
     * @param popupWindow    弹出窗口
     */
    private void dismissKeyboard(Boolean isKeyboardSend, EditText editText, PopupWindow popupWindow) {
        // 检查 EditText 和 PopupWindow 是否为 null
        if (editText == null || popupWindow == null) {
            // 如果 EditText 或 PopupWindow为null，记录错误并返回
            Log.e(TAG, "EditText 或 PopupWindow 为 null，无法关闭键盘弹窗");
            return;
        }

        // 标记是否需要发送手动输入文本
        this.isKeyboardSend = isKeyboardSend;
        // 清除 EditText 的焦点
        editText.clearFocus();
        // 隐藏弹出窗口
        popupWindow.dismiss();
    }

    /**
     * 背景变暗
     *
     * @param alpha 透明度，范围从 0.0 到 1.0
     */
    private void darkenBackground(Float alpha) {
        if (alpha == null || alpha < 0.0f || alpha > 1.0f) {
            // 透明度值无效，使用默认值
            Log.w(TAG, "Invalid alpha value, using 0.5 instead.");
            alpha = 0.5f;
        }

        Window window = MainActivity.this.getWindow();
        try {
            WindowManager.LayoutParams lp = window.getAttributes(); // 获取当前窗口的布局参数
            lp.alpha = alpha;  // 设置窗口的透明度
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND); // 为窗口添加背后模糊效果的标志
            window.setAttributes(lp); // 应用更新后的布局参数到窗口
        } catch (Exception e) {
            // 处理设置窗口属性时可能出现的异常
            Log.e(TAG, "Failed to darken background", e);
        }
    }

    /**
     * 手机震动
     *
     * @param milliseconds 震动时长
     */
    private void vibrate(long milliseconds) {
        // 检查延迟时间是否为非负数
        if (milliseconds < 0) {
            Log.w(TAG, "Invalid milliseconds value, using 0 instead.");
            milliseconds = 100;
        }

        // 获取 Vibrator 服务的实例
        Vibrator vibrator = (Vibrator) MainActivity.this.getSystemService(VIBRATOR_SERVICE);

        // 检查设备是否支持震动功能
        if (vibrator != null && vibrator.hasVibrator()) {
            // 创建一个震动 milliseconds 毫秒的 VibrationEffect
            VibrationEffect vibrationEffect = VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE);
            vibrator.vibrate(vibrationEffect); // 执行震动
        }
    }
}