package com.example.jarvis;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jarvis.model.AppInfo;
import com.example.jarvis.model.Message;
import com.example.jarvis.utils.ChatToGPTUtil;
import com.example.jarvis.utils.DialogueInfoAdapter;
import com.example.jarvis.utils.HttpRequestsUtil;
import com.example.jarvis.utils.KeyboardUtil;
import com.example.jarvis.utils.LogUtil;
import com.example.jarvis.utils.SlotUtil;
import com.example.jarvis.utils.SpeechToTextUtil;
import com.example.jarvis.utils.TextToSpeechUtil;
import com.example.jarvis.utils.ToastUtil;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * 补充对话界面
 */
public class ExtraActivity extends AppCompatActivity implements ExtraEditDialog.ExtraEditDialogListener {
    private static final String TAG = "ExtraActivity";
    private static final Integer MESSAGE_CODE = 612;

    private final List<String> hint = Arrays.asList("点击白色文本框，我会读出其中的内容", "点击绿色文本框可以更改其中的内容"); // 系统提示
    private final List<Message> messages = new ArrayList<>(); // 对话信息列表
    private final SpeechToTextUtil speechToTextUtil = new SpeechToTextUtil(this); // 语音识别工具类（已弃用）
    private final VoiceRecognitionUtil voiceRecognitionUtil = new VoiceRecognitionUtil(this); // 语音识别工具类

    private JSONObject slots; // 场景所需 slots
    private AppInfo selectedApp; // 用户选择的应用信息
    private AlertDialog alertDialog; // 等待弹窗
    private RecyclerView recyclerView; // 对话框滚动视图
    private TextToSpeechUtil textToSpeechUtil; // 语音合成工具类
    private DialogueInfoAdapter dialogueInfoAdapter; // 对话框布局适配器

    private String answer = ""; // 用户的回复
    private String question = ""; // 补充问题
    private String extraKeyboardEditText = ""; // 补充对话界面 手动输入框内容

    private Integer position = 0; // 选中对话的下标

    private Boolean isSwitchActivated = Boolean.FALSE; // 输入切换按钮是否激活
    private Boolean allQuestionsAnswered = Boolean.FALSE; // 问题是否全部回答完毕
    private Boolean isVoiceInputConfirmed = Boolean.FALSE; // 语音输入是否确认

    // 语音输入按钮
    Button extra_asr;
    // 语音输入栏
    LinearLayout extra_input;
    // 返回按钮
    ImageButton extra_return;
    // 输入切换按钮
    ImageButton extra_switch;
    // 键盘输入框
    EditText extra_keyboard_edit;
    // 键盘输入发送按钮
    Button extra_keyboard_send;

