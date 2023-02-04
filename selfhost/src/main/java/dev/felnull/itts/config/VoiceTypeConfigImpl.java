package dev.felnull.itts.config;

import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import dev.felnull.itts.core.config.voicetype.VoiceTypeConfig;

public class VoiceTypeConfigImpl implements VoiceTypeConfig {
    private final boolean enable;
    private final long checkTime;

    protected VoiceTypeConfigImpl(JsonObject jo) {
        this.enable = jo.getBoolean("enable", DEFAULT_ENABLE);
        this.checkTime = jo.getLong("check_time", DEFAULT_CHECK_TIME);
    }

    @Override
    public boolean isEnable() {
        return enable;
    }

    @Override
    public long getCheckTime() {
        return checkTime;
    }

    protected JsonObject toJson() {
        var jo = new JsonObject();
        jo.put("enable", JsonPrimitive.of(enable), "有効かどうか");
        jo.put("check_time", JsonPrimitive.of(checkTime), "APIが利用可能かどうか確認する間隔(ms)");
        return jo;
    }
}
