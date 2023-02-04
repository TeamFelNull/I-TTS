package dev.felnull.itts.core.config;


import org.jetbrains.annotations.Nullable;

public interface ConfigAccess {
    @Nullable
    Config loadConfig();
}
