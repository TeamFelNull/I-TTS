package dev.felnull.ttsvoice.core.tts.saidtext;

import dev.felnull.ttsvoice.core.tts.VCEventType;
import dev.felnull.ttsvoice.core.voice.Voice;
import net.dv8tion.jda.api.entities.User;

public record VCEventSaidText(VCEventType eventType, User user) implements SaidText {
    @Override
    public String getText() {
        return "TEST";
    }

    @Override
    public Voice getVoice() {
        return null;
    }
}
