package dev.felnull.ttsvoice.core.util;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

public class JsonUtils {
    /**
     * Jsonから文字列を取得する、存在しない場合は空の文字列を返す
     *
     * @param jo      Jsonオブジェクト
     * @param keyName キー名
     * @return 文字列
     */
    @NotNull
    public static String getStringOrEmpty(@NotNull JsonObject jo, @NotNull String keyName) {
        var jp = jo.getAsJsonPrimitive(keyName);
        if (jp == null)
            return "";
        if (!jp.isString())
            return "";
        return jp.getAsString();
    }
}
