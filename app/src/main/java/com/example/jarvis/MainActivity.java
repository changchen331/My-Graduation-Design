package com.example.jarvis;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jarvis.model.AppInfo;
import com.example.jarvis.permission.PermissionListener;
import com.example.jarvis.permission.PermissionRequest;
import com.example.jarvis.utils.AppSelectorAdapter;
import com.example.jarvis.utils.ChatToGPTUtil;
import com.example.jarvis.utils.KeyboardStateMonitor;
import com.example.jarvis.utils.KeyboardUtil;
import com.example.jarvis.utils.LogUtil;
import com.example.jarvis.utils.SpeechToTextUtil;
import com.example.jarvis.utils.VibratorUtil;
import com.example.jarvis.utils.VoiceRecognitionUtil;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 主界面
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private final VoiceRecognitionUtil voiceRecognitionUtil = new VoiceRecognitionUtil(this); // 语音识别工具类
    private final SpeechToTextUtil speechToTextUtil = new SpeechToTextUtil(this); // 语音识别工具类（已弃用）
    private final PermissionRequest permissionRequest = new PermissionRequest(); // 权限申请

    private List<AppInfo> apps; // 应用信息列表
    // 测试（槽位列表）
    private final List<String> slots = Arrays.asList("KTV团购", "上门家政服务", "医院", "在线买药", "家电修理", "度假村", "快递", "戏曲演出订票", "手机维修", "打车", "播放有声书", "播放电视剧", "查询路线", "民宿预定", "生鲜采购", "看牙", "网络购物", "羽毛球馆", "血压记录", "订购外卖", "记账", "购买景点门票", "购买火车票", "跟团游预定", "陪诊服务", "预定电影票", "预定邮轮船票", "预约体检", "预约餐厅");

    private AlertDialog alertDialog; // 等待弹窗
    private AppInfo selectedApp;

    private Integer position = 0; // 应用选择下标

    private String userInput = ""; // 用户输入
    private String keyboardText = ""; // 手动输入框内容
    private String first_question = ""; // 起始问题

    private Boolean isASRActivated = Boolean.FALSE; // 语音识别是否激活
    private Boolean isKeyboardSend = Boolean.FALSE; // 是否发送手动输入的文本
    private Boolean isASRTextActivated = Boolean.FALSE; // 语音识别文本是否激活

    // 动态申请的权限
    protected String[] requestPermissionArray = new String[]{android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    // 声明一个集合，在后面的代码中用来存储用户拒绝授权的权限
    protected List<String> deniedPermissionList = new ArrayList<>();

    // 语音输入按钮
    ImageButton main_asr;
    // 发送语音文本按钮
    ImageButton main_send;
    // 键盘输入按钮
    ImageButton main_keyboard;
    // 语音输入文本框
    TextView main_asr_text;

    private final Handler handler = new Handler(msg -> {
        if (msg.what == ChatToGPTUtil.MESSAGE_CODE) {
            parseResponse(msg.obj.toString()); // 解析返回结果
            jumpToExtra(selectedApp); // 跳转至补充对话界面
        }
        return true;
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化
        initApps(); // 初始化应用信息
        initPermissions(); // 初始化权限
        initAlertDialog(); // 初始化等待弹窗
        initVoiceRecognitionUtil(findViewById(R.id.main_asr_text)); // 初始化语音识别对象

        // 捕捉 UI
        main_asr = findViewById(R.id.main_asr); // 语音输入按钮
        main_send = findViewById(R.id.main_send); // 发送语音文本按钮
        main_keyboard = findViewById(R.id.main_keyboard); // 键盘输入按钮
        main_asr_text = findViewById(R.id.main_asr_text); // 语音输入文本框

        // 点击语音输入按钮
        main_asr.setOnClickListener(v -> {
            if (!isASRActivated) {
                // 语音识别激活
                VibratorUtil.vibrate(this, 200); // 交互反馈
//                speechToTextUtil.startListening();
                voiceRecognitionUtil.startListening(); // 开始识别
                isASRActivated = Boolean.TRUE;
                main_asr.setActivated(true);

                // 语音识别文本休眠
                isASRTextActivated = Boolean.FALSE;
                main_asr_text.setTextColor(getColor(R.color.asr_text_empty));
                main_asr_text.setText(R.string.asr_text); // 重置语音识别文本
                main_asr_text.setVisibility(View.VISIBLE); // 显示语音识别框
            } else {
                // 语音识别休眠
                VibratorUtil.vibrate(this, 300); // 交互反馈
                voiceRecognitionUtil.stopListening(); // 结束识别
                isASRActivated = Boolean.FALSE;
                main_asr.setActivated(false);
                main_asr.setVisibility(View.VISIBLE);  // 显示语音输入按钮

                // 语音识别文本激活
                isASRTextActivated = Boolean.TRUE;
                main_asr_text.setTextColor(getColor(R.color.asr_text_filled));
            }
        });

        // 点击发送语音文本按钮
        main_send.setOnClickListener(v -> {
            // 已经输入语音
            if (isASRTextActivated) {
                // 发送语音识别文本
                main_asr_text.setVisibility(View.INVISIBLE); // 隐藏语音识别框
//                getFirstQuestion(userInput); // 获取第一个问题

                // 语音识别文本休眠
                isASRTextActivated = Boolean.FALSE;
                main_asr_text.setTextColor(getColor(R.color.asr_text_empty));
                main_asr_text.setText(R.string.asr_text); // 重置语音识别文本

                // 显示应用选择弹窗
                showApplicationSelectionPopWindow(main_asr_text);
            }
        });

        // 设置键盘输入按钮是否可见（解开注释则不可见，否则默认可见）
//        main_keyboard.setVisibility(Integer.parseInt(getResources().getString(R.string.visibility)));
        // 点击键盘输入按钮
        main_keyboard.setOnClickListener(v -> {
            // 语音识别休眠
            main_asr_text.setVisibility(View.INVISIBLE); // 隐藏语音输入文本展示框
            main_asr_text.setText(R.string.asr_text); // 重置语音识别文本

            // 语音识别文本休眠
            isASRTextActivated = Boolean.FALSE;
            main_asr_text.setTextColor(getColor(R.color.asr_text_empty));

            // 语音识别按钮休眠
            isASRActivated = Boolean.FALSE;
            main_asr.setActivated(false);

            // 显示文本输入弹窗
            showKeyboardPopWindow(main_asr_text);
        });
    }

    /**
     * 初始化权限
     */
    private void initPermissions() {
        // Android 6.0 以上动态申请权限
        permissionRequest.requestRuntimePermission(this, requestPermissionArray, new PermissionListener() {
            @Override
            public void onGranted() {
                LogUtil.info(TAG, "initPermissions", "所有权限已被授予", Boolean.TRUE);
            }

            //用户勾选“不再提醒”拒绝权限后，关闭程序再打开程序只进入该方法！
            @Override
            public void onDenied(List<String> deniedPermissions) {
                deniedPermissionList = deniedPermissions;
                for (String deniedPermission : deniedPermissions)
                    LogUtil.warning(TAG, "initPermissions", "被拒绝权限：" + deniedPermission, Boolean.TRUE);
            }
        });
    }

    /**
     * 初始化语音识别对象
     */
    private void initVoiceRecognitionUtil(TextView asrText) {
        voiceRecognitionUtil.init(new RecognizerListener() {
            @Override
            public void onVolumeChanged(int volume, byte[] data) {
            }

            @Override
            public void onBeginOfSpeech() {
            }

            @Override
            public void onEndOfSpeech() {
            }

            @Override
            public void onResult(RecognizerResult results, boolean isLast) {
                if (results != null) userInput = results.getResultString();
                else {
                    userInput = "识别失败";
                    LogUtil.warning(TAG, "initVoiceRecognitionUtil_onResult", "语音识别失败", Boolean.TRUE);
                }
                asrText.setText(userInput);
            }

            @Override
            public void onError(SpeechError error) {
            }

            @Override
            public void onEvent(int eventType, int arg1, int arg2, Bundle extras) {
            }
        }, code -> {
            if (code != ErrorCode.SUCCESS)
                LogUtil.warning(TAG, "initVoiceRecognitionUtil", "语音识别初始化失败，错误码：" + code, Boolean.TRUE);
            else
                LogUtil.info(TAG, "initVoiceRecognitionUtil", "语音识别初始化成功，错误码：" + code, Boolean.TRUE);
        });
    }

    /**
     * 初始化应用信息
     */
    private void initApps() {
//        apps = AppInfoFetcher.getAllInstalledApps(this); // 获取所有应用信息

        // 测试
        apps = new ArrayList<>();
        Drawable emptyImage = ContextCompat.getDrawable(this, R.drawable.empty_image);
        for (String slot : slots) {
            AppInfo app = new AppInfo();
            app.setAppName(slot);
            app.setAppIcon(emptyImage);
            apps.add(app);
        }
    }

    /**
     * 初始化一个不可取消的对话框
     */
    private void initAlertDialog() {
        // 创建并显示对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请稍后").setMessage("语悦正在思考，请耐心等待...").setCancelable(false); // 禁止通过点击外部取消
        alertDialog = builder.create();
    }

    /**
     * 显示应用选择弹窗
     *
     * @param parentView 指定显示窗口
     */
    private void showApplicationSelectionPopWindow(View parentView) {
        // 处理无效的参数
        if (parentView == null || apps == null || apps.isEmpty()) {
            LogUtil.warning(TAG, "showApplicationSelectionPopWindow", "parentView 或 apps 为空，无法显示应用选择弹窗", Boolean.TRUE);
            return;
        }

        // 背景变暗
        darkenBackground(0.3f);

        // 应用选择弹窗
        @SuppressLint("InflateParams") View activity_application_selection = getLayoutInflater().inflate(R.layout.activity_application_selection, null);

        // 创建 GridLayoutManager 实例
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1);
        // 设置 recyclerView 为横向滚动
        gridLayoutManager.setOrientation(RecyclerView.HORIZONTAL);

        // 创建 RecyclerViewAdapter 实例
        AppSelectorAdapter appSelectorAdapter = new AppSelectorAdapter(apps);
        // 点击 项视图（itemView）获取所选应用的下标
        appSelectorAdapter.setOnItemClickListener((view, position) -> {
            if (position >= 0 && position < apps.size()) this.position = position;
        });

        // 应用选择视图
        RecyclerView recyclerView = activity_application_selection.findViewById(R.id.application_selection);
        // 将 gridLayoutManager 设置为 recyclerView 的布局管理器
        recyclerView.setLayoutManager(gridLayoutManager);
        // 将 recyclerViewAdapter 设置为 recyclerView 的适配器
        recyclerView.setAdapter(appSelectorAdapter);

        // 创建 PopupWindow 实例
        PopupWindow main_application_selection = new PopupWindow(activity_application_selection, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT, false);
        // 软键盘不会遮挡弹窗
        main_application_selection.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        // 显示应用选择弹窗
        main_application_selection.showAtLocation(parentView, Gravity.BOTTOM, 0, 0);

        // 应用选择弹窗取消按钮
        Button application_selection_cancel = activity_application_selection.findViewById(R.id.application_selection_cancel);
        // 点击应用选择弹窗取消按钮
        application_selection_cancel.setOnClickListener(v -> {
            main_application_selection.dismiss(); // 退出应用选择弹窗
        });

        // 应用选择弹窗确认按钮
        Button application_selection_confirm = activity_application_selection.findViewById(R.id.application_selection_confirm);
        // 点击应用选择弹窗确认按钮
        application_selection_confirm.setOnClickListener(v -> {
            if (position >= 0 && position < apps.size()) {
                // 获取选择的应用信息
                selectedApp = apps.get(position);
                // 获取起始问题
                // 测试
                String input = selectedApp.getAppName();
                getFirstQuestion(input, selectedApp);
            }
            main_application_selection.dismiss(); // 退出应用选择弹窗
        });

        // 设置退出弹窗时的监听器（恢复背景）
        main_application_selection.setOnDismissListener(() -> {
            darkenBackground(0.1f); // 背景变暗
            alertDialog.show(); // 显示等待弹窗
        });
    }

    /**
     * 获取第一个问题
     *
     * @param input 输入文本
     */
    private void getFirstQuestion(String input, AppInfo app) {
        // 对输入的文本进行处理
        String textTrimmed = input.trim();
        // 用户是否输入了文本
        if (!textTrimmed.isEmpty()) {
            position = 0; // 重置选择应用的下标（默认为第一个应用）
            ChatToGPTUtil.getStart(this, app.getAppName(), handler); // 发送需求 + 所有应用信息
        }
    }

    /**
     * 解析返回结果
     *
     * @param responseBody 返回体
     */
    private void parseResponse(String responseBody) {
        try {
            JSONObject jsonObject = new JSONObject(responseBody);
            JSONArray choices = jsonObject.getJSONArray("choices");
            first_question = choices.getJSONObject(0).getJSONObject("message").getString("content");
            LogUtil.info(TAG, "parseResponse", first_question, Boolean.TRUE);
        } catch (JSONException e) {
            LogUtil.error(TAG, "parseResponse", "未找到 content 字段", e);
        }
    }

    /**
     * 启动 ExtraActivity 并传递 AppInfo 对象
     *
     * @param appInfo 要传递的 AppInfo 对象
     */
    private void jumpToExtra(AppInfo appInfo) {
        // 创建指向 ExtraActivity 的 Intent
        Intent intent = new Intent(this, ExtraActivity.class);

        // 将 AppInfo 对象添加到 Intent 额外数据中
        intent.putExtra("first_question", first_question);
        intent.putExtra("selected_application", appInfo);

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
        if (parentView == null) {
            LogUtil.warning(TAG, "showKeyboardPopWindow", "parentView 为 null，无法显示文本输入弹窗", Boolean.TRUE);
            return;
        }

        // 背景变暗
        darkenBackground(0.3f);

        // 手动输入文本弹窗
        @SuppressLint("InflateParams") View activity_main_keyboard = getLayoutInflater().inflate(R.layout.activity_main_keyboard, null);

        // 创建 PopupWindow 实例
        PopupWindow main_keyboard_window = new PopupWindow(activity_main_keyboard, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, true);
        // 软键盘不会遮挡输入框
        main_keyboard_window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        // 显示手动输入弹窗
        main_keyboard_window.showAtLocation(parentView, Gravity.BOTTOM, 0, 0);

        // 手动输入文本输入框
        EditText main_window_keyboard_edit = activity_main_keyboard.findViewById(R.id.main_keyboard_edit);
        main_window_keyboard_edit.setText(keyboardText); // 设置文本输入框的内容
        main_window_keyboard_edit.requestFocus(); // 请求文本输入框的焦点
        // 弹出软键盘（异步）
        main_window_keyboard_edit.postDelayed(() -> KeyboardUtil.showSoftInput(main_window_keyboard_edit), 100);
        // 选中之前输入的文本
        if (main_window_keyboard_edit.getText().length() > 0) {
            main_window_keyboard_edit.setSelection(0, main_window_keyboard_edit.getText().length());
        }
        // 监听编辑文本时的动作（按下回车）
        main_window_keyboard_edit.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND)
                dismissKeyboard(Boolean.TRUE, main_window_keyboard_edit, main_keyboard_window); // 关闭键盘弹窗
            return false;
        });

        // 手动输入文本发送按钮
        ImageButton main_window_keyboard_send = activity_main_keyboard.findViewById(R.id.main_keyboard_send);
        // 点击发送按钮
        main_window_keyboard_send.setOnClickListener(v -> {
            dismissKeyboard(Boolean.TRUE, main_window_keyboard_edit, main_keyboard_window); // 关闭键盘弹窗
        });

        // 监听软键盘状态变化
        new KeyboardStateMonitor(parentView).addSoftKeyboardStateListener(new KeyboardStateMonitor.SoftKeyboardStateListener() {
            @Override
            public void onSoftKeyboardOpened(int keyboardHeightInPx) {
                // 软键盘打开
            }

            @Override
            public void onSoftKeyboardClosed() {
                // 软键盘关闭，关闭键盘弹窗
                dismissKeyboard(Boolean.FALSE, main_window_keyboard_edit, main_keyboard_window);
            }
        });

        // 设置退出弹窗时的监听器
        main_keyboard_window.setOnDismissListener(() -> {
            // 是否要发送输入文本
            if (isKeyboardSend) {
                userInput = main_window_keyboard_edit.getText().toString();
                // 检查输入文本是否为空
                if (userInput.isEmpty()) {
                    darkenBackground(1f); // 恢复背景
                    return;
                }

                // 发送文本
//                getFirstQuestion(userInput);
                keyboardText = ""; // 还原输入框的内容
                isKeyboardSend = Boolean.FALSE; // 还原标记

                // 显示应用选择弹窗
                showApplicationSelectionPopWindow(parentView);
            } else {
                darkenBackground(1f); // 恢复背景
                keyboardText = main_window_keyboard_edit.getText().toString(); // 记录已输入但没有发送的文本
            }
        });
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
            LogUtil.warning(TAG, "dismissKeyboard", "EditText 或 PopupWindow 为 null，无法关闭键盘弹窗", Boolean.TRUE);
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
        // 判断透明度是否有效
        if (alpha == null || alpha < 0.0f || alpha > 1.0f) {
            LogUtil.warning(TAG, "darkenBackground", "Invalid alpha value, using default value instead", Boolean.TRUE);
            alpha = 0.5f; // 设置为默认值
        }
        // 获取窗口对象
        Window window = getWindow();
        try {
            WindowManager.LayoutParams lp = window.getAttributes(); // 获取当前窗口的布局参数
            lp.alpha = alpha; // 设置窗口的透明度
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND); // 为窗口添加背后模糊效果的标志
            window.setAttributes(lp); // 应用更新后的布局参数到窗口
        } catch (Exception e) {
            LogUtil.error(TAG, "darkenBackground", "Failed to darken background", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SpeechToTextUtil.SPEECH_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            userInput = speechToTextUtil.onSpeechResult(resultCode, data);
            LogUtil.info(TAG, "onActivityResult", userInput, Boolean.TRUE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        alertDialog.dismiss(); // 关闭等待弹窗
        darkenBackground(1.0f); // 恢复背景
    }
}