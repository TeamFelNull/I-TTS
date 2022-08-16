package dev.felnull.ttsvoice.audio.loader;

import dev.felnull.fnjl.util.FNDataUtil;
import dev.felnull.ttsvoice.audio.player.TmpFileVoiceTrackLoader;
import dev.felnull.ttsvoice.audio.player.URLVoiceTrackLoader;
import dev.felnull.ttsvoice.audio.player.VoiceTrackLoader;
import dev.felnull.ttsvoice.tts.TTSVoice;
import dev.felnull.ttsvoice.voice.URLVoiceType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VoiceLoaderManager {
    private static final Logger LOGGER = LogManager.getLogger(VoiceLoaderManager.class);
    private static final VoiceLoaderManager INSTANCE = new VoiceLoaderManager();
    private static final File TMP_FOLDER = new File("./tmp");
    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new BasicThreadFactory.Builder().namingPattern("voice-loader-%d").daemon(true).build());
    private final Map<TTSVoice, VoiceCache> caches = new HashMap<>();
    private final Map<TTSVoice, CompletableFuture<VoiceCache>> tasks = new HashMap<>();

    public static VoiceLoaderManager getInstance() {
        return INSTANCE;
    }

    public void init(Timer timer) throws IOException {
        FileUtils.deleteDirectory(TMP_FOLDER);
        TMP_FOLDER.mkdirs();

        TimerTask cashManageTask = new TimerTask() {
            public void run() {
                clearCash();
            }
        };
        timer.scheduleAtFixedRate(cashManageTask, 0, 60 * 1000);
    }

    public File getTmpFolder(UUID id) {
        return new File(TMP_FOLDER, id.toString());
    }

    private void clearCash() {
        synchronized (caches) {
            List<TTSVoice> rm = new ArrayList<>();
            for (Map.Entry<TTSVoice, VoiceCache> entry : caches.entrySet()) {
                try {
                    if (entry.getValue().isUnnecessary()) {
                        entry.getValue().deleteCacheFile();
                        rm.add(entry.getKey());
                    }
                } catch (Exception ex) {
                    LOGGER.error("Failed to update cache", ex);
                    rm.add(entry.getKey());
                }
            }
            rm.forEach(caches::remove);
        }
    }

    public VoiceTrackLoader getTrackLoader(TTSVoice voice) {
        try {
            return getTrackLoader_(voice);
        } catch (Exception ex) {
            LOGGER.error("Failed to load audio data", ex);
        }
        return null;
    }

    private VoiceTrackLoader getTrackLoader_(TTSVoice voice) throws Exception {
        if (!voice.isCached() && voice.voiceType() instanceof URLVoiceType urlVoiceType) {
            var u = urlVoiceType.getSayVoiceSoundURL(voice.sayVoice());
            if (u != null)
                return new URLVoiceTrackLoader(u);
        }
        synchronized (caches) {
            var c = caches.get(voice);
            if (c != null)
                return c.createTrackLoader();
        }

        CompletableFuture<VoiceCache> cf;
        synchronized (tasks) {
            cf = tasks.computeIfAbsent(voice, v -> {
                var icf = CompletableFuture.supplyAsync(() -> {
                    var l = loadTmpFileVoice(v);
                    if (l == null)
                        return null;
                    VoiceCache c;
                    synchronized (caches) {
                        l.setAlready(true);
                        c = new VoiceCache(l);
                        caches.put(v, c);
                    }
                    return c;
                }, executorService);
                icf.thenRunAsync(() -> {
                    synchronized (tasks) {
                        tasks.remove(voice);
                    }
                }, executorService);
                return icf;
            });
        }
        var cg = cf.get();
        if (cg == null)
            return null;
        return cg.createTrackLoader();
    }

    private TmpFileVoiceTrackLoader loadTmpFileVoice(TTSVoice voice) {
        InputStream voiceStream;
        try {
            voiceStream = voice.voiceType().getSayVoiceSound(voice.sayVoice());
        } catch (Exception ex) {
            LOGGER.error("Failed to get audio data", ex);
            return null;
        }

        if (voiceStream == null)
            return null;

        var uuid = UUID.randomUUID();
        var file = getTmpFolder(uuid);
        try {
            FNDataUtil.bufInputToOutput(voiceStream, new FileOutputStream(file));
        } catch (IOException ex) {
            if (file.exists())
                file.delete();
            LOGGER.error("Failed to write audio data cash", ex);
            return null;
        }
        return new TmpFileVoiceTrackLoader(uuid, voice.isCached());
    }
}
