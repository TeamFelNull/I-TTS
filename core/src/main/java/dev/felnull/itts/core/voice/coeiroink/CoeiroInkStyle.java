package dev.felnull.itts.core.voice.coeiroink;

import com.google.gson.JsonObject;

/**
 * Coeiroinkの話者スタイル
 *
 * @param name 名前
 * @param id   ID
 * @author MORIMORI0317
 */
public record CoeiroinkStyle(String name, int id) {
    /**
     * Jsonから話者スタイルを取得
     *
     * @param jo Json
     * @return 話者スタイル
     */
    public static CoeiroinkStyle of(JsonObject jo) {
        return new CoeiroinkStyle(jo.get("styleName").getAsString(), jo.get("styleId").getAsInt());
    }
}
