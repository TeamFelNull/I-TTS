package dev.felnull.ttsvoice.tts;

import dev.felnull.ttsvoice.tts.sayedtext.SayedText;
import dev.felnull.ttsvoice.voice.VoiceType;

public record TTSVoice(SayedText sayVoice, VoiceType voiceType) {
    public boolean isCached() {
        return voiceType.isCached(sayVoice);
    }
}
