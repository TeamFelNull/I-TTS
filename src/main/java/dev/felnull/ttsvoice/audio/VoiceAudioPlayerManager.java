package dev.felnull.ttsvoice.audio;

import com.sedmelluq.discord.lavaplayer.container.MediaContainerRegistry;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import dev.felnull.ttsvoice.discord.BotLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class VoiceAudioPlayerManager {
    private static final Logger LOGGER = LogManager.getLogger(VoiceAudioPlayerManager.class);
    private static final VoiceAudioPlayerManager INSTANCE = new VoiceAudioPlayerManager();
    private final AudioPlayerManager audioPlayerManager;
    private final Map<BotLocation, AudioScheduler> SCHEDULERS = new HashMap<>();

    public VoiceAudioPlayerManager() {
        this.audioPlayerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerLocalSource(audioPlayerManager);
        audioPlayerManager.registerSourceManager(new HttpAudioSourceManager(MediaContainerRegistry.DEFAULT_REGISTRY));
    }

    public void clearSchedulers(BotLocation bag) {
        synchronized (SCHEDULERS) {
            var as = SCHEDULERS.remove(bag);
            if (as != null)
                as.dispose();
        }
    }

    public static VoiceAudioPlayerManager getInstance() {
        return INSTANCE;
    }

    public synchronized AudioScheduler getScheduler(BotLocation bag) {
        synchronized (SCHEDULERS) {
            return SCHEDULERS.computeIfAbsent(bag, n -> new AudioScheduler(audioPlayerManager.createPlayer(), bag));
        }
    }

    public AudioPlayerManager getAudioPlayerManager() {
        return audioPlayerManager;
    }
}
