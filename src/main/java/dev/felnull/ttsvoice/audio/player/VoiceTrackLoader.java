package dev.felnull.ttsvoice.audio.player;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.felnull.ttsvoice.audio.AudioScheduler;

public interface VoiceTrackLoader {
    AudioTrack loaded();

    default void setAudioScheduler(AudioScheduler scheduler) {
    }

    default void end() {
    }
}
