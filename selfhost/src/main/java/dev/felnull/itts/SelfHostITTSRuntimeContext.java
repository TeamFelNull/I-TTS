package dev.felnull.itts;

import dev.felnull.itts.config.SelfHostConfigManager;
import dev.felnull.itts.core.ITTSRuntimeContext;
import dev.felnull.itts.core.cache.GlobalCacheAccess;
import dev.felnull.itts.core.config.ConfigContext;
import dev.felnull.itts.core.log.LogContext;
import dev.felnull.itts.core.savedata.SaveDataAccess;
import dev.felnull.itts.savedata.SelfHostSaveDataManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class SelfHostITTSRuntimeContext implements ITTSRuntimeContext {
    private final LogContext logContext = new LogContextImpl();

    @Override
    public @NotNull ConfigContext getConfigContext() {
        return SelfHostConfigManager.getInstance();
    }

    @Override
    public @NotNull SaveDataAccess getSaveDataAccess() {
        return SelfHostSaveDataManager.getInstance();
    }

    @Override
    public @Nullable Supplier<GlobalCacheAccess> getGlobalCacheAccessFactory() {
        return null;
    }

    @Override
    public @NotNull LogContext getLogContext() {
        return logContext;
    }
}
