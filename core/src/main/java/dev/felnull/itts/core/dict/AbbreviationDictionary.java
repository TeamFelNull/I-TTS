package dev.felnull.itts.core.dict;

import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

public class AbbreviationDictionary extends RegexReplaceBaseDictionary {
    private static final Pattern URL_REGEX = Pattern.compile("http(s)?://([\\w-]+\\.)+[\\w-]+(/[\\w\\- ./?%&=~#:,]*)?");
    private static final Pattern CODE_BLOCK_REGEX = Pattern.compile("```(.|\n)*```");
    private final Map<Pattern, Function<String, String>> replaces;

    public AbbreviationDictionary() {
        ImmutableMap.Builder<Pattern, Function<String, String>> repls = new ImmutableMap.Builder<>();
        repls.put(URL_REGEX, str -> "ユーアールエル省略");
        repls.put(CODE_BLOCK_REGEX, str -> "コードブロック省略");

        this.replaces = repls.build();
    }

    @Override
    public boolean isBuiltIn() {
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "省略辞書";
    }

    @Override
    public @NotNull String getId() {
        return "abbreviation";
    }

    @Override
    protected @NotNull Map<Pattern, Function<String, String>> getReplaces(long guildId) {
        return replaces;
    }

    @Override
    public @NotNull @Unmodifiable Map<String, String> getShowInfo(long guildId) {
        return ImmutableMap.of("https://...", "URL省略", "``` コードブロック ```", "コードブロック省略");
    }

    @Override
    public int getPriority() {
        return 4;
    }
}
