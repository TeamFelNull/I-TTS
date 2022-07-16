package dev.felnull.ttsvoice.tts;

import dev.felnull.ttsvoice.tts.sayvoice.ISayVoice;
import dev.felnull.ttsvoice.voice.VoiceType;

public record TTSVoice(ISayVoice sayVoice, VoiceType voiceType) {
    public boolean isCached() {
        return voiceType.isCached(sayVoice);
    }
}
