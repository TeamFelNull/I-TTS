package dev.felnull.itts.core.voice.voicevox;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.UUID;

public record VoicevoxSpeaker(String name, UUID uuid, List<VoicevoxStyle> styles) {
    public static VoicevoxSpeaker of(JsonObject jo) {
        ImmutableList.Builder<VoicevoxStyle> styles = new ImmutableList.Builder<>();
        var ja = jo.getAsJsonArray("styles");
        for (JsonElement je : ja) {
            styles.add(VoicevoxStyle.of(je.getAsJsonObject()));
        }
        return new VoicevoxSpeaker(jo.get("name").getAsString(), UUID.fromString(jo.get("speaker_uuid").getAsString()), styles.build());
    }
}
