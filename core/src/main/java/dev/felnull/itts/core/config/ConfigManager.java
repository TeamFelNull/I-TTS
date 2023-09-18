package dev.felnull.itts.core.config;

import dev.felnull.itts.core.ITTSBaseManager;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * コンフィグの管理
 *
 * @author MORIMORI0317
 */
public class ConfigManager implements ITTSBaseManager {

    /**
     * コンフィグ取得用コンテキスト
     */
    private final ConfigContext configAccess;

    /**
     * コンフィグ
     */
    private Config config;

    /**
     * コンストラクタ
     *
     * @param configContext コンフィグのコンテキスト
     */
    public ConfigManager(ConfigContext configContext) {
        this.configAccess = configContext;
    }

    @Override
    public @NotNull CompletableFuture<?> init() {
        return CompletableFuture.supplyAsync(configAccess::loadConfig, getAsyncExecutor())
                .thenAcceptAsync(cfg -> {
                    this.config = cfg;

                    if (this.config == null) {
                        throw new RuntimeException("config access does not exist");
                    }

                    if (this.config.getBotToken().isEmpty()) {
                        throw new RuntimeException("Bot token is empty/Botトークンが空です");
                    }

                    getITTSLogger().info("Configuration setup completed");
                }, getAsyncExecutor());
    }

    public Config getConfig() {
        return config;
    }
}
