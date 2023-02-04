package dev.felnull.itts.config;

import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import dev.felnull.itts.core.config.Config;
import dev.felnull.itts.core.config.voicetype.VoiceTextVoiceTypeConfig;
import dev.felnull.itts.core.config.voicetype.VoiceVoxEngineBaseVoiceTypeConfig;
import dev.felnull.itts.utils.Json5Utils;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ConfigImpl implements Config {
    private final String botToken;
    private final int themeColor;
    private final long cacheTime;
    private final VoiceTextVoiceTypeConfigImpl voiceTextConfig;
    private final VoiceVoxEngineBaseVoiceTypeConfigImpl voicevoxConfig;
    private final VoiceVoxEngineBaseVoiceTypeConfigImpl coeirolnkConfig;
    private final VoiceVoxEngineBaseVoiceTypeConfigImpl sharevoxConfig;

    public ConfigImpl(JsonObject jo) {
        this.botToken = Json5Utils.getStringOrElse(jo, "bot_token", DEFAULT_BOT_TOKEN);
        this.themeColor = jo.getInt("theme_color", DEFAULT_THEME_COLOR);
        this.cacheTime = jo.getLong("cache_time", DEFAULT_CACHE_TIME);
        this.voiceTextConfig = new VoiceTextVoiceTypeConfigImpl(Optional.ofNullable(jo.getObject("voice_text")).orElseGet(JsonObject::new));
        this.voicevoxConfig = new VoiceVoxEngineBaseVoiceTypeConfigImpl(Optional.ofNullable(jo.getObject("voicevox")).orElseGet(JsonObject::new));
        this.coeirolnkConfig = new VoiceVoxEngineBaseVoiceTypeConfigImpl(Optional.ofNullable(jo.getObject("coeirolnk")).orElseGet(JsonObject::new));
        this.sharevoxConfig = new VoiceVoxEngineBaseVoiceTypeConfigImpl(Optional.ofNullable(jo.getObject("sharevox")).orElseGet(JsonObject::new));
    }

    public JsonObject toJson() {
        var jo = new JsonObject();
        jo.put("config_version", new JsonPrimitive(VERSION), "コンフィグのバージョン 変更しないでください！");
        jo.put("bot_token", JsonPrimitive.of(this.botToken), "BOTのトークン");
        jo.put("theme_color", new JsonPrimitive(this.themeColor), "テーマカラー");
        jo.put("cache_time", new JsonPrimitive(this.cacheTime), "キャッシュを保存する期間(ms)");
        jo.put("voice_text", this.voiceTextConfig.toJson(), "VoiceTextのコンフィグ");
        jo.put("voicevox", this.voicevoxConfig.toJson(), "VOICEVOXのコンフィグ");
        jo.put("coeirolnk", this.coeirolnkConfig.toJson(), "COEIROLNKのコンフィグ");
        jo.put("sharevox", this.sharevoxConfig.toJson(), "SHAREVOXのコンフィグ");
        return jo;
    }

    @Override
    public @NotNull String getBotToken() {
        return botToken;
    }

    @Override
    public int getThemeColor() {
        return themeColor;
    }

    @Override
    public long getCacheTime() {
        return cacheTime;
    }

    @Override
    public VoiceTextVoiceTypeConfig getVoiceTextConfig() {
        return voiceTextConfig;
    }

    @Override
    public VoiceVoxEngineBaseVoiceTypeConfig getVoicevoxConfig() {
        return voicevoxConfig;
    }

    @Override
    public VoiceVoxEngineBaseVoiceTypeConfig getCoeirolnkConfig() {
        return coeirolnkConfig;
    }

    @Override
    public VoiceVoxEngineBaseVoiceTypeConfig getSharevoxConfig() {
        return sharevoxConfig;
    }


}
