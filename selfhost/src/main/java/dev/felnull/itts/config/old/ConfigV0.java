package dev.felnull.itts.config.old;

import blue.endless.jankson.JsonObject;
import com.google.common.collect.ImmutableList;
import dev.felnull.itts.config.ConfigLoader;
import dev.felnull.itts.utils.Json5Utils;

import java.util.List;
import java.util.Optional;

public record ConfigV0(
        String botToken,
        int themeColor,
        long cacheTime,
        VoiceTextConfigV0 voiceTextConfig,
        VoicevoxConfigV0 voicevoxConfig,
        VoicevoxConfigV0 coeirolnkConfig,
        VoicevoxConfigV0 sharevoxConfig
) {

    public static ConfigLoader<ConfigV0> LOADER = new ConfigLoader<>() {
        @Override
        public ConfigV0 load(JsonObject json5) {
            String botToken = Json5Utils.getStringOrElse(json5, "bot_token", "");
            int themeColor = json5.getInt("theme_color", 0xFF00FF);
            long cacheTime = json5.getLong("cache_time", 180000);
            VoiceTextConfigV0 voiceTextConfig = VoiceTextConfigV0.fromJson(Optional.ofNullable(json5.getObject("voice_text")).orElseGet(JsonObject::new));
            VoicevoxConfigV0 voicevoxConfig = VoicevoxConfigV0.fromJson(Optional.ofNullable(json5.getObject("voicevox")).orElseGet(JsonObject::new));
            VoicevoxConfigV0 coeirolnkConfig = VoicevoxConfigV0.fromJson(Optional.ofNullable(json5.getObject("coeirolnk")).orElseGet(JsonObject::new));
            VoicevoxConfigV0 sharevoxConfig = VoicevoxConfigV0.fromJson(Optional.ofNullable(json5.getObject("sharevox")).orElseGet(JsonObject::new));

            return new ConfigV0(
                    botToken,
                    themeColor,
                    cacheTime,
                    voiceTextConfig,
                    voicevoxConfig,
                    coeirolnkConfig,
                    sharevoxConfig
            );
        }

        @Override
        public ConfigV0 migrate(Object oldConfig) {
            throw new AssertionError("Oldest config");
        }
    };

    public record VoiceTextConfigV0(boolean enable, String apiKey) {

        public static VoiceTextConfigV0 fromJson(JsonObject jo) {
            boolean enable = jo.getBoolean("enable", true);
            String apiKey = Json5Utils.getStringOrElse(jo, "api_key", "");
            return new VoiceTextConfigV0(enable, apiKey);
        }
    }

    public record VoicevoxConfigV0(boolean enable, List<String> apiUrls, long checkTime) {

        public static VoicevoxConfigV0 fromJson(JsonObject jo) {
            boolean enable = jo.getBoolean("enable", true);

            List<String> loadApiUrls = Json5Utils.getStringListOfJsonArray(jo, "api_url");
            List<String> apiUrls = loadApiUrls.isEmpty() ? ImmutableList.of("") : loadApiUrls;

            long checkTime = jo.getLong("check_time", 15000);
            return new VoicevoxConfigV0(enable, apiUrls, checkTime);
        }
    }
}
