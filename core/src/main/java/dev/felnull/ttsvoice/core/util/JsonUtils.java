package dev.felnull.ttsvoice.core.util;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

public class JsonUtils {
    /**
     * Jsonから文字列を取得する、存在しない場合は空の文字列を返す
     *
     * @param jo  Jsonオブジェクト
     * @param key キー
     * @return 文字列
     */
    @NotNull
    public static String getStringOrEmpty(@NotNull JsonObject jo, @NotNull String key) {
        var jp = jo.getAsJsonPrimitive(key);
        if (jp == null)
            return "";
        if (!jp.isString())
            return "";
        return jp.getAsString();
    }

    /**
     * Jsonから文字列を取得する
     *
     * @param jo           Jsonオブジェクト
     * @param key          キー
     * @param defaultValue 存在しない場合の初期値
     * @return 文字列
     */
    public static String getString(@NotNull JsonObject jo, @NotNull String key, String defaultValue) {
        if (!jo.has(key) || !jo.get(key).isJsonPrimitive() || !jo.get(key).getAsJsonPrimitive().isString())
            return defaultValue;

        return jo.get(key).getAsString();
    }

    /**
     * JsonからInt値を取得する
     *
     * @param jo           Jsonオブジェクト
     * @param key          キー
     * @param defaultValue 存在しない場合の初期値
     * @return int値
     */
    public static int getInt(@NotNull JsonObject jo, @NotNull String key, int defaultValue) {
        if (!jo.has(key) || !jo.get(key).isJsonPrimitive() || !jo.get(key).getAsJsonPrimitive().isNumber())
            return defaultValue;

        return jo.get(key).getAsInt();
    }

    /**
     * Jsonから真偽値を取得
     *
     * @param jo           Jsonオブジェクト
     * @param key          キー
     * @param defaultValue 存在しない場合の初期値
     * @return 真偽値
     */
    public static boolean getBoolean(@NotNull JsonObject jo, @NotNull String key, boolean defaultValue) {
        if (!jo.has(key) || !jo.get(key).isJsonPrimitive() || !jo.get(key).getAsJsonPrimitive().isBoolean())
            return defaultValue;

        return jo.get(key).getAsBoolean();
    }

    /**
     * JsonからLong値を取得する
     *
     * @param jo           Jsonオブジェクト
     * @param key          キー
     * @param defaultValue 存在しない場合の初期値
     * @return long値
     */
    public static long getLong(@NotNull JsonObject jo, @NotNull String key, long defaultValue) {
        if (!jo.has(key) || !jo.get(key).isJsonPrimitive() || !jo.get(key).getAsJsonPrimitive().isNumber())
            return defaultValue;

        return jo.get(key).getAsLong();
    }
}
