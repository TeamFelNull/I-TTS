package dev.felnull.itts.core.dict;

import dev.felnull.itts.core.ITTSRuntimeUse;
import dev.felnull.itts.core.savedata.SaveDataManager;
import dev.felnull.itts.core.savedata.legacy.LegacyDictData;
import dev.felnull.itts.core.savedata.legacy.LegacySaveDataLayer;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * グローバル辞書
 *
 * @author MORIMORI0317
 */
public class GlobalDictionary extends RegexReplaceBaseDictionary implements ITTSRuntimeUse {
    @Override
    public boolean isBuiltIn() {
        return false;
    }

    @Override
    public @NotNull String getName() {
        return "グローバル辞書";
    }

    @Override
    public @NotNull String getId() {
        return "global";
    }

    @Override
    public @NotNull @Unmodifiable Map<String, String> getShowInfo(long guildId) {
        LegacySaveDataLayer legacySaveDataLayer = SaveDataManager.getInstance().getLegacySaveDataLayer();
        return legacySaveDataLayer.getAllGlobalDictData().stream()
                .collect(Collectors.toMap(LegacyDictData::getTarget, LegacyDictData::getRead));
    }

    @Override
    public int getDefaultPriority() {
        return 3;
    }

    @Override
    protected @NotNull Map<Pattern, Function<String, String>> getReplaces(long guildId) {
        LegacySaveDataLayer legacySaveDataLayer = SaveDataManager.getInstance().getLegacySaveDataLayer();
        return legacySaveDataLayer.getAllGlobalDictData().stream()
                .flatMap(n -> {
                    try {
                        Pattern pattern = Pattern.compile(n.getTarget());
                        return Stream.of(Pair.of(pattern, n.getRead()));
                    } catch (PatternSyntaxException e) {
                        getITTSLogger().warn("Invalid regex pattern in global dict: {}", n.getTarget());
                        return Stream.empty();
                    }
                })
                .collect(Collectors.toMap(Pair::getLeft, patternStringPair -> n -> patternStringPair.getRight()));
    }
}
