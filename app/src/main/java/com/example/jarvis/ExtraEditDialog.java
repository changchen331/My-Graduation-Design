package com.example.jarvis;

import static com.example.jarvis.utils.KeyboardUtil.hideSoftInput;
import static com.example.jarvis.utils.KeyboardUtil.showSoftInput;
import static com.example.jarvis.utils.VibratorUtil.vibrate;

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

/**
 * ✔ 提供额外编辑功能和信息发送的对话框。
 */
public class ExtraEditDialog extends DialogFragment {
    private final LinearLayout linearLayout; // 输入栏
    private final String question; // 问题
    private final String answer; // 回复
    private ExtraEditDialogListener listener; // 回调接口
    private KeyboardStateMonitor keyboardStateMonitor; // 软键盘监听器
    private boolean isKeyboardActivate = false; // 键盘输入是否激活

    private final String temp; // 语音识别结果（模拟）

    public ExtraEditDialog(LinearLayout linearLayout, String question, String answer, String temp) {
        this.linearLayout = linearLayout;
        this.question = question;
        this.answer = answer;

        this.temp = temp;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置对话框样式
        this.setStyle(DialogFragment.STYLE_NO_FRAME, R.style.MyDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 文本编辑 Dialog
        View dialog_extra_edit = inflater.inflate(R.layout.dialog_extra_edit, container, true);
        // 取消按钮
        ImageButton extra_edit_cancel = dialog_extra_edit.findViewById(R.id.extra_edit_cancel);
        // 问题展示框
        TextView extra_edit_question = dialog_extra_edit.findViewById(R.id.extra_edit_question);
        // 回复展示框
        EditText extra_edit_answer = dialog_extra_edit.findViewById(R.id.extra_edit_answer);
        // 语音识别开始按钮
        ImageButton extra_edit_asr_start = dialog_extra_edit.findViewById(R.id.extra_edit_asr_start);
        // 语音识别结束按钮
        ImageButton extra_edit_asr_end = dialog_extra_edit.findViewById(R.id.extra_edit_asr_end);
        // 键盘输入按钮
        ImageButton extra_edit_keyboard = dialog_extra_edit.findViewById(R.id.extra_edit_keyboard);
        // 确认按钮
        ImageButton extra_edit_confirm = dialog_extra_edit.findViewById(R.id.extra_edit_confirm);

        // 隐藏输入栏
        linearLayout.setVisibility(View.GONE);

        // 点击 取消按钮
        extra_edit_cancel.setOnClickListener(v -> dismiss());

        // 设置问题 & 回复
        extra_edit_question.setText(question);
        extra_edit_answer.setText(answer);

        // 点击 语音识别开始按钮
        extra_edit_asr_start.setOnClickListener(v -> {
            // 开始录音
            vibrate(getContext(), 200); // 开始录音（震动）
            /*code*/

            // 键盘输入状态休眠
            if (isKeyboardActivate) {
                extra_edit_answer.clearFocus(); // 清除输入框焦点
                hideSoftInput(requireContext()); // 隐藏软键盘
                isKeyboardActivate = false;
            }
            extra_edit_keyboard.setVisibility(View.INVISIBLE); // 隐藏键盘输入按钮

            // 语音识别激活
            extra_edit_asr_start.setVisibility(View.INVISIBLE); // 隐藏语音识别开始按钮
            extra_edit_asr_end.setVisibility(View.VISIBLE); // 显示语音识别结束按钮
            extra_edit_confirm.setVisibility(View.GONE); // 隐藏确认按钮
            extra_edit_answer.setEnabled(false); // 设置回复展示框为不可用状态

            // 重置语音识别文本
            extra_edit_answer.setTextColor(ContextCompat.getColor(requireContext(), R.color.asr_text_empty));
            extra_edit_answer.setText(R.string.asr_text);
        });

        // 点击 语音识别结束按钮
        extra_edit_asr_end.setOnClickListener(v -> {
            // 结束录音
            /*code*/
            vibrate(getContext(), 200);

            // 语音识别
            extra_edit_asr_end.setVisibility(View.INVISIBLE); // 隐藏语音识别结束按钮 等待语音识别结束
            /*code*/
            String text = temp.trim(); // 获取语音识别结果（模拟）

            // 显示语音识别结果
            extra_edit_answer.setTextColor(ContextCompat.getColor(requireContext(), R.color.asr_text_filled));
            extra_edit_answer.setText(text);

            // 语音识别休眠
            extra_edit_asr_start.setVisibility(View.VISIBLE); // 显示语音识别开始按钮
            extra_edit_keyboard.setVisibility(View.VISIBLE); // 显示键盘输入按钮
            extra_edit_confirm.setVisibility(View.VISIBLE); // 显示确认按钮
            extra_edit_answer.setEnabled(true); // 设置回复展示框为可用状态
        });

        // 点击 键盘输入按钮
        extra_edit_keyboard.setOnClickListener(v -> {
            // 显示软键盘
            isKeyboardActivate = true;
            extra_edit_answer.requestFocus(); // 请求回复展示框的焦点
            extra_edit_answer.postDelayed(() -> showSoftInput(extra_edit_answer), 100); // 弹出软键盘
            // 将光标移至文本末尾
            if (extra_edit_answer.getText().length() > 0) {
                extra_edit_answer.setSelection(extra_edit_answer.getText().length());
            }
        });

        // 监听软键盘状态变化
        keyboardStateMonitor = new KeyboardStateMonitor(linearLayout);
        keyboardStateMonitor.addSoftKeyboardStateListener(new KeyboardStateMonitor.SoftKeyboardStateListener() {
            @Override
            public void onSoftKeyboardOpened(int keyboardHeightInPx) {
                // 软键盘打开
                isKeyboardActivate = true;
            }

            @Override
            public void onSoftKeyboardClosed() {
                // 软键盘关闭
                extra_edit_answer.clearFocus();
                isKeyboardActivate = false;
            }
        });

        // 点击 确认按钮
        extra_edit_confirm.setOnClickListener(v -> {
            if (listener != null) {
                listener.onExtraEditConfirm(extra_edit_answer.getText().toString());
            }
            dismiss();
        });

        return dialog_extra_edit;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // 检查 context 是否是 ExtraEditDialogListener 的实例
        if (context instanceof ExtraEditDialogListener) {
            listener = (ExtraEditDialogListener) context;
        } else {
            throw new ClassCastException(context + " must implement ExtraEditDialogListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null; // 清除 listener 引用
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        // 隐藏软键盘
        hideSoftInput(requireContext());
        // 显示输入栏
        linearLayout.setVisibility(View.VISIBLE);
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