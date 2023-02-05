package dev.felnull.itts.core.tts.saidtext;

import dev.felnull.itts.core.tts.VCEventType;
import dev.felnull.itts.core.voice.Voice;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

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

    @Override
    public boolean updateSurvive(@NotNull @Unmodifiable List<SaidText> currentQueue) {
        var cqvt = currentQueue.stream()
                .filter(r -> r instanceof VCEventSaidText vst && vst.member.getUser().getIdLong() == this.member.getUser().getIdLong())
                .map(r -> (VCEventSaidText) r)
                .toList();

        int in = cqvt.indexOf(this);
        if (in < 0)
            return true;

        int size = cqvt.size();
        if (size <= in + 1)
            return true;

        for (int i = in + 1; i < size; i++) {
            boolean ie = cqvt.get(i).eventType.isJoin();
            if (ie == eventType.isJoin())
                return false;
        }

        return true;
    }
}
