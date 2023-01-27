package dev.felnull.ttsvoice.core.voice;

public record SimpleVoice(VoiceType voiceType) implements Voice {
    @Override
    public VoiceType getVoiceType() {
        return voiceType;
    }
}
