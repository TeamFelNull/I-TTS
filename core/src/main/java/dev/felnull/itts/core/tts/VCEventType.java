package dev.felnull.itts.core.tts;

import dev.felnull.itts.core.util.TTSUtils;
import dev.felnull.itts.core.voice.Voice;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;

import java.util.Objects;

public enum VCEventType {
    JOIN((voice, member, join, left) -> getMemberName(voice, member) + "が接続しました", true),
    LEAVE((voice, member, join, left) -> getMemberName(voice, member) + "が切断しました", false),
    MOVE_FROM((voice, member, join, left) -> getMemberName(voice, member) + "が" + getChannelName(left) + "から移動してきました", true),
    MOVE_TO((voice, member, join, left) -> getMemberName(voice, member) + "が" + getChannelName(join) + "へ移動しました", false);
    private final VCEventMessage vcEventMessage;
    private final boolean join;

    VCEventType(VCEventMessage saidMessage, boolean join) {
        this.vcEventMessage = saidMessage;
        this.join = join;
    }

    public boolean isJoin() {
        return join;
    }

    public String getMessage(Voice voice, Member member, AudioChannelUnion join, AudioChannelUnion left) {
        return this.vcEventMessage.getMessage(voice, member, join, left);
    }

    private static String getChannelName(StandardGuildChannel channel) {
        Objects.requireNonNull(channel);
        return TTSUtils.getTTSChannelName(channel);
    }

    private static String getMemberName(Voice voice, Member member) {
        return TTSUtils.getTTSName(voice, member);
    }

    private static interface VCEventMessage {
        String getMessage(Voice voice, Member member, AudioChannelUnion join, AudioChannelUnion left);
    }
}
