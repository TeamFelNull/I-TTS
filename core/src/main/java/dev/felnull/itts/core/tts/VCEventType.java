package dev.felnull.itts.core.tts;

import dev.felnull.itts.core.util.TTSUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;

public enum VCEventType {
    JOIN((member, join, left) -> getMemberTTSName(member) + "が接続しました"),
    LEAVE((member, join, left) -> getMemberTTSName(member) + "が切断しました"),
    MOVE_FROM((member, join, left) -> getMemberTTSName(member) + "が" + getChannelTTSName(left) + "から移動してきました"),
    MOVE_TO((member, join, left) -> getMemberTTSName(member) + "が" + getChannelTTSName(join) + "へ移動しました");

    /*
    FORCE_MOVE_FROM((user, join, left) -> TTSUtils.getTTSName(user) + "が" + getChannelTTS(left, user) + "から移動させられました"),
    FORCE_LEAVE((user, join, left) -> TTSUtils.getTTSName(user) + "が切断されました"),
    FORCE_MOVE_TO((user, join, left) -> TTSUtils.getTTSName(user) + "が" + getChannelTTS(join, user) + "へ移動させられました");*/
    private final VCEventMessage vcEventMessage;

    VCEventType(VCEventMessage saidMessage) {
        this.vcEventMessage = saidMessage;
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
