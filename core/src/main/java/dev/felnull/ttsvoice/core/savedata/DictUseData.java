package dev.felnull.ttsvoice.core.savedata;

import org.jetbrains.annotations.NotNull;

public interface DictUseData {
    int VERSION = 0;
    int DEFAULT_PRIORITY = 0;

    @NotNull
    String getDictId();

    int getPriority();

    void setPriority(int priority);
}
