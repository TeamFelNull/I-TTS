package dev.felnull.ttsvoice.audio.loader;

import dev.felnull.ttsvoice.Main;
import dev.felnull.ttsvoice.audio.player.TmpFileVoiceTrackLoader;

import java.util.ArrayList;
import java.util.List;

public class VoiceCache {
    private final TmpFileVoiceTrackLoader originalTrackLoader;
    private final List<TmpFileVoiceTrackLoader> children = new ArrayList<>();
    private long lastTime;

    public VoiceCache(TmpFileVoiceTrackLoader trackLoader) {
        this.originalTrackLoader = trackLoader;
        this.lastTime = System.currentTimeMillis();
    }

    public void update() {
        lastTime = System.currentTimeMillis();
    }

    private boolean isTimeOut() {
        return System.currentTimeMillis() - lastTime >= (long) Main.CONFIG.cashTime() * 60L * 1000L;
    }

    private boolean isForceTimeOut() {
        return System.currentTimeMillis() - lastTime >= 60L * 60L * 1000L;
    }

    public TmpFileVoiceTrackLoader createTrackLoader() {
        update();
        if (originalTrackLoader == null)
            return null;
        var c = originalTrackLoader.createCopy();
        synchronized (children) {
            children.add(c);
        }
        return c;
    }

    public boolean isUnnecessary() {
        if (isForceTimeOut())
            return true;
        if (originalTrackLoader == null)
            return true;

        if (isTimeOut() && originalTrackLoader.isAlready()) {
            synchronized (children) {
                return children.stream().allMatch(TmpFileVoiceTrackLoader::isAlready);
            }
        }
        return false;
    }

    public void deleteCacheFile() {
        if (originalTrackLoader == null) return;

        var f = originalTrackLoader.getTmpFile();
        if (f.exists())
            f.delete();
    }
}
