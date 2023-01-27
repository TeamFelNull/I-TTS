package dev.felnull.ttsvoice.core.voice;

public interface Voice {
    static Voice simple(VoiceType voiceType) {
        return new SimpleVoice(voiceType);
    }

    VoiceType getVoiceType();
}
