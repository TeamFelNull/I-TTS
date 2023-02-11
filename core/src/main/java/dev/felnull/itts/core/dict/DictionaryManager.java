package dev.felnull.itts.core.dict;

import com.google.common.collect.ImmutableList;
import dev.felnull.itts.core.ITTSRuntimeUse;
import dev.felnull.itts.core.savedata.DictUseData;
import dev.felnull.itts.core.savedata.SaveDataManager;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class DictionaryManager implements ITTSRuntimeUse {
    private final Dictionary globalDictionary = new GlobalDictionary();
    private final Dictionary abbreviationDictionary = new AbbreviationDictionary();
    private final Dictionary unitDictionary = new UnitDictionary();
    private final Dictionary romajiDictionary = new RomajiDictionary();
    private final List<Dictionary> dictionaries = ImmutableList.of(globalDictionary, abbreviationDictionary, unitDictionary, romajiDictionary);

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
                .sorted(Comparator.comparingInt(DictUseData::getPriority));
        AtomicReference<String> retText = new AtomicReference<>(text);

        allDict.forEach(ud -> {
            var dict = getDictionary(ud.getDictId(), guildId);
            if (dict != null)
                retText.set(dict.apply(retText.get()));
        });

        return retText.get();
    }

    @NotNull
    @Unmodifiable
    public List<Pair<String, Integer>> getDefault() {
        return ImmutableList.of(Pair.of(globalDictionary.getId(), 0), Pair.of(abbreviationDictionary.getId(), 0), Pair.of(romajiDictionary.getId(), 0));
    }
}
