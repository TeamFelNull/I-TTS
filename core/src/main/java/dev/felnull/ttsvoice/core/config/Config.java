package dev.felnull.ttsvoice.core.config;

import org.jetbrains.annotations.NotNull;

public interface Config {
    int VERSION = 0;
    String DEFAULT_NAME = "おしゃべり君";
    String DEFAULT_BOT_TOKEN = "";
    int DEFAULT_THEME_COLOR = 0xFF00FF;

    @NotNull
    String getName();

    @NotNull
    String getBotToken();

    int getThemeColor();
}
