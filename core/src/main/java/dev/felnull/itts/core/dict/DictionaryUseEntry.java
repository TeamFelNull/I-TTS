package dev.felnull.itts.core.dict;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 辞書使用データのエントリ
 *
 * @param dictionaryId 辞書ID
 * @param enable       有効状態(nullならデフォルト定義)
 * @param priority     優先度(nullならデフォルト定義)
 */
public record DictionaryUseEntry(@NotNull String dictionaryId, @Nullable Boolean enable, @Nullable Integer priority) {
}
