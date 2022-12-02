package dev.felnull.ttsvoice.config.json5;

import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import dev.felnull.ttsvoice.core.config.Config;
import dev.felnull.ttsvoice.utils.Json5Utils;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class Json5Config implements Config {
    private final String name;
    private final String botToken;
    private final int themeColor;
    private final Json5VoiceTextVoiceTypeConfig voiceTextConfig;
    private final Json5VoiceVoxEngineBaseVoiceTypeConfig voicevoxConfig;
    private final Json5VoiceVoxEngineBaseVoiceTypeConfig coeirolnkConfig;
    private final Json5VoiceVoxEngineBaseVoiceTypeConfig sharevoxConfig;

    public Json5Config(JsonObject jo) {
        this.name = Json5Utils.getStringOrElse(jo, "name", DEFAULT_NAME);
        this.botToken = Json5Utils.getStringOrElse(jo, "bot_token", DEFAULT_BOT_TOKEN);
        this.themeColor = jo.getInt("theme_color", DEFAULT_THEME_COLOR);
        this.voiceTextConfig = new Json5VoiceTextVoiceTypeConfig(Optional.ofNullable(jo.getObject("voice_text")).orElseGet(JsonObject::new));
        this.voicevoxConfig = new Json5VoiceVoxEngineBaseVoiceTypeConfig(Optional.ofNullable(jo.getObject("voicevox")).orElseGet(JsonObject::new));
        this.coeirolnkConfig = new Json5VoiceVoxEngineBaseVoiceTypeConfig(Optional.ofNullable(jo.getObject("coeirolnk")).orElseGet(JsonObject::new));
        this.sharevoxConfig = new Json5VoiceVoxEngineBaseVoiceTypeConfig(Optional.ofNullable(jo.getObject("sharevox")).orElseGet(JsonObject::new));
    }

    public JsonObject toJson() {
        var jo = new JsonObject();
        jo.put("name", JsonPrimitive.of(this.name), "名前");
        jo.put("bot_token", JsonPrimitive.of(this.botToken), "トークン");
        jo.put("theme_color", new JsonPrimitive(this.themeColor), "テーマカラー");

        jo.put("voice_text", this.voiceTextConfig.toJson(), "VoiceTextのコンフィグ");
        jo.put("voicevox", this.voicevoxConfig.toJson(), "VOICEVOXのコンフィグ");
        jo.put("coeirolnk", this.coeirolnkConfig.toJson(), "COEIROLNKのコンフィグ");
        jo.put("sharevox", this.sharevoxConfig.toJson(), "SHAREVOXのコンフィグ");
        return jo;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull String getBotToken() {
        return botToken;
    }

    @Override
    public int getThemeColor() {
        return themeColor;
    }
}
