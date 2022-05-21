package dev.felnull.ttsvoice;

import com.google.gson.JsonObject;

public record Config(String botToken, String voiceVoxURL, String voiceTextAPIKey, int cashTime) {

    public static Config of(JsonObject jo) {
        return new Config(jo.get("BotToken").getAsString(), jo.get("VoiceVoxURL").getAsString(), jo.get("VoiceTextAPIKey").getAsString(), jo.get("CashTime").getAsInt());
    }

    public static Config createDefault() {
        return new Config("", "http://localhost:50021", "", 3);
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
        return jo;
    }
}
