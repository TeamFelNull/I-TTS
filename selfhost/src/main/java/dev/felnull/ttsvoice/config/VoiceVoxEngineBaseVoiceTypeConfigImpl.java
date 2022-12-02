package dev.felnull.ttsvoice.config;

import blue.endless.jankson.JsonObject;
import dev.felnull.ttsvoice.core.config.voicetype.VoiceVoxEngineBaseVoiceTypeConfig;
import dev.felnull.ttsvoice.utils.Json5Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public class VoiceVoxEngineBaseVoiceTypeConfigImpl extends VoiceTypeConfigImpl implements VoiceVoxEngineBaseVoiceTypeConfig {
    private final List<String> apiUrls;

    protected VoiceVoxEngineBaseVoiceTypeConfigImpl(JsonObject jo) {
        super(jo);
        var loadApiUrals = Json5Utils.getStringListOfJsonArray(jo, "api_url");
        this.apiUrls = loadApiUrals.isEmpty() ? DEFAULT_API_URLS : loadApiUrals;
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
