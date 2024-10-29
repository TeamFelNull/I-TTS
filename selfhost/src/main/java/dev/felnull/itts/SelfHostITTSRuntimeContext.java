package dev.felnull.itts;

import dev.felnull.itts.config.SelfHostConfigManager;
import dev.felnull.itts.core.ITTSRuntimeContext;
import dev.felnull.itts.core.cache.GlobalCacheAccess;
import dev.felnull.itts.core.config.ConfigContext;
import dev.felnull.itts.core.log.LogContext;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * セルフホスト用ランタイムのコンテキスト
 *
 * @author MORIMORI0317
 */
public class SelfHostITTSRuntimeContext implements ITTSRuntimeContext {

    /**
     * ログコンテキスト
     */
    private final LogContext logContext = new LogContextImpl();

    @Override
    public @NotNull ConfigContext getConfigContext() {
        return SelfHostConfigManager.getInstance();
    }

    @Override
    public @Nullable Supplier<GlobalCacheAccess> getGlobalCacheAccessFactory() {
        return null;
    }

    @Override
    public @NotNull LogContext getLogContext() {
        return logContext;
    }

    /**
     * ログコンテキストの実装
     *
     * @author MORIMORI0317
     */
    private static class LogContextImpl implements LogContext {
        @Override
        public @NotNull Logger getLogger() {
            return Main.LOGGER;
        }
    }
}
