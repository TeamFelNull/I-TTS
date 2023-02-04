package dev.felnull.itts.core.dict;

import org.jetbrains.annotations.NotNull;

public interface Dictionary {
    @NotNull
    String apply(@NotNull String text);

    boolean isBuildIn();

    @NotNull
    String getName();

    @NotNull
    String getId();
}
