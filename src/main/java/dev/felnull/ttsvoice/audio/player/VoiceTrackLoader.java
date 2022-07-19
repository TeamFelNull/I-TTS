package dev.felnull.ttsvoice.audio.player;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.felnull.ttsvoice.audio.AudioScheduler;

import java.util.concurrent.CompletableFuture;

public interface VoiceTrackLoader {
    CompletableFuture<AudioTrack> loaded();

    default void setAudioScheduler(AudioScheduler scheduler) {
    }

    default void end() {
    }
}
