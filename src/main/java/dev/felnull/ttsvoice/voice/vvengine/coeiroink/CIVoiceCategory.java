package dev.felnull.ttsvoice.voice.vvengine.coeiroink;

import dev.felnull.ttsvoice.voice.VoiceEngine;

public class CIVoiceEngine implements VoiceEngine {
    @Override
    public String getTitle() {
        return "COEIROINK";
    }

    @Override
    public String getId() {
        return CoeiroInkManager.NAME;
    }

    public static CIVoiceEngine getInstance(){
        return new CIVoiceEngine();
    }
}
