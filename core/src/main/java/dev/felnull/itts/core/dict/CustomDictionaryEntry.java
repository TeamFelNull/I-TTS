package dev.felnull.itts.core.dict;

import org.jetbrains.annotations.NotNull;

/**
 * カスタム辞書データのエントリ
 *
 * @param target      置き換え対象の文字列
 * @param read        置き換え後の文字列
 * @param replaceType 置き換えタイプ
 */
public record CustomDictionaryEntry(@NotNull String target, @NotNull String read,
                                    @NotNull ReplaceType replaceType) {
}
