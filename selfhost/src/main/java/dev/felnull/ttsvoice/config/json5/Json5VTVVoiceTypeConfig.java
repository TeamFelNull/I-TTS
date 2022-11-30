package dev.felnull.ttsvoice.config.json5;

import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import dev.felnull.ttsvoice.core.config.voicetype.VTVVoiceTypeConfig;
import dev.felnull.ttsvoice.utils.Json5Utils;

public class Json5VTVVoiceTypeConfig extends VTVVoiceTypeConfig {
    protected Json5VTVVoiceTypeConfig(JsonObject jo) {
        if (jo == null)
            return;
        this.enable = jo.getBoolean("enable", this.enable);
        this.cashTime = jo.getLong("cash_time", this.cashTime);
        this.checkTime = jo.getLong("check_time", this.checkTime);
        this.apiKey = Json5Utils.getStringOrElse(jo, "api_key", this.apiKey);
    }

    protected static JsonObject toJson(VTVVoiceTypeConfig config) {
        var jo = new JsonObject();
        jo.put("enable", JsonPrimitive.of(config.isEnable()), "有効かどうか");
        jo.put("cash_time", JsonPrimitive.of(config.getCashTime()), "キャッシュを保存する期間(ms)");
        jo.put("check_time", JsonPrimitive.of(config.getCheckTime()), "APIが利用可能かどうか確認する間隔(ms)");
        jo.put("api_key", JsonPrimitive.of(config.getApiKey()), "APIキー");
        return jo;
    }
}
