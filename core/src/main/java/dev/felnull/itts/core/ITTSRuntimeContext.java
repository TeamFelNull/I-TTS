package dev.felnull.itts.core;

import dev.felnull.itts.core.cache.GlobalCacheAccess;
import dev.felnull.itts.core.config.ConfigContext;
import dev.felnull.itts.core.log.LogContext;
import dev.felnull.itts.core.savedata.SaveDataAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * ランタイム作成に必要なコンテキスト
 *
 * @author MORIMORI0317
 */
public interface ITTSRuntimeContext {

    /**
     * コンフィグコンテキストを取得
     *
     * @return コンフィグコンテキスト
     */
    @NotNull
    ConfigContext getConfigContext();

    /**
     * セーブデータコンテキストを取得
     *
     * @return セーブデータコンテキスト
     */
    @NotNull
    SaveDataAccess getSaveDataAccess();

    /**
     * グローバルキャッシュアクセスのサプライヤーを取得
     *
     * @return グローバルキャッシュアクセスのサプライヤー
     */
    @Nullable
    Supplier<GlobalCacheAccess> getGlobalCacheAccessFactory();

    /**
     * ログコンテキストを取得
     *
     * @return ログコンテキスト
     */
    @NotNull
    LogContext getLogContext();
}
