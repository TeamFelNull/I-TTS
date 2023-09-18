package dev.felnull.itts.config;

import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import dev.felnull.itts.core.config.voicetype.VoiceTypeConfig;

/**
 * 声タイプコンフィグの実装
 *
 * @author MORIMORI0317
 */
public class VoiceTypeConfigImpl implements VoiceTypeConfig {
    /**
     * 有効かどうか
     */
    private final boolean enable;

    /**
     * コンストラクタ
     *
     * @param jo Json
     */
    protected VoiceTypeConfigImpl(JsonObject jo) {
        this.enable = jo.getBoolean("enable", DEFAULT_ENABLE);
    }

    @Override
    public boolean isEnable() {
        return enable;
    }

    /**
     * Jsonへ変換
     *
     * @return Json
     */
    protected JsonObject toJson() {
        JsonObject jo = new JsonObject();
        jo.put("enable", JsonPrimitive.of(enable), "有効かどうか");
        return jo;
    }
}
