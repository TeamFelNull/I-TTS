package dev.felnull.itts.core.tts;

import dev.felnull.itts.core.ITTSRuntimeUse;
import dev.felnull.itts.core.metrics.MetricsRegistry;
import dev.felnull.itts.core.savedata.SaveDataManager;
import dev.felnull.itts.core.savedata.repository.DataRepository;
import dev.felnull.itts.core.savedata.repository.TTSCountData;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.concurrent.CompletableFuture;

/**
 * 読み上げ文字数の集計を非同期で記録するレコーダー
 */
public final class TTSCountRecorder implements ITTSRuntimeUse {

    /**
     * メトリクスレジストリ nullの場合はメトリクス公開無効
     */
    private final MetricsRegistry metricsRegistry;

    /**
     * コンストラクタ
     *
     * @param metricsRegistry メトリクスレジストリ
     */
    public TTSCountRecorder(MetricsRegistry metricsRegistry) {
        this.metricsRegistry = metricsRegistry;
    }

    /**
     * 読み上げ文字数を記録する
     *
     * @param botDiscordId   BOTのDiscord ID
     * @param guildDiscordId サーバーのDiscord ID
     * @param charCount      文字数
     */
    public void record(long botDiscordId, long guildDiscordId, int charCount) {
        if (charCount <= 0) {
            return;
        }

        if (metricsRegistry != null) {
            try {
                metricsRegistry.getOrCreateCharCounter(botDiscordId, guildDiscordId).increment(charCount);
                metricsRegistry.getOrCreateMessageCounter(botDiscordId, guildDiscordId).increment();
                metricsRegistry.getOrCreateCharCounter(botDiscordId, null).increment(charCount);
                metricsRegistry.getOrCreateMessageCounter(botDiscordId, null).increment();
            } catch (Throwable t) {
                getITTSLogger().warn("Failed to update metrics counter", t);
            }
        }

        CompletableFuture.runAsync(() -> writeToDatabase(botDiscordId, guildDiscordId, charCount), getAsyncExecutor())
                .exceptionally(throwable -> {
                    getITTSLogger().warn("Failed to record TTS count", throwable);
                    return null;
                });
    }

    private void writeToDatabase(long botDiscordId, long guildDiscordId, int charCount) {
        DataRepository repo = SaveDataManager.getInstance().getRepository();
        if (repo == null) {
            return;
        }

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        TTSCountData serverData = repo.getServerTTSCount(botDiscordId, guildDiscordId, today);
        TTSCountData globalData = repo.getGlobalTTSCount(botDiscordId, today);

        serverData.addCount(charCount, 1L);
        globalData.addCount(charCount, 1L);
    }
}
