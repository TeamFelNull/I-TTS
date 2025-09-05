package dev.felnull.itts.core.migration.data;

/**
 * 古いJSON辞書データの構造
 */
public record LegacyJsonDictData(
        String target,
        String read
) {
}