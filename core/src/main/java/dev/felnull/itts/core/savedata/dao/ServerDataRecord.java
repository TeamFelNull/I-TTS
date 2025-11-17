package dev.felnull.itts.core.savedata.dao;

import org.jetbrains.annotations.Nullable;

/**
 * サーバーデータのレコード
 *
 * @param defaultVoiceTypeKeyId   デフォルトの音声タイプのキーID
 * @param ignoreRegex             無視する正規表現
 * @param needJoin                参加時のみ読み上げるかどうか
 * @param overwriteAloud          読み上げを上書きするかどうか
 * @param notifyMove              参加時に読み上げるかどうか
 * @param readLimit               名前の最大読み上げ数
 * @param nameReadLimit           名前の最大読み上げ数
 * @param autoDisconnectModeKeyId 自動切断モード
 */
public record ServerDataRecord(@Nullable Integer defaultVoiceTypeKeyId,
                               @Nullable String ignoreRegex,
                               boolean needJoin,
                               boolean overwriteAloud,
                               boolean notifyMove,
                               int readLimit,
                               int nameReadLimit,
                               int autoDisconnectModeKeyId) {
}
