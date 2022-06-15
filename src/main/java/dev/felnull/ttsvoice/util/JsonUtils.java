package dev.felnull.ttsvoice.util;

import com.google.gson.JsonObject;

public class JsonUtils {
    public static Boolean getBoolean(JsonObject jo, String name) {
        if (jo.has(name) && jo.get(name).isJsonPrimitive()) {
            var jp = jo.get(name).getAsJsonPrimitive();
            if (jp.isBoolean())
                return jp.getAsBoolean();
        }
        return null;
    }
}
