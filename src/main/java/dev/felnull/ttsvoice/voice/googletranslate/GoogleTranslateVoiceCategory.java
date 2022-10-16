package dev.felnull.ttsvoice.voice.googletranslate;

import dev.felnull.ttsvoice.voice.VoiceCategory;

public class GoogleTranslateVoiceCategory implements VoiceCategory {
    private static final GoogleTranslateVoiceCategory INSTANCE = new GoogleTranslateVoiceCategory();

    @Override
    public String getTitle() {
        return "GoogleTTS";
    }

    @Override
    public String getId() {
        return "google-tts-";
    }

    public static GoogleTranslateVoiceCategory getInstance() {
        return INSTANCE;
    }
}
