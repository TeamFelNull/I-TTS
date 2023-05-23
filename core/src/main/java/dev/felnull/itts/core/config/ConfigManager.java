package dev.felnull.itts.core.config;

import dev.felnull.itts.core.ITTSBaseManager;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ConfigManager implements ITTSBaseManager {
    private final ConfigContext configAccess;
    private Config config;

    public ConfigManager(ConfigContext configAccess) {
        this.configAccess = configAccess;
    }

    @Override
    public @NotNull CompletableFuture<?> init() {
        return CompletableFuture.supplyAsync(configAccess::loadConfig, getAsyncExecutor())
                .thenAcceptAsync(cfg -> {
                    this.config = cfg;

                    if (this.config == null)
                        throw new RuntimeException("config access does not exist");

                    if (this.config.getBotToken().isEmpty())
                        throw new RuntimeException("Bot token is empty/Botトークンが空です");

                    getITTSLogger().info("Configuration setup completed");
                }, getAsyncExecutor());
    }

    public Config getConfig() {
        return config;
    }
}
