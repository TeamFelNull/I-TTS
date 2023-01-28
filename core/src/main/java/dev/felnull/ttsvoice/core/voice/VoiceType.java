package dev.felnull.ttsvoice.core.voice;

import dev.felnull.ttsvoice.core.audio.loader.VoiceTrackLoader;

public interface VoiceType {
    String getName();

    String getId();

    boolean isAvailable();

    VoiceCategory getCategory();

    VoiceTrackLoader createVoiceTrackLoader(String text);
}
