package dev.felnull.ttsvoice.core.config;

import dev.felnull.ttsvoice.core.TTSVoiceRuntime;

public class ConfigManager {
    private final TTSVoiceRuntime runtime;
    private final ConfigAccess configAccess;
    private Config config;

    public ConfigManager(TTSVoiceRuntime runtime, ConfigAccess configAccess) {
        this.runtime = runtime;
        this.configAccess = configAccess;
    }

    public boolean init() {
        this.config = configAccess.loadConfig(runtime);
        if (this.config == null)
            return false;

        return true;
    }

    public Config getConfig() {
        return config;
    }
}
