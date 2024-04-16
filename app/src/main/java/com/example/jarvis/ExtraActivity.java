package com.example.jarvis;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jarvis.model.AppInfo;
import com.example.jarvis.model.Message;
import com.example.jarvis.utils.ExtraDialogRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 补充对话界面
 */
public class ExtraActivity extends AppCompatActivity {
    private static final String TAG = "ExtraActivity";
    private final List<Message> messages = new ArrayList<>(); // 对话信息列表
    private final List<String> hint = Arrays.asList("点击白色文本框，我会读出其中的内容", "点击绿色文本框可以更改其中的内容"); // 系统提示
    private AppInfo selectedApp; // 用户选择的应用信息
    private List<String> questions; // 补充问题
    private final List<String> revisedAnswers = Arrays.asList("复旦大学附属华山医院", "我现在的位置", "现在"); // 更改后的回答（模拟）
    private ExtraDialogRecyclerViewAdapter extraDialogRecyclerViewAdapter; // 对话框布局适配器
    private RecyclerView recyclerView; // 对话框滚动视图
    private String extraKeyboardEditText = ""; // 补充对话界面 手动输入框内容
    private boolean isVoiceInputConfirmed = false; // 语音输入是否确认
    private boolean isSwitchActivated = false; // 输入切换按钮是否激活
    private boolean allQuestionsAnswered = false; // 问题是否全部回答完毕


