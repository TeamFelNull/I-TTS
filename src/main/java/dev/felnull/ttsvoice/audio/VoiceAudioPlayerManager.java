package dev.felnull.ttsvoice.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class VoiceAudioPlayerManager {
    private static final Logger LOGGER = LogManager.getLogger(VoiceAudioPlayerManager.class);
    private static final VoiceAudioPlayerManager INSTANCE = new VoiceAudioPlayerManager();
    private final AudioPlayerManager audioPlayerManager;
    private final Map<Long, AudiScheduler> SCHEDULERS = new HashMap<>();

    public VoiceAudioPlayerManager() {
        this.audioPlayerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerLocalSource(audioPlayerManager);
    }

    public static VoiceAudioPlayerManager getInstance() {
        return INSTANCE;
    }

    public synchronized AudiScheduler getScheduler(long guildId) {
        return SCHEDULERS.computeIfAbsent(guildId, n -> new AudiScheduler(audioPlayerManager.createPlayer(), guildId));
    }

    public AudioTrack loadFile(File file) throws ExecutionException, InterruptedException {
        AudioTrack[] retTrack = new AudioTrack[1];
        audioPlayerManager.loadItem(file.getAbsolutePath(), new AudioLoadResultHandler() {
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
        return retTrack[0];
    }
}
