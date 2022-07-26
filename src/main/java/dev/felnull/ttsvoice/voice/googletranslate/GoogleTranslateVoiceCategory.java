package dev.felnull.ttsvoice.voice.googletranslate;

import dev.felnull.ttsvoice.voice.VoiceCategory;

public class GoogleTranslateVoiceCategory implements VoiceCategory {
    @Override
    public String getTitle() {
        return "Google翻訳TTS";
    }

    @Override
    public String getId() {
        return "google-translate-tts-";
    }

    public static GoogleTranslateVoiceCategory getInstance(){
        return new GoogleTranslateVoiceCategory();
    }
}
