package com.example.jarvis;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jarvis.model.AppInfo;
import com.example.jarvis.model.Message;
import com.example.jarvis.utils.ExtraDialogRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExtraActivity extends AppCompatActivity {
    private static final String TAG = "ExtraActivity";
    private final List<Message> messages = new ArrayList<>(); // 对话信息列表
    private final List<String> hint = Arrays.asList("点击白色文本框，我会读出其中的内容哦~", "点击绿色文本框可以更改其中的内容"); // 系统提示（模拟）
    private AppInfo selectedApp; // 用户选择的应用信息
    private List<String> questions; // 补充问题
    private final List<String> revisedAnswers = Arrays.asList("复旦大学附属华山医院", "我现在的位置", "现在"); // 更改后的回答（模拟）
    private ExtraDialogRecyclerViewAdapter extraDialogRecyclerViewAdapter; // 对话框布局适配器
    private RecyclerView recyclerView; // 滚动视图
    private boolean isVoiceInputConfirmed = false; // 语音输入是否确认
    private boolean isEditASRActivated = false; // 语音输入是否确认
    private boolean allQuestionsAnswered = false; // 问题是否全部回答完毕

    private final List<String> answers = Arrays.asList("长海医院", "泰宝华庭", "周五下午两点"); // 用户的回答（模拟）
    private int schedule = 0; // 对话的进度

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extra);

        // 从 Intent 中获取用户选择的应用
        selectedApp = getIntent().getParcelableExtra("selected_applications");
        // 从 Intent 中获取补充问题
        ArrayList<String> extraQuestions = getIntent().getStringArrayListExtra("extra_questions");
        if (extraQuestions != null) questions = extraQuestions;
        else questions = new ArrayList<>();

        // 返回按钮
        ImageButton extra_return = findViewById(R.id.extra_return);
        extra_return.setOnClickListener(v -> finish()); // 点击 返回按钮 返回主界面（MainActivity）

        // 初始化 对话视图 & 对话弹窗
        if (recyclerView == null && extraDialogRecyclerViewAdapter == null) {
            recyclerView = ExtraActivity.this.findViewById(R.id.extra_dialog);
            extraDialogRecyclerViewAdapter = initRecyclerView();
            receive(hint.get(0));
        }

        // 语音输入按钮
        Button extra_asr = findViewById(R.id.extra_asr);
        // 点击语音输入按钮
        extra_asr.setOnClickListener(v -> {
            if (!allQuestionsAnswered) {
                // 开始录音（应该有一个开始录音的方法）
                /*code*/
                vibrate(200); // 模拟开始录音（震动）
                // 语音输入弹窗
                showAsrPopWindow(extra_asr);
            } else {
                // 语音识别按钮处于激活状态
                openApplication(ExtraActivity.this, selectedApp);
            }
        });

        // 键盘输入
        ImageButton extra_keyboard = findViewById(R.id.extra_keyboard);
        // 设置键盘输入按钮是否可见（解开注释则不可见，否则默认可见）
        extra_keyboard.setVisibility(Integer.parseInt(getResources().getString(R.string.visibility)));
        // 点击 键盘输入按钮
        extra_keyboard.setOnClickListener(v -> {
            // 键盘输入
            Toast.makeText(ExtraActivity.this, "键盘输入", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * 初始化对话弹窗
     *
     * @return RecyclerViewAdapter 实例
     */
    private ExtraDialogRecyclerViewAdapter initRecyclerView() {
        messages.add(new Message(questions.get(schedule), Message.TYPE_RECEIVED)); // 添加信息

        // 创建 GridLayoutManager 实例
        GridLayoutManager gridLayoutManager = new GridLayoutManager(ExtraActivity.this, 1);
        gridLayoutManager.setOrientation(RecyclerView.VERTICAL); // 设置 recyclerView 为竖向滚动
        recyclerView.setLayoutManager(gridLayoutManager); // 将 gridLayoutManager 设置为 recyclerView 的布局管理器

        // 创建 RecyclerViewAdapter 实例
        ExtraDialogRecyclerViewAdapter extraDialogRecyclerViewAdapter = new ExtraDialogRecyclerViewAdapter(messages);
//        extraDialogRecyclerViewAdapter.setButtonClickListener(this::showEditPopWindow); // 点击 编辑按钮 弹出编辑文本弹窗
        // 点击 项视图
        extraDialogRecyclerViewAdapter.setOnItemClickListener((view, position) -> {
            // 读出项视图的内容（语音合成）
            /*code*/
            // 模拟语音合成
            if (messages.get(position).getType() == Message.TYPE_RECEIVED) {
                Toast.makeText(ExtraActivity.this, messages.get(position).getContent(), Toast.LENGTH_SHORT).show();
            } else showEditPopWindow(position); // 弹出 编辑文本弹窗
        });
        recyclerView.setAdapter(extraDialogRecyclerViewAdapter); // 将 recyclerViewAdapter 设置为 recyclerView 的适配器
        return extraDialogRecyclerViewAdapter;
    }

    /**
     * 接收信息
     *
     * @param message 接收的信息
     */
    private void receive(String message) {
        new Handler().postDelayed(() -> {
            messages.add(new Message(message, Message.TYPE_RECEIVED)); // 添加信息
            extraDialogRecyclerViewAdapter.notifyItemInserted(messages.size() - 1);
            recyclerView.scrollToPosition(messages.size() - 1);
        }, 500);
    }

    /**
     * 显示语音识别弹窗
     *
     * @param button 语音输入按钮
     */
    private void showAsrPopWindow(Button button) {
        // 处理无效的视图参数
        if (recyclerView == null) return;

        button.setVisibility(View.INVISIBLE); // 隐藏语音输入按钮
        darkenBackground(0.3f); // 背景变暗

        // 语音输入弹窗
        @SuppressLint("InflateParams") View activity_extra_asr = getLayoutInflater().inflate(R.layout.activity_extra_asr, null);

        // 创建 PopupWindow 实例
        PopupWindow extra_asr_window = new PopupWindow(activity_extra_asr, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, false);
        extra_asr_window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE); // 软键盘不会遮挡输入框
        extra_asr_window.showAtLocation(recyclerView, Gravity.BOTTOM, 0, 0); // 显示语音识别弹窗

        // 语音识别文本展示框
        TextView extra_asr_text = activity_extra_asr.findViewById(R.id.extra_asr_text);
        // 语音输入结束按钮
        Button extra_asr_end = activity_extra_asr.findViewById(R.id.extra_asr_end);
        // 语音识别取消按钮
        ImageButton extra_asr_cancel = activity_extra_asr.findViewById(R.id.extra_asr_cancel);
        // 语音识别确认按钮
        ImageButton extra_asr_confirm = activity_extra_asr.findViewById(R.id.extra_asr_confirm);

        //点击 语音输入结束按钮
        extra_asr_end.setOnClickListener(v -> {
            // 结束录音（应该有一个结束录音的方法）
            /*code*/
            vibrate(300); // 模拟结束录音（震动）
            extra_asr_end.setVisibility(View.INVISIBLE); // 隐藏语音输入识别结束按钮

            // 语音识别
            /*code*/
            String text = answers.get(schedule);

            // 语音识别文本激活
            extra_asr_text.setTextColor(ExtraActivity.this.getColor(R.color.asr_text_filled));
            extra_asr_text.setText(text); // 显示语音识别文本
            extra_asr_cancel.setVisibility(View.VISIBLE); // 显示语音识别取消按钮
            extra_asr_confirm.setVisibility(View.VISIBLE); // 显示语音识别确认按钮
        });

        // 点击 语音识别取消按钮
        extra_asr_cancel.setOnClickListener(v -> extra_asr_window.dismiss());

        // 点击 语音识别确认按钮
        extra_asr_confirm.setOnClickListener(v -> {
            isVoiceInputConfirmed = true; // 语音输入确认
            messages.add(new Message(extra_asr_text.getText().toString(), Message.TYPE_SENT)); // 添加语音识别结果
            extraDialogRecyclerViewAdapter.notifyItemInserted(messages.size() - 1); // 更新对话弹窗
            schedule++; // 问题进度 +1
            extra_asr_window.dismiss(); // 退出语音识别弹窗
        });

        extra_asr_window.setOnDismissListener(() -> {
            darkenBackground(1f); // 还原背景

            if (isVoiceInputConfirmed) {
                if (schedule == answers.size()) {
                    allQuestionsAnswered = true; // 问题已经全部回答完毕
                    // 激活语音识别按钮
                    button.setText(R.string.extra_asr_activated);
                    button.setActivated(true);
                } else {
                    // 显示下一个问题
                    if (schedule == 1) receive(hint.get(1));
                    receive(questions.get(schedule));
                    isVoiceInputConfirmed = false;
                }
            }

            button.setVisibility(View.VISIBLE); // 显示语音输入按钮
        });
    }

    /**
     * 显示文本编辑弹窗
     *
     * @param position 所选择对话的下标
     */
    private void showEditPopWindow(int position) {
        darkenBackground(0.3f); // 背景变暗

        // 文本编辑弹窗
        @SuppressLint("InflateParams") View activity_extra_edit = getLayoutInflater().inflate(R.layout.activity_extra_edit, null);
        // 问题展示框
        TextView extra_edit_question = activity_extra_edit.findViewById(R.id.extra_edit_question);
        // 设置问题
        if (position < 7) extra_edit_question.setText(messages.get(position - 2).getContent());
        else extra_edit_question.setText(messages.get(position - 1).getContent());
        // 回答展示框
        TextView extra_edit_answer = activity_extra_edit.findViewById(R.id.extra_edit_answer);
        extra_edit_answer.setText(messages.get(position).getContent()); // 设置回答

        // 创建 PopupWindow 实例
        PopupWindow extra_edit = new PopupWindow(activity_extra_edit, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT, false);
        extra_edit.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE); // 软键盘不会遮挡输入框
        extra_edit.showAtLocation(recyclerView, Gravity.CENTER, 0, 0); // 显示文本编辑弹窗

        // 取消按钮
        ImageButton extra_edit_cancel = activity_extra_edit.findViewById(R.id.extra_edit_cancel);
        extra_edit_cancel.setOnClickListener(v -> extra_edit.dismiss()); // 点击 取消按钮

        // 语音识别按钮
        ImageButton extra_edit_asr = activity_extra_edit.findViewById(R.id.extra_edit_asr);
        // 按下 语音识别按钮
        extra_edit_asr.setOnClickListener(v -> {
            if (!isEditASRActivated) {
                // 开始录音（应该有一个开始录音的方法）
                /*code*/
                vibrate(200); // 模拟开始录音（震动）
                // 编辑弹窗语音识别激活
                isEditASRActivated = true;
                extra_edit_asr.setActivated(true);
                // 重置语音识别文本
                extra_edit_answer.setTextColor(ExtraActivity.this.getColor(R.color.asr_text_empty));
                extra_edit_answer.setText(R.string.asr_text);
            } else {
                // 结束录音（应该有一个结束录音的方法）
                /*code*/
                vibrate(300); // 模拟结束录音（震动）
                extra_edit_asr.setVisibility(View.INVISIBLE); // 隐藏语音识别按钮（等待语音识别产生结果）

                // 语音识别
                /*code*/
                // 模拟语音识别结果
                String text = revisedAnswers.get((position - 2) / 2);

                // 编辑弹窗语音识别休眠
                extra_edit_asr.setActivated(false);
                extra_edit_answer.setTextColor(ExtraActivity.this.getColor(R.color.asr_text_filled));

                // 显示语音识别结果
                extra_edit_answer.setText(text);
                isEditASRActivated = false;
            }
        });

        // 键盘输入按钮
        ImageButton extra_edit_keyboard = activity_extra_edit.findViewById(R.id.extra_edit_keyboard);
        extra_edit_keyboard.setOnClickListener(v -> {
            // 键盘输入
            Toast.makeText(ExtraActivity.this, "键盘输入", Toast.LENGTH_SHORT).show();
        });

        // 确认按钮
        ImageButton extra_edit_confirm = activity_extra_edit.findViewById(R.id.extra_edit_confirm);
        // 按下 确认按钮
        extra_edit_confirm.setOnClickListener(v -> {
            // 更新回答信息
            messages.get(position).setContent(extra_edit_answer.getText().toString());
            extraDialogRecyclerViewAdapter.notifyItemChanged(position);
            extra_edit.dismiss(); // 退出文本编辑弹窗
        });

        extra_edit.setOnDismissListener(() -> darkenBackground(1f));
    }

    /**
     * 背景变暗
     *
     * @param alpha 透明度，范围从 0.0 到 1.0
     */
    private void darkenBackground(Float alpha) {
        if (alpha == null || alpha < 0.0f || alpha > 1.0f) alpha = 0.5f; // 透明度值无效，使用默认值

        Window window = ExtraActivity.this.getWindow();
        try {
            WindowManager.LayoutParams lp = window.getAttributes(); // 获取当前窗口的布局参数
            lp.alpha = alpha; // 设置窗口的透明度
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND); // 为窗口添加背后模糊效果的标志
            window.setAttributes(lp); // 应用更新后的布局参数到窗口
        } catch (Exception e) {
            // 处理设置窗口属性时可能出现的异常
            Log.e(TAG, "Failed to darken background", e);
        }
    }

    /**
     * 启动指定应用
     *
     * @param context 当前上下文环境
     * @param app     应用信息
     */
    private void openApplication(Context context, AppInfo app) {
        // 获取应用的启动 Intent
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(app.getPackageName());
        // 检查是否成功获取到启动 Intent
        if (intent != null) {
            try {
                context.startActivity(intent); // 启动应用
            } catch (ActivityNotFoundException e) {
                // 处理无法找到对应的 Activity 的情况
                Log.e(TAG, "Activity not found for package: " + app.getPackageName(), e);
                Toast.makeText(context, "没有权限启动应用。", Toast.LENGTH_SHORT).show();
            }
        } else {
            // 处理无法获取启动 Intent 的情况
            Log.e(TAG, "No launch intent found for package: " + app.getPackageName());
            Toast.makeText(context, "无法启动应用。", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 手机震动
     *
     * @param milliseconds 震动时长
     */
    private void vibrate(long milliseconds) {
        // 获取 Vibrator 服务的实例
        Vibrator vibrator = (Vibrator) ExtraActivity.this.getSystemService(VIBRATOR_SERVICE);

        // 检查设备是否支持震动功能
        if (vibrator != null && vibrator.hasVibrator()) {
            // 创建一个震动 milliseconds 毫秒的 VibrationEffect
            VibrationEffect vibrationEffect = VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE);
            try {
                vibrator.vibrate(vibrationEffect); // 执行震动
            } catch (IllegalArgumentException e) {
                // 处理震动无法执行的情况，例如不合法的震动时长
                Log.e(TAG, "Failed to vibrate", e);
            }
        }
    }
}