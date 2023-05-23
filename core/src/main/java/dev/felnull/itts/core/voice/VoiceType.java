package dev.felnull.itts.core.voice;

public interface VoiceType {
    String getName();

    String getId();

    boolean isAvailable();

    VoiceCategory getCategory();

    Voice createVoice(long guildId, long userId);
}
