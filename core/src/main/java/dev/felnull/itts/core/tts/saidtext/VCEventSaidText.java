package dev.felnull.itts.core.tts.saidtext;

import dev.felnull.itts.core.tts.VCEventType;
import dev.felnull.itts.core.voice.Voice;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;

public class VCEventSaidText implements SaidText {
    private final Voice voice;
    private final VCEventType eventType;
    private final Member member;
    private final AudioChannelUnion join;
    private final AudioChannelUnion left;

    public VCEventSaidText(Voice voice, VCEventType eventType, Member member, AudioChannelUnion join, AudioChannelUnion left) {
        this.voice = voice;
        this.eventType = eventType;
        this.member = member;
        this.join = join;
        this.left = left;
    }

    @Override
    public String getText() {
        return eventType.getMessage(member, join, left);
    }

    @Override
    public Voice getVoice() {
        return voice;
    }

    public VCEventType getEventType() {
        return eventType;
    }

    public Member getMember() {
        return member;
    }
}
