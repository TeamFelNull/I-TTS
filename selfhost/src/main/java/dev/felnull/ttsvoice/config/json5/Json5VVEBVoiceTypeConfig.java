package dev.felnull.ttsvoice.config.json5;

import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import dev.felnull.ttsvoice.core.config.voicetype.VVEBVoiceTypeConfig;
import dev.felnull.ttsvoice.utils.Json5Utils;

public class Json5VVEBVoiceTypeConfig extends VVEBVoiceTypeConfig {
    protected Json5VVEBVoiceTypeConfig(JsonObject jo) {
        if (jo == null)
            return;
        this.enable = jo.getBoolean("enable", this.enable);
        this.cashTime = jo.getLong("cash_time", this.cashTime);
        this.checkTime = jo.getLong("check_time", this.checkTime);
        this.apiUrls = Json5Utils.getStringListOfJsonArray(jo, "api_url");
    }

    protected static JsonObject toJson(VVEBVoiceTypeConfig config) {
        var jo = new JsonObject();
        jo.put("enable", JsonPrimitive.of(config.isEnable()), "有効かどうか");
        jo.put("cash_time", JsonPrimitive.of(config.getCashTime()), "キャッシュを保存する期間(ms)");
        jo.put("check_time", JsonPrimitive.of(config.getCheckTime()), "APIが利用可能かどうか確認する間隔(ms)");
        jo.put("api_url", Json5Utils.toJsonArray(config.getApiUrls()), "EngineのURL");
        return jo;
    }
}
