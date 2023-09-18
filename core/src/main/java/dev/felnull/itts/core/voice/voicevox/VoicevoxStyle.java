package dev.felnull.itts.core.voice.voicevox;

import com.google.gson.JsonObject;

/**
 * VOICEVOXの話者スタイル
 *
 * @param name 名前
 * @param id   ID
 * @author MORIMORI0317
 */
public record VoicevoxStyle(String name, int id) {
    /**
     * Jsonから話者スタイルを取得
     *
     * @param jo Json
     * @return 話者スタイル
     */
    public static VoicevoxStyle of(JsonObject jo) {
        return new VoicevoxStyle(jo.get("name").getAsString(), jo.get("id").getAsInt());
    }
}
