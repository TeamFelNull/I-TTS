package dev.felnull.itts.savedata;

import com.google.common.collect.ImmutableMap;
import dev.felnull.itts.Main;
import dev.felnull.itts.core.util.ApoptosisObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * キーデータ形式のセーブデータ管理
 *
 * @param <K> キー
 * @param <S> セーブデータの型
 * @author MORIMORI0317
 */
public class KeySaveDataManage<K extends Record & SaveDataKey, S extends SaveDataBase> {

    /**
     * データ保持期間
     */
    private static final long HOLD_DATA_TIME = 1000 * 60 * 10;

    /**
     * セーブデータのエントリ
     */
    private final Map<K, SaveDataEntry> saveDataEntries = new ConcurrentHashMap<>();

    /**
     * ロック
     */
    private final Map<K, Object> locks = new ConcurrentHashMap<>();

    /**
     * セーブフォルダ
     */
    private final File saveFolder;

    /**
     * セーブデータの作成サプライヤー
     */
    private final Supplier<S> newSaveDataFactory;

    /**
     * セーブデータファイルの検索
     */
    private final SaveDataKey.SavedFileFinder<K> savedFileFinder;

    /**
     * コンストラクタ
     *
     * @param saveFolder         セーブデータの保存先フォルダ
     * @param newSaveDataFactory 新規セーブデータ作成サプライヤー
     * @param savedFileFinder    セブデータファイルの検索
     */
    public KeySaveDataManage(File saveFolder, Supplier<S> newSaveDataFactory, SaveDataKey.SavedFileFinder<K> savedFileFinder) {
        this.saveFolder = saveFolder;
        this.newSaveDataFactory = newSaveDataFactory;
        this.savedFileFinder = savedFileFinder;
    }

    /**
     * セーブデータを取得
     *
     * @param key キー
     * @return セーブデータ
     */
    public S get(K key) {
        try {
            return load(key).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * セーブデータを読み込む
     *
     * @param key キー
     * @return 読み込みの非同期CompletableFuture
     */
    public CompletableFuture<S> load(K key) {
        synchronized (locks.computeIfAbsent(key, k -> new Object())) {
            return saveDataEntries.computeIfAbsent(key, ky -> new SaveDataEntry(key, computeInitAsync(key, Main.runtime.getAsyncWorkerExecutor()))).getSaveData();
        }
    }

    private void unload(K key) {
        SaveDataEntry p;
        Object lc = locks.get(key);
        if (lc == null) {
            return;
        }

        synchronized (lc) {
            locks.remove(key);
            p = saveDataEntries.remove(key);
            if (p == null) {
                return;
            }
            SaveDataBase sd;
            try {
                sd = p.getSaveData().get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }

            String name = sd.getName() + ": " + key;

            try {
                sd.dispose();
                Main.runtime.getLogger().debug("Successfully unloaded save data. ({})", name);
            } catch (Exception ex) {
                Main.runtime.getLogger().error("Failed to unloaded save data ({}).", name, ex);
            }
        }
    }

    @NotNull
    @Unmodifiable
    public Map<K, S> getAll() {
        return loadAll().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, it -> {
                    try {
                        return it.getValue().get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }));
    }

    /**
     * 全てを読み込む
     *
     * @return 読み込みの非同期CompletableFuture
     */
    @NotNull
    @Unmodifiable
    public Map<K, CompletableFuture<S>> loadAll() {
        List<K> keys = savedFileFinder.find(saveFolder);
        Map<K, CompletableFuture<S>> ret = new HashMap<>();

        for (K key : keys) {
            synchronized (locks.computeIfAbsent(key, k -> new Object())) {
                ret.put(key, saveDataEntries.computeIfAbsent(key, ky ->
                        new SaveDataEntry(key, computeInitAsync(key, Main.runtime.getHeavyWorkerExecutor()))).getSaveData());
            }
        }

        return ImmutableMap.copyOf(ret);
    }

    private CompletableFuture<S> computeInitAsync(K key, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            S ni = newSaveDataFactory.get();
            String name = ni.getName() + ": " + key;
            try {
                ni.init(key.getSavedFile(saveFolder), key);
                Main.runtime.getLogger().debug("Succeeded in loading the existing save data. ({})", name);
            } catch (Exception ex) {
                Main.runtime.getLogger().error("Failed to initialize save data ({}), This data will not be saved.", name, ex);
            }
            return ni;
        }, executor);
    }

    /**
     * セーブデータのエントリ
     *
     * @author MORIMORI0317
     */
    private class SaveDataEntry extends ApoptosisObject {

        /**
         * キー
         */
        private final K key;

        /**
         * セーブデータ
         */
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
