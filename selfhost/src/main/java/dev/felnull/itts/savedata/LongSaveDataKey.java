package dev.felnull.itts.savedata;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Longs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Long値のセーブデータキー
 *
 * @param id ID
 * @author MORIMORI0317
 */
public record LongSaveDataKey(long id) implements SaveDataKey {
    /**
     * セーブデータの検索
     */
    private static final SavedFileFinderImpl SAVED_FILE_FINDER = new SavedFileFinderImpl();

    @Override
    public File getSavedFile(File folder) {
        return new File(folder, id + ".json");
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }

    public static SavedFileFinder<LongSaveDataKey> getFinder() {
        return SAVED_FILE_FINDER;
    }

    /**
     * セーブデータ検索の実装
     *
     * @author MORIMORI0317
     */
    private static class SavedFileFinderImpl implements SavedFileFinder<LongSaveDataKey> {
        @Override
        public @Unmodifiable @NotNull List<LongSaveDataKey> find(File folder) {
            File[] files = folder.listFiles();

            if (files == null) {
                return ImmutableList.of();
            }

            return Arrays.stream(files)
                    .map(File::getName)
                    .filter(it -> it.length() > ".json".length())
                    .map(it -> it.substring(0, it.length() - ".json".length()))
                    .map(Longs::tryParse)
                    .filter(Objects::nonNull)
                    .map(LongSaveDataKey::new)
                    .toList();
        }
    }
}
