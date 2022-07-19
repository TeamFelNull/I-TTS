package dev.felnull.ttsvoice.voice.vvengine.coeiroink;

import dev.felnull.ttsvoice.voice.vvengine.VVEVoiceType;
import dev.felnull.ttsvoice.voice.vvengine.VVEngineManager;

public class CIVoiceType extends VVEVoiceType {
    public CIVoiceType(int vveId, String name, String styleName) {
        super(CoeiroInkManager.NAME, vveId, name, styleName);
    }

    @Override
    public VVEngineManager getEngineManager() {
        return CoeiroInkManager.getInstance();
    }
}
