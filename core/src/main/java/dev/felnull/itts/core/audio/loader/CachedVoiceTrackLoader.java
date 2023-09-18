package dev.felnull.itts.core.audio.loader;

import com.google.common.hash.HashCode;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.felnull.itts.core.ITTSRuntimeUse;
import dev.felnull.itts.core.audio.VoiceAudioManager;
import dev.felnull.itts.core.cache.CacheUseEntry;
import dev.felnull.itts.core.cache.StreamOpener;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * キャッシュを取る音声の読み込み
 *
 * @author MORIMORI0317
 */
public class CachedVoiceTrackLoader implements VoiceTrackLoader, ITTSRuntimeUse {

    /**
     * 音声識別用ハッシュ
     */
    private final HashCode hash;

    /**
     * 音声のストリーム取得用オープナー
     */
    private final StreamOpener streamOpener;

    /**
     * キャッシュのエントリ
     */
    private final AtomicReference<CacheUseEntry> cacheEntry = new AtomicReference<>();

    /**
     * コンストラクタ
     *
     * @param hash         音声識別用ハッシュ
     * @param streamOpener 音声のストリーム取得用オープナー
     */
    public CachedVoiceTrackLoader(HashCode hash, StreamOpener streamOpener) {
        this.hash = hash;
        this.streamOpener = streamOpener;
    }

    @Override
    public CompletableFuture<AudioTrack> load() {
        return getCacheManager().loadOrRestore(hash, streamOpener)
                .thenApplyAsync(this::loadTack, getAsyncExecutor());
    }

    private AudioTrack loadTack(CacheUseEntry cacheUseEntry) {
        cacheEntry.set(cacheUseEntry);
        VoiceAudioManager vam = getVoiceAudioManager();
        AtomicReference<AudioTrack> retTrack = new AtomicReference<>();

        try {
            vam.getAudioPlayerManager().loadItem(cacheUseEntry.file().getAbsolutePath(), new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    retTrack.set(track);
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                }

                @Override
                public void noMatches() {
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                }
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        AudioTrack ret = retTrack.get();
        if (ret == null) {
            throw new RuntimeException("Failed to load track");
        }

        return ret;
    }

    @Override
    public void dispose() {
        CacheUseEntry ce = cacheEntry.get();
        if (ce != null) {
            ce.useLock().unlock();
        }
    }
}
