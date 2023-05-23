package dev.felnull.itts.core.dict;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Pattern;

public abstract class RegexReplaceBaseDictionary implements Dictionary {
    @NotNull
    abstract protected Map<Pattern, Function<String, String>> getReplaces(long guildId);

    @Override
    public @NotNull String apply(@NotNull String text, long guildId) {
        var replaces = getReplaces(guildId);
        AtomicReference<String> ret = new AtomicReference<>(text);
        replaces.forEach((pattern, rep) -> ret.set(pattern.matcher(ret.get()).replaceAll(res -> rep.apply(res.group()))));
        return ret.get();
    }
}
