package dev.felnull.itts.savedata;

import dev.felnull.itts.Main;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

public class KeySaveDataManage<K, S extends SaveDataBase> {
    private static final long HOLD_DATA_TIME = 1000 * 60 * 60;
    private final Map<K, SaveDataEntry> saveDataEntries = new ConcurrentHashMap<>();
    private final Map<K, Object> locks = new ConcurrentHashMap<>();
    private final Function<K, S> newSaveDataFactory;

    public KeySaveDataManage(Function<K, S> newSaveDataFactory) {
        this.newSaveDataFactory = newSaveDataFactory;
    }

    public S get(K key) {
        try {
            return load(key).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<S> load(K key) {
        synchronized (locks.computeIfAbsent(key, k -> new Object())) {
            return saveDataEntries.computeIfAbsent(key, ky -> new SaveDataEntry(key, computeInitAsync(key))).getSaveData();
        }
    }

    private void unload(K key) {
        SaveDataEntry p;
        var lc = locks.get(key);
        if (lc == null) return;

        synchronized (lc) {
            locks.remove(key);
            p = saveDataEntries.remove(key);
            if (p == null) return;
            p.brake();
            SaveDataBase sd;
            try {
                sd = p.getSaveData().get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }

            try {
                sd.dispose();
            } catch (Exception ex) {
                Main.RUNTIME.getLogger().error("Failed to unloaded save data ({}).", sd.getName(), ex);
            }
        }
    }

    @NotNull
    @Unmodifiable
    public Map<K, S> getAllLoaded() {
        return saveDataEntries.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
            try {
                return entry.getValue().getSaveData().get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    private CompletableFuture<S> computeInitAsync(K key) {
        return CompletableFuture.supplyAsync(() -> {
            var ni = newSaveDataFactory.apply(key);
            try {
                ni.init();
            } catch (Exception ex) {
                Main.RUNTIME.getLogger().error("Failed to initialize save data ({}), This data will not be saved.", ni.getName(), ex);
            }
            return ni;
        }, Main.RUNTIME.getAsyncWorkerExecutor());
    }

    private class SaveDataEntry {
        private final AtomicLong lastUseTime = new AtomicLong(System.currentTimeMillis());
        private final AtomicBoolean broken = new AtomicBoolean();
        private final K key;
        private final CompletableFuture<S> saveData;

        private SaveDataEntry(K key, CompletableFuture<S> saveData) {
            this.key = key;
            this.saveData = saveData;
            scheduleCheckTimer(this::check, HOLD_DATA_TIME + 300L);
        }

        private void brake() {
            broken.set(true);
        }

        public CompletableFuture<S> getSaveData() {
            this.lastUseTime.set(System.currentTimeMillis());
            return saveData;
        }

        private void check() {
            if (broken.get()) return;

            long now = System.currentTimeMillis();
            long lastTime = lastUseTime.get();
            long eqTime = now - lastTime;

            if (eqTime >= HOLD_DATA_TIME) {
                unload(key);
                return;
            }

            scheduleCheckTimer(this::check, HOLD_DATA_TIME - eqTime + 300L);
        }

        private void scheduleCheckTimer(Runnable runnable, long delay) {
            Main.RUNTIME.getTimer().schedule(new TimerTask() {
                @Override
                public void run() {
                    CompletableFuture.runAsync(runnable, Main.RUNTIME.getAsyncWorkerExecutor());
                }
            }, delay);
        }
    }
}
