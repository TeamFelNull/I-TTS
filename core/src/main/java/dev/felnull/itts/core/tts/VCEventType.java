package dev.felnull.itts.core.tts;

import dev.felnull.itts.core.util.TTSUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;

import java.util.Objects;

public enum VCEventType {
    JOIN((member, join, left) -> getMemberName(member) + "が接続しました", true),
    LEAVE((member, join, left) -> getMemberName(member) + "が切断しました", false),
    MOVE_FROM((member, join, left) -> getMemberName(member) + "が" + getChannelName(left) + "から移動してきました", true),
    MOVE_TO((member, join, left) -> getMemberName(member) + "が" + getChannelName(join) + "へ移動しました", false);
    private final VCEventMessage vcEventMessage;
    private final boolean join;

    VCEventType(VCEventMessage saidMessage, boolean join) {
        this.vcEventMessage = saidMessage;
        this.join = join;
    }

    public boolean isJoin() {
        return join;
    }

    public String getMessage(Member member, AudioChannelUnion join, AudioChannelUnion left) {
        return this.vcEventMessage.getMessage(member, join, left);
    }

    private static String getChannelName(StandardGuildChannel channel) {
        Objects.requireNonNull(channel);
        return TTSUtils.getTTSChannelName(channel);
    }

    private static String getMemberName(Member member) {
        return TTSUtils.getTTSName(member);
    }

    private static interface VCEventMessage {
        String getMessage(Member member, AudioChannelUnion join, AudioChannelUnion left);
    }
}
