package dev.felnull.itts.core;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * ITTSマネージャーのベース
 *
 * @author MORIMORI0317
 */
public interface ITTSBaseManager extends ITTSRuntimeUse {

    /**
     * 初期化
     *
     * @return 初期化のCompletableFuture
     */
    @NotNull
    CompletableFuture<?> init();
}
