package com.example.jarvis;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jarvis.model.AppInfo;
import com.example.jarvis.utils.AppInfoFetcher;
import com.example.jarvis.utils.RecyclerViewAdapter;
import com.example.jarvis.utils.SoftKeyboardStateHelper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO 完成 补充对话界面
 * TODO 完善 获取应用数据类
 * TODO 完善 文件工具类完善
 */
public class MainActivity extends AppCompatActivity {
    private List<AppInfo> apps; // 应用信息列表
    private Integer position; // 应用选择下标
    private String keyboardText = ""; // 手动输入框内容
    private Boolean isASRActivated = Boolean.FALSE; // 语音识别是否激活
    private Boolean isASRTextActivated = Boolean.FALSE; // 语音识别文本是否激活
    private Boolean isKeyboardSend = Boolean.FALSE; // 是否发送手动输入的文本

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
                isASRTextActivated = Boolean.FALSE;
                main_asr_text.setTextColor(this.getColor(R.color.main_asr_text_empty));
                // 重置语音识别文本
                main_asr_text.setText(R.string.main_asr_text);

                // 显示语音识别框
                main_asr_text.setVisibility(View.VISIBLE);

                // 语音识别激活
                isASRActivated = Boolean.TRUE;
                main_asr.setActivated(true);

                // 开始录音（应该有一个开始录音的方法）
                /*code*/
                vibrate(200); // 模拟开始录音（震动）
            } else {
                // 结束录音（应该有一个结束录音的方法）
                /*code*/
                vibrate(300); // 模拟结束录音（震动）

                // 隐藏语音识别按钮（等待语音识别产生结果）
                main_asr.setVisibility(View.INVISIBLE);

                // 语音识别
                /*code*/
                String text = "你好先生，我是Jarvis。"; // 模拟语音识别结果

                // 语音识别文本激活
                isASRTextActivated = Boolean.TRUE;
                main_asr_text.setTextColor(this.getColor(R.color.main_asr_text_filled));
                // 显示语音识别文本
                main_asr_text.setText(text);

                // 语音识别休眠
                isASRActivated = Boolean.FALSE;
                main_asr.setActivated(false);

                // 显示语音输入按钮
                main_asr.setVisibility(View.VISIBLE);
            }
        });

        // 发送语音文本按钮
        ImageButton main_send = findViewById(R.id.main_send);
        // 点击发送语音文本按钮
        main_send.setOnClickListener(v -> {
            // 已经输入语音
            if (isASRTextActivated) {
                // 隐藏语音识别框
                main_asr_text.setVisibility(View.INVISIBLE);

                // 发送语音识别文本 + 接收返回数据
                sendAndGet(main_asr_text.getText().toString());

                // 语音识别文本休眠
                isASRTextActivated = Boolean.FALSE;
                main_asr_text.setTextColor(this.getColor(R.color.main_asr_text_empty));
                // 重置语音识别文本
                main_asr_text.setText(R.string.main_asr_text);

                // 显示应用选择弹窗
                showApplicationSelectionPopWindow(main_asr_text);
            }
        });

        // 手动输入按钮
        ImageButton main_keyboard = findViewById(R.id.main_keyboard);
        // 设置手动输入按钮是否可见（解开注释则不可见，否则默认可见）
