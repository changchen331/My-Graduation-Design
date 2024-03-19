package com.example.jarvis;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    //语音输入文本展示框（建议使用实时语音识别）
    TextView voiceToText = findViewById(R.id.voice_to_text);
    //语音输入开始按钮
    Button voiceInputStart = findViewById(R.id.voice_input_start);
    //语音输入结束按钮
    Button voiceInputEnd = findViewById(R.id.voice_input_end);
    //发送语音文本按钮
    Button sendVoiceText = findViewById(R.id.sendVoiceText);

    //手动输入按钮
    Button keyboard = findViewById(R.id.keyboard);
    //手动输入文本输入（输入框+按钮）
    FrameLayout keyboardInput = findViewById(R.id.keyboard_input);
    //手动输入文本输入框
    EditText keyboardInputEdit = findViewById(R.id.keyboard_input_edit);
    //系统服务，用于控制软键盘
    InputMethodManager systemService = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
    //手动输入文本发送按钮
    ImageButton keyboardInputSend = findViewById(R.id.keyboard_input_send);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        voiceInputStart.setOnClickListener(v -> {
            //点击语音输入开始按钮
            voiceToText.setText(R.string.voice_to_text_ASR); //还原语音识别文本
            voiceToText.setVisibility(View.VISIBLE); //显示语音识别框
            voiceInputStart.setVisibility(View.INVISIBLE); //隐藏语音输入开始按钮

            //开始录音？（不确定）
            /*code*/

            //显示语音输入结束按钮的代码只有在“非实时语音识别”的情况下才会启用
            voiceInputEnd.setVisibility(View.VISIBLE); //显示语音输入结束按钮
        });
        voiceInputEnd.setOnClickListener(v -> {
            //点击语音输入结束按钮
            voiceInputEnd.setVisibility(View.INVISIBLE); //隐藏结束语音输入按钮
            voiceInputStart.setVisibility(View.VISIBLE); //显示开始语音输入按钮

            //结束录音？（不确定）
            /*code*/

            //语音识别
            String text = "你好先生，我是Jarvis。";
            /*code*/

            //显示语音识别结果
            voiceToText.setText(text);
        });
        sendVoiceText.setOnClickListener(v -> {
            //点击发送语音文本按钮
            if (!getResources().getString(R.string.voice_to_text_ASR).equalsIgnoreCase(voiceToText.getText().toString())) {
                //已经输入语音
                voiceToText.setVisibility(View.INVISIBLE); //隐藏语音识别框

                //发送语音识别文本（至服务器？）
                Toast.makeText(MainActivity.this, voiceToText.getText().toString(), Toast.LENGTH_SHORT).show();
                /*code*/

                //重置语音识别文本
                voiceToText.setText(R.string.voice_to_text_ASR);
            } else {
                //没有输入语音（弹出提示“请先输入语音”）
//                    Snackbar.make(findViewById(R.id.voice_to_text), "请先进行语音输入！", Snackbar.LENGTH_LONG)
//                            .setAction("好的", new View.OnClickListener() {
//                                @Override
//                                public void onClick(View view) {
//                                    // 用户点击“好的”按钮后的操作
//                                }
//                            }).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("警告");
                builder.setMessage("请先进行语音输入！");
                builder.setPositiveButton("确定", (dialog, which) -> {
                    // 用户点击“确定”按钮后的操作
                    /*code*/
                });
                builder.setNegativeButton("取消", null); // 第二个参数为null表示点击“取消”按钮后不执行任何操作
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        keyboard.setOnClickListener(v -> {
            //按下文本输入按钮
            //关闭语音识别模式
            voiceToText.setVisibility(View.INVISIBLE); //隐藏语音输入文本展示框
            voiceToText.setText(R.string.voice_to_text_ASR); //重置语音识别文本
            voiceInputEnd.setVisibility(View.INVISIBLE); //隐藏语音输入结束按钮
            voiceInputStart.setVisibility(View.VISIBLE); //显示语音输入开始按钮

            //开启手动输入模式
            keyboardInputEdit.setSelection(0, keyboardInputEdit.getText().toString().length()); //选中之前输入的文本
            keyboardInput.setVisibility(View.VISIBLE); //显示手动输入框
            keyboardInputEdit.requestFocus(); //将焦点设置到该输入框上
            systemService.showSoftInput(keyboardInputEdit, InputMethodManager.SHOW_IMPLICIT); //自动弹出软键盘
        });

        /**
         * 软键盘的事件监听，通常软键盘的收起方式大致3种：
         *           1.点击软键盘右下角的“发送”按钮（系统收起）
         *           2.输入框焦点时按界面底部的“返回”按钮（系统收起）
         *           3.点击软键盘和输入框的外部（自发收起）
         */
        keyboardInputEdit.setOnEditorActionListener((v, actionId, event) -> {
            //1. 捕捉软键盘自带的“发送”按钮，类似于键盘的enter键（系统收起）
            //退出手动输入模式
            systemService.hideSoftInputFromWindow(keyboardInputEdit.getWindowToken(), 0); //收起软键盘
            keyboardInput.setVisibility(View.INVISIBLE); //隐藏手动输入框

            //发送手动输入文本
            //如果用户输入了文本
            if ("".equalsIgnoreCase(keyboardInputEdit.getText().toString())) {
                //发送文本（至云服务器？）
                Toast.makeText(MainActivity.this, keyboardInputEdit.getText().toString(), Toast.LENGTH_SHORT).show();
                /*code*/
                //清空输入框的内容
                keyboardInputEdit.setText("");
            }

            return false;
        });
        keyboardInputEdit.setOnKeyListener((v, keyCode, event) -> {
            //2.捕捉系统底部的返回按钮（系统收起）
            //退出手动输入模式
            systemService.hideSoftInputFromWindow(keyboardInputEdit.getWindowToken(), 0); //收起软键盘
            keyboardInput.setVisibility(View.INVISIBLE); //隐藏手动输入框

            return false;
        });
        //3.点击软键盘和输入框的外部（自发收起）
        //详情见函数：public boolean onTouchEvent(MotionEvent event)


        keyboardInputSend.setOnClickListener(v -> {
            //当按下文本输入的发送按钮之后，清空文本输入框的文本内容，隐藏文本输入框
            //隐藏文本输入框
            keyboardInput.setVisibility(View.INVISIBLE);

            //如果用户输入了文本
            if ("".equalsIgnoreCase(keyboardInputEdit.getText().toString())) {
                //发送文本（至云服务器？）
                Toast.makeText(MainActivity.this, keyboardInputEdit.getText().toString(), Toast.LENGTH_SHORT).show();
                /*code*/
                //清空输入框的内容
                keyboardInputEdit.setText("");
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //3.点击软键盘和输入框的外部（自发收起）
        //退出手动输入模式
        systemService.hideSoftInputFromWindow(keyboardInputEdit.getWindowToken(), 0); //收起软键盘
        keyboardInput.setVisibility(View.INVISIBLE); //隐藏手动输入框

        return super.onTouchEvent(event);
    }
}
