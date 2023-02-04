package dev.felnull.itts.core.tts.saidtext;

import dev.felnull.itts.core.tts.VCEventType;
import dev.felnull.itts.core.voice.Voice;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;

public record VCEventSaidText(Voice voice, VCEventType eventType, Member member, AudioChannelUnion join,
                              AudioChannelUnion left) implements SaidText {
    @Override
    public String getText() {
        return eventType.getMessage(member, join, left);
    }

    @Override
    public Voice getVoice() {
        return voice;
    }
}
