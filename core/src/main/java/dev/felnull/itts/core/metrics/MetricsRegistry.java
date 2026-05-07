package dev.felnull.itts.core.metrics;

import io.micrometer.core.instrument.Counter;
import org.jetbrains.annotations.NotNull;

/**
 * メトリクスレジストリの抽象
 * 実装はPrometheus版とNoOp版を切り替える
 */
public interface MetricsRegistry {

    /**
     * BOT全体合計を表すサーバーIDの予約値
     */
    long GLOBAL_SERVER_ID = 0L;

    /**
     * NoOp実装を返すファクトリ
     *
     * @return 何もしないMetricsRegistry
     */
    @NotNull
    static MetricsRegistry noop() {
        return NoOpMetricsRegistry.INSTANCE;
    }

    /**
     * 文字数Counterをラベル付きで取得
     *
     * @param botId    BOTのID
     * @param serverId サーバーID GLOBAL_SERVER_IDの場合はBOT全体
     * @return Counter
     */
    @NotNull
    Counter getOrCreateCharCounter(long botId, long serverId);

    /**
     * メッセージ数Counterをラベル付きで取得
     *
     * @param botId    BOTのID
     * @param serverId サーバーID GLOBAL_SERVER_IDの場合はBOT全体
     * @return Counter
     */
    @NotNull
    Counter getOrCreateMessageCounter(long botId, long serverId);
}
