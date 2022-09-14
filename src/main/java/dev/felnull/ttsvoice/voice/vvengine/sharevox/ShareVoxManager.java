package dev.felnull.ttsvoice.voice.vvengine.sharevox;

import com.google.gson.JsonObject;
import dev.felnull.ttsvoice.Main;
import dev.felnull.ttsvoice.voice.SimpleAliveChecker;
import dev.felnull.ttsvoice.voice.vvengine.VVEVoiceType;
import dev.felnull.ttsvoice.voice.vvengine.VVEngineManager;

import java.util.List;

public class ShareVoxManager extends VVEngineManager {
    private static final ShareVoxManager INSTANCE = new ShareVoxManager();
    public static final String NAME = "sharevox";
    public static final SimpleAliveChecker ALIVE_CHECKER = new SimpleAliveChecker(() -> Main.getConfig().voiceConfig().enableCoeiroInk(), () -> getInstance().aliveCheck());

    public static ShareVoxManager getInstance() {
        return INSTANCE;
    }

    @Override
    public List<String> getAllEngineURLs() {
        return Main.getConfig().coeiroInkURLs();
    }

    @Override
    protected VVEVoiceType createVoiceType(JsonObject jo, String name, boolean neta) {
        return new SVVoiceType(jo.get("id").getAsInt(), name, jo.get("name").getAsString(), neta);
    }

    @Override
    protected String getName() {
        return NAME;
    }

    @Override
    public boolean isAlive() {
        return ALIVE_CHECKER.isAlive();
    }
}
