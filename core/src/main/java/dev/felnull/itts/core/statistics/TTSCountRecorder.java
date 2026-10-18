package dev.felnull.itts.core.statistics;

import dev.felnull.itts.core.ITTSRuntimeUse;
import dev.felnull.itts.core.statistics.repository.StatisticsRepository;
import dev.felnull.itts.core.voice.Voice;
import dev.felnull.itts.core.voice.VoiceCategory;
import dev.felnull.itts.core.voice.VoiceType;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 読み上げ文字数の集計を非同期で記録するレコーダー
 */
public final class TTSCountRecorder implements ITTSRuntimeUse {

    /**
     * 完了待ちの書き込み
     */
    private final Set<CompletableFuture<Void>> pending = ConcurrentHashMap.newKeySet();

    /**
     * 排他用ロック
     */
    private final Object lock = new Object();

    /**
     * 受付停止フラグ
     */
    private boolean closed;

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

        synchronized (lock) {
            if (closed) {
                return;
            }

            CompletableFuture<Void> future = CompletableFuture.runAsync(
                            () -> writeToDatabase(botDiscordId, guildDiscordId, capturedVoiceTypeId, capturedVoiceCategoryId, charCount),
                            getAsyncExecutor()
                    )
                    .handle((unused, throwable) -> {
                        if (throwable != null) {
                            getITTSLogger().warn("Failed to record TTS count", throwable);
                        }
                        return null;
                    });

            pending.add(future);
            future.whenComplete((unused, throwable) -> pending.remove(future));
        }
    }

    /**
     * 新規の記録を受け付けなくし、実行中の書き込みの完了を待つ
     *
     * @param timeout 待機時間の上限
     * @param unit    待機時間の単位
     * @return 全ての書き込みが完了したらtrue、時間切れまたは中断されたらfalse
     */
    public boolean shutdownAndAwait(long timeout, TimeUnit unit) {
        CompletableFuture<?>[] snapshot;
        synchronized (lock) {
            closed = true;
            snapshot = pending.toArray(new CompletableFuture<?>[0]);
        }

        try {
            CompletableFuture.allOf(snapshot).get(timeout, unit);
            return true;
        } catch (TimeoutException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (ExecutionException e) {
            // handleで例外を吸収しているため到達しない
            throw new IllegalStateException(e);
        }
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
