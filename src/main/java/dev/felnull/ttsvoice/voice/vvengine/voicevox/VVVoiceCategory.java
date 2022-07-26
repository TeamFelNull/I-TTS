package dev.felnull.ttsvoice.voice.vvengine.voicevox;

import dev.felnull.ttsvoice.voice.VoiceEngine;
import dev.felnull.ttsvoice.voice.vvengine.coeiroink.CIVoiceEngine;

public class VVVoiceEngine implements VoiceEngine {
    @Override
    public String getTitle() {
        return "VOICEVOX";
    }

    @Override
    public String getId() {
        return VoiceVoxManager.NAME;
    }

    public static VVVoiceEngine getInstance(){
        return new VVVoiceEngine();
    }
}
