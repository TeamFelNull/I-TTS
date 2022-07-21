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

    public static Integer getInteger(JsonObject jo, String name) {
        if (jo.has(name) && jo.get(name).isJsonPrimitive()) {
            var jp = jo.get(name).getAsJsonPrimitive();
            if (jp.isNumber())
                return jp.getAsInt();
        }
        return null;
    }

    public static String getString(JsonObject jo, String name) {
        if (jo.has(name) && jo.get(name).isJsonPrimitive()) {
            var jp = jo.get(name).getAsJsonPrimitive();
            if (jp.isString())
                return jp.getAsString();
        }
        return null;
    }
}
