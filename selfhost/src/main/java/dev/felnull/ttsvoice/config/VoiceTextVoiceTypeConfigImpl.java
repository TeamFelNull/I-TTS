package dev.felnull.ttsvoice.config;

import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import dev.felnull.ttsvoice.core.config.voicetype.VoiceTextVoiceTypeConfig;
import dev.felnull.ttsvoice.utils.Json5Utils;
import org.jetbrains.annotations.NotNull;

public class VoiceTextVoiceTypeConfigImpl extends VoiceTypeConfigImpl implements VoiceTextVoiceTypeConfig {
    private final String apiKey;

    protected VoiceTextVoiceTypeConfigImpl(JsonObject jo) {
        super(jo);
        this.apiKey = Json5Utils.getStringOrElse(jo, "api_key", DEFAULT_API_KEY);
    }

    @Override
    protected JsonObject toJson() {
        var jo = super.toJson();
        jo.put("api_key", JsonPrimitive.of(apiKey), "APIキー");
        return jo;
    }

    @Override
    public @NotNull String getApiKey() {
        return apiKey;
    }
}
