package dev.felnull.itts.core.audio.loader;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.concurrent.CompletableFuture;

public interface VoiceTrackLoader {
    CompletableFuture<AudioTrack> load();

    void dispose();
}
