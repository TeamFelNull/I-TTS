package dev.felnull.ttsvoice.audio.player;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.felnull.ttsvoice.audio.VoiceAudioPlayerManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutionException;

public record URLVoiceTrackLoader(String url) implements VoiceTrackLoader {
    private static final Logger LOGGER = LogManager.getLogger(URLVoiceTrackLoader.class);

    @Override
    public AudioTrack loaded() {
        AudioTrack[] retTrack = new AudioTrack[1];
        try {
            VoiceAudioPlayerManager.getInstance().getAudioPlayerManager().loadItem(url, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    retTrack[0] = track;
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    LOGGER.error("Multiple audio urls");
                }

                @Override
                public void noMatches() {
                    LOGGER.error("Audio url not found");
                }

                @Override
                public void loadFailed(FriendlyException ex) {
                    LOGGER.error("Audio url load failure", ex);
                }
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        return retTrack[0];
    }
}
