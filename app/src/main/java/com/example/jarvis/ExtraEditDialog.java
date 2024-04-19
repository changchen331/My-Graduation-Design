package com.example.jarvis;

import static com.example.jarvis.utils.KeyboardUtil.hideSoftInput;
import static com.example.jarvis.utils.KeyboardUtil.showSoftInput;
import static com.example.jarvis.utils.VibratorUtil.vibrate;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
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
 * 额外对话界面 发送信息编辑 对话框
 */
public class ExtraEditDialog extends DialogFragment {
    private final String temp; // 语音识别结果（模拟）
    private final String question; // 问题
    private final String answer; // 回复
    private final LinearLayout linearLayout; // 额外对话界面 输入栏
    private ExtraEditDialogListener listener; // 回调接口

    public ExtraEditDialog(String question, String answer, LinearLayout linearLayout, String temp) {
        this.question = question;
        this.answer = answer;
        this.linearLayout = linearLayout;
        this.temp = temp;
        //设置背景透明
        this.setStyle(DialogFragment.STYLE_NO_FRAME, R.style.MyDialog);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 隐藏输入栏
        linearLayout.setVisibility(View.INVISIBLE);
        linearLayout.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.extra_input_invisible));

        // 文本编辑 Dialog
        @SuppressLint("InflateParams") View dialog_extra_edit = inflater.inflate(R.layout.dialog_extra_edit, null);
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

        // 点击 取消按钮
        extra_edit_cancel.setOnClickListener(v -> dismiss());

        // 设置问题
        extra_edit_question.setText(question);
        // 设置回复
        extra_edit_answer.setText(answer);

        // 点击 语音识别开始按钮
        extra_edit_asr_start.setOnClickListener(v -> {
            // 开始录音
            vibrate(getContext(), 200); // 开始录音（震动）
            /*code*/
            // 语音识别激活
            extra_edit_asr_start.setVisibility(View.INVISIBLE); // 隐藏语音识别开始按钮
            extra_edit_keyboard.setVisibility(View.INVISIBLE); // 隐藏键盘输入按钮
            extra_edit_asr_end.setVisibility(View.VISIBLE); // 显示语音识别结束按钮
            // 设置回复展示框为不可用状态
            extra_edit_answer.setEnabled(false);
            // 重置语音识别文本
            extra_edit_answer.setTextColor(ContextCompat.getColor(requireContext(), R.color.asr_text_empty));
            extra_edit_answer.setText(R.string.asr_text);
        });

        // 点击 语音识别结束按钮
        extra_edit_asr_end.setOnClickListener(v -> {
            // 结束录音
            /*code*/
            vibrate(getContext(), 300);
            extra_edit_asr_end.setVisibility(View.INVISIBLE); // 隐藏语音识别结束按钮 等待语音识别结束
            // 语音识别
            /*code*/
            // 获取语音识别结果（模拟）
            String text = temp.trim();
            // 显示语音识别结果
            extra_edit_answer.setTextColor(ContextCompat.getColor(requireContext(), R.color.asr_text_filled));
            extra_edit_answer.setText(text);
            // 语音识别休眠
            extra_edit_asr_start.setVisibility(View.VISIBLE); // 显示语音识别开始按钮
            extra_edit_keyboard.setVisibility(View.VISIBLE); // 显示键盘输入按钮
            // 设置回复展示框为可用状态
            extra_edit_answer.setEnabled(true);
        });

        // 点击 键盘输入按钮
        extra_edit_keyboard.setOnClickListener(v -> {
            // 请求回复展示框的焦点
            extra_edit_answer.requestFocus();
            // 显示软键盘
            extra_edit_answer.postDelayed(() -> showSoftInput(extra_edit_answer), 100);
            // 选中之前输入的文本
            if (extra_edit_answer.getText().length() > 0) {
                extra_edit_answer.setSelection(0, extra_edit_answer.getText().length());
            }
        });

        // 点击 软键盘和输入框的外部
//        dialog_extra_edit.setOnTouchListener((v, event) -> {
//            if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
//                dialog_extra_edit.clearFocus(); // 清除文本框焦点
//                hideSoftInput(ExtraEditDialog.this.getContext()); // 隐藏软键盘
//                return true;
//            }
//            return false;
//        });

        // 监听 编辑文本时的动作（按下发送）
        extra_edit_answer.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                // 清除回复展示框的焦点
                extra_edit_answer.clearFocus();
                // 隐藏软键盘
                extra_edit_answer.postDelayed(() -> hideSoftInput(requireContext()), 100);
            }
            return false;
        });

        // 监听 软键盘状态变化
        new KeyboardStateMonitor(extra_edit_answer).addSoftKeyboardStateListener(new KeyboardStateMonitor.SoftKeyboardStateListener() {
            @Override
            public void onSoftKeyboardOpened(int keyboardHeightInPx) {
                // 软键盘打开
            }

            @Override
            public void onSoftKeyboardClosed() {
                // 软键盘关闭
                extra_edit_answer.clearFocus(); // 清除回答展示框的焦点
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
        if (context instanceof ExtraEditDialogListener) {
            listener = (ExtraEditDialogListener) context;
        } else {
            throw new ClassCastException(context + " must implement ExtraEditDialogListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        // 隐藏软键盘
        hideSoftInput(requireContext());
        // 显示输入栏
        linearLayout.setVisibility(View.VISIBLE);
        linearLayout.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.extra_input_visible));
    }

    public interface ExtraEditDialogListener {
        void onExtraEditConfirm(String answer);
    }
}