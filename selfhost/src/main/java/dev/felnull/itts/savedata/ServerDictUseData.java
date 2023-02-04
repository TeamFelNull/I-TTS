package dev.felnull.itts.savedata;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.felnull.itts.Main;
import dev.felnull.itts.core.savedata.DictUseData;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerDictUseData extends SaveDataBase {
    private final long guildId;
    private final Map<String, DictUseData> serverDictUseData = new ConcurrentHashMap<>();

    protected ServerDictUseData(long guildId) {
        super(new File(SelfHostSaveDataManager.DICT_USE_DATA_FOLDER, guildId + ".json"));
        this.guildId = guildId;
    }

    @Override
    public String getName() {
        return "Server Dict Use Data: " + guildId;
    }

    @Override
    protected void loadFromJson(@NotNull JsonObject jo) {
        serverDictUseData.clear();
        var djo = jo.getAsJsonObject("data");
        for (Map.Entry<String, JsonElement> entry : djo.entrySet()) {
            if (entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isNumber())
                serverDictUseData.put(entry.getKey(), new DictUseDataImpl(entry.getKey(), entry.getValue().getAsInt()));
        }
    }

    @Override
    protected void saveToJson(@NotNull JsonObject jo) {
        var djo = new JsonObject();
        serverDictUseData.forEach((dictId, dictData) -> djo.addProperty(dictId, dictData.getPriority()));
        jo.add("data", djo);
    }

    @Override
    protected int getVersion() {
        return DictUseData.VERSION;
    }


    public List<DictUseData> getAllDictUseData() {
        return ImmutableList.copyOf(serverDictUseData.values());
    }

    public DictUseData getDictUseData(String dictId) {
        return serverDictUseData.get(dictId);
    }

    public void addDictUserData(String dictId, int priority) {
        serverDictUseData.put(dictId, new DictUseDataImpl(dictId, priority));
        dirty();
    }

    public void removeDictUseData(String dictId) {
        serverDictUseData.remove(dictId);
        dirty();
    }

    @Override
    public void setDefault() {
        var df = Main.RUNTIME.getDictionaryManager().getDefault();
        for (Pair<String, Integer> dict : df) {
            serverDictUseData.put(dict.getLeft(), new DictUseDataImpl(dict.getLeft(), dict.getRight()));
        }
    }

    private class DictUseDataImpl implements DictUseData {
        private final String dictId;
        private final AtomicInteger priority = new AtomicInteger(INIT_PRIORITY);

        public DictUseDataImpl(String dictId, int priority) {
            this.dictId = dictId;
            this.priority.set(priority);
        }

        @Override
        public @NotNull String getDictId() {
            return this.dictId;
        }

        @Override
        public int getPriority() {
            return this.priority.get();
        }

        @Override
        public void setPriority(int priority) {
            this.priority.set(priority);
            dirty();
        }
    }
}
