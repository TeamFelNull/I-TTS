package dev.felnull.itts.core.voice;

import dev.felnull.itts.core.audio.loader.VoiceTrackLoader;

public interface VoiceType {
    String getName();

    String getId();

    boolean isAvailable();

    VoiceCategory getCategory();

    VoiceTrackLoader createVoiceTrackLoader(String text);
}
