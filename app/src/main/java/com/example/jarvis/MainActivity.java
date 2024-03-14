package com.example.jarvis;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //语音输入文本展示框（建议实时语音识别）
        TextView voiceToText = findViewById(R.id.voice_to_text);
        //语音输入开始按钮
        Button voiceInputStart = findViewById(R.id.voice_input_start);
        //语音输入结束按钮
        Button voiceInputEnd = findViewById(R.id.voice_input_end);
        voiceInputStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击语音输入开始按钮
                voiceToText.setVisibility(View.VISIBLE); //显示语音识别框
                voiceInputStart.setVisibility(View.INVISIBLE); //隐藏语音输入开始按钮

                //显示语音输入结束按钮的代码只有在“非实时语音识别”的情况下才会启用
                voiceInputEnd.setVisibility(View.VISIBLE); //显示语音输入结束按钮

                //语音识别
                /*code*/
            }
        });
        voiceInputEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击语音输入结束按钮
                voiceInputEnd.setVisibility(View.INVISIBLE); //隐藏结束语音输入按钮
                voiceInputStart.setVisibility(View.VISIBLE); //显示开始语音输入按钮
                //展示语音识别结果
                String text = "你好先生，我是Jarvis。";
                voiceToText.setText(text);
            }
        });

        //发送语音文本按钮
        Button sendVoiceText = findViewById(R.id.sendVoiceText);
        sendVoiceText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击发送语音文本按钮
                if (!getResources().getString(R.string.voice_to_text_ASR).equalsIgnoreCase(voiceToText.getText().toString())) {
                    //已经输入语音
                    voiceToText.setVisibility(View.INVISIBLE); //隐藏语音识别框
                    //以防万一
                    voiceInputEnd.setVisibility(View.INVISIBLE); //隐藏语音输入结束按钮
                    voiceInputStart.setVisibility(View.VISIBLE); //显示语音输入开始按钮
                    //发送文本
                    Toast.makeText(MainActivity.this, voiceToText.getText().toString(), Toast.LENGTH_SHORT).show();
                    //重置文本
                    voiceToText.setText(R.string.voice_to_text_ASR);
                } else {
                    //没有输入语音
                    Snackbar.make(findViewById(R.id.voice_to_text), "请先进行语音输入！", Snackbar.LENGTH_LONG)
                            .setAction("好的", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    // 用户点击“好的”按钮后的操作
                                }
                            }).show();
                }
            }
        });


        //手动输入文本输入（输入框+按钮）
        FrameLayout keyboardInput = findViewById(R.id.keyboard_input);
        //手动输入文本输入框
        EditText keyboardInputEdit = findViewById(R.id.keyboard_input_edit);
        //手动输入文本发送按钮
        Button keyboardInputSend = findViewById(R.id.keyboard_input_send);
        //手动输入按钮
        Button keyboard = findViewById(R.id.keyboard);
        keyboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //当拿下文本输入按钮时，开启手动输入模式
                voiceToText.setVisibility(View.INVISIBLE); //隐藏语音识别框
                keyboardInputEdit.setSelection(0, keyboardInputEdit.getText().toString().length()); //选中之前输入的文本
                keyboardInputEdit.requestFocus(); //将焦点设置到该输入框上
                keyboardInput.setVisibility(View.VISIBLE); //显示手动输入框
            }
        });
        keyboardInputEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    //当手动输入文本输入框失去焦点时，隐藏文本输入框
                    keyboardInput.setVisibility(View.INVISIBLE);
                }
            }
        });
        keyboardInputSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //当按下文本输入的发送按钮之后，清空文本输入框的文本内容，隐藏文本输入框
                keyboardInput.setVisibility(View.INVISIBLE); //隐藏文本输入框
                keyboardInputEdit.setText(""); //清空输入框的内容
                //发送文本
                /*code*/
            }
        });

    }
}
