package com.example.jarvis;

import static com.example.jarvis.utils.AppUtil.openApp;
import static com.example.jarvis.utils.KeyboardUtil.hideSoftInput;
import static com.example.jarvis.utils.KeyboardUtil.showSoftInput;
import static com.example.jarvis.utils.VibratorUtil.vibrate;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jarvis.model.AppInfo;
import com.example.jarvis.model.Message;
import com.example.jarvis.utils.DialogueInfoAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 补充对话界面
 */
public class ExtraActivity extends AppCompatActivity implements ExtraEditDialog.ExtraEditDialogListener {
    private final List<String> answers = Arrays.asList("长海医院", "泰宝华庭", "周五下午两点"); // 用户的回答（模拟）
    private final List<String> revisedAnswers = Arrays.asList("复旦大学附属华山医院", "我现在的位置", "现在"); // 更改后的回答（模拟）
    private int schedule = 0; // 对话的进度（模拟）
    private static final String TAG = "ExtraActivity";
    private final List<Message> messages = new ArrayList<>(); // 对话信息列表
    private final List<String> hint = Arrays.asList("点击白色文本框，我会读出其中的内容", "点击绿色文本框可以更改其中的内容"); // 系统提示
    private AppInfo selectedApp; // 用户选择的应用信息
    private List<String> questions; // 补充问题
    private RecyclerView recyclerView; // 对话框滚动视图
    private DialogueInfoAdapter dialogueInfoAdapter; // 对话框布局适配器
    private int position; // 选中对话的下标
    private String extraKeyboardEditText = ""; // 补充对话界面 手动输入框内容
    private boolean isSwitchActivated = false; // 输入切换按钮是否激活
    private boolean isVoiceInputConfirmed = false; // 语音输入是否确认
    private boolean allQuestionsAnswered = false; // 问题是否全部回答完毕

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extra);

        // 初始化 ExtraActivity
        initExtraActivity();

        // 返回按钮
        ImageButton extra_return = findViewById(R.id.extra_return);
        // 输入栏
        LinearLayout extra_input = findViewById(R.id.extra_input);
        // 语音输入按钮
        Button extra_asr = findViewById(R.id.extra_asr);
        // 输入切换按钮
        ImageButton extra_switch = findViewById(R.id.extra_switch);
        extra_switch.setVisibility(Integer.parseInt(getString(R.string.visibility))); // 设置输入切换按钮是否可见
        // 键盘输入框
        EditText extra_keyboard_edit = findViewById(R.id.extra_keyboard_edit);
        // 键盘输入发送按钮
        Button extra_keyboard_send = findViewById(R.id.extra_keyboard_send);

        // 点击 返回按钮
        extra_return.setOnClickListener(v -> finish()); // 返回主界面（MainActivity）

        // 点击 语音输入按钮
        extra_asr.setOnClickListener(v -> {
            if (!allQuestionsAnswered) {
                // 开始录音（应该有一个开始录音的方法）
                vibrate(ExtraActivity.this, 200); // 模拟开始录音（震动）
                /*code*/
                // 弹出 语音输入弹窗
                showASRPopWindow(extra_input, extra_switch, extra_asr);
            } else {
                // 语音识别按钮处于激活状态
                vibrate(ExtraActivity.this, 200); // 交互反馈
                // 打开应用
                openApp(ExtraActivity.this, selectedApp.getPackageName());
            }
        });

        // 监听编辑文本时的动作（按下回车）
        extra_keyboard_edit.setOnEditorActionListener((v, actionId, event) -> {
            // 发送键盘输入信息
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendKeyboard(extra_keyboard_edit);
                schedule++; // 问题进度 +1
                if (schedule == answers.size()) {
                    // 问题已经全部回答完毕
                    // 键盘输入休眠
                    extra_keyboard_edit.clearFocus(); // 清除焦点
                    hideSoftInput(ExtraActivity.this); // 隐藏软键盘
                    extra_switch.setVisibility(View.INVISIBLE); // 隐藏输入切换按钮
                    extra_keyboard_edit.setVisibility(View.INVISIBLE); // 隐藏键盘输入框
                    extra_keyboard_send.setVisibility(View.INVISIBLE); // 隐藏键盘输入发送按钮
                    isSwitchActivated = false; // 将 isSwitchActivated 设置为 false
                    extra_switch.setActivated(false); // 输入切换按钮休眠
                    // 语音识别按钮激活
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
                // 键盘输入休眠
                extra_keyboard_edit.clearFocus(); // 清除焦点
                hideSoftInput(ExtraActivity.this); // 隐藏软键盘
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

        // 点击 输入切换按钮
        extra_switch.setOnClickListener(v -> {
            if (!isSwitchActivated) {
                // 输入切换按钮未激活
                extra_asr.setVisibility(View.INVISIBLE); // 隐藏语音输入按钮
                extra_keyboard_edit.setVisibility(View.VISIBLE); // 显示键盘输入框
                extra_keyboard_send.setVisibility(View.VISIBLE); // 显示键盘输入发送按钮
                extra_keyboard_edit.setText(extraKeyboardEditText); // 设置文本输入框的内容
                extra_keyboard_edit.requestFocus(); // 请求文本输入框的焦点
                extra_keyboard_edit.setSelection(extraKeyboardEditText.length()); // 将光标移至文本末尾
                // 弹出软键盘
                extra_keyboard_edit.postDelayed(() -> showSoftInput(extra_keyboard_edit), 100);
                // 输入切换按钮激活
                isSwitchActivated = true;
                extra_switch.setActivated(true);
            } else {
                // 输入切换按钮已激活
                extra_keyboard_edit.clearFocus(); // 清除焦点
                hideSoftInput(ExtraActivity.this); // 隐藏软键盘
                extra_keyboard_edit.setVisibility(View.INVISIBLE); // 隐藏键盘输入框
                extra_keyboard_send.setVisibility(View.INVISIBLE); // 隐藏键盘输入发送按钮
                extra_asr.setVisibility(View.VISIBLE); // 显示语音输入按钮
                extraKeyboardEditText = extra_keyboard_edit.getText().toString(); // 获取文本输入框的内容
                // 输入切换按钮休眠
                isSwitchActivated = false;
                extra_switch.setActivated(false);
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
        dialogueInfoAdapter = new DialogueInfoAdapter(messages);
        dialogueInfoAdapter.setButtonClickListener(this::showEditPopWindow); // 点击 编辑按钮 弹出发送信息编辑弹窗
        // 点击 项视图
        dialogueInfoAdapter.setOnItemClickListener((view, position) -> {
            // 检查位置是否有效
            if (position < 0 || position >= messages.size()) {
                // 处理无效的 position，例如通过日志记录或抛出异常
                Log.e(TAG, "Invalid position in messages list: " + position);
                return;
            }

            // 获取项视图的信息
            this.position = position;
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
                // 使用 Toast 来模拟语音合成的效果
                Toast.makeText(ExtraActivity.this, content, Toast.LENGTH_SHORT).show();
            } else {
                // 点击的项视图为发送的信息
                vibrate(ExtraActivity.this, 200); // 交互反馈
                // 弹出发送信息编辑弹窗
                showEditPopWindow(position);
            }
        });
        // 将 recyclerViewAdapter 设置为 recyclerView 的适配器
        recyclerView.setAdapter(dialogueInfoAdapter);
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

        // 接收消息
        new Handler().postDelayed(() -> {
            messages.add(new Message(content, Message.TYPE_RECEIVED)); // 将新消息添加到消息列表
            // 通知适配器有新项插入，并更新 RecyclerView
            dialogueInfoAdapter.notifyItemInserted(messages.size() - 1);
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
        dialogueInfoAdapter.notifyItemInserted(messages.size() - 1);
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
    private void showASRPopWindow(LinearLayout linearLayout, ImageButton imageButton, Button button) {
        // 背景变暗
        darkenBackground(0.3f);
        // 隐藏输入栏
        linearLayout.setVisibility(View.GONE);

        // 语音输入弹窗
        @SuppressLint("InflateParams") View activity_extra_asr = getLayoutInflater().inflate(R.layout.activity_extra_asr, null);
        // 创建 PopupWindow 实例
        PopupWindow extra_asr_window = new PopupWindow(activity_extra_asr, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, false);
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
            vibrate(ExtraActivity.this, 300); // 模拟结束录音（震动）
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
            // 显示输入栏
            linearLayout.setVisibility(View.VISIBLE);
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
                    isVoiceInputConfirmed = false; // 设置 isVoiceInputConfirmed 为 false
                }
            }
        });
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

        // 获取问题
        String question = messages.get(position - (position < 3 ? 2 : 1)).getContent();
        if (question == null || question.isEmpty()) {
            Log.e(TAG, "Question text is null or empty string.");
            return;
        }
        // 获取回复
        String answer = messages.get(position).getContent();
        if (answer == null || answer.isEmpty()) {
            Log.w(TAG, "Answer text is null or empty string.");
            return;
        }

        // 获取语音识别结果（模拟）
        String temp = revisedAnswers.get((position - 2) / 2);

        // 显示对话框
        ExtraEditDialog extraEditDialog = new ExtraEditDialog(findViewById(R.id.extra_input), question, answer, temp);
        extraEditDialog.show(getSupportFragmentManager(), "extraEditDialog");
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

    /**
     * 点击 编辑文本 Dialog 确认按钮
     *
     * @param answer 回复文本
     */
    @Override
    public void onExtraEditConfirm(String answer) {
        if (!answer.equalsIgnoreCase(messages.get(position).getContent())) {
            messages.get(position).setContent(answer);
            dialogueInfoAdapter.notifyItemChanged(position);
        }
    }
}