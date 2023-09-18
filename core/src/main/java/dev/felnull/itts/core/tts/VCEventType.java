package dev.felnull.itts.core.tts;

import dev.felnull.itts.core.util.TTSUtils;
import dev.felnull.itts.core.voice.Voice;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;

import java.util.Objects;

/**
 * VCイベントの種類
 *
 * @author MORIMORI0317
 */
public enum VCEventType {
    /**
     * 参加時
     */
    JOIN((voice, member, join, left) -> getMemberName(voice, member) + "が接続しました", true),

    /**
     * 退出時
     */
    LEAVE((voice, member, join, left) -> getMemberName(voice, member) + "が切断しました", false),

    /**
     * 別のチャンネルへから移動してきたとき
     */
    MOVE_FROM((voice, member, join, left) -> getMemberName(voice, member) + "が" + getChannelName(left) + "から移動してきました", true),

    /**
     * 別のチャンネルへ移動したとき
     */
    MOVE_TO((voice, member, join, left) -> getMemberName(voice, member) + "が" + getChannelName(join) + "へ移動しました", false);

    /**
     * VCイベントのメッセージ
     */
    private final VCEventMessage vcEventMessage;

    /**
     * 参加したかどうか
     */
    private final boolean join;

    VCEventType(VCEventMessage saidMessage, boolean join) {
        this.vcEventMessage = saidMessage;
        this.join = join;
    }

    public boolean isJoin() {
        return join;
    }

    /**
     * メッセージの文字列を取得
     *
     * @param voice  音声タイプ
     * @param member メンバー
     * @param join   参加チャンネル
     * @param left   退出チャンネル
     * @return メッセージ文字列
     */
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

    /**
     * VCイベントメッセージのインターフェイス
     *
     * @author MORIMORI0317
     */
    private interface VCEventMessage {
        String getMessage(Voice voice, Member member, AudioChannelUnion join, AudioChannelUnion left);
    }
}
