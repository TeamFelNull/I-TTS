package dev.felnull.ttsvoice.voice.vvengine.voicevox;

import dev.felnull.ttsvoice.voice.VoiceCategory;
import dev.felnull.ttsvoice.voice.vvengine.VVEVoiceType;
import dev.felnull.ttsvoice.voice.vvengine.VVEngineManager;

public class VVVoiceType extends VVEVoiceType {
    public VVVoiceType(int vveId, String name, String styleName, boolean neta) {
        super(VoiceVoxManager.NAME, vveId, name, styleName, neta);
    }

    @Override
    public VVEngineManager getEngineManager() {
        return VoiceVoxManager.getInstance();
    }

    @Override
    public float getVolume() {
        return 1.5f;
    }

    @Override
    public VoiceCategory getCategory() {
        return VVVoiceCategory.getInstance();
    }
}
