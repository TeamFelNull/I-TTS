package dev.felnull.ttsvoice.voice.vvengine.voicevox;

import dev.felnull.ttsvoice.voice.vvengine.VVEVoiceType;
import dev.felnull.ttsvoice.voice.vvengine.VVEngineManager;

public class VVVoiceType extends VVEVoiceType {
    public VVVoiceType(int vveId, String name, String styleName) {
        super(VoiceVoxManager.NAME, vveId, name, styleName);
    }

    @Override
    public VVEngineManager getEngineManager() {
        return VoiceVoxManager.getInstance();
    }
}
