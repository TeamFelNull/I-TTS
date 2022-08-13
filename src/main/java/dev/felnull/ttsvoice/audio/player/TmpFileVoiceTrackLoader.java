package dev.felnull.ttsvoice.audio.player;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.felnull.ttsvoice.audio.OldAudioScheduler;
import dev.felnull.ttsvoice.audio.VoiceAudioPlayerManager;
import dev.felnull.ttsvoice.audio.loader.VoiceLoaderManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class TmpFileVoiceTrackLoader implements VoiceTrackLoader {
    private static final Logger LOGGER = LogManager.getLogger(TmpFileVoiceTrackLoader.class);
    private final UUID uuid;
    private final boolean cached;
    private boolean already;
    public OldAudioScheduler audioScheduler;

    public TmpFileVoiceTrackLoader(UUID uuid, boolean cached) {
        this.uuid = uuid;
        this.cached = cached;
    }

    @Override
    public AudioTrack loaded() {
        AudioTrack[] retTrack = new AudioTrack[1];
        try {
            VoiceAudioPlayerManager.getInstance().getAudioPlayerManager().loadItem(VoiceLoaderManager.getInstance().getTmpFolder(uuid).getAbsolutePath(), new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    retTrack[0] = track;
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    LOGGER.error("Multiple audio files");
                }

                @Override
                public void noMatches() {
                    LOGGER.error("Audio file not found");
                }

                @Override
                public void loadFailed(FriendlyException ex) {
                    LOGGER.error("Audio file load failure", ex);
                }
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        return retTrack[0];
    }

    @Override
    public void setAudioScheduler(OldAudioScheduler scheduler) {
        this.audioScheduler = scheduler;
    }

    @Override
    public void end() {
        already = true;
        if (!cached) {
            var file = VoiceLoaderManager.getInstance().getTmpFolder(uuid);
            if (file.exists())
                file.delete();
        }
    }

    public File getTmpFile() {
        return VoiceLoaderManager.getInstance().getTmpFolder(uuid);
    }

    public TmpFileVoiceTrackLoader createCopy() {
        return new TmpFileVoiceTrackLoader(uuid, cached);
    }

    public boolean isAlready() {
        return already || (audioScheduler != null && this.audioScheduler.isDestroy());
    }

    public void setAlready(boolean already) {
        this.already = already;
    }
}
