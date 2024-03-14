package com.example.jarvis;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //语音输入文本展示框（实时语音识别）
        TextView voiceText = findViewById(R.id.voice_to_text);

        //语音输入
        Button voiceInput = findViewById(R.id.button2);
        voiceInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                voiceText.setVisibility(View.VISIBLE);
            }
        });

        //发送语音文本
        Button sendVoiceText = findViewById(R.id.button3);
        sendVoiceText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                voiceText.setVisibility(View.INVISIBLE);
            }
        });
    }
}