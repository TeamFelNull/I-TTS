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

public class CacheManager implements ITTSRuntimeUse {
    private static final File LOCAL_CACHE_FOLDER = new File("./tmp");
    private final Map<HashCode, CompletableFuture<LocalCache>> localCaches = new ConcurrentHashMap<>();
    private final Supplier<GlobalCacheAccess> globalCacheAccessFactory;

    public CacheManager(@Nullable Supplier<GlobalCacheAccess> globalCacheAccessFactory) {
        try {
            FileUtils.deleteDirectory(LOCAL_CACHE_FOLDER);
            FNDataUtil.wishMkdir(LOCAL_CACHE_FOLDER);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.globalCacheAccessFactory = globalCacheAccessFactory;
    }

    public CompletableFuture<CacheUseEntry> loadOrRestore(@NotNull HashCode key, @NotNull StreamOpener loadOpener) {
        return localCaches.computeIfAbsent(key, ky -> createLocalCache(ky, loadOpener)).thenApplyAsync(LocalCache::restore, getAsyncExecutor());
    }

    private CompletableFuture<LocalCache> createLocalCache(HashCode key, StreamOpener loadOpener) {
        CompletableFuture<File> cf;
        var lcFile = getLocalCacheFile(key);

        if (globalCacheAccessFactory != null) {
            cf = CompletableFuture.supplyAsync(() -> {
                try (var gca = globalCacheAccessFactory.get()) {
                    byte[] data = gca.get(key);

                    if (data == null) {
                        gca.lock(key);

                        data = gca.get(key);
                        if (data == null) {
                            try (var in = new BufferedInputStream(loadOpener.openStream());) {
                                data = in.readAllBytes();
                            }
                            gca.set(key, data);
                        }

                        gca.unlock(key);
                    }

                    Files.write(lcFile.toPath(), data);

                    return lcFile;
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }, getAsyncExecutor());

        } else {
            cf = CompletableFuture.supplyAsync(() -> {

                try (var in = loadOpener.openStream(); var out = new FileOutputStream(lcFile)) {
                    FNDataUtil.inputToOutputBuff(in, out);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }

                return lcFile;
            }, getAsyncExecutor());
        }
        return cf.thenApplyAsync((file) -> new LocalCache(key, file), getAsyncExecutor());
    }

    private File getLocalCacheFile(HashCode hashCode) {
        return new File(LOCAL_CACHE_FOLDER, hashCode.toString());
    }

    protected void disposeCache(HashCode hashCode) {
        var lc = localCaches.remove(hashCode);
        if (lc != null)
            lc.thenAcceptAsync(LocalCache::dispose, getAsyncExecutor());
    }
}
