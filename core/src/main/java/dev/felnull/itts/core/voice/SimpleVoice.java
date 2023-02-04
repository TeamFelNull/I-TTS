package dev.felnull.itts.core.voice;

public record SimpleVoice(VoiceType voiceType) implements Voice {
    @Override
    public VoiceType getVoiceType() {
        return voiceType;
    }
}
