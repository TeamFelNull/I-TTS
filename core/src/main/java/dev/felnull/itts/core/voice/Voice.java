package dev.felnull.itts.core.voice;

import dev.felnull.itts.core.audio.loader.VoiceTrackLoader;

public interface Voice {
    boolean isAvailable();

    VoiceTrackLoader createVoiceTrackLoader(String text);

    VoiceType getVoiceType();
}
