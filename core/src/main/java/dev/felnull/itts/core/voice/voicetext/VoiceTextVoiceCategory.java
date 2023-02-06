package dev.felnull.itts.core.voice.voicetext;

import dev.felnull.itts.core.TTSVoiceRuntime;
import dev.felnull.itts.core.voice.VoiceCategory;

public class VoiceTextVoiceCategory implements VoiceCategory {
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
        return TTSVoiceRuntime.getInstance().getVoiceManager().getVoiceTextManager().isAvailable();
    }
}
