package dev.felnull.itts.core.migration.data;

import org.jetbrains.annotations.Nullable;

/**
 * 古いJSONサーバーデータの構造
 */
public record LegacyJsonServerData(
        @Nullable String defaultVoiceType,
        @Nullable String ignoreRegex,
        boolean needJoin,
        boolean overwriteAloud,
        boolean notifyMove,
        int readLimit,
        int nameReadLimit
) {
}