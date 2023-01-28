package dev.felnull.ttsvoice.core.cache;

import com.google.common.hash.HashCode;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class CacheManager {
    private final Map<HashCode, LocalCache> localCaches = new ConcurrentHashMap<>();
    private final GlobalCacheAccess globalCacheAccess;

    public CacheManager(GlobalCacheAccess globalCacheAccess) {
        this.globalCacheAccess = globalCacheAccess;
    }

    public CompletableFuture<CacheUseEntry> loadOrRestore(@NotNull HashCode key, @NotNull StreamOpener loadOpener) {
        var lc = localCaches.get(key);
        if (lc == null) {

        }
        return null;
    }

    protected void disposeLocalCache(HashCode hashCode) {
        var lc = localCaches.remove(hashCode);
        if (lc != null)
            lc.dispose();
    }
}
