package dev.felnull.ttsvoice.voice.vvengine.sharevox;

import dev.felnull.ttsvoice.voice.VoiceCategory;
import dev.felnull.ttsvoice.voice.vvengine.VVEVoiceType;
import dev.felnull.ttsvoice.voice.vvengine.VVEngineManager;

public class SVVoiceType extends VVEVoiceType {
    public SVVoiceType(int vveId, String name, String styleName, boolean neta) {
        super(ShareVoxManager.NAME, vveId, name, styleName, neta);
    }

    @Override
    public VVEngineManager getEngineManager() {
        return ShareVoxManager.getInstance();
    }

    @Override
    public VoiceCategory getCategory() {
        return SVVoiceCategory.getInstance();
    }
}
