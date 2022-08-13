package dev.felnull.ttsvoice.audio;

import com.sedmelluq.discord.lavaplayer.container.MediaContainerRegistry;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import dev.felnull.ttsvoice.Main;
import dev.felnull.ttsvoice.discord.BotLocation;
import dev.felnull.ttsvoice.tts.TTSVoiceEntry;
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

    public void reloadSchedulers(BotLocation botLocation) {
        synchronized (SCHEDULERS) {
            var s = SCHEDULERS.get(botLocation);
            if (s != null)
                s.reload();
        }
       /* synchronized (SCHEDULERS) {
            var as = SCHEDULERS.remove(bag);
            if (as != null)
                as.dispose();
        }*/
    }

    public static VoiceAudioPlayerManager getInstance() {
        return INSTANCE;
    }

    public synchronized AudioScheduler getScheduler(BotLocation botLocation) {
        synchronized (SCHEDULERS) {
            return SCHEDULERS.computeIfAbsent(botLocation, n -> new AudioScheduler(botLocation, audioPlayerManager.createPlayer()));
        }
    }

    public AudioPlayerManager getAudioPlayerManager() {
        return audioPlayerManager;
    }

    public void addTTS(BotLocation botLocation, TTSVoiceEntry entry) {
        var sc = getScheduler(botLocation);
        if (Main.getServerSaveData(botLocation.guildId()).isOverwriteAloud()) {
            synchronized (sc.ttsQueue) {
                sc.ttsQueue.clear();
            }
            sc.stop();
            sc.start(entry);
        } else {
            sc.addEntry(entry);
        }
    }
}
