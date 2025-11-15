package dev.felnull.itts.core.dict;

import com.google.common.collect.ImmutableMap;
import com.ibm.icu.text.Transliterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;

/**
 * ローマ字辞書
 *
 * @author MORIMORI0317
 */
public class RomajiDictionary implements Dictionary {
    @Override
    public @NotNull String apply(@NotNull String text, long guildId) {
        Transliterator transliterator = Transliterator.getInstance("Latin-Hiragana");
        return transliterator.transliterate(text);
    }

    @Override
    public boolean isBuiltIn() {
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

    @Override
    public @NotNull @Unmodifiable Map<String, String> getShowInfo(long guildId) {
        return ImmutableMap.of("ローマ字を平仮名へ変換", "Katyou -> かちょう");
    }

    @Override
    public int getDefaultPriority() {
        return 4;
    }
}
