package dev.felnull.itts.core.tts.saidtext;

import dev.felnull.itts.core.voice.Voice;

public interface SaidText {
    static SaidText literal(Voice voice, String text) {
        return new LiteralSaidText(voice, text);
    }

    String getText();

    Voice getVoice();
}
