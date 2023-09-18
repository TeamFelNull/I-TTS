package dev.felnull.itts.savedata;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.File;
import java.util.List;

/**
 * セーブデータのキー
 *
 * @author MORIMORI0317
 */
public interface SaveDataKey {
    /**
     * セーブデータのファイルを取得
     *
     * @param folder セーブデータフォルダー
     * @return ファイル
     */
    File getSavedFile(File folder);

    /**
     * セーブデータファイルの検索
     *
     * @param <T> セーブデータのキー
     * @author MORIMORI0317
     */
    interface SavedFileFinder<T extends SaveDataKey> {

        /**
         * 探す
         *
         * @param folder 検索先フォルダー
         * @return ファイル
         */
        @Unmodifiable
        @NotNull
        List<T> find(File folder);
    }
}
