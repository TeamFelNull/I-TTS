package dev.felnull.itts.core.tts.saidtext;

import dev.felnull.itts.core.ITTSRuntimeUse;
import dev.felnull.itts.core.tts.VCEventType;
import dev.felnull.itts.core.voice.Voice;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;

import java.util.concurrent.CompletableFuture;

public class VCEventSaidText implements SaidText, ITTSRuntimeUse {
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
    public CompletableFuture<String> getText() {
        return CompletableFuture.supplyAsync(() -> eventType.getMessage(voice, member, join, left)
                , getAsyncExecutor());
    }

    @Override
    public CompletableFuture<Voice> getVoice() {
        return CompletableFuture.completedFuture(voice);
    }

    public VCEventType getEventType() {
        return eventType;
    }

    public Member getMember() {
        return member;
    }
}
