package dev.felnull.itts.core.migration.data;

import org.jetbrains.annotations.Nullable;

/**
 * 古いJSONユーザーデータの構造
 */
public record LegacyJsonUserData(
        @Nullable String voiceType,
        boolean deny,
        @Nullable String nickName
) {
}