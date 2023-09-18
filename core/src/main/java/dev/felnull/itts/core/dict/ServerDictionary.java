package dev.felnull.itts.core.dict;

import dev.felnull.itts.core.ITTSRuntimeUse;
import dev.felnull.itts.core.savedata.DictData;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * サーバー辞書
 *
 * @author MORIMORI0317
 */
public class ServerDictionary extends RegexReplaceBaseDictionary implements ITTSRuntimeUse {
    @Override
    public boolean isBuiltIn() {
        return false;
    }

    @Override
    public @NotNull String getName() {
        return "サーバー辞書";
    }

    @Override
    public @NotNull String getId() {
        return "server";
    }

    @Override
    public @NotNull @Unmodifiable Map<String, String> getShowInfo(long guildId) {
        return getSaveDataManager().getAllServerDictData(guildId).stream()
                .collect(Collectors.toMap(DictData::getTarget, DictData::getRead));
    }

    @Override
    public int getDefaultPriority() {
        return 2;
    }

    @Override
    protected @NotNull Map<Pattern, Function<String, String>> getReplaces(long guildId) {
        return getSaveDataManager().getAllServerDictData(guildId).stream()
                .map(n -> Pair.of(Pattern.compile(n.getTarget()), n.getRead()))
                .collect(Collectors.toMap(Pair::getLeft, patternStringPair -> n -> patternStringPair.getRight()));
    }
}
