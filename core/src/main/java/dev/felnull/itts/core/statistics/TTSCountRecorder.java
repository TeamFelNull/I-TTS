package dev.felnull.itts.core.statistics;

import dev.felnull.itts.core.ITTSRuntimeUse;
import dev.felnull.itts.core.statistics.repository.StatisticsRepository;
import dev.felnull.itts.core.voice.Voice;
import dev.felnull.itts.core.voice.VoiceCategory;
import dev.felnull.itts.core.voice.VoiceType;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.concurrent.CompletableFuture;

/**
 * 読み上げ文字数の集計を非同期で記録するレコーダー
 */
public final class TTSCountRecorder implements ITTSRuntimeUse {

    /**
     * 読み上げ文字数を記録する
     *
     * @param botDiscordId   BOTのDiscord ID
     * @param guildDiscordId サーバーのDiscord ID
     * @param voice          読み上げに使用したボイス
     * @param charCount      文字数
     */
    public void record(long botDiscordId, long guildDiscordId, @Nullable Voice voice, int charCount) {
        if (charCount <= 0) {
            return;
        }

        String voiceTypeId = null;
        String voiceCategoryId = null;
        if (voice != null) {
            VoiceType voiceType = voice.getVoiceType();
            if (voiceType != null) {
                voiceTypeId = voiceType.getStatisticsName();
                VoiceCategory category = voiceType.getCategory();
                if (category != null) {
                    voiceCategoryId = category.getId();
                }
            }
        }

        String capturedVoiceTypeId = voiceTypeId;
        String capturedVoiceCategoryId = voiceCategoryId;

        CompletableFuture.runAsync(
                        () -> writeToDatabase(botDiscordId, guildDiscordId, capturedVoiceTypeId, capturedVoiceCategoryId, charCount),
                        getAsyncExecutor()
                )
                .exceptionally(throwable -> {
                    getITTSLogger().warn("Failed to record TTS count", throwable);
                    return null;
                });
    }

    private void writeToDatabase(long botDiscordId,
                                 long guildDiscordId,
                                 @Nullable String voiceTypeId,
                                 @Nullable String voiceCategoryId,
                                 int charCount) {
        StatisticsRepository repo = StatisticsManager.getInstance().getRepository();
        if (repo == null) {
            return;
        }

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        repo.increment(botDiscordId, guildDiscordId, voiceTypeId, voiceCategoryId, today, charCount, 1L);
    }
}
