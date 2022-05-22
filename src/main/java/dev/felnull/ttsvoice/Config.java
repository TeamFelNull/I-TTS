package dev.felnull.ttsvoice;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;

public record Config(String botToken, String voiceVoxURL, String voiceTextAPIKey, int cashTime, String ignoreRegex,
                     boolean overwriteAloud, List<Long> inmAllowServers, List<Long> inmDenyUser) {

    public static Config of(JsonObject jo) {
        ImmutableList.Builder<Long> inmAllowBuilder = new ImmutableList.Builder<>();
        var iabja = jo.getAsJsonArray("InmAllowServers");
        for (JsonElement entry : iabja) {
            inmAllowBuilder.add(entry.getAsLong());
        }
        ImmutableList.Builder<Long> inmDenyBuilder = new ImmutableList.Builder<>();
        var idbja = jo.getAsJsonArray("InmDenyUser");
        for (JsonElement entry : idbja) {
            inmDenyBuilder.add(entry.getAsLong());
        }
        return new Config(jo.get("BotToken").getAsString(), jo.get("VoiceVoxURL").getAsString(), jo.get("VoiceTextAPIKey").getAsString(), jo.get("CashTime").getAsInt(), jo.get("IgnoreRegex").getAsString(), jo.get("OverwriteAloud").getAsBoolean(), inmAllowBuilder.build(), inmDenyBuilder.build());
    }

    public static Config createDefault() {
        return new Config("", "http://localhost:50021", "", 3, "(!|/|\\$|`).*", true, ImmutableList.of(600929948529590272L, 436404936151007241L, 949532003093577739L), ImmutableList.of());
    }

    public void check() {
        if (botToken.isEmpty())
            throw new IllegalStateException("Bot token is empty");
        if (voiceVoxURL.isEmpty())
            throw new IllegalStateException("VoiceVox url is empty");
        if (voiceTextAPIKey.isEmpty())
            throw new IllegalStateException("VoiceText api key is empty");
        if (cashTime < 0)
            throw new IllegalStateException("Cash time must be greater than or equal to 0");
    }

    public JsonObject toJson() {
        var jo = new JsonObject();
        jo.addProperty("BotToken", botToken);
        jo.addProperty("VoiceVoxURL", voiceVoxURL);
        jo.addProperty("VoiceTextAPIKey", voiceTextAPIKey);
        jo.addProperty("CashTime", cashTime);
        jo.addProperty("IgnoreRegex", ignoreRegex);
        jo.addProperty("OverwriteAloud", overwriteAloud);
        var imja = new JsonArray();
        for (Long allow : inmAllowServers) {
            imja.add(allow);
        }
        jo.add("InmAllowServers", imja);
        var idja = new JsonArray();
        for (Long entry : inmDenyUser) {
            idja.add(entry);
        }
        jo.add("InmDenyUser", idja);
        return jo;
    }
}
