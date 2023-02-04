package dev.felnull.itts.core.cache;

import java.io.File;

public record CacheUseEntry(File file, UseLock useLock) {
}
