package dev.felnull.ttsvoice.voice.vvengine.voicevox;

import com.google.gson.JsonObject;
import dev.felnull.ttsvoice.Main;
import dev.felnull.ttsvoice.voice.vvengine.VVEVoiceType;
import dev.felnull.ttsvoice.voice.vvengine.VVEngineManager;

import java.util.List;

public class VoiceVoxManager extends VVEngineManager {
    private static final VoiceVoxManager INSTANCE = new VoiceVoxManager();
    public static final String NAME = "voicevox";

    public static VoiceVoxManager getInstance() {
        return INSTANCE;
    }

    @Override
    public List<String> getEngineURLs() {
        return Main.getConfig().voiceVoxURLs();
    }

    @Override
    protected VVEVoiceType createVoiceType(JsonObject jo, String name) {
        return new VVVoiceType(jo.get("id").getAsInt(), name, jo.get("name").getAsString());
    }

    @Override
    protected String getName() {
        return NAME;
    }
}
