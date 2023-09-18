package dev.felnull.itts.core.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import dev.felnull.itts.core.ITTSRuntimeUse;
import dev.felnull.itts.core.audio.loader.VoiceTrackLoader;
import dev.felnull.itts.core.tts.saidtext.SaidText;
import dev.felnull.itts.core.util.TTSUtils;
import dev.felnull.itts.core.voice.Voice;
import net.dv8tion.jda.api.managers.AudioManager;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * オーディオ再生のスケジュール
 *
 * @author MORIMORI0317
 */
public class VoiceAudioScheduler extends AudioEventAdapter implements ITTSRuntimeUse {
    /**
     * オーディオマネージャー
     */
    private final AudioManager audioManager;

    /**
     * ボイスオーディオマネージャー
     */
    private final VoiceAudioManager voiceAudioManager;

    /**
     * オーディオプレイヤー
     */
    private final AudioPlayer audioPlayer;

    /**
     * 現在の読み込み済み読み上げテキスト
     */
    private final AtomicReference<Pair<LoadedSaidText, Runnable>> currentLoaded = new AtomicReference<>();

    /**
     * サーバーID
     */
    private final long guildId;

    /**
     * コンストラクタ
     *
     * @param audioManager      オーディオマネージャー
     * @param voiceAudioManager ボイスオーディオマネージャー
     * @param guildId           サーバーID
     */
    public VoiceAudioScheduler(AudioManager audioManager, VoiceAudioManager voiceAudioManager, long guildId) {
        this.audioManager = audioManager;
        this.voiceAudioManager = voiceAudioManager;
        this.audioPlayer = voiceAudioManager.getAudioPlayerManager().createPlayer();
        this.guildId = guildId;
        this.audioPlayer.addListener(this);
        this.audioManager.setSendingHandler(new VoiceAudioHandler(audioPlayer));
    }

    /**
     * 破棄
     */
    public void dispose() {
        stop();
        this.audioManager.setSendingHandler(null);
    }

    /**
     * 読み込みを開始
     *
     * @param saidText 読み上げテキスト
     * @return 読み込み済み読み上げテキストの非同期読み込みCompletableFuture
     */
    public CompletableFuture<LoadedSaidText> load(SaidText saidText) {
        CompletableFuture<String> textCf = saidText.getText();
        CompletableFuture<Voice> voiceCf = saidText.getVoice();

        return textCf.thenApplyAsync(text -> {
                    String sayText = getDictionaryManager().applyDict(text, guildId);

                    Voice voice;
                    try {
                        voice = voiceCf.get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }

                    Objects.requireNonNull(voice, "Voice is null");

                    return Pair.of(TTSUtils.roundText(voice, guildId, sayText, false), voice);
                }, getAsyncExecutor())
                .thenComposeAsync((sayTextVoice) -> {
                    VoiceTrackLoader vtl = sayTextVoice.getRight().createVoiceTrackLoader(sayTextVoice.getLeft());
                    return vtl.load().thenApplyAsync(r -> new LoadedSaidText(saidText, r, vtl::dispose), getAsyncExecutor());
                }, getAsyncExecutor());
    }

    /**
     * 再生を一時停止
     */
    public void stop() {
        currentLoaded.set(null);
        audioPlayer.stopTrack();
    }

    /**
     * 再生を開始
     *
     * @param loadedSaidText 読み込み済み読み上げテキスト
     * @param playEndRun     再生終了後の処理
     */
    public void play(LoadedSaidText loadedSaidText, Runnable playEndRun) {
        currentLoaded.set(Pair.of(loadedSaidText, playEndRun));
        audioPlayer.playTrack(loadedSaidText.getTrack());
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        Pair<LoadedSaidText, Runnable> old = currentLoaded.getAndSet(null);
        if (old != null) {
            old.getLeft().setAlreadyUsed(true);
            old.getRight().run();
        }
    }
}
