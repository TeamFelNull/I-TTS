package dev.felnull.itts.core.savedata.legacy;

import org.jetbrains.annotations.NotNull;

/**
 * 辞書使用データ
 *
 * @author MORIMORI0317
 */
public interface LegacyDictUseData {

    /**
     * 辞書IDを取得
     *
     * @return 辞書ID
     */
    @NotNull
    String getDictId();

    /**
     * 優先度を取得
     *
     * @return 優先度
     */
    int getPriority();

    /**
     * 優先度を変更
     *
     * @param priority 優先度
     */
    void setPriority(int priority);
}
