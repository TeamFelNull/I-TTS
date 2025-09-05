package dev.felnull.itts.core.migration.data;

import org.jetbrains.annotations.Nullable;

/**
 * 古いJSONユーザーデータの構造
 *
 * @param voiceType 音声タイプ
 * @param deny      読み上げ拒否かどうか
 * @param nickName  ニックネーム
 */
public record LegacyJsonUserData(
        @Nullable String voiceType,
        boolean deny,
        @Nullable String nickName
) {
}