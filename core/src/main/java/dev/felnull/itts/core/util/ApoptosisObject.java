package dev.felnull.itts.core.util;

import dev.felnull.itts.core.ITTSRuntime;

import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public abstract class ApoptosisObject {
    private final AtomicLong lastExtensionTime = new AtomicLong(System.currentTimeMillis());
    private final AtomicReference<TimerTask> task = new AtomicReference<>();
    private final AtomicBoolean broken = new AtomicBoolean();
    private final long lifeTime;

    protected ApoptosisObject(long lifeTime) {
        this.lifeTime = lifeTime;
        scheduleCheckTimer(this::check, this.lifeTime + 300L);
    }

    abstract protected void lifeEnd(boolean force);

    public void extensionLife() {
        this.lastExtensionTime.set(System.currentTimeMillis());
    }

    public void broke() {
        broken.set(true);
        var tsk = task.get();
        if (tsk != null)
            tsk.cancel();
        lifeEnd(true);
    }

    private void check() {
        if (broken.get()) return;
        task.set(null);

        long now = System.currentTimeMillis();
        long lastTime = lastExtensionTime.get();
        long eqTime = now - lastTime;

        if (eqTime >= lifeTime) {
            broken.set(true);
            lifeEnd(false);
            return;
        }

        scheduleCheckTimer(this::check, lifeTime - eqTime + 300L);
    }

    private void scheduleCheckTimer(Runnable runnable, long delay) {
        var tsk = new TimerTask() {
            @Override
            public void run() {
                CompletableFuture.runAsync(runnable, ITTSRuntime.getInstance().getAsyncWorkerExecutor());
            }
        };
        task.set(tsk);
        ITTSRuntime.getInstance().getTimer().schedule(tsk, delay);
    }

}
