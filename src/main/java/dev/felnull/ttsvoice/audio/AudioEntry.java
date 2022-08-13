package dev.felnull.ttsvoice.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.felnull.ttsvoice.audio.player.VoiceTrackLoader;

public record AudioEntry(VoiceTrackLoader trackLoader, AudioTrack audioTrack) {
}