package dev.felnull.itts.core.tts.saidtext;

import dev.felnull.itts.core.ITTSRuntimeUse;
import dev.felnull.itts.core.tts.VCEventType;
import dev.felnull.itts.core.voice.Voice;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;

import java.util.concurrent.CompletableFuture;

/**
 * VCイベントでの読み上げテキスト
 *
 * @author MORIMORI0317
 */
public class VCEventSaidText implements SaidText, ITTSRuntimeUse {

    /**
     * 音声タイプ
     */
    private final Voice voice;

    /**
     * VCイベントの種類
     */
    private final VCEventType eventType;

    /**
     * メンバー
     */
    private final Member member;

    /**
     * 参加チャンネル
     */
    private final AudioChannelUnion join;

    /**
     * 退出チャンネル
     */
    private final AudioChannelUnion left;

    /**
     * コンストラクタ
     *
     * @param voice     音声タイプ
     * @param eventType VCイベントタイプ
     * @param member    メンバー
     * @param join      参加チャンネル
     * @param left      退出チャンネル
     */
    public VCEventSaidText(Voice voice, VCEventType eventType, Member member, AudioChannelUnion join, AudioChannelUnion left) {
        this.voice = voice;
        this.eventType = eventType;
        this.member = member;
        this.join = join;
        this.left = left;
    }

    @Override
    public CompletableFuture<String> getText() {
        return CompletableFuture.supplyAsync(() -> eventType.getMessage(voice, member, join, left), getAsyncExecutor());
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
