package dev.felnull.itts;

import dev.felnull.itts.core.ITTSRuntimeContext;
import dev.felnull.itts.core.RuntimeInfo;
import dev.felnull.itts.core.cache.GlobalCacheAccess;
import dev.felnull.itts.core.config.ConfigContext;
import dev.felnull.itts.core.log.LogContext;
import dev.felnull.itts.config.SelfHostConfigManager;
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
     * 実行環境の情報
     */
    private final RuntimeInfo runtimeInfo;

    /**
     * ログコンテキスト
     */
    private final LogContext logContext = new LogContextImpl();

    /**
     * コンストラクタ
     *
     * @param runtimeInfo 実行環境の情報
     */
    public SelfHostITTSRuntimeContext(RuntimeInfo runtimeInfo) {
        this.runtimeInfo = runtimeInfo;
    }

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

    @Override
    public @NotNull RuntimeInfo getRuntimeInfo() {
        return runtimeInfo;
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
