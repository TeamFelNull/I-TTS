package dev.felnull.itts.core.tts;

import dev.felnull.itts.core.util.TTSUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;

public enum VCEventType {
    JOIN((member, join, left) -> getMemberTTSName(member) + "が接続しました", true),
    LEAVE((member, join, left) -> getMemberTTSName(member) + "が切断しました", false),
    MOVE_FROM((member, join, left) -> getMemberTTSName(member) + "が" + getChannelTTSName(left) + "から移動してきました", true),
    MOVE_TO((member, join, left) -> getMemberTTSName(member) + "が" + getChannelTTSName(join) + "へ移動しました", false);
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

    private static String getChannelTTSName(Channel channel) {
        var tn = TTSUtils.getTTSChannelName(channel);
        return "別のチャンネル";
    }

    private static String getMemberTTSName(Member member) {
        return TTSUtils.getTTSName(member.getUser());
    }

    private static interface VCEventMessage {
        String getMessage(Member member, AudioChannelUnion join, AudioChannelUnion left);
    }
}
