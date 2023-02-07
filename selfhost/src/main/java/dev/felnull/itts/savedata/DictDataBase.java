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

public abstract class DictDataBase extends SaveDataBase {
    private final Map<String, DictData> dictEntries = new ConcurrentHashMap<>();

    @Override
    protected int getVersion() {
        return DictData.VERSION;
    }

    @Override
    protected void loadFromJson(@NotNull JsonObject jo) {
        dictEntries.clear();
        var djo = jo.getAsJsonObject("data");
        if (djo != null) {
            for (Map.Entry<String, JsonElement> entry : djo.entrySet()) {
                if (entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isString())
                    dictEntries.put(entry.getKey(), new DictDataImpl(entry.getKey(), entry.getValue().getAsString()));
            }
        }
    }

    @Override
    protected void saveToJson(@NotNull JsonObject jo) {
        var djo = new JsonObject();
        dictEntries.values().forEach(dict -> djo.addProperty(dict.getTarget(), dict.getRead()));
        jo.add("data", djo);
    }

    @NotNull
    @Unmodifiable
    public List<DictData> getAllDictData() {
        return ImmutableList.copyOf(dictEntries.values());
    }

    @Nullable
    public DictData getDictData(@NotNull String target) {
        return dictEntries.get(target);
    }

    public void addDictData(@NotNull String target, @NotNull String read) {
        dictEntries.put(target, new DictDataImpl(target, read));
    }

    public void removeDictData(@NotNull String target) {
        dictEntries.remove(target);
    }

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
