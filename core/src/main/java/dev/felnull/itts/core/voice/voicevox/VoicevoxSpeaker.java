package dev.felnull.itts.core.voice.voicevox;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.UUID;

/**
 * VOICEVOXの話者
 *
 * @param name   名前
 * @param uuid   UUID
 * @param styles スタイル
 * @author MORIMORI0317
 */
public record VoicevoxSpeaker(String name, UUID uuid, List<VoicevoxStyle> styles) {

    /**
     * Jsonから話者を取得
     *
     * @param jo Json
     * @return 話者
     */
    public static VoicevoxSpeaker of(JsonObject jo) {
        ImmutableList.Builder<VoicevoxStyle> styles = new ImmutableList.Builder<>();
        JsonArray ja = jo.getAsJsonArray("styles");
        for (JsonElement je : ja) {
            styles.add(VoicevoxStyle.of(je.getAsJsonObject()));
        }
        return new VoicevoxSpeaker(jo.get("name").getAsString(), UUID.fromString(jo.get("speaker_uuid").getAsString()), styles.build());
    }
}
