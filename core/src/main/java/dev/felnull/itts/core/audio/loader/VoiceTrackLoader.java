package dev.felnull.itts.core.audio.loader;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.concurrent.CompletableFuture;

/**
 * 音声読み込み
 *
 * @author MORIMORI0317
 */
public interface VoiceTrackLoader {

    /**
     * 読み込みを行いトラックを取得する
     *
     * @return 読み込み済みトラックのCompletableFuture
     */
    CompletableFuture<AudioTrack> load();

    /**
     * 破棄
     */
    void dispose();
}
