package dev.felnull.ttsvoice.core.savedata;

import org.jetbrains.annotations.NotNull;

public interface DictData {
    int VERSION = 0;

    @NotNull
    String getTarget();

    @NotNull
    String getRead();
}
