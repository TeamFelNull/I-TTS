package dev.felnull.itts.core.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Prometheus形式のメトリクスレジストリ実装
 * Counterのキャッシュとシステム系メトリクスのバインドを行う
 */
public final class PrometheusMetricsRegistry implements MetricsRegistry {

    /**
     * 文字数Counterのメトリクス名
     */
    private static final String CHAR_METRIC_NAME = "itts_spoken_chars_total";

    /**
     * 文字数Counterの説明
     */
    private static final String CHAR_METRIC_DESCRIPTION = "Total spoken characters delivered to TTS API";

    /**
     * メッセージ数Counterのメトリクス名
     */
    private static final String MESSAGE_METRIC_NAME = "itts_spoken_messages_total";

    /**
     * メッセージ数Counterの説明
     */
    private static final String MESSAGE_METRIC_DESCRIPTION = "Total spoken messages delivered to TTS API";

    /**
     * Prometheus形式のレジストリ
     */
    private final PrometheusMeterRegistry registry;

    /**
     * 起動時刻のミリ秒
     */
    private final long bootAt;

    /**
     * 文字数Counterのキャッシュ
     */
    private final ConcurrentMap<String, Counter> charCounters = new ConcurrentHashMap<>();

    /**
     * メッセージ数Counterのキャッシュ
     */
    private final ConcurrentMap<String, Counter> messageCounters = new ConcurrentHashMap<>();

    /**
     * コンストラクタ
     */
    public PrometheusMetricsRegistry() {
        this.registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        this.bootAt = System.currentTimeMillis();

        Gauge.builder("itts_up", () -> 1.0d)
                .description("Bot liveness indicator")
                .register(registry);

        Gauge.builder("itts_uptime_seconds", () -> (System.currentTimeMillis() - bootAt) / 1000.0d)
                .description("Bot uptime in seconds")
                .register(registry);

        new JvmMemoryMetrics().bindTo(registry);
        new JvmGcMetrics().bindTo(registry);
        new ProcessorMetrics().bindTo(registry);
    }

    /**
     * Prometheusレジストリを取得
     *
     * @return レジストリ
     */
    @NotNull
    public PrometheusMeterRegistry getRegistry() {
        return registry;
    }

    @Override
    @NotNull
    public Counter getOrCreateCharCounter(long botId, long serverId) {
        return getOrCreate(charCounters, CHAR_METRIC_NAME, CHAR_METRIC_DESCRIPTION, botId, serverId);
    }

    @Override
    @NotNull
    public Counter getOrCreateMessageCounter(long botId, long serverId) {
        return getOrCreate(messageCounters, MESSAGE_METRIC_NAME, MESSAGE_METRIC_DESCRIPTION, botId, serverId);
    }

    /**
     * Counterをキャッシュから取得もしくは生成する
     *
     * @param cache       Counterキャッシュ
     * @param metricName  メトリクス名
     * @param description メトリクスの説明
     * @param botId       BOTのID
     * @param serverId    サーバーID GLOBAL_SERVER_IDの場合はBOT全体
     * @return Counter
     */
    @NotNull
    private Counter getOrCreate(@NotNull ConcurrentMap<String, Counter> cache,
                                @NotNull String metricName,
                                @NotNull String description,
                                long botId,
                                long serverId) {
        String serverTag = serverId == GLOBAL_SERVER_ID ? "global" : String.valueOf(serverId);
        String key = botId + "|" + serverTag;
        return cache.computeIfAbsent(key, k -> Counter.builder(metricName)
                .description(description)
                .tag("bot_id", String.valueOf(botId))
                .tag("server_id", serverTag)
                .register(registry));
    }
}
