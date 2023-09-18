package dev.felnull.itts.config;

import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import dev.felnull.itts.core.config.Config;
import dev.felnull.itts.core.config.voicetype.VoiceTextConfig;
import dev.felnull.itts.core.config.voicetype.VoicevoxConfig;
import dev.felnull.itts.utils.Json5Utils;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * コンフィグの実装
 *
 * @author MORIMORI0317
 */
public class ConfigImpl implements Config {

    /**
     * BOTのトークン
     */
    private final String botToken;

    /**
     * テーマカラー
     */
    private final int themeColor;

    /**
     * キャッシュの保持期間
     */
    private final long cacheTime;

    /**
     * VoiceTextのコンフィグ
     */
    private final VoiceTextConfigImpl voiceTextConfig;

    /**
     * VOICEVOXのコンフィグ
     */
    private final VoicevoxConfigImpl voicevoxConfig;

    /**
     * COEIROLNKのコンフィグ
     */
    private final VoicevoxConfigImpl coeirolnkConfig;

    /**
     * SHAREVOXのコンフィグ
     */
    private final VoicevoxConfigImpl sharevoxConfig;

    /**
     * コンストラクタ
     *
     * @param jo Json
     */
    public ConfigImpl(JsonObject jo) {
        this.botToken = Json5Utils.getStringOrElse(jo, "bot_token", DEFAULT_BOT_TOKEN);
        this.themeColor = jo.getInt("theme_color", DEFAULT_THEME_COLOR);
        this.cacheTime = jo.getLong("cache_time", DEFAULT_CACHE_TIME);
        this.voiceTextConfig = new VoiceTextConfigImpl(Optional.ofNullable(jo.getObject("voice_text")).orElseGet(JsonObject::new));
        this.voicevoxConfig = new VoicevoxConfigImpl(Optional.ofNullable(jo.getObject("voicevox")).orElseGet(JsonObject::new));
        this.coeirolnkConfig = new VoicevoxConfigImpl(Optional.ofNullable(jo.getObject("coeirolnk")).orElseGet(JsonObject::new));
        this.sharevoxConfig = new VoicevoxConfigImpl(Optional.ofNullable(jo.getObject("sharevox")).orElseGet(JsonObject::new));
    }

    /**
     * Jsonへ変換
     *
     * @return 変換済みJson
     */
    public JsonObject toJson() {
        JsonObject jo = new JsonObject();
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
    public VoiceTextConfig getVoiceTextConfig() {
        return voiceTextConfig;
    }

    @Override
    public VoicevoxConfig getVoicevoxConfig() {
        return voicevoxConfig;
    }

    @Override
    public VoicevoxConfig getCoeirolnkConfig() {
        return coeirolnkConfig;
    }

    @Override
    public VoicevoxConfig getSharevoxConfig() {
        return sharevoxConfig;
    }


}
