package dev.felnull.ttsvoice.audio.player;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.felnull.ttsvoice.audio.OldAudioScheduler;

public interface VoiceTrackLoader {
    AudioTrack loaded();

    default void setAudioScheduler(OldAudioScheduler scheduler) {
    }

    default void end() {
    }
}
