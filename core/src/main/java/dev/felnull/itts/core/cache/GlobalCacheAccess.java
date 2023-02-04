package dev.felnull.itts.core.cache;

import com.google.common.hash.HashCode;
import org.jetbrains.annotations.NotNull;


public interface GlobalCacheAccess extends AutoCloseable {
    byte[] get(@NotNull HashCode hashCode);

    void set(@NotNull HashCode hashCode, byte[] data);

    void lock(@NotNull HashCode hashCode);

    void unlock(@NotNull HashCode hashCode);
}
