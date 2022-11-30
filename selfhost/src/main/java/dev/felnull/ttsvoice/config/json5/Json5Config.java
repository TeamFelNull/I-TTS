package dev.felnull.ttsvoice.config.json5;

import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import dev.felnull.ttsvoice.core.config.Config;
import dev.felnull.ttsvoice.utils.Json5Utils;

public class Json5Config extends Config {
    public Json5Config(JsonObject jo) {
        if (jo == null)
            return;
        this.name = Json5Utils.getStringOrElse(jo, "name", this.name);
        this.botToken = Json5Utils.getStringOrElse(jo, "bot_token", this.name);
        this.themeColor = jo.getInt("theme_color", this.themeColor);
        this.voiceTextConfig = new Json5VTVVoiceTypeConfig(jo.getObject("voice_text"));
        this.voicevoxConfig = new Json5VVEBVoiceTypeConfig(jo.getObject("voicevox"));
        this.coeirolnkConfig = new Json5VVEBVoiceTypeConfig(jo.getObject("coeirolnk"));
        this.sharevoxConfig = new Json5VVEBVoiceTypeConfig(jo.getObject("sharevox"));
    }

    public JsonObject toJson() {
        var jo = new JsonObject();
        jo.put("name", JsonPrimitive.of(this.name), "名前");
        jo.put("bot_token", JsonPrimitive.of(this.botToken), "トークン");
        jo.put("theme_color", new JsonPrimitive(this.themeColor), "テーマカラー");

        jo.put("voice_text", Json5VTVVoiceTypeConfig.toJson(this.voiceTextConfig), "VoiceTextのコンフィグ");
        jo.put("voicevox", Json5VVEBVoiceTypeConfig.toJson(this.voicevoxConfig), "VOICEVOXのコンフィグ");
        jo.put("coeirolnk", Json5VVEBVoiceTypeConfig.toJson(this.coeirolnkConfig), "COEIROLNKのコンフィグ");
        jo.put("sharevox", Json5VVEBVoiceTypeConfig.toJson(this.sharevoxConfig), "SHAREVOXのコンフィグ");
        return jo;
    }
}
