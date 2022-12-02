package dev.felnull.ttsvoice.core.config.voicetype;

public interface VoiceTypeConfig {
    boolean DEFAULT_ENABLE = true;
    long DEFAULT_CACHE_TIME = 180000;
    long DEFAULT_CHECK_TIME = 15000;

    boolean isEnable();

    long getCacheTime();

    long getCheckTime();
}
