package dev.felnull.ttsvoice.core.config;

import dev.felnull.ttsvoice.core.config.voicetype.VoiceTextVoiceTypeConfig;
import dev.felnull.ttsvoice.core.config.voicetype.VoiceVoxEngineBaseVoiceTypeConfig;
import org.jetbrains.annotations.NotNull;

public interface Config {
    int VERSION = 0;
    String DEFAULT_BOT_TOKEN = "";
    int DEFAULT_THEME_COLOR = 0xFF00FF;

    @NotNull
    String getBotToken();

    int getThemeColor();

    VoiceTextVoiceTypeConfig getVoiceTextConfig();

    VoiceVoxEngineBaseVoiceTypeConfig getVoicevoxConfig();

    VoiceVoxEngineBaseVoiceTypeConfig getCoeirolnkConfig();

    VoiceVoxEngineBaseVoiceTypeConfig getSharevoxConfig();
}
