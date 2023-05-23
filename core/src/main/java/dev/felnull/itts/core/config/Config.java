package dev.felnull.itts.core.config;

import dev.felnull.itts.core.config.voicetype.VoiceTextConfig;
import dev.felnull.itts.core.config.voicetype.VoicevoxConfig;
import org.jetbrains.annotations.NotNull;

public interface Config {
    int VERSION = 0;
    String DEFAULT_BOT_TOKEN = "";
    int DEFAULT_THEME_COLOR = 0xFF00FF;
    long DEFAULT_CACHE_TIME = 180000;

    @NotNull
    String getBotToken();

    int getThemeColor();

    long getCacheTime();

    VoiceTextConfig getVoiceTextConfig();

    VoicevoxConfig getVoicevoxConfig();

    VoicevoxConfig getCoeirolnkConfig();

    VoicevoxConfig getSharevoxConfig();
}
