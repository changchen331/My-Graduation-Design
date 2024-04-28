package com.example.jarvis.utils;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

public class TextToSpeechUtil implements TextToSpeech.OnInitListener {
    private static final String TAG = "TTSUtil";
    private static final float voice_speed = 0.7f; // 语速
    public static float voice_pitch = 1.5f; // 音调
    private final Context context;
    private TextToSpeech tts;
    private boolean isInitialized = false;

    public TextToSpeechUtil(Context context) {
        this.context = context;
        initializeTts();
    }

    private void initializeTts() {
        tts = new TextToSpeech(context, this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.getDefault());
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "The Language specified is not supported.");
            } else isInitialized = true;
        } else Log.e(TAG, "Initialization failed.");
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public void speak(String text) {
        if (isInitialized) {
            tts.setLanguage(Locale.CHINESE);
            tts.setSpeechRate(voice_speed);
            tts.setPitch(voice_pitch);

            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, String.valueOf(System.currentTimeMillis()));
        } else Log.e(TAG, "TTS engine not initialized.");
    }

    public void stopSpeaking() {
        if (isInitialized) tts.stop();
    }

    public void shutdownTts() {
        if (tts != null) tts.shutdown();
    }
}
