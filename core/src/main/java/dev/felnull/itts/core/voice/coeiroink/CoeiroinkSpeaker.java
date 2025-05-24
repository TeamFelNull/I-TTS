package dev.felnull.itts.core.voice.coeiroink;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.UUID;

/**
 * Coeiroinkの話者
 *
 * @param speakerName   名前
 * @param speakerUuid   UUID
 * @param styles スタイル
 * @author MORIMORI0317
 */
public record CoeiroinkSpeaker(String speakerName, UUID speakerUuid, List<CoeiroinkStyle> styles) {

    /**
     * Jsonから話者を取得
     *
     * @param jo Json
     * @return 話者
     */
    public static CoeiroinkSpeaker of(JsonObject jo) {
        ImmutableList.Builder<CoeiroinkStyle> styles = new ImmutableList.Builder<>();
        JsonArray ja = jo.getAsJsonArray("styles");
        for (JsonElement je : ja) {
            styles.add(CoeiroinkStyle.of(je.getAsJsonObject()));
        }
        return new CoeiroinkSpeaker(jo.get("speakerName").getAsString(), UUID.fromString(jo.get("speakerUuid").getAsString()), styles.build());
    }
}
