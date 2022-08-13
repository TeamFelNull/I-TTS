package dev.felnull.ttsvoice.audio;

import dev.felnull.ttsvoice.audio.loader.VoiceLoaderManager;
import dev.felnull.ttsvoice.tts.TTSVoiceEntry;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class AudioLoader {
    private final ExecutorService loadExecutorServices;
    private final AudioScheduler audioScheduler;
    protected final Map<TTSVoiceEntry, CompletableFuture<AudioEntry>> loadingEntry = new HashMap<>();

    public AudioLoader(AudioScheduler audioScheduler) {
        this.audioScheduler = audioScheduler;
        this.loadExecutorServices = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new BasicThreadFactory.Builder().namingPattern("voice-tack-loader-" + audioScheduler.botLocation.guildId() + "-" + audioScheduler.botLocation.botUserId() + "-%d").daemon(true).build());
    }

    public void stopLoad(TTSVoiceEntry entry) {
        synchronized (loadingEntry) {
            var cf = loadingEntry.remove(entry);
            if (cf != null)
                cf.cancel(true);
            entry.trackerDepose();
        }
    }

    public void stopAllLoad() {
        synchronized (loadingEntry) {
            loadingEntry.forEach((t, c) -> {
                c.cancel(true);
                t.trackerDepose();
            });
            loadingEntry.clear();
        }
    }

    public void reload() {
        stopAllLoad();
    }

    public void addLoadEntry(TTSVoiceEntry entry, Consumer<AudioEntry> loadComped, ExecutorService executorService) {
        var vlm = VoiceLoaderManager.getInstance();
        var cf = CompletableFuture.supplyAsync(() -> {
            return vlm.getTrackLoader(entry.voice());
        }, loadExecutorServices).thenApplyAsync(trackLoader -> {
            return new AudioEntry(trackLoader, trackLoader.loaded());
        }, loadExecutorServices);

        synchronized (loadingEntry) {
            loadingEntry.put(entry, cf);
        }
        cf.thenAcceptAsync(loadComped, executorService);
    }
}
