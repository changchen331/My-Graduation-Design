package com.example.jarvis;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jarvis.model.AppInfo;
import com.example.jarvis.utils.AppInfoFetcher;
import com.example.jarvis.utils.RecyclerViewAdapter;
import com.example.jarvis.utils.SoftKeyboardStateHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * TODO: 系统横屏的时候，页面可以滑动显示
 * TODO: 手动输入弹出窗口的发送逻辑
 * TODO: send() 方法逻辑
 */
public class MainActivity extends AppCompatActivity {
    private List<AppInfo> apps = null; // 应用信息列表
    private Integer position = 0; // 应用选择下标
    private Boolean isKeyboardSend = Boolean.FALSE; // 是否发送手动输入的文本

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 语音输入文本框
        TextView main_asr_text = findViewById(R.id.main_asr_text);
        // 语音输入开始按钮
        ImageButton main_asr_start = findViewById(R.id.main_asr_start);
        // 语音输入结束按钮
        ImageButton main_asr_end = findViewById(R.id.main_asr_end);

        // 点击语音输入开始按钮
        main_asr_start.setOnClickListener(v -> {
            // 还原语音识别文本
            main_asr_text.setText(R.string.main_asr_text);
            // 显示语音识别框
            main_asr_text.setVisibility(View.VISIBLE);
            // 隐藏语音输入开始按钮
            main_asr_start.setVisibility(View.INVISIBLE);
            // 显示语音输入结束按钮
            main_asr_end.setVisibility(View.VISIBLE);

            //开始录音
            /*code*/
            // 模拟开始录音（震动）
            vibrate(200);
        });

        // 点击语音输入结束按钮
        main_asr_end.setOnClickListener(v -> {
            //结束录音
            /*code*/
            // 模拟结束录音（震动）
            vibrate(300);

            // 隐藏结束语音输入按钮
            main_asr_end.setVisibility(View.INVISIBLE);

            //语音识别
            /*code*/
            // 模拟语音识别结果
            String text = "你好先生，我是Jarvis。";

            // 显示语音识别结果
            main_asr_text.setText(text);
            // 显示开始语音输入按钮
            main_asr_start.setVisibility(View.VISIBLE);
        });

        // 发送语音文本按钮
        ImageButton main_send = findViewById(R.id.main_send);
        // 点击发送语音文本按钮
        main_send.setOnClickListener(v -> {
            // 已经输入语音
            if (!getResources().getString(R.string.main_asr_text).equalsIgnoreCase(main_asr_text.getText().toString())) {
                // 隐藏语音识别框
                main_asr_text.setVisibility(View.INVISIBLE);

                // 发送语音识别文本
                send(main_asr_text.getText().toString());

                // 还原语音识别文本
                main_asr_text.setText(R.string.main_asr_text);

//                // 接收后端返回的应用列表
//                /*code*/
//                // 模拟接收后端返回的应用列表（筛选出第三方应用）
//                List<AppInfo> list = new ArrayList<>();
//                for (int i = 0; i < (Math.min(1, apps.size())); i++) {
//                    list.add(apps.get(i));
//                }
//                this.apps = list;

                // 显示应用选择弹窗
                showApplicationSelectionPopWindow(main_asr_text);
            }
        });

