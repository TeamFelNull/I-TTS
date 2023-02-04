package dev.felnull.itts.core.tts.saidtext;

import dev.felnull.itts.core.tts.VCEventType;
import dev.felnull.itts.core.voice.Voice;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;

public interface SaidText {
    static SaidText literal(Voice voice, String text) {
        return new LiteralSaidText(voice, text);
    }

    static SaidText vcEvent(Voice voice, VCEventType eventType, Member member, AudioChannelUnion join,
                            AudioChannelUnion left) {
        return new VCEventSaidText(voice, eventType, member, join, left);
    }

    String getText();

    Voice getVoice();
}
