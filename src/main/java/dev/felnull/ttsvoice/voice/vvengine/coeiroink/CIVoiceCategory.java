package dev.felnull.ttsvoice.voice.vvengine.coeiroink;

import dev.felnull.ttsvoice.voice.VoiceCategory;

public class CIVoiceCategory implements VoiceCategory {
    @Override
    public String getTitle() {
        return "COEIROINK";
    }

    @Override
    public String getId() {
        return CoeiroInkManager.NAME;
    }

    public static CIVoiceCategory getInstance(){
        return new CIVoiceCategory();
    }
}
