package dev.felnull.itts.core;

import dev.felnull.itts.core.cache.GlobalCacheAccess;
import dev.felnull.itts.core.config.ConfigContext;
import dev.felnull.itts.core.log.LogContext;
import dev.felnull.itts.core.savedata.SaveDataAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public interface ITTSRuntimeContext {
    @NotNull
    ConfigContext getConfigContext();

    @NotNull
    SaveDataAccess getSaveDataAccess();

    @Nullable
    Supplier<GlobalCacheAccess> getGlobalCacheAccessFactory();

    @NotNull
    LogContext getLogContext();
}
