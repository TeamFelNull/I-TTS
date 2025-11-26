package dev.felnull.itts.core.savedata.repository;

import dev.felnull.itts.core.dict.CustomDictionaryEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * カスタム辞書データ
 */
public interface CustomDictionaryData {

    /**
     * すべてのエントリを取得
     *
     * @return IDとエントリのリスト
     */
    @NotNull
    @Unmodifiable
    List<IdCustomDictionaryEntryPair> getAll();

    /**
     * 置き換え対象からエントリを取得
     *
     * @param target 置き換え対象
     * @return IDとエントリのリスト
     */
    @NotNull
    @Unmodifiable
    List<IdCustomDictionaryEntryPair> getByTarget(@NotNull String target);

    /**
     * 辞書エントリを追加
     *
     * @param dictionaryEntry 辞書エントリ
     */
    void add(@NotNull CustomDictionaryEntry dictionaryEntry);

    /**
     * 指定したIDのエントリを削除する
     *
     * @param entryId エントリID
     */
    void remove(int entryId);
}
