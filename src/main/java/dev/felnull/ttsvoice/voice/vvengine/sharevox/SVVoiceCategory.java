package dev.felnull.ttsvoice.voice.vvengine.sharevox;

import dev.felnull.ttsvoice.voice.VoiceCategory;

public class SVVoiceCategory implements VoiceCategory {
    private static final SVVoiceCategory INSTANCE = new SVVoiceCategory();

    @Override
    public String getTitle() {
        return "SHAREVOX";
    }

    @Override
    public String getId() {
        return ShareVoxManager.NAME;
    }

    public static SVVoiceCategory getInstance() {
        return INSTANCE;
    }
}
