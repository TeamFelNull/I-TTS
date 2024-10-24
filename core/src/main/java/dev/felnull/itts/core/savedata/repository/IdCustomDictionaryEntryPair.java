package dev.felnull.itts.core.savedata.repository;

import dev.felnull.itts.core.dict.CustomDictionaryEntry;
import org.jetbrains.annotations.NotNull;

/**
 * IDとカスタム辞書エントリのペア
 *
 * @param id    ID
 * @param entry エントリ
 */
public record IdCustomDictionaryEntryPair(int id, @NotNull CustomDictionaryEntry entry) {
}
