package dev.felnull.itts.config;

import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import dev.felnull.itts.core.config.voicetype.VoicevoxConfig;
import dev.felnull.itts.utils.Json5Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * VOICEVOXコンフィグの実装
 *
 * @author MORIMORI0317
 */
public class VoicevoxConfigImpl extends VoiceTypeConfigImpl implements VoicevoxConfig {

    /**
     * エンジンのURL
     */
    private final List<String> apiUrls;

    /**
     * 応答確認時間
     */
    private final long checkTime;

    /**
     * コンストラクタ
     *
     * @param jo Json
     */
    protected VoicevoxConfigImpl(JsonObject jo) {
        super(jo);
        List<String> loadApiUrals = Json5Utils.getStringListOfJsonArray(jo, "api_url");
        this.apiUrls = loadApiUrals.isEmpty() ? DEFAULT_API_URLS : loadApiUrals;
        this.checkTime = jo.getLong("check_time", DEFAULT_CHECK_TIME);
    }

    @Override
    protected JsonObject toJson() {
        JsonObject jo = super.toJson();
        jo.put("api_url", Json5Utils.toJsonArray(this.apiUrls), "EngineのURL");
        jo.put("check_time", JsonPrimitive.of(checkTime), "APIが利用可能かどうか確認する間隔(ms)");
        return jo;
    }

    @Override
    public @NotNull @Unmodifiable List<String> getApiUrls() {
        return apiUrls;
    }

    @Override
    public long getCheckTime() {
        return checkTime;
    }

}
