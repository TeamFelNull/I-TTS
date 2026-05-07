package dev.felnull.itts.core.savedata.dao;

import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;

/**
 * 読み上げ文字数集計のレコード
 *
 * @param botId        BOTのDiscord ID
 * @param serverId     サーバーのDiscord ID nullの場合はBOT全体合計
 * @param date         集計日
 * @param charCount    読み上げ文字数
 * @param messageCount 読み上げメッセージ数
 */
public record TTSCountRecord(long botId,
                             @Nullable Long serverId,
                             LocalDate date,
                             long charCount,
                             long messageCount
) {
}
