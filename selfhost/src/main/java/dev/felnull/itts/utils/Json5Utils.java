package dev.felnull.itts.utils;

import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.function.Function;

/**
 * Json5関係のユーティリティ
 *
 * @author MORIMORI0317
 */
public class Json5Utils {
    private Json5Utils() {
    }

    /**
     * Jsonから文字列を取得する、存在しない場合は空の文字列を返す
     *
     * @param jo      Jsonオブジェクト
     * @param keyName キー名
     * @param elseStr 存在品場合の文字列
     * @return 文字列
     */
    @NotNull
    public static String getStringOrElse(@NotNull JsonObject jo, @NotNull String keyName, @NotNull String elseStr) {
        JsonElement je = jo.get(keyName);
        if (je instanceof JsonPrimitive primitive) {
            return primitive.asString();
        }
        return elseStr;
    }

    /**
     * Json配列からリストを取得
     *
     * @param jo      Json
     * @param keyName キー名
     * @param getter  プリミティブから対象の型への変換
     * @param <T>     対象の型
     * @return リスト
     */
    @NotNull
    @Unmodifiable
    public static <T> List<T> getListOfJsonArray(@NotNull JsonObject jo, @NotNull String keyName, Function<JsonPrimitive, T> getter) {
        JsonElement jsonElement = jo.get(keyName);

        if (jsonElement instanceof JsonArray jsonArray) {
            return ofJsonArray(jsonArray, getter);
        }

        if (jsonElement instanceof JsonPrimitive jsonPrimitive) {
            return ImmutableList.of(getter.apply(jsonPrimitive));
        }

        return ImmutableList.of();
    }

    private static <T> List<T> ofJsonArray(JsonArray jsonArray, Function<JsonPrimitive, T> getter) {
        ImmutableList.Builder<T> builder = new ImmutableList.Builder<>();
        for (JsonElement jsonElement : jsonArray) {
            if (jsonElement instanceof JsonPrimitive primitive) {
                builder.add(getter.apply(primitive));
            }
        }
        return builder.build();
    }

    /**
     * 文字列のJson配列からリストを取得
     *
     * @param jo      Json
     * @param keyName キー名
     * @return リスト
     */
    @NotNull
    @Unmodifiable
    public static List<String> getStringListOfJsonArray(@NotNull JsonObject jo, @NotNull String keyName) {
        return getListOfJsonArray(jo, keyName, JsonPrimitive::asString);
    }

    /**
     * ListからJson配列へ変換
     *
     * @param list リスト
     * @return Json配列
     */
    @NotNull
    public static JsonArray toJsonArray(@NotNull List<?> list) {
        JsonArray ja = new JsonArray();
        for (Object o : list) {
            ja.add(new JsonPrimitive(o));
        }
        return ja;
    }
}
