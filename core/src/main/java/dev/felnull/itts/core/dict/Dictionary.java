package dev.felnull.itts.core.dict;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;

public interface Dictionary {
    @NotNull
    String apply(@NotNull String text, long guildId);

    boolean isBuiltIn();

    @NotNull
    String getName();

    @NotNull
    String getId();

    @NotNull
    @Unmodifiable
    Map<String, String> getShowInfo(long guildId);

    int getPriority();
}
