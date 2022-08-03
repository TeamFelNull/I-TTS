package dev.felnull.ttsvoice.voice.vvengine.coeiroink;

import com.google.gson.JsonObject;
import dev.felnull.ttsvoice.Main;
import dev.felnull.ttsvoice.voice.vvengine.VVEVoiceType;
import dev.felnull.ttsvoice.voice.vvengine.VVEngineManager;

import java.util.List;

public class CoeiroInkManager extends VVEngineManager {
    private static final CoeiroInkManager INSTANCE = new CoeiroInkManager();
    public static final String NAME = "coeiroink";

    public static CoeiroInkManager getInstance() {
        return INSTANCE;
    }

    @Override
    public List<String> getEngineURLs() {
        return Main.getConfig().coeiroInkURLs();
    }

    @Override
    protected VVEVoiceType createVoiceType(JsonObject jo, String name) {
        return new CIVoiceType(jo.get("id").getAsInt(), name, jo.get("name").getAsString());
    }

    @Override
    protected String getName() {
        return NAME;
    }
}
