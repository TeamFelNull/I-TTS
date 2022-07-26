package dev.felnull.ttsvoice.voice.voicetext;

import dev.felnull.ttsvoice.voice.VoiceEngine;

public class VTVoiceEngine implements VoiceEngine {
    @Override
    public String getTitle() {
        return "VoiceText";
    }

    @Override
    public String getId() {
        return "voicetext-";
    }

    public static VTVoiceEngine getInstance(){
        return new VTVoiceEngine();
    }
}
