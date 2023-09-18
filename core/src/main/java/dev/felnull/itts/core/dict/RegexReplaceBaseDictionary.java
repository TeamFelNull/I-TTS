package dev.felnull.itts.core.dict;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * 正規表現置き換え辞書のベース
 *
 * @author MORIMORI0317
 */
public abstract class RegexReplaceBaseDictionary implements Dictionary {
    /**
     * 置き換える正規表現と置き換えファンクションのMAP
     *
     * @param guildId サーバーID
     * @return 正規表現と置き換えファンクションのMAP
     */
    @NotNull
    protected abstract Map<Pattern, Function<String, String>> getReplaces(long guildId);

    @Override
    public @NotNull String apply(@NotNull String text, long guildId) {
        Map<Pattern, Function<String, String>> replaces = getReplaces(guildId);
        AtomicReference<String> ret = new AtomicReference<>(text);
        replaces.forEach((pattern, rep) -> ret.set(pattern.matcher(ret.get()).replaceAll(res -> rep.apply(res.group()))));
        return ret.get();
    }
}
