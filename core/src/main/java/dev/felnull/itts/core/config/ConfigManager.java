package dev.felnull.itts.core.config;

import dev.felnull.itts.core.ITTSRuntime;

public class ConfigManager {
    private final ConfigAccess configAccess;
    private Config config;

    public ConfigManager(ConfigAccess configAccess) {
        this.configAccess = configAccess;
    }

    public boolean init() {
        this.config = configAccess.loadConfig();
        if (this.config == null)
            return false;

        if (this.config.getBotToken().isEmpty()) {
            ITTSRuntime.getInstance().getLogger().error("Bot token is empty/Botトークンが空です");
            return false;
        }

        return true;
    }

    public Config getConfig() {
        return config;
    }
}
