package dev.felnull.itts.savedata;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.felnull.itts.core.savedata.DictData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 辞書データベースの実装
 *
 * @author MORIMORI0317
 */
public abstract class DictDataBase extends SaveDataBase {
    /**
     * 辞書エントリ
     */
    private final Map<String, DictData> dictEntries = new ConcurrentHashMap<>();

    @Override
    protected int getVersion() {
        return DictData.VERSION;
    }

    @Override
    protected void loadFromJson(@NotNull JsonObject jo) {
        dictEntries.clear();
        JsonObject djo = jo.getAsJsonObject("data");
        if (djo != null) {
            for (Map.Entry<String, JsonElement> entry : djo.entrySet()) {
                if (entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isString()) {
                    dictEntries.put(entry.getKey(), new DictDataImpl(entry.getKey(), entry.getValue().getAsString()));
                }
            }
        }
    }

    @Override
    protected void saveToJson(@NotNull JsonObject jo) {
        JsonObject djo = new JsonObject();
        dictEntries.values().forEach(dict -> djo.addProperty(dict.getTarget(), dict.getRead()));
        jo.add("data", djo);
    }

    @NotNull
    @Unmodifiable
    public List<DictData> getAllDictData() {
        return ImmutableList.copyOf(dictEntries.values());
    }

    /**
     * 辞書データを取得
     *
     * @param target 対象の文字
     * @return 辞書データ
     */
    @Nullable
    public DictData getDictData(@NotNull String target) {
        return dictEntries.get(target);
    }

    /**
     * 辞書データを追加
     *
     * @param target 対象の文字
     * @param read   読み
     */
    public void addDictData(@NotNull String target, @NotNull String read) {
        dictEntries.put(target, new DictDataImpl(target, read));
        dirty();
    }

    /**
     * 辞書データを削除
     *
     * @param target 対象の文字
     */
    public void removeDictData(@NotNull String target) {
        dictEntries.remove(target);
        dirty();
    }

    /**
     * 辞書データの実装
     *
     * @param target 対象の文字
     * @param read   読み
     * @author MORIMORI0317
     */
    private record DictDataImpl(String target, String read) implements DictData {
        @Override
        public @NotNull String getTarget() {
            return target;
        }

        @Override
        public @NotNull String getRead() {
            return read;
        }
    }
}
