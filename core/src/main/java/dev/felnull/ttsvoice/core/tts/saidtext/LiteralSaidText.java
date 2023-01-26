package dev.felnull.ttsvoice.core.tts.saidtext;

import dev.felnull.ttsvoice.core.voice.Voice;

public record LiteralSaidText(Voice voice, String text) implements SaidText {
    @Override
    public String getText() {
        return text;
    }

    @Override
    public Voice getVoice() {
        return voice;
    }
}
