package dev.felnull.itts.core.savedata.legacy;

import org.jetbrains.annotations.NotNull;

/**
 * 置き換え辞書のデータ
 *
 * @author MORIMORI0317
 */
public interface LegacyDictData {
    /**
     * 置き換え対象の文字列の文字列を取得
     *
     * @return 置き換え対象の文字列の文字列
     */
    @NotNull
    String getTarget();

    /**
     * 置き換え後の文字列を取得
     *
     * @return 置き換え後の文字列
     */
    @NotNull
    String getRead();
}
