package dev.felnull.itts.core.migration.data;

/**
 * 古いJSON辞書データの構造
 *
 * @param target 対象文字列
 * @param read   読み方
 */
public record LegacyJsonDictData(
        String target,
        String read
) {
}