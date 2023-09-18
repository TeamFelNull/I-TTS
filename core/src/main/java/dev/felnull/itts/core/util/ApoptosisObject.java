package dev.felnull.itts.core.util;

import dev.felnull.itts.core.ITTSRuntime;
import dev.felnull.itts.core.ImmortalityTimer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 時間がたつと自動的に削除を行うオブジェクト
 *
 * @author MORIMORI0317
 */
public abstract class ApoptosisObject {

    /**
     * 最終実行時間
     */
    private final AtomicLong lastExtensionTime = new AtomicLong(System.currentTimeMillis());

    /**
     * タスク
     */
    private final AtomicReference<ImmortalityTimer.ImmortalityTimerTask> task = new AtomicReference<>();

    /**
     * 使用済みで壊れているかどうか
     */
    private final AtomicBoolean broken = new AtomicBoolean();

    /**
     * 生存時間
     */
    private final long lifeTime;

    /**
     * コンストラクタ
     *
     * @param lifeTime 生存時間
     */
    protected ApoptosisObject(long lifeTime) {
        this.lifeTime = lifeTime;
        scheduleCheckTimer(this::check, this.lifeTime + 300L);
    }

    /**
     * 削除時の処理
     *
     * @param force 強制かどうか
     */
    protected abstract void lifeEnd(boolean force);

    /**
     * 実行時間
     */
    public void extensionLife() {
        this.lastExtensionTime.set(System.currentTimeMillis());
    }

    /**
     * 壊す
     */
    public void broke() {
        broken.set(true);
        ImmortalityTimer.ImmortalityTimerTask tsk = task.get();
        if (tsk != null) {
            tsk.cancel();
        }

        lifeEnd(true);
    }

    private void check() {
        if (broken.get()) {
            return;
        }
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
        ImmortalityTimer.ImmortalityTimerTask tsk = new ImmortalityTimer.ImmortalityTimerTask() {
            @Override
            public void run() {
                CompletableFuture.runAsync(runnable, ITTSRuntime.getInstance().getAsyncWorkerExecutor());
            }
        };
        task.set(tsk);
        ITTSRuntime.getInstance().getImmortalityTimer().schedule(tsk, delay);
    }

}
