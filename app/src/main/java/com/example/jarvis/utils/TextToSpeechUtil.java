package com.example.jarvis.utils;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class TextToSpeechUtil implements TextToSpeech.OnInitListener {
    private static final String TAG = "TextToSpeechUtil";
    private static final Float voice_speed = 1.0f; // 语速
    private static final Float voice_pitch = 1.0f; // 音调

    private final Context context;
    private TextToSpeech tts;

    private Boolean isInitialized = Boolean.FALSE;

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

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
                LogUtil.debug(TAG, "onInit", "The Language specified is not supported", Boolean.TRUE);
            else isInitialized = Boolean.TRUE;

        } else LogUtil.warning(TAG, "onInit", "Initialization failed", Boolean.TRUE);
    }

    public Boolean isInitialized() {
        return isInitialized;
    }

    public void speak(String text) {
        if (isInitialized) {
            tts.setLanguage(Locale.CHINESE);
            tts.setSpeechRate(voice_speed);
            tts.setPitch(voice_pitch);

            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, String.valueOf(System.currentTimeMillis()));
        } else LogUtil.warning(TAG, "speak", "TTS engine not initialized", Boolean.TRUE);
    }

    public void stopSpeaking() {
        if (isInitialized) tts.stop();
    }

    public void shutdownTts() {
        if (tts != null) tts.shutdown();
    }
}
