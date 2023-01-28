package dev.felnull.ttsvoice.core.audio.loader;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.concurrent.CompletableFuture;

public interface VoiceTrackLoader {
    Pair<CompletableFuture<AudioTrack>, Runnable> load();

    void dispose();
}
