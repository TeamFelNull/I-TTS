package dev.felnull.ttsvoice.data;

import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;

public record VoiceConfig(boolean enableVoiceVox, boolean enableCoeiroInk, boolean enableVoiceText,
                          boolean enableGoogleTranslateTts, boolean enableInm, boolean enableCookie) {
    public static VoiceConfig createDefault() {
        return new VoiceConfig(true, true, true, true, true, true);
    }

    public static VoiceConfig of(JsonObject jo) {
        boolean enableVoiceVox = jo.getBoolean("VoiceVox", true);
        boolean enableCoeiroInk = jo.getBoolean("CoeiroInk", true);
        boolean enableVoiceText = jo.getBoolean("VoiceText", true);
        boolean enableGoogleTranslateTts = jo.getBoolean("GoogleTranslateTTS", true);
        boolean enableInm = jo.getBoolean("Inm", true);
        boolean enableCookie = jo.getBoolean("Cookie", true);

        return new VoiceConfig(enableVoiceVox, enableCoeiroInk, enableVoiceText, enableGoogleTranslateTts, enableInm, enableCookie);
    }

    public JsonObject toJson() {
        var jo = new JsonObject();
        jo.put("VoiceVox", JsonPrimitive.of(enableVoiceVox));
        jo.put("CoeiroInk", JsonPrimitive.of(enableCoeiroInk));
        jo.put("VoiceText", JsonPrimitive.of(enableVoiceText));
        jo.put("GoogleTranslateTTS", JsonPrimitive.of(enableGoogleTranslateTts));
        jo.put("Inm", JsonPrimitive.of(enableInm));
        jo.put("Cookie", JsonPrimitive.of(enableCookie));
        return jo;
    }
}
