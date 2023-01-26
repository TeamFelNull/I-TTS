package dev.felnull.ttsvoice.core.tts;

import dev.felnull.ttsvoice.core.util.TTSUtils;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;

public enum VCEventType {
    CONNECT((user, join, left) -> TTSUtils.getTTSName(user) + "が接続しました"),
    JOIN((user, join, left) -> TTSUtils.getTTSName(user) + "が参加しました"),
    MOVE_FROM((user, join, left) -> TTSUtils.getTTSName(user) + "が" + getChannelTTS(left, user) + "から移動してきました"),
    FORCE_MOVE_FROM((user, join, left) -> TTSUtils.getTTSName(user) + "が" + getChannelTTS(left, user) + "から移動させられました"),
    LEAVE((user, join, left) -> TTSUtils.getTTSName(user) + "が退出しました"),
    FORCE_LEAVE((user, join, left) -> TTSUtils.getTTSName(user) + "が切断されました"),
    MOVE_TO((user, join, left) -> TTSUtils.getTTSName(user) + "が" + getChannelTTS(join, user) + "へ移動しました"),
    FORCE_MOVE_TO((user, join, left) -> TTSUtils.getTTSName(user) + "が" + getChannelTTS(join, user) + "へ移動させられました");
    private final SaidMessage saidMessage;

    VCEventType(SaidMessage saidMessage) {
        this.saidMessage = saidMessage;
    }

    public String getMessage(User user, AudioChannelUnion join, AudioChannelUnion left) {
        return this.saidMessage.getMessage(user, join, left);
    }

    private static String getChannelTTS(Channel channel, User user) {
        var tn = TTSUtils.getTTSChannelName(channel);

        return "別のチャンネル";
    }

    private static interface SaidMessage {
        String getMessage(User user, AudioChannelUnion join, AudioChannelUnion left);
    }
}
