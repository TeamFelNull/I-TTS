package dev.felnull.itts.config;

import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import dev.felnull.itts.core.config.voicetype.VoiceTextConfig;
import dev.felnull.itts.utils.Json5Utils;
import org.jetbrains.annotations.NotNull;

/**
 * VoiceTextコンフィグの実装
 *
 * @author MORIMORI0317
 */
public class VoiceTextConfigImpl extends VoiceTypeConfigImpl implements VoiceTextConfig {

    /**
     * APIキー
     */
    private final String apiKey;

    /**
     * コンストラクタ
     *
     * @param jo Json
     */
    protected VoiceTextConfigImpl(JsonObject jo) {
        super(jo);
        this.apiKey = Json5Utils.getStringOrElse(jo, "api_key", DEFAULT_API_KEY);
    }

    @Override
    protected JsonObject toJson() {
        JsonObject jo = super.toJson();
        jo.put("api_key", JsonPrimitive.of(apiKey), "APIキー");
        return jo;
    }

    @Override
    public @NotNull String getApiKey() {
        return apiKey;
    }
}
