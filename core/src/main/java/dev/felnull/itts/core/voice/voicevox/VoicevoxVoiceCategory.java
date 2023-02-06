package dev.felnull.itts.core.voice.voicevox;

import dev.felnull.itts.core.voice.VoiceCategory;

import java.util.Locale;

public class VoicevoxVoiceCategory implements VoiceCategory {
    private final VoicevoxManager manager;

    public VoicevoxVoiceCategory(VoicevoxManager voicevoxManager) {
        this.manager = voicevoxManager;
    }

    @Override
    public String getName() {
        return manager.getName().toUpperCase(Locale.ROOT);
    }

    @Override
    public String getId() {
        return manager.getName().toLowerCase(Locale.ROOT);
    }

    @Override
    public boolean isAvailable() {
        return manager.isAvailable();
    }
}
