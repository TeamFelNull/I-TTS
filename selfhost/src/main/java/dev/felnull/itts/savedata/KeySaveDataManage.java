package dev.felnull.itts.savedata;

import dev.felnull.itts.Main;
import dev.felnull.itts.core.util.ApoptosisObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

public class KeySaveDataManage<K, S extends SaveDataBase> {
    private static final long HOLD_DATA_TIME = 1000 * 60 * 10;
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

    private class SaveDataEntry extends ApoptosisObject {
        private final K key;
        private final CompletableFuture<S> saveData;

        private SaveDataEntry(K key, CompletableFuture<S> saveData) {
            super(HOLD_DATA_TIME);
            this.key = key;
            this.saveData = saveData;
        }

        public CompletableFuture<S> getSaveData() {
            extensionLife();
            return saveData;
        }

        @Override
        protected void lifeEnd(boolean force) {
            unload(key);
        }
    }
}
