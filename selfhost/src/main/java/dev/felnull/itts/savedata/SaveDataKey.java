package dev.felnull.itts.savedata;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.File;
import java.util.List;

public interface SaveDataKey {
    File getSavedFile(File folder);

    interface SavedFileFinder<T extends SaveDataKey> {
        @Unmodifiable
        @NotNull
        List<T> find(File folder);
    }
}
