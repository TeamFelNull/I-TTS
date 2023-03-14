package dev.felnull.itts.core.dict;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.felnull.itts.core.ITTSRuntimeUse;
import dev.felnull.itts.core.savedata.DictData;
import dev.felnull.itts.core.savedata.DictUseData;
import dev.felnull.itts.core.savedata.SaveDataManager;
import dev.felnull.itts.core.util.JsonUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class DictionaryManager implements ITTSRuntimeUse {
    private static final int FILE_VERSION = 0;
    private final Dictionary globalDictionary = new GlobalDictionary();
    private final Dictionary serverDictionary = new ServerDictionary();
    private final Dictionary abbreviationDictionary = new AbbreviationDictionary();
    private final Dictionary unitDictionary = new UnitDictionary();
    private final Dictionary romajiDictionary = new RomajiDictionary();
    private final List<Dictionary> dictionaries = ImmutableList.of(globalDictionary, serverDictionary, abbreviationDictionary, unitDictionary, romajiDictionary);

    @Nullable
    public Dictionary getDictionary(@NotNull String id, long guildId) {
        return dictionaries.stream()
                .filter(r -> id.equals(r.getId()))
                .findAny()
                .orElse(null);
    }

    public boolean isEnable(@NotNull Dictionary dictionary, long guildId) {
        SaveDataManager sdm = getSaveDataManager();
        DictUseData dud = sdm.getDictUseData(guildId, dictionary.getId());
        return dud != null;
    }

    @Unmodifiable
    @NotNull
    public List<Dictionary> getAllDictionaries(long guildId) {
        return dictionaries;
    }

    public String applyDict(String text, long guildId) {
        var allDict = getSaveDataManager().getAllDictUseData(guildId).stream()
                .sorted(Comparator.comparingInt(DictUseData::getPriority).reversed());
        AtomicReference<String> retText = new AtomicReference<>(text);

        allDict.forEach(ud -> {
            var dict = getDictionary(ud.getDictId(), guildId);
            if (dict != null)
                retText.set(dict.apply(retText.get(), guildId));
        });

        return retText.get();
    }

    @NotNull
    @Unmodifiable
    public List<Pair<String, Integer>> getDefault() {
        return ImmutableList.of(Pair.of(globalDictionary.getId(), globalDictionary.getPriority()), Pair.of(serverDictionary.getId(), serverDictionary.getPriority()), Pair.of(abbreviationDictionary.getId(), abbreviationDictionary.getPriority()), Pair.of(romajiDictionary.getId(), romajiDictionary.getPriority()));
    }

    public void serverDictSaveToJson(@NotNull JsonObject jo, long guildId) {
        jo.addProperty("version", FILE_VERSION);

        JsonObject entry = new JsonObject();

        List<DictData> allDict = getSaveDataManager().getAllServerDictData(guildId);
        for (DictData dictData : allDict) {
            entry.addProperty(dictData.getTarget(), dictData.getRead());
        }

        jo.add("entry", entry);
    }

    public List<DictData> serverDictLoadFromJson(@NotNull JsonObject jo, long guildId, boolean overwrite) {
        List<DictData> ret = new ArrayList<>();

        int version = JsonUtils.getInt(jo, "version", -1);

        if (version != FILE_VERSION)
            throw new RuntimeException("Unsupported dictionary file version.");

        if (jo.get("entry").isJsonObject()) {
            JsonObject entry = jo.getAsJsonObject("entry");
            SaveDataManager sdm = getSaveDataManager();

            for (Map.Entry<String, JsonElement> en : entry.entrySet()) {
                String target = en.getKey();

                if (!en.getValue().isJsonPrimitive() || !en.getValue().getAsJsonPrimitive().isString())
                    continue;

                String read = en.getValue().getAsString();

                DictData pre = sdm.getServerDictData(guildId, target);

                if (!overwrite && pre != null)
                    continue;

                sdm.addServerDictData(guildId, target, read);

                DictData ndata = Objects.requireNonNull(sdm.getServerDictData(guildId, target));
                if (!ndata.equals(pre))
                    ret.add(ndata);
            }
        }

        return ret;
    }
}
