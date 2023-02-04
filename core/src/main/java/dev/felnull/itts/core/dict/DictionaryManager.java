package dev.felnull.itts.core.dict;

import com.google.common.collect.ImmutableList;
import dev.felnull.itts.core.TTSVoiceRuntime;
import dev.felnull.itts.core.savedata.DictUseData;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class DictionaryManager {
    private final Dictionary abbreviationDictionary = new AbbreviationDictionary();
    private final Dictionary unitDictionary = new UnitDictionary();
    private final List<Dictionary> buildInDictionaries = ImmutableList.of(abbreviationDictionary, unitDictionary);

    @Nullable
    public Dictionary getDictionary(@NotNull String id, long guildId) {
        return buildInDictionaries.stream()
                .filter(r -> id.equals(r.getId()))
                .findAny()
                .orElse(null);
    }

    @Unmodifiable
    @NotNull
    public List<Dictionary> getAllDictionaries(long guildId) {
        return buildInDictionaries;
    }

    public String applyDict(String text, long guildId) {
        var allDict = TTSVoiceRuntime.getInstance().getSaveDataManager().getAllDictUseData(guildId).stream()
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
        return ImmutableList.of(Pair.of(abbreviationDictionary.getId(), 0));
    }
}
