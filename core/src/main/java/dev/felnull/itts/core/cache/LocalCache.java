package dev.felnull.itts.core.cache;

import com.google.common.hash.HashCode;
import dev.felnull.itts.core.ITTSRuntimeUse;
import dev.felnull.itts.core.ImmortalityTimer;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ローカルキャッシュ
 *
 * @author MORIMORI0317
 */
public class LocalCache implements ITTSRuntimeUse {
    /**
     * 最終使用時間
     */
    private final AtomicLong lastUseTime = new AtomicLong(System.currentTimeMillis());

    /**
     * ロックの数
     */
    private final AtomicInteger useLockCount = new AtomicInteger();

    /**
     * タイマーが動いているか
     */
    private final AtomicBoolean runningTimer = new AtomicBoolean();

    /**
     * 破棄済みかどうか
     */
    private final AtomicBoolean destroy = new AtomicBoolean();

    /**
     * キャッシュのキー
     */
    private final HashCode hashCode;

    /**
     * 保存先ファイル
     */
    private final File file;

    /**
     * コンストラクタ
     *
     * @param hashCode キーとしてのハッシュコード
     * @param file     保存先ファイル
     */
    public LocalCache(HashCode hashCode, File file) {
        this.hashCode = hashCode;
        this.file = file;
    }

    /**
     * キャッシュのを取得
     *
     * @return キャッシュエントリ
     */
    public CacheUseEntry restore() {
        if (isDestroy()) {
            throw new IllegalStateException("Already destroyed");
        }

        useLockCount.incrementAndGet();

        AtomicBoolean unlocked = new AtomicBoolean();
        UseLock ul = () -> {
            if (!unlocked.compareAndSet(false, true)) {
                return;
            }

            if (useLockCount.decrementAndGet() <= 0) {
                lastUseTime.set(System.currentTimeMillis());
                scheduleCheckTimer(this::check, getCacheTime() + 300L);
            }
        };

        return new CacheUseEntry(file, ul);
    }

    /**
     * 破棄
     */
    protected void dispose() {
        destroy.set(true);
        if (file.exists() && !file.delete()) {
            throw new RuntimeException("Failed to delete file");
        }
    }

    public boolean isDestroy() {
        return destroy.get();
    }

    private void check() {
        if (isDestroy()) {
            return;
        }

        if (useLockCount.get() > 0) {
            return;
        }

        long now = System.currentTimeMillis();
        long lastTime = lastUseTime.get();
        long eqTime = now - lastTime;

        if (eqTime >= getCacheTime()) {
            getCacheManager().disposeCache(hashCode);
            return;
        }

        scheduleCheckTimer(this::check, getCacheTime() - eqTime + 300L);
    }

    private long getCacheTime() {
        return getConfigManager().getConfig().getCacheTime();
    }

    private void scheduleCheckTimer(Runnable runnable, long delay) {
        if (!runningTimer.compareAndSet(false, true)) {
            return;
        }

        getImmortalityTimer().schedule(new ImmortalityTimer.ImmortalityTimerTask() {
            @Override
            public void run() {
                runningTimer.set(false);
                CompletableFuture.runAsync(runnable, getAsyncExecutor());
            }
        }, delay);
    }
}
