package dev.felnull.ttsvoice.core.voice.voicetext;

import dev.felnull.ttsvoice.core.voice.VoiceCategory;

public class VTVoiceCategory implements VoiceCategory {
    @Override
    public String getName() {
        return "VoiceText";
    }

    @Override
    public String getId() {
        return "voicetext";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
