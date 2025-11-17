package dev.felnull.itts.core.savedata.dao;

import org.jetbrains.annotations.Nullable;

/**
 * サーバー別ユーザーデータのレコード
 *
 * @param voiceTypeKeyId 音声タイプのキーID
 * @param deny           拒否情報
 * @param nickName       ニックネーム情報
 */
public record ServerUserDataRecord(@Nullable Integer voiceTypeKeyId,
                                   boolean deny,
                                   @Nullable String nickName
) {
}
