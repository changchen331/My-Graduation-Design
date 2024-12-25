package com.example.jarvis;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.example.jarvis.utils.KeyboardStateMonitor;
import com.example.jarvis.utils.KeyboardUtil;
import com.example.jarvis.utils.LogUtil;
import com.example.jarvis.utils.VibratorUtil;
import com.example.jarvis.utils.VoiceRecognitionUtil;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechError;

/**
 * 提供额外编辑功能和信息发送的对话框
 */
public class ExtraEditDialog extends DialogFragment {
    private final static String TAG = "ExtraEditDialog";

    private final VoiceRecognitionUtil voiceRecognitionUtil; // 语音识别工具类
    private final LinearLayout linearLayout; // 输入栏
    private final String question; // 问题

    private ExtraEditDialogListener listener; // 回调接口
    private KeyboardStateMonitor keyboardStateMonitor; // 软键盘监听器

    private String answer; // 回复
    private Boolean isKeyboardActivate = Boolean.FALSE; // 键盘输入是否激活

    public ExtraEditDialog(Context context, LinearLayout linearLayout, String question, String answer) {
        voiceRecognitionUtil = new VoiceRecognitionUtil(context);
        this.linearLayout = linearLayout;
        this.question = question;
        this.answer = answer;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.MyDialog); //设置对话框样式
    }

    /**
     * 初始化语音识别对象
     */
    private void initVoiceRecognitionUtil(EditText asrText) {
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
                if (results != null) answer = results.getResultString();
                else {
                    answer = "识别失败";
                    LogUtil.warning(TAG, "initVoiceRecognitionUtil_onResult", "语音识别失败", Boolean.TRUE);
                }
                asrText.setText(answer);
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View dialog_extra_edit = inflater.inflate(R.layout.dialog_extra_edit, container, true); // 文本编辑 Dialog
        ImageButton extra_edit_cancel = dialog_extra_edit.findViewById(R.id.extra_edit_cancel); // 取消按钮
        TextView extra_edit_question = dialog_extra_edit.findViewById(R.id.extra_edit_question); // 问题展示框
        EditText extra_edit_answer = dialog_extra_edit.findViewById(R.id.extra_edit_answer); // 回复展示框
        ImageButton extra_edit_asr_start = dialog_extra_edit.findViewById(R.id.extra_edit_asr_start); // 语音识别开始按钮
        ImageButton extra_edit_asr_end = dialog_extra_edit.findViewById(R.id.extra_edit_asr_end); // 语音识别结束按钮
        ImageButton extra_edit_keyboard = dialog_extra_edit.findViewById(R.id.extra_edit_keyboard); // 键盘输入按钮
        ImageButton extra_edit_confirm = dialog_extra_edit.findViewById(R.id.extra_edit_confirm); // 确认按钮

        // 初始化语音识别
        initVoiceRecognitionUtil(dialog_extra_edit.findViewById(R.id.extra_edit_answer));
        // 隐藏输入栏
        linearLayout.setVisibility(View.GONE);

        // 点击 取消按钮
        extra_edit_cancel.setOnClickListener(v -> dismiss());

        // 设置问题 & 回复
        extra_edit_question.setText(question);
        extra_edit_answer.setText(answer);

        // 点击 语音识别开始按钮
        extra_edit_asr_start.setOnClickListener(v -> {
            // 键盘输入状态休眠
            if (isKeyboardActivate) {
                extra_edit_answer.clearFocus(); // 清除输入框焦点
                KeyboardUtil.hideSoftInput(requireContext()); // 隐藏软键盘
                isKeyboardActivate = Boolean.FALSE;
            }
            extra_edit_keyboard.setVisibility(View.INVISIBLE); // 隐藏键盘输入按钮

            // 语音识别激活
            VibratorUtil.vibrate(getContext(), 200); // 交互反馈
            voiceRecognitionUtil.startListening(); // 开始语音识别
            extra_edit_answer.setEnabled(false); // 设置回复展示框为不可用状态
            extra_edit_confirm.setVisibility(View.GONE); // 隐藏确认按钮
            extra_edit_asr_start.setVisibility(View.INVISIBLE); // 隐藏语音识别开始按钮
            extra_edit_asr_end.setVisibility(View.VISIBLE); // 显示语音识别结束按钮
            extra_edit_answer.setTextColor(ContextCompat.getColor(requireContext(), R.color.asr_text_empty));
            extra_edit_answer.setText(R.string.asr_text); // 重置语音识别文本
        });

        // 点击 语音识别结束按钮
        extra_edit_asr_end.setOnClickListener(v -> {
            // 语音识别休眠
            VibratorUtil.vibrate(getContext(), 200); // 交互反馈
            voiceRecognitionUtil.stopListening(); // 结束语音识别
            extra_edit_answer.setTextColor(ContextCompat.getColor(requireContext(), R.color.asr_text_filled));
            extra_edit_asr_start.setVisibility(View.VISIBLE); // 显示语音识别开始按钮
            extra_edit_keyboard.setVisibility(View.VISIBLE); // 显示键盘输入按钮
            extra_edit_confirm.setVisibility(View.VISIBLE); // 显示确认按钮
            extra_edit_answer.setEnabled(true); // 设置回复展示框为可用状态
        });

        // 点击 键盘输入按钮
        extra_edit_keyboard.setOnClickListener(v -> {
            // 显示软键盘
            isKeyboardActivate = Boolean.TRUE;
            extra_edit_answer.requestFocus(); // 请求回复展示框的焦点
            extra_edit_answer.postDelayed(() -> KeyboardUtil.showSoftInput(extra_edit_answer), 100); // 弹出软键盘
            // 将光标移至文本末尾
            if (extra_edit_answer.getText().length() > 0)
                extra_edit_answer.setSelection(extra_edit_answer.getText().length());
        });

        // 监听软键盘状态变化
        keyboardStateMonitor = new KeyboardStateMonitor(linearLayout);
        keyboardStateMonitor.addSoftKeyboardStateListener(new KeyboardStateMonitor.SoftKeyboardStateListener() {
            @Override
            public void onSoftKeyboardOpened(int keyboardHeightInPx) {
                isKeyboardActivate = Boolean.TRUE; // 软键盘打开
            }

            @Override
            public void onSoftKeyboardClosed() {
                // 软键盘关闭
                extra_edit_answer.clearFocus();
                isKeyboardActivate = Boolean.FALSE;
            }
        });

        // 点击 确认按钮
        extra_edit_confirm.setOnClickListener(v -> {
            if (listener != null)
                listener.onExtraEditConfirm(extra_edit_answer.getText().toString());
            dismiss();
        });

        return dialog_extra_edit;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // 检查 context 是否是 ExtraEditDialogListener 的实例
        if (context instanceof ExtraEditDialogListener)
            listener = (ExtraEditDialogListener) context;
        else
            LogUtil.warning(TAG, "onAttach", context + " must implement ExtraEditDialogListener", Boolean.TRUE);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null; // 清除 listener 引用
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        KeyboardUtil.hideSoftInput(requireContext()); // 隐藏软键盘
        linearLayout.setVisibility(View.VISIBLE); // 显示输入栏
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 移除软键盘监听器
        if (keyboardStateMonitor != null) keyboardStateMonitor.removeGlobalLayoutListener();
    }

    /**
     * 额外编辑对话框的回调接口。
     */
    public interface ExtraEditDialogListener {
        void onExtraEditConfirm(String answer);
    }
}