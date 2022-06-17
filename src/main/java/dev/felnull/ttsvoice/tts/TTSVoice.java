package dev.felnull.ttsvoice.tts;

import dev.felnull.ttsvoice.tts.sayvoice.ISayVoice;

public record TTSVoice(ISayVoice sayVoice, IVoiceType voiceType) {
}
