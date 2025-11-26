package dev.felnull.itts.core.savedata.legacy.impl;

import dev.felnull.itts.core.savedata.legacy.LegacyDictData;
import org.jetbrains.annotations.NotNull;

/**
 * レガシー辞書データの実装
 *
 * @param target 対象の言葉
 * @param read   読み
 */
record LegacyDictDataImpl(String target, String read) implements LegacyDictData {

    @Override
    public @NotNull String getTarget() {
        return target;
    }

    @Override
    public @NotNull String getRead() {
        return read;
    }
}
