package dev.felnull.itts.core.migration.data;

import org.jetbrains.annotations.Nullable;

/**
 * 古いJSONサーバーデータの構造
 *
 * @param defaultVoiceType デフォルトの音声タイプ
 * @param ignoreRegex      無視する正規表現
 * @param needJoin         VCに参加が必要かどうか
 * @param overwriteAloud   読み上げを上書きするかどうか
 * @param notifyMove       移動通知をするかどうか
 * @param readLimit        読み上げ文字数制限
 * @param nameReadLimit    名前読み上げ文字数制限
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