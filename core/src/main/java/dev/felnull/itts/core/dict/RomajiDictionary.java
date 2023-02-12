package dev.felnull.itts.core.dict;

import com.ibm.icu.text.Transliterator;
import org.jetbrains.annotations.NotNull;

public class RomajiDictionary implements Dictionary {
    @Override
    public @NotNull String apply(@NotNull String text, long guildId) {
        var transliterator = Transliterator.getInstance("Latin-Hiragana");
        return transliterator.transliterate(text);
    }

    @Override
    public boolean isBuildIn() {
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "ローマ字読み辞書";
    }

    @Override
    public @NotNull String getId() {
        return "romaji";
    }
}