        // 手动输入文本弹窗（没有把它写在 showKeyboardPopWindow 里面是为了要保留其中 textView 的内容）
        @SuppressLint("InflateParams") View activity_main_keyboard = getLayoutInflater().inflate(R.layout.activity_main_keyboard, null);
        // 手动输入按钮
        ImageButton main_keyboard = findViewById(R.id.main_keyboard);
        // 设置手动输入按钮是否可见（解开注释则不可见，否则默认可见）
//        main_keyboard.setVisibility(Integer.parseInt(getResources().getString(R.string.invisible)));
        // 点击手动输入按钮
        main_keyboard.setOnClickListener(v -> {
            // 隐藏语音输入文本展示框
            main_asr_text.setVisibility(View.INVISIBLE);
            // 重置语音识别文本
            main_asr_text.setText(R.string.main_asr_text);
            // 隐藏语音输入结束按钮
            main_asr_end.setVisibility(View.INVISIBLE);
            // 显示语音输入开始按钮
            main_asr_start.setVisibility(View.VISIBLE);

            // 显示文本输入弹窗
            showKeyboardPopWindow(main_asr_text, activity_main_keyboard);

            // 如果发送了手动输入文本
//            if (isKeyboardSend) {
//                // 显示应用选择弹窗
//                new Handler().postDelayed(() -> showApplicationSelectionPopWindow(main_asr_start), 100);
//            }
            Log.v("isKeyboardSend", String.valueOf(isKeyboardSend));
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

        // 创建 LinearLayoutManager 实例
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        // 设置 recyclerView 为横向滚动
        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        // 将 linearLayoutManager 设置为 recyclerView 的布局管理器
        recyclerView.setLayoutManager(linearLayoutManager);

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
        PopupWindow main_application_selection = new PopupWindow(activity_application_selection, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        // 软键盘不会遮挡弹窗
        main_application_selection.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        // 背景变暗
        darkenBackground(0.3f);
        // 显示应用选择弹窗
        main_application_selection.showAtLocation(parentView, Gravity.BOTTOM, 0, 0);

        // 应用选择弹窗取消按钮
        ImageButton application_selection_cancel = activity_application_selection.findViewById(R.id.application_selection_cancel);
        if (application_selection_cancel == null) {
            // 处理找不到取消按钮的情况
            return;
        }
        application_selection_cancel.setOnClickListener(v -> {
            // 退出应用选择弹窗
            main_application_selection.dismiss();
        });

        // 应用选择弹窗确认按钮
        ImageButton application_selection_confirm = activity_application_selection.findViewById(R.id.application_selection_confirm);
        if (application_selection_confirm == null) {
            // 处理找不到确认按钮的情况
            return;
        }
        application_selection_confirm.setOnClickListener(v -> {
            if (position >= 0 && position < apps.size()) {
                AppInfo selectedApp = apps.get(position);
                // 发送选择的应用信息
                /*code*/
                // 模拟发送选择的应用信息
                Toast.makeText(MainActivity.this, selectedApp.getAppName(), Toast.LENGTH_SHORT).show();

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
            Log.e("getApplicationInformation", "Failed to get installed apps", e);
            return Collections.emptyList(); // 返回空列表
        }
    }

    /**
     * 显示文本输入弹窗
     *
     * @param parentView 指定显示窗口
     */
    private void showKeyboardPopWindow(View parentView, View currentView) {
        if (parentView == null || currentView == null) {
            // 处理无效的视图参数
            return;
        }

        //手动输入文本输入框
        EditText main_window_keyboard_edit = currentView.findViewById(R.id.main_keyboard_edit);
        //手动输入文本发送按钮
        ImageButton main_window_keyboard_send = currentView.findViewById(R.id.main_keyboard_send);
        if (main_window_keyboard_edit == null || main_window_keyboard_send == null) {
            // 处理找不到控件的情况
            return;
        }

        // 创建 PopupWindow 实例
        PopupWindow main_window_keyboard = new PopupWindow(currentView, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, true);
        // 软键盘不会遮挡输入框
        main_window_keyboard.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        // 开启手动输入
        //背景变暗
        darkenBackground(0.3f);
        //显示手动输入弹窗
        main_window_keyboard.showAtLocation(parentView, Gravity.BOTTOM, 0, 0);
        //请求文本输入框的焦点 + 弹出软键盘（异步）
        popUpSoftKeyboard(main_window_keyboard_edit);
        //选中之前输入的文本
        if (main_window_keyboard_edit.getText().length() > 0) {
            main_window_keyboard_edit.setSelection(0, main_window_keyboard_edit.getText().length());
        }

        // 监听编辑文本时的动作
        main_window_keyboard_edit.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // 发送手动输入文本
                this.isKeyboardSend = send(main_window_keyboard_edit.getText().toString());
                // 清空输入框的内容
                main_window_keyboard_edit.setText("");
                // 退出手动输入
                exitManualInput(main_window_keyboard_edit, main_window_keyboard);
            }

            return false;
        });

        // 点击发送按钮
        main_window_keyboard_send.setOnClickListener(v -> {
            // 发送手动输入文本
            this.isKeyboardSend = send(main_window_keyboard_edit.getText().toString());
            // 清空输入框的内容
            main_window_keyboard_edit.setText("");
            // 退出手动输入
            exitManualInput(main_window_keyboard_edit, main_window_keyboard);
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
                // 退出手动输入
                exitManualInput(main_window_keyboard_edit, main_window_keyboard);
            }
        });

        // 设置退出弹窗时的监听器（恢复背景）
        main_window_keyboard.setOnDismissListener(() -> darkenBackground(1f));
    }

    /**
     * 发送输入文本
     *
     * @param requirement 需求
     */
    private Boolean send(String requirement) {
        String text = requirement.trim();
        // 用户输入了文本
        if (!text.isEmpty()) {
            // 获取所有应用信息
            apps = getApplicationInformation(MainActivity.this);
            // 重置选择应用的下标（默认为第一个应用）
            position = 0;

            // 发送需求 + 所有应用信息
            /*code*/
            // 模拟发送语需求 + 所有应用信息
            Toast.makeText(MainActivity.this, text + "\n" + apps.size(), Toast.LENGTH_SHORT).show();

            // 接收后端返回的应用列表
            /*code*/
            // 模拟接收后端返回的应用列表
            List<AppInfo> list = new ArrayList<>();
            for (int i = 0; i < (Math.min(2, apps.size())); i++) {
                list.add(apps.get(i));
            }
            this.apps = list;
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
     * 退出手动输入（清除焦点 + 收起软键盘 -> 收起文本框）
     *
     * @param view        键盘输入框
     * @param popupWindow 键盘输入弹窗
     */
    private void exitManualInput(View view, PopupWindow popupWindow) {
        // 检查 view 是否为 null
        if (view != null) {
            // 使手动输入文本输入框失去焦点
            view.clearFocus();
        }

        // 检查 popupWindow 是否为 null
        if (popupWindow != null && popupWindow.isShowing()) {
            // 收起文本框
            popupWindow.dismiss();
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
                WindowManager.LayoutParams lp = window.getAttributes();
                lp.alpha = alpha;
                window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
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