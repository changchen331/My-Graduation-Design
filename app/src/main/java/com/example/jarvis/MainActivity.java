package com.example.jarvis;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jarvis.model.AppInfo;
import com.example.jarvis.utils.AppInfoFetcher;
import com.example.jarvis.utils.SoftKeyboardStateHelper;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //语音输入文本框
        TextView main_asr_text = findViewById(R.id.main_asr_text);
        //语音输入开始按钮
        ImageButton main_asr_start = findViewById(R.id.main_asr_start);
        //语音输入结束按钮
        ImageButton main_asr_end = findViewById(R.id.main_asr_end);
        //点击 语音输入开始按钮
        main_asr_start.setOnClickListener(v -> {
            //还原语音识别文本
            main_asr_text.setText(R.string.main_asr_text);

            //显示语音识别框
            main_asr_text.setVisibility(View.VISIBLE);

            //隐藏语音输入开始按钮
            main_asr_start.setVisibility(View.INVISIBLE);

            //显示语音输入结束按钮
            main_asr_end.setVisibility(View.VISIBLE);

            //开始录音
            /*code*/
        });
        //点击 语音输入结束按钮
        main_asr_end.setOnClickListener(v -> {
            //结束录音
            /*code*/

            //隐藏结束语音输入按钮
            main_asr_end.setVisibility(View.INVISIBLE);

            //显示开始语音输入按钮
            main_asr_start.setVisibility(View.VISIBLE);

            //语音识别
            /*code*/
            String text = "你好先生，我是Jarvis。";

            //显示语音识别结果
            main_asr_text.setText(text);
        });

        //发送语音文本按钮
        ImageButton main_send = findViewById(R.id.main_send);
        //点击 发送语音文本按钮
        main_send.setOnClickListener(v -> {
            if (!getResources().getString(R.string.main_asr_text).equalsIgnoreCase(main_asr_text.getText().toString())) {
                //已经输入语音
                //隐藏语音识别框
                main_asr_text.setVisibility(View.INVISIBLE);

                //发送语音识别文本
                /*code*/
                Toast.makeText(MainActivity.this, main_asr_text.getText().toString(), Toast.LENGTH_LONG).show();

                //获取所有应用信息
                getApplicationInformation(MainActivity.this);
            }
        });

        //手动输入文本框
        @SuppressLint("InflateParams") View activity_main_keyboard = getLayoutInflater().inflate(R.layout.activity_main_keyboard, null);
        //手动输入按钮
        ImageButton main_keyboard = findViewById(R.id.main_keyboard);
        //设置手动输入按钮是否可见（解开注释不可见，否则默认可见）
