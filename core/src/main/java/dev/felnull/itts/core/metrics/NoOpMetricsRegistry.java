package dev.felnull.itts.core.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.noop.NoopCounter;
import org.jetbrains.annotations.NotNull;

/**
 * 何もしないMetricsRegistry実装
 * メトリクス公開無効時に利用しnullチェックを不要にする
 */
final class NoOpMetricsRegistry implements MetricsRegistry {

    /**
     * 共有インスタンス
     */
    static final NoOpMetricsRegistry INSTANCE = new NoOpMetricsRegistry();

    /**
     * 文字数Counterの共有NoOpインスタンス
     */
    private static final Counter NOOP_CHAR_COUNTER = new NoopCounter(
            new Meter.Id("itts_spoken_chars_total", Tags.empty(), null, null, Meter.Type.COUNTER));

    /**
     * メッセージ数Counterの共有NoOpインスタンス
     */
    private static final Counter NOOP_MESSAGE_COUNTER = new NoopCounter(
            new Meter.Id("itts_spoken_messages_total", Tags.empty(), null, null, Meter.Type.COUNTER));

    /**
     * コンストラクタ
     * シングルトン用に外部からの生成を抑止する
     */
    private NoOpMetricsRegistry() {
    }

    @Override
    @NotNull
    public Counter getOrCreateCharCounter(long botId, long serverId) {
        return NOOP_CHAR_COUNTER;
    }

    @Override
    @NotNull
    public Counter getOrCreateMessageCounter(long botId, long serverId) {
        return NOOP_MESSAGE_COUNTER;
    }
}
