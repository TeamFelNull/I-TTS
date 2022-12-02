package dev.felnull.ttsvoice.config.json5;

import blue.endless.jankson.JsonObject;
import dev.felnull.ttsvoice.core.config.voicetype.VoiceVoxEngineBaseVoiceTypeConfig;
import dev.felnull.ttsvoice.utils.Json5Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public class Json5VoiceVoxEngineBaseVoiceTypeConfig extends Json5VoiceTypeConfig implements VoiceVoxEngineBaseVoiceTypeConfig {
    private final List<String> apiUrls;

    protected Json5VoiceVoxEngineBaseVoiceTypeConfig(JsonObject jo) {
        super(jo);
        this.apiUrls = Json5Utils.getStringListOfJsonArray(jo, "api_url");
    }

    @Override
    protected JsonObject toJson() {
        var jo = super.toJson();
        jo.put("api_url", Json5Utils.toJsonArray(this.apiUrls), "EngineのURL");
        return jo;
    }

    @Override
    public @NotNull @Unmodifiable List<String> getApiUrls() {
        return apiUrls;
    }
}
