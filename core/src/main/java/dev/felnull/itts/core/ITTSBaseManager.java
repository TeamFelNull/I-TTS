package dev.felnull.itts.core;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public interface ITTSBaseManager extends ITTSRuntimeUse {
    @NotNull
    CompletableFuture<?> init();
}
