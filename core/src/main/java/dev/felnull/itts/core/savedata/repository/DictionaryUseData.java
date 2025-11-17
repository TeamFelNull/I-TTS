package dev.felnull.itts.core.savedata.repository;

import org.jetbrains.annotations.Nullable;

/**
 * 辞書使用データ
 */
public interface DictionaryUseData {

    /**
     * 有効状態を取得
     *
     * @return 有効状態
     */
    @Nullable
    Boolean isEnable();

    /**
     * 有効状態を変更
     *
     * @param enable 有効状態
     */
    void setEnable(@Nullable Boolean enable);

    /**
     * 優先度を取得
     *
     * @return 優先度
     */
    @Nullable
    Integer getPriority();

    /**
     * 優先度を変更
     *
     * @param priority 優先度
     */
    void setPriority(@Nullable Integer priority);

}
