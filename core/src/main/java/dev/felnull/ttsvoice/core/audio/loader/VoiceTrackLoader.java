package dev.felnull.ttsvoice.core.audio.loader;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public interface VoiceTrackLoader {
    AudioTrack load();

    void dispose();
}
