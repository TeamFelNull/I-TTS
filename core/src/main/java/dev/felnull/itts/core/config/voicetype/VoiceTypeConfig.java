package dev.felnull.itts.core.config.voicetype;

public interface VoiceTypeConfig {
    boolean DEFAULT_ENABLE = true;
    long DEFAULT_CHECK_TIME = 15000;

    boolean isEnable();

    long getCheckTime();
}