    private final List<String> answers = Arrays.asList("长海医院", "泰宝华庭", "周五下午两点"); // 用户的回答（模拟）
    private boolean isEditASRActivated = false; // 编辑弹窗 语音输入是否激活
    private int schedule = 0; // 对话的进度（模拟）

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extra);

        // 初始化 ExtraActivity
        initExtraActivity();

        // 返回按钮
        ImageButton extra_return = findViewById(R.id.extra_return);
        extra_return.setOnClickListener(v -> finish()); // 点击 返回按钮 返回主界面（MainActivity）

        // 语音输入按钮
        Button extra_asr = findViewById(R.id.extra_asr);
        // 输入切换按钮
        ImageButton extra_switch = findViewById(R.id.extra_switch);
        // 点击语音输入按钮
        extra_asr.setOnClickListener(v -> {
            if (!allQuestionsAnswered) {
                // 开始录音（应该有一个开始录音的方法）
                /*code*/
                vibrate(200); // 模拟开始录音（震动）
                // 弹出 语音输入弹窗
                showASRPopWindow(extra_switch, extra_asr);
            } else {
                // 语音识别按钮处于激活状态
                vibrate(200); // 交互反馈
                // 打开应用
                openApplication(ExtraActivity.this, selectedApp);
            }
        });

        // 键盘输入框
        EditText extra_keyboard_edit = findViewById(R.id.extra_keyboard_edit);
        // 键盘输入发送按钮
        Button extra_keyboard_send = findViewById(R.id.extra_keyboard_send);
        // 监听编辑文本时的动作（按下回车）
        extra_keyboard_edit.setOnEditorActionListener((v, actionId, event) -> {
            // 发送键盘输入信息
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                sendKeyboard(extra_keyboard_edit);
                schedule++; // 问题进度 +1
                if (schedule == answers.size()) {
                    // 问题已经全部回答完毕
                    // 退出键盘输入模式
                    hideSoftKeyboard(extra_keyboard_edit); // 清除焦点 + 收起软键盘（异步）
                    extra_switch.setVisibility(View.INVISIBLE); // 隐藏输入切换按钮
                    extra_keyboard_edit.setVisibility(View.INVISIBLE); // 隐藏键盘输入框
                    extra_keyboard_send.setVisibility(View.INVISIBLE); // 隐藏键盘输入发送按钮
                    isSwitchActivated = false; // 将 isSwitchActivated 设置为 false
                    extra_switch.setActivated(false); // 输入切换按钮休眠
                    // 激活语音识别按钮
                    extra_asr.setActivated(true);
                    extra_asr.setText(R.string.extra_asr_activated);
                    extra_asr.setVisibility(View.VISIBLE); // 显示语音识别按钮
                    // 设置 allQuestionsAnswered 为 true
                    allQuestionsAnswered = true;
                } else {
                    // 问题并未全部回答
                    // 显示下一个问题
                    if (schedule < questions.size()) {
                        if (schedule == 1) receive(hint.get(1), 300); // 显示第二个提示
                        receive(questions.get(schedule), 600);
                    }
                }
            }
            return false;
        });
        // 点击 键盘输入发送按钮
        extra_keyboard_send.setOnClickListener(v -> {
            //  发送键盘输入消息
            sendKeyboard(extra_keyboard_edit);
            schedule++; // 问题进度 +1
            if (schedule == answers.size()) {
                // 问题已经全部回答完毕
                // 退出键盘输入模式
                hideSoftKeyboard(extra_keyboard_edit); // 清除焦点 + 收起软键盘（异步）
                extra_switch.setVisibility(View.INVISIBLE); // 隐藏输入切换按钮
                extra_keyboard_edit.setVisibility(View.INVISIBLE); // 隐藏键盘输入框
                extra_keyboard_send.setVisibility(View.INVISIBLE); // 隐藏键盘输入发送按钮
                isSwitchActivated = false; // 将 isSwitchActivated 设置为 false
                extra_switch.setActivated(false); // 输入切换按钮休眠
                // 激活语音识别按钮
                extra_asr.setActivated(true);
                extra_asr.setText(R.string.extra_asr_activated);
                extra_asr.setVisibility(View.VISIBLE); // 显示语音识别按钮
                // 设置 allQuestionsAnswered 为 true
                allQuestionsAnswered = true;
            } else {
                // 问题并未全部回答
                // 显示下一个问题
                if (schedule < questions.size()) {
                    if (schedule == 1) receive(hint.get(1), 300); // 显示第二个提示
                    receive(questions.get(schedule), 600);
                }
            }
        });

        // 设置输入切换按钮是否可见
        extra_switch.setVisibility(Integer.parseInt(getResources().getString(R.string.visibility)));
        // 点击 输入切换按钮
        extra_switch.setOnClickListener(v -> {
            if (!isSwitchActivated) {
                // 输入切换按钮未激活
                extra_asr.setVisibility(View.INVISIBLE); // 隐藏语音输入按钮
                isSwitchActivated = true; // 将 isSwitchActivated 设置为 true
                extra_switch.setActivated(true);  // 输入切换按钮激活
                extra_keyboard_edit.setText(extraKeyboardEditText); // 设置文本输入框的内容
                extra_keyboard_edit.setSelection(extraKeyboardEditText.length());
                extra_keyboard_edit.setVisibility(View.VISIBLE); // 显示键盘输入框
                extra_keyboard_send.setVisibility(View.VISIBLE); // 显示键盘输入发送按钮
                // 请求文本输入框的焦点 + 弹出软键盘（异步）
                popUpSoftKeyboard(extra_keyboard_edit);
            } else {
                // 输入切换按钮已激活
                hideSoftKeyboard(extra_keyboard_edit); // 清除焦点 + 收起软键盘（异步）
                isSwitchActivated = false; // 将 isSwitchActivated 设置为 false
                extra_switch.setActivated(false); // 输入切换按钮休眠
                extraKeyboardEditText = extra_keyboard_edit.getText().toString(); // 获取文本输入框的内容
                extra_keyboard_edit.setVisibility(View.INVISIBLE); // 隐藏键盘输入框
                extra_keyboard_send.setVisibility(View.INVISIBLE); // 隐藏键盘输入发送按钮
                extra_asr.setVisibility(View.VISIBLE); // 显示语音输入按钮
            }
        });
    }

    /**
     * 初始化 ExtraActivity
     */
    private void initExtraActivity() {
        // 从 Intent 中获取用户选择的应用
        selectedApp = getIntent().getParcelableExtra("selected_applications");
        // 从 Intent 中获取补充问题
        ArrayList<String> extraQuestions = getIntent().getStringArrayListExtra("extra_questions");
        if (extraQuestions != null) questions = extraQuestions;
        else questions = new ArrayList<>();
        // 检查消息列表是否为空 或 索引是否有效
        if (questions.isEmpty() || schedule >= questions.size()) {
            // 通过日志记录异常情况
            Log.w(TAG, "Invalid schedule index or questions list is empty.");
            // 添加默认消息
            messages.add(new Message("No Received message", Message.TYPE_RECEIVED));
        } else {
            // 添加新消息到对话列表
            messages.add(new Message(questions.get(schedule), Message.TYPE_RECEIVED));
        }

        // 创建 LinearLayoutManager 实例
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ExtraActivity.this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL); // 设置 recyclerView 为竖向滚动
        // 滚动对话框
        recyclerView = ExtraActivity.this.findViewById(R.id.extra_dialog);
        recyclerView.setLayoutManager(linearLayoutManager); // 将 linearLayoutManager 设置为 recyclerView 的布局管理器
        // 设置在软键盘弹出之后不会遮挡 RecyclerView 的内容
        recyclerView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> recyclerView.postDelayed(() -> recyclerView.scrollToPosition(messages.size() - 1), 100));

        // 创建 RecyclerViewAdapter 实例
        extraDialogRecyclerViewAdapter = new ExtraDialogRecyclerViewAdapter(messages);
        extraDialogRecyclerViewAdapter.setButtonClickListener(this::showEditPopWindow); // 点击 编辑按钮 弹出发送信息编辑弹窗
        // 点击 项视图
        extraDialogRecyclerViewAdapter.setOnItemClickListener((view, position) -> {
            // 检查位置是否有效
            if (position < 0 || position >= messages.size()) {
                // 处理无效的 position，例如通过日志记录或抛出异常
                Log.e(TAG, "Invalid position in messages list: " + position);
                return;
            }
            // 获取项视图的信息
            Message message = messages.get(position);
            if (message.getType() == Message.TYPE_RECEIVED) {
                // 点击的项视图为接收的信息
                String content = message.getContent();
                if (content == null || content.isEmpty()) {
                    Log.e(TAG, "Message content null or empty string.");
                    return;
                }
                // 读出项视图的内容（语音合成）
                /*code*/
                // 这里使用 Toast 来模拟语音合成的效果
                Toast.makeText(ExtraActivity.this, content, Toast.LENGTH_SHORT).show();
            } else {
                // 点击的项视图为发送的信息
                // 交互反馈
                vibrate(200);
                // 弹出发送信息编辑弹窗
                showEditPopWindow(position);
            }
        });
        // 将 recyclerViewAdapter 设置为 recyclerView 的适配器
        recyclerView.setAdapter(extraDialogRecyclerViewAdapter);

        // 发送第一个提示
        receive(hint.get(0), 500);
    }

    /**
     * 接收信息，并在指定的延迟后更新消息列表
     *
     * @param content     接收的信息内容
     * @param delayMillis 延迟时间（毫秒）
     */
    private void receive(String content, long delayMillis) {
        // 检查 content 是否为空
        if (content == null || content.isEmpty()) {
            Log.e(TAG, "Message content null or empty string.");
            return;
        }
        // 检查延迟时间是否为非负数
        if (delayMillis < 0) {
            Log.w(TAG, "Invalid delayMillis value, using default value instead.");
            delayMillis = 100; // 设置为默认值
        }
        // 接收消息
        new Handler().postDelayed(() -> {
            messages.add(new Message(content, Message.TYPE_RECEIVED)); // 将新消息添加到消息列表
            // 通知适配器有新项插入，并更新 RecyclerView
            extraDialogRecyclerViewAdapter.notifyItemInserted(messages.size() - 1);
            // 滚动到 RecyclerView 的最底部，显示最后一条消息
            recyclerView.scrollToPosition(messages.size() - 1);
        }, delayMillis);
    }

    /**
     * 发送语音识别消息
     *
     * @param content 发送的信息内容
     */
    private void send(String content) {
        // 检查 content 是否为空
        if (content == null || content.isEmpty()) {
            Log.e(TAG, "Message content null or empty string.");
            return;
        }
        // 发送消息
        messages.add(new Message(content, Message.TYPE_SENT)); // 将新消息添加到消息列表
        // 通知适配器有新项插入，并更新 RecyclerView
        extraDialogRecyclerViewAdapter.notifyItemInserted(messages.size() - 1);
        // 滚动到 RecyclerView 的最底部，显示最后一条消息
        recyclerView.scrollToPosition(messages.size() - 1);
    }

    /**
     * 发送键盘输入信息
     *
     * @param editText 文本编辑框
     */
    private void sendKeyboard(EditText editText) {
        // 获取发送信息
        String content = editText.getText().toString();
        // 重置文本输入框的内容
        extraKeyboardEditText = "";
        editText.setText(extraKeyboardEditText);
        // 发送消息
        send(content);
    }

    /**
     * 显示语音识别弹窗
     *
     * @param imageButton 输入切换按钮
     * @param button      语音输入按钮
     */
    private void showASRPopWindow(ImageButton imageButton, Button button) {
        // 背景变暗
        darkenBackground(0.3f);

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
            String text = answers.get(schedule); // 获取语音识别结果（模拟）
            // 检查 text 是否为空，如果为空则使用默认消息
            if (text == null || text.isEmpty()) {
                Log.w(TAG, "Speech recognition returned null or empty string.");
                text = "No Speech recognition message";
            }
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
            // 语音输入确认
            isVoiceInputConfirmed = true; // 设置 isVoiceInputConfirmed 为 true
            send(extra_asr_text.getText().toString()); // 发送语音识别信息
            schedule++; // 问题进度 +1
            // 退出语音识别弹窗
            extra_asr_window.dismiss();
        });

        // 弹窗退出监听
        extra_asr_window.setOnDismissListener(() -> {
            // 还原背景
            darkenBackground(1f);
            // 判断用户是否确认了语音输入的内容
            if (isVoiceInputConfirmed) {
                if (schedule == answers.size()) {
                    // 问题已经全部回答完毕
                    allQuestionsAnswered = true; // 设置 allQuestionsAnswered 为 true
                    imageButton.setVisibility(View.INVISIBLE); // 隐藏输入切换按钮
                    // 激活语音识别按钮
                    button.setText(R.string.extra_asr_activated);
                    button.setActivated(true);
                } else {
                    // 问题并未全部回答
                    // 显示下一个问题
                    if (schedule < questions.size()) {
                        if (schedule == 1) receive(hint.get(1), 300); // 显示第二个提示
                        receive(questions.get(schedule), 600);
                    }
                    // 设置 isVoiceInputConfirmed 为 false
                    isVoiceInputConfirmed = false;
                }
            }
        });
    }

    /**
     * 请求焦点 + 弹出软键盘（异步）
     *
     * @param editText 键盘输入框
     */
    private void popUpSoftKeyboard(EditText editText) {
        // 检查 editText 是否为 null
        if (editText == null) {
            Log.e(TAG, "The provided editText is null.");
            return;
        }
        // 将焦点设置到该输入框上
        editText.requestFocus();
        // 弹出软键盘（异步）
        editText.postDelayed(() -> {
            // 获取输入方法管理器
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                // 弹出软键盘
                inputMethodManager.showSoftInput(editText, 0);
            }
        }, 100);
    }

    /**
     * 清除焦点 + 收起软键盘（异步）
     *
     * @param editText 键盘输入框
     */
    private void hideSoftKeyboard(EditText editText) {
        // 检查 editText 是否为 null
        if (editText == null) {
            Log.e(TAG, "The provided editText is null.");
            return;
        }
        // 清除输入框的焦点
        editText.clearFocus();
        // 收起软键盘
        editText.postDelayed(() -> {
            // 获取当前活动
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                // 从当前焦点视图开始，隐藏软键盘
                inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            }
        }, 100);
    }

    /**
     * 弹出发送信息编辑弹窗
     *
     * @param position 所选择对话的下标
     */
    private void showEditPopWindow(int position) {
        // 检查 position 是否在有效范围内
        if (position < 0 || position >= messages.size()) {
            Log.e(TAG, "Invalid position: " + position);
            return;
        }

        // 背景变暗
        darkenBackground(0.3f);

        // 文本编辑弹窗
        @SuppressLint("InflateParams") View activity_extra_edit = getLayoutInflater().inflate(R.layout.activity_extra_edit, null);

        // 问题展示框
        TextView extra_edit_question = activity_extra_edit.findViewById(R.id.extra_edit_question);
        // 设置问题
        String questionText = position < 7 ? messages.get(position - 2).getContent() : messages.get(position - 1).getContent();
        if (questionText == null || questionText.isEmpty()) {
            Log.e(TAG, "Question text is null or empty string.");
            return;
        }
        extra_edit_question.setText(questionText);

        // 回答展示框
        TextView extra_edit_answer = activity_extra_edit.findViewById(R.id.extra_edit_answer);
        // 设置回答
        String answerText = messages.get(position).getContent();
        if (answerText == null || answerText.isEmpty()) {
            Log.w(TAG, "Answer text is null or empty string.");
            return;
        }
        extra_edit_answer.setText(answerText);

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
                isEditASRActivated = false;
                extra_edit_asr.setActivated(false);
                extra_edit_asr.setVisibility(View.VISIBLE);

                // 显示语音识别结果
                extra_edit_answer.setTextColor(ExtraActivity.this.getColor(R.color.asr_text_filled));
                extra_edit_answer.setText(text);
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
        // 判断透明度是否有效
        if (alpha == null || alpha < 0.0f || alpha > 1.0f) {
            Log.w(TAG, "Invalid alpha value, using default value instead.");
            alpha = 0.5f; // 设置为默认值
        }
        // 获取窗口对象
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
        // 检查延迟时间是否为非负数
        if (milliseconds < 0) {
            Log.w(TAG, "Invalid milliseconds value, using default value instead.");
            milliseconds = 331; // 设置为默认值
        }
        // 获取 Vibrator 服务的实例
        Vibrator vibrator = (Vibrator) ExtraActivity.this.getSystemService(VIBRATOR_SERVICE);
        // 检查设备是否支持震动功能
        if (vibrator != null && vibrator.hasVibrator()) {
            // 创建一个震动 milliseconds 毫秒的 VibrationEffect
            VibrationEffect vibrationEffect = VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE);
            vibrator.vibrate(vibrationEffect); // 执行震动
        }
    }

    /**
     * 点击软键盘和输入框的外部 收起软键盘
     *
     * @param ev 触摸事件对象
     * @return 布尔值，表示事件是否被处理
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // 检查触摸事件的行动类型是否为按下
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            // 获取当前获得焦点的视图
            View currentFocusedView = getCurrentFocus();
            // 如果当前焦点的视图是EditText
            if (currentFocusedView instanceof EditText) {
                // 创建一个Rect对象来获取EditText的全局可见区域
                Rect rect = new Rect();
                // 获取当前焦点视图的全局可见区域
                currentFocusedView.getGlobalVisibleRect(rect);
                // 如果触摸位置不在EditText的可视范围内
                if (!rect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    // 收起软键盘
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (inputMethodManager != null) {
                        inputMethodManager.hideSoftInputFromWindow(currentFocusedView.getWindowToken(), 0);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}