//        main_keyboard.setVisibility(Integer.parseInt(getResources().getString(R.string.invisible)));
        // 点击手动输入按钮
        main_keyboard.setOnClickListener(v -> {
            // 隐藏语音输入文本展示框
            main_asr_text.setVisibility(View.INVISIBLE);

            // 语音识别文本休眠
            isASRTextActivated = Boolean.FALSE;
            main_asr_text.setTextColor(this.getColor(R.color.main_asr_text_empty));
            // 重置语音识别文本
            main_asr_text.setText(R.string.main_asr_text);

            // 语音识别休眠
            isASRActivated = Boolean.FALSE;
            main_asr.setActivated(false);

            // 显示文本输入弹窗
            showKeyboardPopWindow(main_asr_text);
        });
    }

    /**
     * 显示应用选择弹窗
     *
     * @param parentView 指定显示窗口
     */
    private void showApplicationSelectionPopWindow(View parentView) {
        if (parentView == null || apps == null || apps.isEmpty()) {
            // 处理无效的参数
            return;
        }

        // 应用选择弹窗
        @SuppressLint("InflateParams") View activity_application_selection = getLayoutInflater().inflate(R.layout.activity_application_selection, null);
        if (activity_application_selection == null) {
            // 处理布局 inflate 失败的情况
            return;
        }
        // 应用选择视图
        RecyclerView recyclerView = activity_application_selection.findViewById(R.id.application_selection);
        if (recyclerView == null) {
            // 处理找不到 RecyclerView 控件的情况
            return;
        }

        // 创建 GridLayoutManager 实例
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1);
        // 设置 recyclerView 为横向滚动
        gridLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        // 将 gridLayoutManager 设置为 recyclerView 的布局管理器
        recyclerView.setLayoutManager(gridLayoutManager);

        // 创建 RecyclerViewAdapter 实例
        RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(apps);
        // 将 recyclerViewAdapter 设置为 recyclerView 的适配器
        recyclerView.setAdapter(recyclerViewAdapter);
        // 点击 项视图（itemView）获取所选应用的下标
        recyclerViewAdapter.setOnItemClickListener((view, position) -> {
            if (position >= 0 && position < apps.size()) {
                this.position = position;
            }
        });

        // 创建 PopupWindow 实例
        PopupWindow main_application_selection = new PopupWindow(activity_application_selection, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, false);
        // 软键盘不会遮挡弹窗
        main_application_selection.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        // 应用选择弹窗取消按钮
        Button application_selection_cancel = activity_application_selection.findViewById(R.id.application_selection_cancel);
        if (application_selection_cancel == null) {
            // 处理找不到取消按钮的情况
            return;
        }
        // 应用选择弹窗确认按钮
        Button application_selection_confirm = activity_application_selection.findViewById(R.id.application_selection_confirm);
        if (application_selection_confirm == null) {
            // 处理找不到确认按钮的情况
            return;
        }

        // 背景变暗
        darkenBackground(0.3f);
        // 显示应用选择弹窗
        main_application_selection.showAtLocation(parentView, Gravity.BOTTOM, 0, 0);

        // 点击应用选择弹窗确认按钮
        application_selection_cancel.setOnClickListener(v -> {
            // 退出应用选择弹窗
            main_application_selection.dismiss();
        });

        // 点击应用选择弹窗确认按钮
        application_selection_confirm.setOnClickListener(v -> {
            if (position >= 0 && position < apps.size()) {
                // 获取选择的应用信息
                AppInfo selectedApp = apps.get(position);
                // 发送选择的应用信息
                /*code*/
                Toast.makeText(MainActivity.this, selectedApp.getAppName(), Toast.LENGTH_SHORT).show(); // 模拟发送选择的应用信息
                // 打开应用
                openApplication(MainActivity.this, selectedApp);
            }
            // 退出应用选择弹窗
            main_application_selection.dismiss();
        });

        // 设置退出弹窗时的监听器（恢复背景）
        main_application_selection.setOnDismissListener(() -> darkenBackground(1f));
    }

    /**
     * 启动指定应用
     *
     * @param context 指定环境
     * @param app     应用信息
     */
    private void openApplication(Context context, AppInfo app) {
        PackageManager packageManager = context.getPackageManager();
        try {
            Intent intent = packageManager.getLaunchIntentForPackage(app.getPackageName());
            if (intent != null) {
                context.startActivity(intent); //启动应用
            } else {
                // 处理无法获取启动 Intent 的情况
                Log.e("AppLauncher", "No launch intent found for package: " + app.getPackageName());
                Toast.makeText(context, "无法启动应用。", Toast.LENGTH_SHORT).show();
            }
        } catch (ActivityNotFoundException e) {
            // 处理无法找到对应的 Activity 的情况
            Log.e("AppLauncher", "Activity not found for package: " + app.getPackageName(), e);
            Toast.makeText(context, "没有权限启动应用。", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 获取所有应用信息
     *
     * @param context 指定环境
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
            Log.e("GetApplicationInformation", "Failed to get installed apps", e);
            return Collections.emptyList(); // 返回空列表
        }
    }

    /**
     * 显示文本输入弹窗
     *
     * @param parentView 指定显示窗口
     */
    private void showKeyboardPopWindow(View parentView) {
        if (parentView == null) {
            // 处理无效的视图参数
            return;
        }

        // 手动输入文本弹窗
        @SuppressLint("InflateParams") View activity_main_keyboard = getLayoutInflater().inflate(R.layout.activity_main_keyboard, null);
        if (activity_main_keyboard == null) {
            // 处理布局 inflate 失败的情况
            return;
        }

        // 创建 PopupWindow 实例
        PopupWindow main_window_keyboard = new PopupWindow(activity_main_keyboard, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, true);
        // 软键盘不会遮挡输入框
        main_window_keyboard.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        // 手动输入文本输入框
        EditText main_window_keyboard_edit = activity_main_keyboard.findViewById(R.id.main_keyboard_edit);
        // 手动输入文本发送按钮
        ImageButton main_window_keyboard_send = activity_main_keyboard.findViewById(R.id.main_keyboard_send);
        if (main_window_keyboard_edit == null || main_window_keyboard_send == null) {
            // 处理找不到控件的情况
            return;
        }

        // 开启手动输入
        // 背景变暗
        darkenBackground(0.3f);
        // 设置文本输入框的内容
        main_window_keyboard_edit.setText(keyboardText);
        // 显示手动输入弹窗
        main_window_keyboard.showAtLocation(parentView, Gravity.BOTTOM, 0, 0);
        // 请求文本输入框的焦点 + 弹出软键盘（异步）
        popUpSoftKeyboard(main_window_keyboard_edit);
        // 选中之前输入的文本
        if (main_window_keyboard_edit.getText().length() > 0) {
            main_window_keyboard_edit.setSelection(0, main_window_keyboard_edit.getText().length());
        }

        // 监听编辑文本时的动作
        main_window_keyboard_edit.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // 关闭键盘弹窗
                dismissKeyboard(Boolean.TRUE, main_window_keyboard_edit, main_window_keyboard);
            }
            return false;
        });

        // 点击发送按钮
        main_window_keyboard_send.setOnClickListener(v -> {
            // 关闭键盘弹窗
            dismissKeyboard(Boolean.TRUE, main_window_keyboard_edit, main_window_keyboard);
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
                dismissKeyboard(Boolean.FALSE, main_window_keyboard_edit, main_window_keyboard);
            }
        });

        // 设置退出弹窗时的监听器
        main_window_keyboard.setOnDismissListener(() -> {
            // 是否要发送输入文本
            if (isKeyboardSend) {
                // 发送文本
                isKeyboardSend = sendAndGet(main_window_keyboard_edit.getText().toString());
                // 还原输入框的内容
                keyboardText = "";
                // 显示应用选择弹窗（异步）
                new Handler().postDelayed(() -> showApplicationSelectionPopWindow(parentView), 100);
                // 还原标记
                isKeyboardSend = Boolean.FALSE;
            } else {
                // 恢复背景
                darkenBackground(1f);
                // 记录已输入但没有发送的文本
                keyboardText = main_window_keyboard_edit.getText().toString();
            }
        });
    }

    /**
     * 发送输入文本 + 接收返回数据
     *
     * @param text 输入文本
     */
    private Boolean sendAndGet(String text) {
        // 对输入的文本进行处理
        String textTrimmed = text.trim();

        // 用户是否输入了文本
        if (!textTrimmed.isEmpty()) {
            // 获取所有应用信息
            apps = getApplicationInformation(MainActivity.this);
            // 重置选择应用的下标（默认为第一个应用）
            position = 0;

            // 发送需求 + 所有应用信息
            /*code*/

            // 接收后端返回的应用列表
            /*code*/
            // 模拟接收后端返回的应用列表
            apps = apps.stream().filter(app -> !app.isSystemApp()).collect(Collectors.toList());/*.subList(0, 3);*/

            return Boolean.TRUE;
        }
        return Boolean.FALSE;
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
            }, 100); // 延迟时间，可根据实际情况调整
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
            Log.e("DismissKeyboard", "EditText或PopupWindow为null，无法关闭键盘弹窗");
            return;
        }

        // 标记是否需要发送手动输入文本
        this.isKeyboardSend = isKeyboardSend;

        // 尝试清除EditText的焦点
        try {
            editText.clearFocus();
        } catch (Exception e) {
            Log.w("DismissKeyboard", "清除EditText焦点时发生异常", e);
        }
        // 检查PopupWindow是否正在显示
        if (popupWindow.isShowing()) {
            // 隐藏弹出窗口
            try {
                popupWindow.dismiss();
            } catch (Exception e) {
                Log.w("DismissKeyboard", "关闭PopupWindow时发生异常", e);
            }
        }
    }

    /**
     * 背景变暗
     *
     * @param alpha 透明度，范围从 0.0 到 1.0
     */
    private void darkenBackground(Float alpha) {
        if (alpha == null || alpha < 0.0f || alpha > 1.0f) {
            // 透明度值无效，可以设置一个默认值或者记录错误
            alpha = 0.5f; // 例如，设置默认透明度为 50%
        }

        Window window = MainActivity.this.getWindow();
        if (window != null) {
            try {
                // 获取当前窗口的布局参数
                WindowManager.LayoutParams lp = window.getAttributes();
                // 设置窗口的透明度
                lp.alpha = alpha;
                // 为窗口添加背后模糊效果的标志
                window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                // 应用更新后的布局参数到窗口
                window.setAttributes(lp);
            } catch (Exception e) {
                // 处理设置窗口属性时可能出现的异常
                Log.e("DarkenBackground", "Failed to darken background", e);
            }
        }
    }

    /**
     * 手机震动
     *
     * @param milliseconds 震动时长
     */
    private void vibrate(long milliseconds) {
        // 获取 Vibrator 服务的实例
        Vibrator vibrator = (Vibrator) MainActivity.this.getSystemService(VIBRATOR_SERVICE);

        // 检查设备是否支持震动功能
        if (vibrator != null && vibrator.hasVibrator()) {
            // 创建一个震动 milliseconds 毫秒的 VibrationEffect
            VibrationEffect vibrationEffect = VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE);
            try {
                vibrator.vibrate(vibrationEffect); // 执行震动
            } catch (IllegalArgumentException e) {
                // 处理震动无法执行的情况，例如不合法的震动时长
                Log.e("Vibration", "Failed to vibrate", e);
            }
        }
    }
}