package dev.felnull.ttsvoice.audio.player;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.concurrent.CompletableFuture;

public interface VoiceTrackLoader {
    CompletableFuture<AudioTrack> loaded();

    default void afterEnd() {
    }
}
