package dev.felnull.itts.core.tts.saidtext;

import dev.felnull.itts.core.voice.Voice;

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
