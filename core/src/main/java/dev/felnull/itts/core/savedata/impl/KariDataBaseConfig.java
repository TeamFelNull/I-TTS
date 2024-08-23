package dev.felnull.itts.core.savedata.impl;

import dev.felnull.itts.core.config.DataBaseConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public class KariDataBaseConfig implements DataBaseConfig {

    public static final KariDataBaseConfig INSTANCE = new KariDataBaseConfig();

    @Override
    public @NotNull DataBaseType getType() {
        return DataBaseType.SQLITE;
    }

    @Override
    public @NotNull String getHost() {
        return "";
    }

    @Override
    public @Range(from = 0, to = 65535) int getPort() {
        return 0;
    }

    @Override
    public @NotNull String getDatabaseName() {
        return "";
    }

    @Override
    public @NotNull String getUser() {
        return "";
    }

    @Override
    public @NotNull String getPassword() {
        return "";
    }
}
