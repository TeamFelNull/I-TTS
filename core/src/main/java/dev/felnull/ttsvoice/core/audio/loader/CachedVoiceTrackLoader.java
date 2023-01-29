package dev.felnull.ttsvoice.core.audio.loader;

import com.google.common.hash.HashCode;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.felnull.ttsvoice.core.cache.StreamOpener;

import java.util.concurrent.CompletableFuture;

public class CachedVoiceTrackLoader implements VoiceTrackLoader {
    private final HashCode hash;
    private final StreamOpener streamOpener;

    public CachedVoiceTrackLoader(HashCode hash, StreamOpener streamOpener) {
        this.hash = hash;
        this.streamOpener = streamOpener;
    }


    @Override
    public CompletableFuture<AudioTrack> load() {
        return null;
    }

    @Override
    public void dispose() {

    }
}
