package dev.felnull.ttsvoice.core.cache;

import com.google.common.hash.HashCode;
import dev.felnull.ttsvoice.core.TTSVoiceRuntime;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class LocalCache {
    private final AtomicLong lastUseTime = new AtomicLong(System.currentTimeMillis());
    private final AtomicInteger useLockCount = new AtomicInteger();
    private final AtomicBoolean runningTimer = new AtomicBoolean();
    private final AtomicBoolean destroy = new AtomicBoolean();
    private final HashCode hashCode;
    private final File file;

    public LocalCache(HashCode hashCode, File file) {
        this.hashCode = hashCode;
        this.file = file;
    }

    public Pair<File, UseLock> restore() {
        if (isDestroy())
            throw new IllegalStateException("Already destroyed");

        useLockCount.incrementAndGet();
        UseLock ul = () -> {
            if (useLockCount.decrementAndGet() <= 0) {
                lastUseTime.set(System.currentTimeMillis());
                scheduleCheckTimer(this::check, getCacheTime() + 300L);
            }
        };

        return Pair.of(null, ul);
    }

    protected void dispose() {
        destroy.set(true);
    }

    public boolean isDestroy() {
        return destroy.get();
    }

    private void check() {
        if (isDestroy()) return;
        if (useLockCount.get() > 0) return;

        long now = System.currentTimeMillis();
        long lastTime = lastUseTime.get();
        long eqTime = now - lastTime;

        if (eqTime >= getCacheTime()) {
            TTSVoiceRuntime.getInstance().getCacheManager().disposeLocalCache(hashCode);
            return;
        }

        scheduleCheckTimer(this::check, getCacheTime() - eqTime + 300L);
    }

    private long getCacheTime() {
        return TTSVoiceRuntime.getInstance().getConfigManager().getConfig().getCacheTime();
    }

    private void scheduleCheckTimer(Runnable runnable, long delay) {
        if (!runningTimer.compareAndSet(false, true))
            return;

        TTSVoiceRuntime.getInstance().getTimer().schedule(new TimerTask() {
            @Override
            public void run() {
                runningTimer.set(false);
                CompletableFuture.runAsync(runnable, TTSVoiceRuntime.getInstance().getAsyncWorkerExecutor());
            }
        }, delay);
    }
}
