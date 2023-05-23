package dev.felnull.itts.core.voice.voicevox;

import com.google.gson.JsonObject;

public record VoicevoxStyle(String name, int id) {
    public static VoicevoxStyle of(JsonObject jo) {
        return new VoicevoxStyle(jo.get("name").getAsString(), jo.get("id").getAsInt());
    }
}
