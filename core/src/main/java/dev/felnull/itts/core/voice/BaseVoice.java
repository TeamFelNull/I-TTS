package dev.felnull.itts.core.voice;

public abstract class BaseVoice implements Voice {
    protected final VoiceType voiceType;

    protected BaseVoice(VoiceType voiceType) {
        this.voiceType = voiceType;
    }

    @Override
    public boolean isAvailable() {
        return voiceType.isAvailable();
    }

    @Override
    public VoiceType getVoiceType() {
        return voiceType;
    }
}
