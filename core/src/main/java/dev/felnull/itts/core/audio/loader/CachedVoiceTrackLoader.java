package dev.felnull.itts.core.audio.loader;

import com.google.common.hash.HashCode;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.felnull.itts.core.ITTSRuntime;
import dev.felnull.itts.core.cache.CacheUseEntry;
import dev.felnull.itts.core.cache.StreamOpener;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

public class CachedVoiceTrackLoader implements VoiceTrackLoader {
    private final HashCode hash;
    private final StreamOpener streamOpener;
    private final AtomicReference<CacheUseEntry> cacheEntry = new AtomicReference<>();

    public CachedVoiceTrackLoader(HashCode hash, StreamOpener streamOpener) {
        this.hash = hash;
        this.streamOpener = streamOpener;
    }

    @Override
    public CompletableFuture<AudioTrack> load() {
        return ITTSRuntime.getInstance().getCacheManager().loadOrRestore(hash, streamOpener)
                .thenApplyAsync(this::loadTack, ITTSRuntime.getInstance().getAsyncWorkerExecutor());
    }

    private AudioTrack loadTack(CacheUseEntry cacheUseEntry) {
        cacheEntry.set(cacheUseEntry);
        var vam = ITTSRuntime.getInstance().getVoiceAudioManager();
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

        var ret = retTrack.get();
        if (ret == null)
            throw new RuntimeException("Failed to load track");

        return ret;
    }

    @Override
    public void dispose() {
        var ce = cacheEntry.get();
        if (ce != null)
            ce.useLock().unlock();
    }
}
