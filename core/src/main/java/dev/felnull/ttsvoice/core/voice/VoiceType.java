package dev.felnull.ttsvoice.core.voice;

public interface VoiceType {
    String getName();

    String getId();

    boolean isAvailable();

    VoiceCategory getCategory();
}
