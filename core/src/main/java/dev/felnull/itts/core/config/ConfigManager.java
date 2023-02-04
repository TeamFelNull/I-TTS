package dev.felnull.itts.core.config;

import dev.felnull.itts.core.TTSVoiceRuntime;

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
            TTSVoiceRuntime.getInstance().getLogger().error("Bot token is empty");
            return false;
        }

        return true;
    }

    public Config getConfig() {
        return config;
    }
}
