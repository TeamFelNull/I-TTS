package dev.felnull.ttsvoice.voice.googletranslate;

import dev.felnull.ttsvoice.voice.VoiceEngine;

public class GoogleTranslateVoiceEngine implements VoiceEngine {
    @Override
    public String getTitle() {
        return "Google翻訳TTS";
    }

    @Override
    public String getId() {
        return "google-translate-tts-";
    }

    public static GoogleTranslateVoiceEngine getInstance(){
        return new GoogleTranslateVoiceEngine();
    }
}
