package dev.felnull.itts.core.savedata.dao;

import org.jetbrains.annotations.Nullable;

/**
 * 辞書利用データのレコード
 *
 * @param enable   辞書を有効にしているかどうか
 * @param priority 辞書優先度
 */
public record DictionaryUseDataRecord(@Nullable Boolean enable,
                                      @Nullable Integer priority
) {
}
