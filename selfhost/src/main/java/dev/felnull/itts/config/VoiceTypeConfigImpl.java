package dev.felnull.itts.config;

import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import dev.felnull.itts.core.config.voicetype.VoiceTypeConfig;

public class VoiceTypeConfigImpl implements VoiceTypeConfig {
    private final boolean enable;

    protected VoiceTypeConfigImpl(JsonObject jo) {
        this.enable = jo.getBoolean("enable", DEFAULT_ENABLE);
    }

    @Override
    public boolean isEnable() {
        return enable;
    }

    protected JsonObject toJson() {
        var jo = new JsonObject();
        jo.put("enable", JsonPrimitive.of(enable), "有効かどうか");
        return jo;
    }
}
