package dev.felnull.ttsvoice.core.config;

import dev.felnull.ttsvoice.core.config.voicetype.VTVVoiceTypeConfig;
import dev.felnull.ttsvoice.core.config.voicetype.VVEBVoiceTypeConfig;
import org.jetbrains.annotations.NotNull;

public abstract class Config {
    protected static final int VERSION = 0;
    protected String name = "おしゃべり君";
    protected String botToken = "";
    protected int themeColor = 0xFF00FF;
    protected VTVVoiceTypeConfig voiceTextConfig = new VTVVoiceTypeConfig();
    protected VVEBVoiceTypeConfig voicevoxConfig = new VVEBVoiceTypeConfig();
    protected VVEBVoiceTypeConfig coeirolnkConfig = new VVEBVoiceTypeConfig();
    protected VVEBVoiceTypeConfig sharevoxConfig = new VVEBVoiceTypeConfig();

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getBotToken() {
        return botToken;
    }

    public int getThemeColor() {
        return themeColor;
    }
}
