package dev.felnull.ttsvoice.voice.vvengine.voicevox;

import dev.felnull.ttsvoice.voice.VoiceCategory;

public class VVVoiceCategory implements VoiceCategory {
    @Override
    public String getTitle() {
        return "VOICEVOX";
    }

    @Override
    public String getId() {
        return VoiceVoxManager.NAME;
    }

    public static VVVoiceCategory getInstance(){
        return new VVVoiceCategory();
    }
}
