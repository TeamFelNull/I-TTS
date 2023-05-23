package dev.felnull.itts.core.savedata;

import dev.felnull.itts.core.ITTSRuntime;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

public interface DictUseData {
    int VERSION = 0;

    static int initPriority(String dictId) {
        var def = ITTSRuntime.getInstance().getDictionaryManager().getDefault().stream()
                .filter(it -> it.getKey().equals(dictId))
                .findFirst();

        return def.map(Pair::getRight).orElse(-1);
    }

    @NotNull
    String getDictId();

    int getPriority();

    void setPriority(int priority);
}
