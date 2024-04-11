package com.example.jarvis;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
    private final List<String> answers = Arrays.asList("Dongyu Road", "Public transit"); // 用户的回答（模拟）
    private final List<String> revisedAnswers = Arrays.asList("Old Humin Road", "Taxi"); // 更正后的答案（模拟）
    private AppInfo selectedApp; // 用户选择的应用信息
    private List<String> questions; // 补充问题
    private ExtraDialogRecyclerViewAdapter extraDialogRecyclerViewAdapter; // 对话框布局适配器
    private int position = 0; // 问题进度下标
    private boolean isVoiceInputConfirmed = false; // 语音输入是否确认
    private boolean isEditASRActivated = false; // 语音输入是否确认
    private boolean allQuestionsAnswered = false; // 问题是否全部回答完毕

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
        // 点击 返回按钮
        extra_return.setOnClickListener(v -> {
            // 返回主界面（MainActivity）
            finish();
        });

        // 应用选择视图
        RecyclerView recyclerView = ExtraActivity.this.findViewById(R.id.extra_dialog);
        // 初始化对话弹窗
        if (extraDialogRecyclerViewAdapter == null) {
            extraDialogRecyclerViewAdapter = initRecyclerView(recyclerView);
        }
        // 点击 编辑按钮
        extraDialogRecyclerViewAdapter.setButtonClickListener(position -> {
            // 编辑文本
            showEditPopWindow(recyclerView, position);
        });

        // 语音输入按钮
        Button extra_asr = findViewById(R.id.extra_asr);
        // 点击语音输入按钮
        extra_asr.setOnClickListener(v -> {
            if (!allQuestionsAnswered) {
                // 语音输入
                showAsrPopWindow(recyclerView, extra_asr);
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

    // 初始化对话弹窗
    private ExtraDialogRecyclerViewAdapter initRecyclerView(RecyclerView recyclerView) {
        // 添加第一个问题
        messages.add(new Message(questions.get(position), Message.TYPE_RECEIVED));

        // 创建 GridLayoutManager 实例
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1);
        // 设置 recyclerView 为竖向滚动
        gridLayoutManager.setOrientation(RecyclerView.VERTICAL);
        // 将 gridLayoutManager 设置为 recyclerView 的布局管理器
        recyclerView.setLayoutManager(gridLayoutManager);

        // 创建 RecyclerViewAdapter 实例
        ExtraDialogRecyclerViewAdapter extraDialogRecyclerViewAdapter = new ExtraDialogRecyclerViewAdapter(messages);
        // 将 recyclerViewAdapter 设置为 recyclerView 的适配器
        recyclerView.setAdapter(extraDialogRecyclerViewAdapter);

        return extraDialogRecyclerViewAdapter;
    }

    // 显示语音识别弹窗
    private void showAsrPopWindow(RecyclerView recyclerView, Button button) {
        // 处理无效的视图参数
        if (recyclerView == null) return;

        // 隐藏语音输入按钮
        button.setVisibility(View.INVISIBLE);
        // 背景变暗
        darkenBackground(0.3f);

        // 语音输入弹窗
        @SuppressLint("InflateParams") View activity_extra_asr = getLayoutInflater().inflate(R.layout.activity_extra_asr, null);

        // 创建 PopupWindow 实例
        PopupWindow extra_asr_window = new PopupWindow(activity_extra_asr, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, false);
        // 软键盘不会遮挡输入框
        extra_asr_window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        // 显示语音识别弹窗
        extra_asr_window.showAtLocation(recyclerView, Gravity.BOTTOM, 0, 0);

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
            // 隐藏输入识别结束按钮
            extra_asr_end.setVisibility(View.INVISIBLE);

            // 语音识别文本激活
            extra_asr_text.setTextColor(ExtraActivity.this.getColor(R.color.asr_text_filled));
            extra_asr_text.setText(answers.get(position)); // 显示语音识别文本

            // 显示语音识别取消按钮
            extra_asr_cancel.setVisibility(View.VISIBLE);
            // 显示语音识别确认按钮
            extra_asr_confirm.setVisibility(View.VISIBLE);
        });

        // 点击 语音识别取消按钮
        extra_asr_cancel.setOnClickListener(v -> {
            // 退出语音识别弹窗
            extra_asr_window.dismiss();
        });

        // 点击 语音识别确认按钮
        extra_asr_confirm.setOnClickListener(v -> {
            // 语音输入确认
            isVoiceInputConfirmed = true;
            // 录入语音识别结果
            messages.add(new Message(answers.get(position), Message.TYPE_SENT));
            // 更新对话弹窗
            extraDialogRecyclerViewAdapter.notifyItemInserted(messages.size() - 1);
            // 问题进度 +1
            position++;
            // 退出语音识别弹窗
            extra_asr_window.dismiss();
        });

        extra_asr_window.setOnDismissListener(() -> {
            if (isVoiceInputConfirmed) {
                if (position > answers.size() - 1) {
                    // 问题已经全部回答完毕
                    allQuestionsAnswered = true;
                    // 激活语音识别按钮
                    button.setText(R.string.extra_asr_activated);
                    button.setActivated(true);
                } else {
                    // 更新问题
                    messages.add(new Message(questions.get(position), Message.TYPE_RECEIVED));
                    extraDialogRecyclerViewAdapter.notifyItemInserted(messages.size() - 1);
                    recyclerView.scrollToPosition(messages.size() - 1);
                }
                isVoiceInputConfirmed = false;
            }
            // 还原背景
            darkenBackground(1f);
            // 显示语音输入按钮
            button.setVisibility(View.VISIBLE);
        });
    }

    // 显示文本编辑弹窗
    private void showEditPopWindow(RecyclerView recyclerView, int position) {
        // 背景变暗
        darkenBackground(0.3f);

        // 文本编辑弹窗
        @SuppressLint("InflateParams") View activity_extra_edit = getLayoutInflater().inflate(R.layout.activity_extra_edit, null);
        // 创建 PopupWindow 实例
        PopupWindow extra_edit = new PopupWindow(activity_extra_edit, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT, false);

        // 问题展示框
        TextView extra_edit_question = activity_extra_edit.findViewById(R.id.extra_edit_question);
        // 设置问题
        extra_edit_question.setText(messages.get(position - 1).getContent());
        // 回答展示框
        TextView extra_edit_answer = activity_extra_edit.findViewById(R.id.extra_edit_answer);
        // 设置回答
        extra_edit_answer.setText(messages.get(position).getContent());

        // 显示文本编辑弹窗
        extra_edit.showAtLocation(recyclerView, Gravity.CENTER, 0, 0);

        // 取消按钮
        ImageButton extra_edit_cancel = activity_extra_edit.findViewById(R.id.extra_edit_cancel);
        // 点击 取消按钮
        extra_edit_cancel.setOnClickListener(v -> {
            // 退出文本编辑弹窗
            extra_edit.dismiss();
        });

        // 语音识别按钮
        ImageButton extra_edit_asr = activity_extra_edit.findViewById(R.id.extra_edit_asr);
        // 按下 语音识别按钮
        extra_edit_asr.setOnClickListener(v -> {
            if (!isEditASRActivated) {
                // 编辑弹窗语音识别激活
                isEditASRActivated = true;
                extra_edit_asr.setActivated(true);
                // 重置语音识别文本
                extra_edit_answer.setText(R.string.asr_text);
            } else {
                // 编辑弹窗语音识别休眠
                isEditASRActivated = false;
                extra_edit_asr.setActivated(false);
                extra_edit_answer.setText(revisedAnswers.get(position / 2));

                Log.v(TAG, String.valueOf(position));
            }
        });

        // 确认按钮
        ImageButton extra_edit_confirm = activity_extra_edit.findViewById(R.id.extra_edit_confirm);
        // 按下 确认按钮
        extra_edit_confirm.setOnClickListener(v -> {
            // 更新回答信息
            messages.get(position).setContent(extra_edit_answer.getText().toString());
            extraDialogRecyclerViewAdapter.notifyItemChanged(position);
            // 退出文本编辑弹窗
            extra_edit.dismiss();
        });

        extra_edit.setOnDismissListener(() -> darkenBackground(1f));
    }

    /**
     * 背景变暗
     *
     * @param alpha 透明度，范围从 0.0 到 1.0
     */
    private void darkenBackground(Float alpha) {
        if (alpha == null || alpha < 0.0f || alpha > 1.0f) {
            // 透明度值无效，使用默认值
            alpha = 0.5f;
        }

        Window window = ExtraActivity.this.getWindow();
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
                // 启动应用
                context.startActivity(intent);
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
}