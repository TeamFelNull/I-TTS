package dev.felnull.itts.core.dict;

import dev.felnull.itts.core.ITTSRuntimeUse;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GlobalDictionary extends RegexReplaceBaseDictionary implements ITTSRuntimeUse {
    @Override
    public boolean isBuildIn() {
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
    protected @NotNull Map<Pattern, Function<String, String>> getReplaces(long guildId) {
        return getSaveDataManager().getAllGlobalDictData().stream()
                .map(n -> Pair.of(Pattern.compile(n.getTarget()), n.getRead()))
                .collect(Collectors.toMap(Pair::getLeft, patternStringPair -> n -> patternStringPair.getRight()));
    }
}
