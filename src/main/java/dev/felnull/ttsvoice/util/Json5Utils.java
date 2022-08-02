package dev.felnull.ttsvoice.util;

import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.function.Function;

public class Json5Utils {
    public static JsonArray toJsonArray(List<?> list) {
        var ja = new JsonArray();
        for (Object o : list) {
            ja.add(new JsonPrimitive(o));
        }
        return ja;
    }

    public static <T> List<T> ofJsonArray(JsonArray jsonArray, Function<JsonPrimitive, T> getter) {
        ImmutableList.Builder<T> builder = new ImmutableList.Builder<>();
        for (JsonElement jsonElement : jsonArray) {
            if (jsonElement instanceof JsonPrimitive primitive)
                builder.add(getter.apply(primitive));
        }
        return builder.build();
    }

    public static List<String> ofStringJsonArray(JsonArray jsonArray) {
        return ofJsonArray(jsonArray, JsonPrimitive::asString);
    }

    public static List<Long> ofLongJsonArray(JsonArray jsonArray, long defaultValue) {
        return ofJsonArray(jsonArray, p -> p.asLong(defaultValue));
    }

    public static JsonArray getJsonArray(JsonObject jsonObject, String key) {
        return jsonObject.get(JsonArray.class, key);
    }
}
