package dev.felnull.ttsvoice.config.json5;

import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import dev.felnull.ttsvoice.core.config.voicetype.VoiceTypeConfig;

public class Json5VoiceTypeConfig implements VoiceTypeConfig {
    private final boolean enable;
    private final long cacheTime;
    private final long checkTime;

    protected Json5VoiceTypeConfig(JsonObject jo) {
        this.enable = jo.getBoolean("enable", DEFAULT_ENABLE);
        this.cacheTime = jo.getLong("cache_time", DEFAULT_CACHE_TIME);
        this.checkTime = jo.getLong("check_time", DEFAULT_CHECK_TIME);
    }

    @Override
    public boolean isEnable() {
        return enable;
    }

    @Override
    public long getCacheTime() {
        return cacheTime;
    }

    @Override
    public long getCheckTime() {
        return checkTime;
    }

    protected JsonObject toJson() {
        var jo = new JsonObject();
        jo.put("enable", JsonPrimitive.of(enable), "有効かどうか");
        jo.put("cache_time", JsonPrimitive.of(cacheTime), "キャッシュを保存する期間(ms)");
        jo.put("check_time", JsonPrimitive.of(checkTime), "APIが利用可能かどうか確認する間隔(ms)");
        return jo;
    }
}
