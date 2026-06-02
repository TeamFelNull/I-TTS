package dev.felnull.itts.core.cache;

import com.google.common.hash.HashCode;
import dev.felnull.fnjl.util.FNDataUtil;
import dev.felnull.itts.core.ITTSRuntimeUse;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * キャッシュの管理
 *
 * @author MORIMORI0317
 */
public class CacheManager implements ITTSRuntimeUse {

    /**
     * キャッシュ保存用フォルダー
     */
    private static final File LOCAL_CACHE_FOLDER = new File("./tmp");

    /**
     * 合成音声が空の場合の例外メッセージ
     */
    private static final String EMPTY_AUDIO_MESSAGE = "Synthesized audio data is empty";

    /**
     * 保存済みローカルキャッシュ
     */
    private final Map<HashCode, CompletableFuture<LocalCache>> localCaches = new ConcurrentHashMap<>();

    /**
     * グローバルキャッシュアクセスの取得
     */
    private final Supplier<GlobalCacheAccess> globalCacheAccessFactory;

    /**
     * コンストラクタ
     *
     * @param globalCacheAccessFactory グローバルキャッシュアクセスの取得用Supplier
     */
    public CacheManager(@Nullable Supplier<GlobalCacheAccess> globalCacheAccessFactory) {
        try {
            FileUtils.deleteDirectory(LOCAL_CACHE_FOLDER);
            FNDataUtil.wishMkdir(LOCAL_CACHE_FOLDER);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.globalCacheAccessFactory = globalCacheAccessFactory;
    }

    /**
     * キャッシュを読み込む、もしくは生成する
     *
     * @param key        キー
     * @param loadOpener ストリーム生成
     * @return キャッシュエントリのCompletableFuture
     */
    public CompletableFuture<CacheUseEntry> loadOrRestore(@NotNull HashCode key, @NotNull StreamOpener loadOpener) {
        CompletableFuture<LocalCache> cache = localCaches.computeIfAbsent(key, ky -> createLocalCache(ky, loadOpener));

        cache.whenComplete((_, throwable) -> {
            if (throwable != null) {
                localCaches.remove(key, cache);
            }
        });

        return cache.thenApplyAsync(LocalCache::restore, getAsyncExecutor());
    }

    private CompletableFuture<LocalCache> createLocalCache(HashCode key, StreamOpener loadOpener) {
        File lcFile = getLocalCacheFile(key);

        CompletableFuture<File> cf = globalCacheAccessFactory != null
                ? createViaGlobalCache(key, lcFile, loadOpener)
                : createViaLocalSource(lcFile, loadOpener);

        return cf.thenApplyAsync((file) -> new LocalCache(key, file), getAsyncExecutor());
    }

    /**
     * グローバルキャッシュ経由でローカルキャッシュファイルを生成する
     *
     * @param key        キー
     * @param lcFile     ローカルキャッシュファイル
     * @param loadOpener ストリーム生成
     * @return キャッシュファイルのCompletableFuture
     */
    private CompletableFuture<File> createViaGlobalCache(HashCode key, File lcFile, StreamOpener loadOpener) {
        return CompletableFuture.supplyAsync(() -> {
            try (var gca = globalCacheAccessFactory.get()) {
                byte[] data = gca.get(key);

                if (data == null) {
                    gca.lock(key);
                    try {
                        data = gca.get(key);
                        if (data == null) {
                            try (var in = new BufferedInputStream(loadOpener.openStream())) {
                                data = in.readAllBytes();
                            }

                            validateNonEmpty(data);

                            gca.set(key, data);
                        }
                    } finally {
                        gca.unlock(key);
                    }
                }

                Files.write(lcFile.toPath(), data);

                return lcFile;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }, getAsyncExecutor());
    }

    /**
     * 合成元ストリームから直接ローカルキャッシュファイルを生成する
     *
     * @param lcFile     ローカルキャッシュファイル
     * @param loadOpener ストリーム生成
     * @return キャッシュファイルのCompletableFuture
     */
    private CompletableFuture<File> createViaLocalSource(File lcFile, StreamOpener loadOpener) {
        return CompletableFuture.supplyAsync(() -> {

            try (var in = loadOpener.openStream(); var out = new FileOutputStream(lcFile)) {
                FNDataUtil.inputToOutputBuff(in, out);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (lcFile.length() == 0L) {
                if (!lcFile.delete()) {
                    getITTSLogger().warn("Failed to delete empty cache file: {}", lcFile);
                }
                throw new RuntimeException(new IOException(EMPTY_AUDIO_MESSAGE));
            }

            return lcFile;
        }, getAsyncExecutor());
    }

    /**
     * 合成音声データが空でないことを検証する
     *
     * @param data 検証対象データ
     * @throws IOException データが空の場合
     */
    private static void validateNonEmpty(byte[] data) throws IOException {
        if (data.length == 0) {
            throw new IOException(EMPTY_AUDIO_MESSAGE);
        }
    }

    private File getLocalCacheFile(HashCode hashCode) {
        return new File(LOCAL_CACHE_FOLDER, hashCode.toString());
    }

    /**
     * キャッシュを破棄
     *
     * @param hashCode キャッシュのキー用ハッシュコード
     */
    protected void disposeCache(HashCode hashCode) {
        CompletableFuture<LocalCache> lc = localCaches.remove(hashCode);
        if (lc != null) {
            lc.thenAcceptAsync(LocalCache::dispose, getAsyncExecutor())
                    .exceptionally(throwable -> {
                        getITTSLogger().error("Failed to dispose cache", throwable);
                        return null;
                    });
        }
    }
}
