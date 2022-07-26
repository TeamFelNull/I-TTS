package dev.felnull.ttsvoice.voice.voicetext;

import dev.felnull.ttsvoice.voice.VoiceCategory;

public class VTVoiceCategory implements VoiceCategory {
    @Override
    public String getTitle() {
        return "VoiceText";
    }

    @Override
    public String getId() {
        return "voicetext-";
    }

    public static VTVoiceCategory getInstance(){
        return new VTVoiceCategory();
    }
}
