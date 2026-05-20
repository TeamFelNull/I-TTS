package dev.felnull.itts.core.statistics;

import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;

/**
 * 読み上げ文字数集計のレコード
 *
 * @param botId           BOTのDiscord ID
 * @param serverId        サーバーのDiscord ID
 * @param voiceTypeId     ボイスタイプID 不明な場合はnull
 * @param voiceCategoryId ボイスカテゴリID 不明な場合はnull
 * @param date            集計日
 * @param charCount       読み上げ文字数
 * @param messageCount    読み上げメッセージ数
 */
public record TTSCountRecord(long botId,
                             long serverId,
                             @Nullable String voiceTypeId,
                             @Nullable String voiceCategoryId,
                             LocalDate date,
                             long charCount,
                             long messageCount
) {
}