//        main_keyboard.setVisibility(Integer.parseInt(getResources().getString(R.string.invisible))); //不可见
        //按下 手动输入按钮
        main_keyboard.setOnClickListener(v -> {
            //关闭语音识别
            //隐藏语音输入文本展示框
            main_asr_text.setVisibility(View.INVISIBLE);

            //重置语音识别文本
            main_asr_text.setText(R.string.main_asr_text);

            //隐藏语音输入结束按钮
            main_asr_end.setVisibility(View.INVISIBLE);

            //显示语音输入开始按钮
            main_asr_start.setVisibility(View.VISIBLE);

            //弹出文本输入弹出窗
            showKeyboardPopWindow(main_asr_text, activity_main_keyboard);
        });
    }

    /**
     * 弹出文本输入弹窗
     *
     * @param parentView  指定显示窗口
     * @param currentView 指定弹窗
     */
    private void showKeyboardPopWindow(View parentView, View currentView) {
        //手动输入文本输入框
        EditText main_window_keyboard_edit = currentView.findViewById(R.id.main_keyboard_edit);
        //手动输入文本发送按钮
        ImageButton main_window_keyboard_send = currentView.findViewById(R.id.main_keyboard_send);

        //创建PopupWindow实例
        PopupWindow main_window_keyboard = new PopupWindow(currentView, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, true);
        //点击外部区域时消失
//        main_window_keyboard.setOutsideTouchable(true);
        //软键盘不会挡着手动输入框
        main_window_keyboard.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        //开启手动输入（背景变暗 -> 弹出输入框 -> 请求焦点 -> 弹出软键盘（异步） -> 选中文本）
        //背景变暗
        darkenBackground(0.1f);
        //弹出手动输入框
        main_window_keyboard.showAtLocation(parentView, Gravity.BOTTOM, 0, 200);
        //请求文本输入框的焦点 + 弹出软键盘（异步）
        popUpSoftKeyboard(main_window_keyboard_edit);
        //选中之前输入的文本
        if (main_window_keyboard_edit.getText().length() > 0) {
            main_window_keyboard_edit.setSelection(0, main_window_keyboard_edit.getText().length());
        }

        //捕捉软键盘自带的“发送”按钮，类似于键盘的enter键（系统收起）
        main_window_keyboard_edit.setOnEditorActionListener((v, actionId, event) -> {
            //退出手动输入模式
            exitManualInput(main_window_keyboard_edit, main_window_keyboard);

            //如果用户输入了文本
            if (!"".equalsIgnoreCase(main_window_keyboard_edit.getText().toString())) {
                //发送文本
                /*code*/
                Toast.makeText(MainActivity.this, main_window_keyboard_edit.getText().toString(), Toast.LENGTH_SHORT).show();

                //清空输入框的内容
                main_window_keyboard_edit.setText("");
            }
            return false;
        });

        //按下 文本输入的发送按钮
        main_window_keyboard_send.setOnClickListener(v -> {
            //退出手动输入模式
            exitManualInput(main_window_keyboard_edit, main_window_keyboard);

            //如果用户输入了文本
            if (!"".equalsIgnoreCase(main_window_keyboard_edit.getText().toString())) {
                //发送文本
                /*code*/
                Toast.makeText(MainActivity.this, main_window_keyboard_edit.getText().toString(), Toast.LENGTH_LONG).show();

                //清空输入框的内容
                main_window_keyboard_edit.setText("");
            }
        });

        //点击软键盘自带的收起按钮（软键盘收起）
        SoftKeyboardStateHelper softKeyboardStateHelper = new SoftKeyboardStateHelper(main_window_keyboard_edit);
        softKeyboardStateHelper.addSoftKeyboardStateListener(new SoftKeyboardStateHelper.SoftKeyboardStateListener() {
            @Override
            public void onSoftKeyboardOpened(int keyboardHeightInPx) {
                //软键盘打开时
                /*code*/
//                System.out.println("键盘开启");
            }

            @Override
            public void onSoftKeyboardClosed() {
                //软键盘关闭时
//                System.out.println("键盘关闭");
                //退出手动输入
                exitManualInput(main_window_keyboard_edit, main_window_keyboard);
            }
        });

        //设置退出弹窗时的监听器（恢复背景）
        main_window_keyboard.setOnDismissListener(() -> darkenBackground(1f));
    }

    /**
     * 请求焦点 + 弹出软键盘（异步）
     *
     * @param view 键盘输入框
     */
    private void popUpSoftKeyboard(View view) {
        //将焦点设置到该输入框上
        view.requestFocus();

        //弹出软键盘（异步）
        view.postDelayed(() -> {
            InputMethodManager systemService = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE); //系统服务，用于控制软键盘
            systemService.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS); //弹出软键盘
        }, 100);
    }

    /**
     * 退出手动输入（清除焦点 + 收起软键盘 -> 收起文本框）
     *
     * @param view        键盘输入框
     * @param popupWindow 键盘输入弹窗
     */
    private void exitManualInput(View view, PopupWindow popupWindow) {
        //清除焦点 + 收起软键盘
        view.clearFocus(); //使手动输入文本输入框失去焦点
//        InputMethodManager systemService = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE); //系统服务，用于控制软键盘
//        systemService.hideSoftInputFromWindow(view.getWindowToken(), 0); //收起软键盘

        //收起文本框
        popupWindow.dismiss();
    }

    /**
     * 背景变暗
     *
     * @param alpha 透明度
     */
    private void darkenBackground(Float alpha) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = alpha;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setAttributes(lp);
    }

    /**
     * 获取所有应用信息
     *
     * @param context 指定环境
     */
    private void getApplicationInformation(Context context) {
        // 创建 AppInfoFetcher 实例
        AppInfoFetcher appInfoFetcher = new AppInfoFetcher(context);

        // 获取所有已安装应用的信息
        ArrayList<AppInfo> apps = appInfoFetcher.getAllInstalledApps();

        for (AppInfo app : apps) {
            if (!app.isSystemApp()) {
                //仅输出第三方应用信息
                System.out.println(app);
                System.out.println("==================================");
            }
        }
    }
}