    private final Handler handler = new Handler(msg -> {
        if (msg.what == MESSAGE_CODE) {
            parseResponse_Baichuan(msg.obj.toString()); // 解析返回结果
//            parseResponse_GPT(msg.obj.toString()); // 解析返回结果
            if (messages.size() == 4) {
//                receive(hint.get(1)); // 显示第二个提示
            }
            if (!allQuestionsAnswered) receive(question); // 接收信息

        } else if (msg.what == ChatToGPTUtil.MESSAGE_CODE) {
            parseResponse_GPT(msg.obj.toString());

            // 键盘输入休眠
            extra_keyboard_edit.clearFocus(); // 清除焦点
            KeyboardUtil.hideSoftInput(this); // 隐藏软键盘
            extra_switch.setVisibility(View.INVISIBLE); // 隐藏输入切换按钮
            extra_keyboard_edit.setVisibility(View.INVISIBLE); // 隐藏键盘输入框
            extra_keyboard_send.setVisibility(View.INVISIBLE); // 隐藏键盘输入发送按钮
            isSwitchActivated = Boolean.FALSE; // 将 isSwitchActivated 设置为 false
            extra_switch.setActivated(false); // 输入切换按钮休眠

            // 语音识别按钮激活
            extra_asr.setActivated(true);
            extra_asr.setText(R.string.extra_asr_activated);
            extra_asr.setVisibility(View.VISIBLE); // 显示语音识别按钮

            receive(question); // 接收信息
        }

        // 显示接收的问题
        alertDialog.dismiss(); // 关闭等待弹窗
        darkenBackground(1.0f); // 恢复背景
        return true;
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extra);

        // 初始化
        initAlertDialog("请稍后", "语悦正在思考，请耐心等待...", Boolean.FALSE); // 初始化等待弹窗
        initExtraActivity(); // 初始化 ExtraActivity
        textToSpeechUtil = new TextToSpeechUtil(this); // 初始化文字转语音
        @SuppressLint("InflateParams") View activity_extra_asr = getLayoutInflater().inflate(R.layout.activity_extra_asr, null); // 语音输入弹窗
        initVoiceRecognitionUtil(activity_extra_asr.findViewById(R.id.extra_asr_text)); // 初始化语音识别

        // 捕捉 UI
        extra_asr = findViewById(R.id.extra_asr); // 语音输入按钮
        extra_input = findViewById(R.id.extra_input); // 语音输入栏
        extra_return = findViewById(R.id.extra_return); // 返回按钮
        extra_switch = findViewById(R.id.extra_switch); // 输入切换按钮
        extra_keyboard_edit = findViewById(R.id.extra_keyboard_edit); // 键盘输入框
        extra_keyboard_send = findViewById(R.id.extra_keyboard_send); // 键盘输入发送按钮

        // 点击 返回按钮
        extra_return.setOnClickListener(v -> finish()); // 返回主界面（MainActivity）

        // 点击 语音输入按钮
        extra_asr.setOnClickListener(v -> {
            if (!allQuestionsAnswered) {
                // 语音识别
                VibratorUtil.vibrate(this, 200); // 交互反馈
//                speechToTextUtil.startListening();
                voiceRecognitionUtil.startListening(); // 开始语音识别

                // 弹出 语音输入弹窗
                showASRPopWindow(activity_extra_asr, extra_input);
            } else {
                // 打开应用
                VibratorUtil.vibrate(this, 200); // 交互反馈
//                openApp(ExtraActivity.this, selectedApp.getPackageName());
                // 测试
                initAlertDialog("感谢您", "本次实验结束，感谢您的参与~", Boolean.TRUE);
                alertDialog.show();
                new Handler().postDelayed(() -> {
                    alertDialog.dismiss();
                    finish();
                }, 5000);
            }
        });

        // 监听编辑文本时的动作（按下回车）
        extra_keyboard_edit.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                // 等待后端回复
                darkenBackground(0.3f); // 背景变暗
                alertDialog.show(); // 显示等待弹窗

                // 发送键盘输入信息
                sendKeyboard(extra_keyboard_edit);
                getQuestion(); // 获取下一个问题
            }
            return false;
        });
        // 点击 键盘输入发送按钮
        extra_keyboard_send.setOnClickListener(v -> {
            // 等待后端回复
            darkenBackground(0.3f); // 背景变暗
            alertDialog.show(); // 显示等待弹窗

            // 发送键盘输入信息
            sendKeyboard(extra_keyboard_edit);
            getQuestion(); // 获取下一个问题
        });

        // 设置输入切换按钮是否可见
        extra_switch.setVisibility(Integer.parseInt(getString(R.string.visibility)));
        // 点击 输入切换按钮
        extra_switch.setOnClickListener(v -> {
            if (!isSwitchActivated) {
                // 输入切换按钮未激活
                extra_asr.setVisibility(View.INVISIBLE); // 隐藏语音输入按钮
                extra_keyboard_send.setVisibility(View.VISIBLE); // 显示键盘输入发送按钮
                extra_keyboard_edit.setVisibility(View.VISIBLE); // 显示键盘输入框
                extra_keyboard_edit.setText(extraKeyboardEditText); // 设置文本输入框的内容
                extra_keyboard_edit.requestFocus(); // 请求文本输入框的焦点
                extra_keyboard_edit.setSelection(extraKeyboardEditText.length()); // 将光标移至文本末尾
                extra_keyboard_edit.postDelayed(() -> KeyboardUtil.showSoftInput(extra_keyboard_edit), 100); // 弹出软键盘

                // 输入切换按钮激活
                isSwitchActivated = Boolean.TRUE;
                extra_switch.setActivated(true);
            } else {
                // 输入切换按钮已激活
                extra_keyboard_edit.clearFocus(); // 清除焦点
                KeyboardUtil.hideSoftInput(this); // 隐藏软键盘
                extra_keyboard_edit.setVisibility(View.INVISIBLE); // 隐藏键盘输入框
                extra_keyboard_send.setVisibility(View.INVISIBLE); // 隐藏键盘输入发送按钮
                extra_asr.setVisibility(View.VISIBLE); // 显示语音输入按钮
                extraKeyboardEditText = extra_keyboard_edit.getText().toString(); // 获取文本输入框的内容

                // 输入切换按钮休眠
                isSwitchActivated = Boolean.FALSE;
                extra_switch.setActivated(false);
            }
        });
    }

    /**
     * 初始化 ExtraActivity
     */
    private void initExtraActivity() {
        // 从 Intent 中获取用户选择的应用
        selectedApp = getIntent().getParcelableExtra("selected_application");
        // 测试
        LogUtil.debug(TAG, "initExtraActivity", (selectedApp != null ? selectedApp.getAppName() : "空的"), Boolean.TRUE);
        // 获取初始 slot（测试）
        try {
            if (selectedApp != null) {
                slots = new JSONObject(SlotUtil.getSlots(this, selectedApp.getAppName()));
                LogUtil.info(TAG, "initExtraActivity", slots.toString(), Boolean.TRUE);
            } else {
                slots = new JSONObject(SlotUtil.getSlots(this, "打车"));
                LogUtil.warning(TAG, "initExtraActivity", "应用信息为空，使用默认信息", Boolean.TRUE);
            }
        } catch (JSONException e) {
            LogUtil.error(TAG, "initExtraActivity", "未知场景", e);
        }

        // 从 Intent 中获取起始问题
        String first_question = getIntent().getStringExtra("first_question");
        // 检查起始问题是否为空
        if (first_question == null || first_question.isEmpty()) {
            question = "";
            LogUtil.warning(TAG, "initExtraActivity", "初始问题为空，使用默认信息", Boolean.TRUE);
            messages.add(new Message("No Received message", Message.TYPE_RECEIVED)); // 添加默认消息
        } else {
            question = first_question;
            messages.add(new Message(question, Message.TYPE_RECEIVED)); // 添加新消息到对话列表
        }

        // 创建 LinearLayoutManager 实例
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL); // 设置 recyclerView 为竖向滚动

        // 滚动对话框
        recyclerView = findViewById(R.id.extra_dialog);
        recyclerView.setLayoutManager(linearLayoutManager); // 将 linearLayoutManager 设置为 recyclerView 的布局管理器
        // 设置在软键盘弹出之后不会遮挡 RecyclerView 的内容
        recyclerView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> recyclerView.postDelayed(() -> recyclerView.scrollToPosition(messages.size() - 1), 100));

        // 创建 RecyclerViewAdapter 实例
        dialogueInfoAdapter = new DialogueInfoAdapter(messages);
        dialogueInfoAdapter.setButtonClickListener(this::showEditPopWindow); // 点击 编辑按钮 弹出发送信息编辑弹窗
        // 点击 项视图
        dialogueInfoAdapter.setOnItemClickListener((view, position) -> {
            if (position < 0 || position >= messages.size()) {
                // 处理无效的 position
                LogUtil.warning(TAG, "initExtraActivity", "Invalid position in messages list: " + position, Boolean.TRUE);
                return;
            }

            // 获取项视图的信息
            this.position = position;
            Message message = messages.get(position);
            if (message.getType() == Message.TYPE_RECEIVED) {
                // 点击的项视图为接收的信息
                String content = message.getContent();
                if (content == null || content.isEmpty()) {
                    LogUtil.warning(TAG, "initExtraActivity", "Message content null or empty string", Boolean.TRUE);
                    return;
                }
                // 读出项视图的内容（语音合成）
                if (textToSpeechUtil.isInitialized()) textToSpeechUtil.speak(content);
            } else {
                // 点击的项视图为发送的信息
                VibratorUtil.vibrate(this, 200); // 交互反馈
//                showEditPopWindow(position); // 弹出发送信息编辑弹窗
                // 测试
                ToastUtil.showShort(this, "该功能正在研发当中。。。");
            }
        });

        // 将 recyclerViewAdapter 设置为 recyclerView 的适配器
        recyclerView.setAdapter(dialogueInfoAdapter);
        // 发送第一个提示
        receive(hint.get(0));
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
                if (results != null) {
                    answer += results.getResultString().replace("。", "");
                    asrText.setText(answer);
                } else {
                    asrText.setText("识别失败");
                    LogUtil.warning(TAG, "initVoiceRecognitionUtil_onResult", "语音识别失败", Boolean.TRUE);
                }
            }

            @Override
            public void onError(SpeechError error) {
                asrText.setText("您好像没有说话哦");
                LogUtil.warning(TAG, "initVoiceRecognitionUtil_onError", "您好像没有说话哦", Boolean.TRUE);
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
     * 初始化一个不可取消的对话框
     */
    private void initAlertDialog(String title, String message, Boolean isCancelable) {
        // 创建并显示对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(message).setCancelable(isCancelable); // 禁止通过点击外部取消
        alertDialog = builder.create();
    }

    /**
     * 显示语音识别弹窗
     *
     * @param extraAsrActivity 语音输入弹窗
     * @param linearLayout     输入栏
     */
    private void showASRPopWindow(View extraAsrActivity, LinearLayout linearLayout) {

        // 隐藏输入栏
        linearLayout.setVisibility(View.GONE);
        darkenBackground(0.3f); // 背景变暗

        // 创建 PopupWindow 实例
        PopupWindow extra_asr_window = new PopupWindow(extraAsrActivity, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, false);
        extra_asr_window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE); // 软键盘不会遮挡输入框
        extra_asr_window.showAtLocation(recyclerView, Gravity.BOTTOM, 0, 0); // 显示语音识别弹窗

        // 语音识别文本展示框
        TextView extra_asr_text = extraAsrActivity.findViewById(R.id.extra_asr_text);
        // 语音识别取消按钮
        ImageButton extra_asr_cancel = extraAsrActivity.findViewById(R.id.extra_asr_cancel);
        // 语音识别确认按钮
        ImageButton extra_asr_confirm = extraAsrActivity.findViewById(R.id.extra_asr_confirm);
        // 语音输入结束按钮
        Button extra_asr_end = extraAsrActivity.findViewById(R.id.extra_asr_end);

        // 重置语音识别文本
        extra_asr_text.setTextColor(getColor(R.color.asr_text_empty));
        extra_asr_text.setText(getString(R.string.asr_text));

        //点击 语音输入结束按钮
        extra_asr_end.setOnClickListener(v -> {
            // 语音识别休眠
            VibratorUtil.vibrate(this, 300); // 交互反馈
            voiceRecognitionUtil.stopListening(); // 结束语音识别
            extra_asr_end.setVisibility(View.INVISIBLE); // 隐藏语音输入识别结束按钮

            // 语音识别文本激活
            extra_asr_text.setTextColor(getColor(R.color.asr_text_filled));
            extra_asr_cancel.setVisibility(View.VISIBLE); // 显示语音识别取消按钮
            extra_asr_confirm.setVisibility(View.VISIBLE); // 显示语音识别确认按钮
        });

        // 点击 语音识别取消按钮
        extra_asr_cancel.setOnClickListener(v -> extra_asr_window.dismiss());
        // 点击 语音识别确认按钮
        extra_asr_confirm.setOnClickListener(v -> {
            // 语音输入确认
            isVoiceInputConfirmed = Boolean.TRUE; // 设置 isVoiceInputConfirmed 为 true
            // 退出语音识别弹窗
            extra_asr_window.dismiss();
        });

        // 弹窗退出监听
        extra_asr_window.setOnDismissListener(() -> {
            // 还原状态
            linearLayout.setVisibility(View.VISIBLE);
            extra_asr_cancel.setVisibility(View.INVISIBLE); // 隐藏语音识别取消按钮
            extra_asr_confirm.setVisibility(View.INVISIBLE); // 隐藏语音识别确认按钮
            extra_asr_end.setVisibility(View.VISIBLE); // 显示语音输入识别结束按钮

            // 判断用户是否确认了语音输入的内容
            if (isVoiceInputConfirmed) {
                isVoiceInputConfirmed = Boolean.FALSE;
                // 发送语音识别信息
                send(answer);
                alertDialog.show(); // 显示等待弹窗
                getQuestion(); // 获取下一个问题
            } else {
                answer = ""; // 清空用户输入
                darkenBackground(1.0f); // 还原背景
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
            LogUtil.warning(TAG, "showEditPopWindow", "Invalid position: " + position, Boolean.TRUE);
            return;
        }

        // 获取问题
        String question = messages.get(position - (position < 3 ? 2 : 1)).getContent();
        if (question == null || question.isEmpty()) {
            LogUtil.warning(TAG, "showEditPopWindow", "Question text is null or empty string", Boolean.TRUE);
            return;
        }

        // 获取回复
        String answer = messages.get(position).getContent();
        if (answer == null || answer.isEmpty()) {
            LogUtil.warning(TAG, "showEditPopWindow", "Answer text is null or empty string", Boolean.TRUE);
            return;
        }

        // 显示对话框
        ExtraEditDialog extraEditDialog = new ExtraEditDialog(this, findViewById(R.id.extra_input), question, answer);
        extraEditDialog.show(getSupportFragmentManager(), "extraEditDialog");
    }

    /**
     * 获取新问题
     */
    private void getQuestion() {
        JSONObject json = new JSONObject();
        String promptHead = "你是一个负责信息抽取的机器人，需要信息抽取的场景是\"" + selectedApp.getAppName() + "\"。请你根据与用户的对话填充槽位、并不断对槽位为空的部分进行提问，每一次提问的槽位数量为 1。";
        String promptBody = "如果用户所回答的内容中，有不属于你上轮对话中提问的槽位，那么请不要将用户回答内容中出错的部分填入槽位，而是对用户回答中出错的槽位进行重新询问。\n槽位:" + slots.toString() + "\n";
        String promptTail = "上轮对话:{\"gpt\":\"" + question + "\",\"human\":\"" + answer + "\"}";
        String prompt = promptHead + promptBody + promptTail;
        try {
            json.put("prompt", prompt);
            // 测试
            LogUtil.debug(TAG, "getQuestion", json.toString(), Boolean.TRUE);
        } catch (JSONException e) {
            LogUtil.error(TAG, "getQuestion", "JSON 构建失败", e);
        }

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(json.toString(), JSON);

        answer = ""; // 还原用户回复
        HttpRequestsUtil.postSync(getString(R.string.Baichuan2_7B_Base_URL), requestBody, handler, MESSAGE_CODE);
    }

    /**
     * 拆解 Response（Baichuan2_7B_Base）
     *
     * @param responseBody 响应体
     */
    private void parseResponse_Baichuan(String responseBody) {
        JSONObject jsonContent;
        String content;
        try {
            jsonContent = new JSONObject(responseBody);
            if (jsonContent.has("content")) {
                // 获取问题
                content = jsonContent.getString("content");
            } else if (jsonContent.has("conclusion")) {
                allQuestionsAnswered = Boolean.TRUE;
//                question = jsonContent.getString("conclusion");
                ChatToGPTUtil.getConclusion(this, slots, handler);
                return;
            } else {
                LogUtil.warning(TAG, "parseResponse_Baichuan", "未找到 content 字段", Boolean.TRUE);
                return;
            }
        } catch (JSONException e) {
            LogUtil.error(TAG, "parseResponse_Baichuan", "获取 Json 字段失败", e);
            return;
        }

        // 正则表达式来匹配整个 JSON 部分
        String jsonRegex = "\\{[^}]*\\}";
        Pattern jsonPattern = Pattern.compile(jsonRegex, Pattern.DOTALL);
        Matcher jsonMatcher = jsonPattern.matcher(content);

        if (jsonMatcher.find()) {
            // 提取 slots 信息
            String jsonString = jsonMatcher.group();
            try {
                slots = new JSONObject(jsonString);
            } catch (JSONException e) {
                LogUtil.error(TAG, "parseResponse_Baichuan", "提取 slots 失败", e);
                return;
            }
            LogUtil.info(TAG, "parseResponse_Baichuan", slots.toString(), Boolean.TRUE);

            // 提取问题
            question = content.substring(jsonMatcher.end()).trim();
            LogUtil.info(TAG, "parseResponse_Baichuan", question, Boolean.TRUE);
        } else LogUtil.warning(TAG, "parseResponse_Baichuan", "未找到 JSON", Boolean.TRUE);
    }

    /**
     * 解析返回结果（GPT-4o）
     *
     * @param responseBody 返回体
     */
    private void parseResponse_GPT(String responseBody) {
        try {
            JSONObject jsonObject = new JSONObject(responseBody);
            JSONArray choices = jsonObject.getJSONArray("choices");
            question = choices.getJSONObject(0).getJSONObject("message").getString("content");
            LogUtil.info(TAG, "parseResponse_GPT", question, Boolean.TRUE);
        } catch (JSONException e) {
            LogUtil.error(TAG, "parseResponse_GPT", "未找到 content 字段", e);
        }
    }

    /**
     * 接收信息
     *
     * @param content 接收的信息内容
     */
    private void receive(String content) {
        // 检查 content 是否为空
        if (content == null || content.isEmpty()) {
            LogUtil.warning(TAG, "receive", "Message content null or empty string", Boolean.TRUE);
            return;
        }

        // 接收消息
        messages.add(new Message(content, Message.TYPE_RECEIVED)); // 将新消息添加到消息列表
        // 通知适配器有新项插入，并更新 RecyclerView
        dialogueInfoAdapter.notifyItemInserted(messages.size() - 1);
        // 滚动到 RecyclerView 的最底部，显示最后一条消息
        recyclerView.scrollToPosition(messages.size() - 1);
    }

    /**
     * 发送消息
     *
     * @param content 发送的信息内容
     */
    private void send(String content) {
        // 检查 content 是否为空
        if (content == null || content.isEmpty()) {
            LogUtil.warning(TAG, "send", "Message content null or empty string", Boolean.TRUE);
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
        answer = editText.getText().toString();
        // 重置文本输入框的内容
        extraKeyboardEditText = "";
        editText.setText(extraKeyboardEditText);
        // 发送消息
        send(answer);
    }

    /**
     * 背景变暗
     *
     * @param alpha 透明度，范围从 0.0 到 1.0
     */
    private void darkenBackground(Float alpha) {
        // 判断透明度是否有效
        if (alpha == null || alpha < 0.0f || alpha > 1.0f) {
            LogUtil.warning(TAG, "darkenBackground", "Invalid alpha value, using default value instead.", Boolean.TRUE);
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
            View currentFocusedView = getCurrentFocus(); // 获取当前获得焦点的视图
            // 如果当前焦点的视图是 EditText
            if (currentFocusedView instanceof EditText) {
                Rect rect = new Rect(); // 创建一个 Rect 对象来获取 EditText 的全局可见区域
                currentFocusedView.getGlobalVisibleRect(rect); // 获取当前焦点视图的全局可见区域
                // 如果触摸位置不在 EditText 的可视范围内
                if (!rect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    // 收起软键盘
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (inputMethodManager != null)
                        inputMethodManager.hideSoftInputFromWindow(currentFocusedView.getWindowToken(), 0);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SpeechToTextUtil.SPEECH_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            answer = speechToTextUtil.onSpeechResult(resultCode, data);
            LogUtil.info(TAG, "onActivityResult", answer, Boolean.TRUE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textToSpeechUtil != null) textToSpeechUtil.shutdownTts(); // 关闭TTS引擎并释放资源
    }
}