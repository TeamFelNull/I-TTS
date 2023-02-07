package dev.felnull.itts.savedata;

import java.io.File;

public record LongSaveDataKey(long id) implements SaveDataKey {


    @Override
    public File getSavedFile(File folder) {
        return new File(folder, id + ".json");
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }
}
